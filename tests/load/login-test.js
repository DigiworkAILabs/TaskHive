import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * Login Load Test (NFR-PERF-01)
 * Target: < 200ms response time
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
        http_req_duration: ['p(95)<300'],           // NFR‑PERF‑02
        'http_req_duration{name:login}': ['p(95)<200'], // NFR‑PERF‑01
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    const res = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            email: 'admin@taskhive.com',
            password: 'Admin@123',
        }),
        {
            headers: { 'Content-Type': 'application/json' },
            tags: { name: 'login' },
        }
    );

    check(res, {
        'login status 200': (r) => r.status === 200,
        'login < 200ms': (r) => r.timings.duration < 200,
        'has accessToken': (r) => {
            try { return JSON.parse(r.body).data.accessToken !== undefined; }
            catch { return false; }
        },
    });

    sleep(1);
}
