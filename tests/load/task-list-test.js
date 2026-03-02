import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * Task List Load Test (NFR-PERF-01)
 * Target: < 150ms response time
 */
export const options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<300'],                    // NFR‑PERF‑02
        'http_req_duration{name:task_list}': ['p(95)<150'],  // NFR‑PERF‑01
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
    const loginRes = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({ email: 'admin@taskhive.com', password: 'Admin@123' }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    const token = JSON.parse(loginRes.body).data.accessToken;
    return { token };
}

export default function (data) {
    const res = http.get(`${BASE_URL}/api/v1/tasks?page=0&size=20`, {
        headers: { Authorization: `Bearer ${data.token}` },
        tags: { name: 'task_list' },
    });

    check(res, {
        'task list 200': (r) => r.status === 200,
        'task list < 150ms': (r) => r.timings.duration < 150,
    });

    sleep(0.5);
}
