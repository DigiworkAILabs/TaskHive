import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * Admin Dashboard Load Test (NFR-PERF-01)
 * Target: < 500ms response time
 */
export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 200 },
        { duration: '1m', target: 500 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<300'],                     // NFR‑PERF‑02
        'http_req_duration{name:dashboard}': ['p(95)<500'],   // NFR‑PERF‑01
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
    const body = JSON.parse(loginRes.body);
    return { token: body.data.accessToken };
}

export default function (data) {
    const res = http.get(`${BASE_URL}/api/v1/analytics/dashboard/admin`, {
        headers: { Authorization: `Bearer ${data.token}` },
        tags: { name: 'dashboard' },
    });

    check(res, {
        'dashboard 200': (r) => r.status === 200,
        'dashboard < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(0.5);
}
