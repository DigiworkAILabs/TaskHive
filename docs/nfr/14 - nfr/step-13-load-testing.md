# Step 13 — NFR-PERF-01/02/03: Load Testing

**NFR IDs:** NFR-PERF-01, NFR-PERF-02, NFR-PERF-03
**Effort:** ~3 hrs | **Dependencies:** Step 4 (Actuator for metrics)
**Commit:** `feat(testing): add load testing suite (NFR-PERF-01/02/03)`

---

## SRS Requirements

> **NFR-PERF-01:** API response time targets — Login: < 200ms, Task list: < 150ms, Task creation: < 250ms, Dashboard: < 500ms, Search: < 300ms.
> **NFR-PERF-02:** 95th percentile response < 300ms under normal load.
> **NFR-PERF-03:** Support 100–500 concurrent users.

## Current State

No load testing tools or benchmarks configured.

## Implementation

### Tool Choice: k6

k6 is a modern load testing tool (written in Go, scripted in JS). It's simpler than Gatling and doesn't require JVM.

### Install k6

```bash
# Windows (via Chocolatey)
choco install k6

# Or download from https://k6.io/docs/get-started/installation/
```

### New Directory: `tests/load/`

### New File: `tests/load/login-test.js`

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 },   // Ramp up to 50 users
        { duration: '1m', target: 100 },   // Hold at 100
        { duration: '30s', target: 200 },  // Ramp to 200
        { duration: '1m', target: 500 },   // Peak at 500
        { duration: '30s', target: 0 },    // Ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<300'],  // NFR-PERF-02
        http_req_failed: ['rate<0.01'],    // 99% success
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

export default function () {
    // Login
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        email: 'admin@taskhive.com',
        password: 'Admin@123',
    }), { headers: { 'Content-Type': 'application/json' } });

    check(loginRes, {
        'login status 200': (r) => r.status === 200,
        'login < 200ms': (r) => r.timings.duration < 200,  // NFR-PERF-01
    });

    sleep(1);
}
```

### New File: `tests/load/task-list-test.js`

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<300'],
        'http_req_duration{name:task_list}': ['p(95)<150'],  // NFR-PERF-01
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';

export function setup() {
    // Login once, get cookie
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
        email: 'admin@taskhive.com',
        password: 'Admin@123',
    }), { headers: { 'Content-Type': 'application/json' } });

    return { cookies: loginRes.cookies };
}

export default function (data) {
    // Task list (paginated)
    const res = http.get(`${BASE_URL}/api/v1/tasks?page=0&size=20`, {
        tags: { name: 'task_list' },
    });

    check(res, {
        'task list 200': (r) => r.status === 200,
        'task list < 150ms': (r) => r.timings.duration < 150,
    });

    sleep(0.5);
}
```

### New File: `tests/load/run-all.sh`

```bash
#!/bin/bash
# Run all load tests and generate reports
echo "=== TaskHive Load Testing Suite ==="

echo "1/3 Login test..."
k6 run --out json=results/login.json tests/load/login-test.js

echo "2/3 Task list test..."
k6 run --out json=results/task-list.json tests/load/task-list-test.js

echo "3/3 Dashboard test..."
k6 run --out json=results/dashboard.json tests/load/dashboard-test.js

echo "=== All tests complete ==="
```

### SRS Target Thresholds

| Endpoint | Target | k6 Threshold |
|----------|--------|-------------|
| Login | < 200ms | `http_req_duration{name:login} < 200` |
| Task list | < 150ms | `http_req_duration{name:task_list} < 150` |
| Task create | < 250ms | `http_req_duration{name:task_create} < 250` |
| Dashboard | < 500ms | `http_req_duration{name:dashboard} < 500` |
| Search | < 300ms | `http_req_duration{name:search} < 300` |
| **Overall** | **p95 < 300ms** | `http_req_duration: ['p(95)<300']` |

## Verification

```bash
# Run single test
k6 run tests/load/login-test.js

# Run with custom URL
k6 run --env BASE_URL=http://staging.taskhive.com tests/load/login-test.js

# Check thresholds pass ✓ or fail ✗ in output
```
