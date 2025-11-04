# Payment Processing Strategies - Load Tests

This directory contains k6 load tests for benchmarking three different payment processing strategies using coroutines in Kotlin.

## Strategies

The application supports three different concurrency strategies:

| Strategy | Header Value | Description |
|----------|--------------|-------------|
| Async Coroutine | `ASYNC_COROUTINE` | Non-blocking concurrent processing using async/await |
| Sequential | `SEQUENTIAL` | Sequential processing (baseline) |
| Blocking Thread | `BLOCKING_THREAD` | Concurrent processing with blocking threads |

## Test Files

- **`payments_test.js`**: Main k6 test script with modular request logic and configurable scenarios
- **`run_payment_tests.sh`**: Wrapper script that runs all three tests independently and generates separate reports
- **`rinha.js`**: Original competition test script (legacy)
- **`requests.js`**: Shared HTTP client utilities (legacy)

## Running the Tests

### Prerequisites

1. Install k6: https://grafana.com/docs/k6/latest/set-up/install-k6/
2. Ensure the backend is running on `http://localhost:9999`
3. Ensure payment processors are running (if required)

### Run All Tests

To run all three strategies and generate separate reports:

```bash
cd k6-test
./run_payment_tests.sh
```

This will create three report files:
- `relatorio_async_coroutine.json`
- `relatorio_sequential.json`
- `relatorio_blocking_thread.json`

### Run Individual Tests

To run a specific strategy:

```bash
# Async Coroutine strategy
k6 run -e STRATEGY=ASYNC_COROUTINE payments_test.js --summary-export=relatorio_async_coroutine.json

# Sequential strategy
k6 run -e STRATEGY=SEQUENTIAL payments_test.js --summary-export=relatorio_sequential.json

# Blocking Thread strategy
k6 run -e STRATEGY=BLOCKING_THREAD payments_test.js --summary-export=relatorio_blocking_thread.json
```

### Run All Strategies in a Single Test

To run all strategies concurrently in one test execution:

```bash
k6 run -e STRATEGY=ALL payments_test.js --summary-export=relatorio_combined.json
```

## Test Configuration

You can customize the test parameters by editing `payments_test.js`:

```javascript
const baseScenarios = {
  ASYNC_COROUTINE: {
    executor: 'constant-vus',
    exec: 'asyncCoroutineScenario',
    vus: 50,          // Number of virtual users
    duration: '30s',  // Test duration
  },
  SEQUENTIAL: {
    executor: 'constant-vus',
    exec: 'sequentialScenario',
    vus: 20,
    duration: '30s',
  },
  BLOCKING_THREAD: {
    executor: 'constant-vus',
    exec: 'blockingThreadScenario',
    vus: 10,
    duration: '30s',
  },
};
```

## Understanding the Reports

Each JSON report contains comprehensive metrics including:

- **Latency percentiles**: p50, p90, p95, p99
- **Request duration**: min, avg, max
- **Throughput**: requests per second
- **Error rates**: percentage of failed requests
- **Total duration**: overall test execution time
- **HTTP metrics**: request/response times, data transfer

### Viewing Reports

To view a formatted report summary:

```bash
# Using jq
cat relatorio_async_coroutine.json | jq

# View specific metrics
cat relatorio_async_coroutine.json | jq '.metrics.http_req_duration'
```

## Comparing Strategies

After running all tests, you can compare the strategies:

```bash
# Compare p99 latencies
echo "=== P99 Latencies ==="
echo -n "ASYNC_COROUTINE: " && cat relatorio_async_coroutine.json | jq '.metrics.http_req_duration.values."p(99)"'
echo -n "SEQUENTIAL: " && cat relatorio_sequential.json | jq '.metrics.http_req_duration.values."p(99)"'
echo -n "BLOCKING_THREAD: " && cat relatorio_blocking_thread.json | jq '.metrics.http_req_duration.values."p(99)"'

# Compare error rates
echo -e "\n=== Error Rates ==="
echo -n "ASYNC_COROUTINE: " && cat relatorio_async_coroutine.json | jq '.metrics.errors.values.rate'
echo -n "SEQUENTIAL: " && cat relatorio_sequential.json | jq '.metrics.errors.values.rate'
echo -n "BLOCKING_THREAD: " && cat relatorio_blocking_thread.json | jq '.metrics.errors.values.rate'
```

## Advanced Options

### Web Dashboard

To view real-time test progress in a web dashboard:

```bash
export K6_WEB_DASHBOARD=true
export K6_WEB_DASHBOARD_PORT=5665
export K6_WEB_DASHBOARD_OPEN=true
export K6_WEB_DASHBOARD_EXPORT='report.html'

k6 run -e STRATEGY=ASYNC_COROUTINE payments_test.js
```

### Custom Executors

You can modify scenarios to use different executors:

- **`constant-vus`**: Fixed number of virtual users
- **`ramping-vus`**: Gradually increase/decrease VUs
- **`constant-arrival-rate`**: Fixed request rate
- **`ramping-arrival-rate`**: Variable request rate

Example:

```javascript
ASYNC_COROUTINE: {
  executor: 'ramping-vus',
  startVUs: 10,
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
}
```

## Troubleshooting

### Backend not responding

Ensure the backend is running:
```bash
curl http://localhost:9999/payments-summary
```

### k6 not found

Install k6 following the official guide: https://grafana.com/docs/k6/latest/set-up/install-k6/

### Permission denied on script

Make the script executable:
```bash
chmod +x run_payment_tests.sh
```

## Original Tests

The original `rinha.js` and `run-tests.sh` files are preserved for the Rinha de Backend competition testing.
