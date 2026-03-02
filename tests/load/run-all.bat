@echo off
echo ============================================
echo   TaskHive Load Testing Suite (NFR-PERF-01/02/03)
echo ============================================
echo.

if not exist results mkdir results

echo [1/5] Login test...
k6 run --out json=results/login.json tests/load/login-test.js
echo.

echo [2/5] Task list test...
k6 run --out json=results/task-list.json tests/load/task-list-test.js
echo.

echo [3/5] Task create test...
k6 run --out json=results/task-create.json tests/load/task-create-test.js
echo.

echo [4/5] Dashboard test...
k6 run --out json=results/dashboard.json tests/load/dashboard-test.js
echo.

echo [5/5] Search test...
k6 run --out json=results/search.json tests/load/search-test.js
echo.

echo ============================================
echo   All tests complete! Results in results/
echo ============================================
