import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * Task Search Load Test (NFR-PERF-01)
 * Target: < 300ms response time
 */
export const options = {
    stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<300'],                   // NFR‑PERF‑02
        'http_req_duration{name:search}': ['p(95)<300'],    // NFR‑PERF‑01
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const SEARCH_TERMS = ['task', 'review', 'deploy', 'bug', 'test', 'urgent'];

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
    const query = SEARCH_TERMS[Math.floor(Math.random() * SEARCH_TERMS.length)];

    const res = http.get(
        `${BASE_URL}/api/v1/tasks/search?query=${query}&page=0&size=10`,
        {
            headers: { Authorization: `Bearer ${data.token}` },
            tags: { name: 'search' },
        }
    );

    check(res, {
        'search 200': (r) => r.status === 200,
        'search < 300ms': (r) => r.timings.duration < 300,
    });

    sleep(0.5);
}
