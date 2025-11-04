# Quick Start Guide - k6 Payment Strategy Load Tests

This is a quick reference for running k6 load tests on the three payment processing strategies.

## Prerequisites

```bash
# Install k6 following the official instructions for your platform:
# https://grafana.com/docs/k6/latest/set-up/install-k6/

# Quick install examples (see official docs for latest commands):

# macOS
brew install k6

# Ubuntu/Debian - See: https://k6.io/docs/get-started/installation/#debian-ubuntu
# Follow the official APT repository instructions

# Windows (using Chocolatey)
choco install k6

# Or download binary directly from:
# https://github.com/grafana/k6/releases
```

## File Structure

```
k6-test/
├── payments_test.js           # Main k6 test script (modular, configurable)
├── run_payment_tests.sh       # Wrapper to run all 3 tests
├── compare_reports.sh         # Compare results across strategies
├── PAYMENT_TESTS_README.md    # Full documentation
├── EXAMPLE_OUTPUT.md          # Example results and analysis
└── QUICK_START.md            # This file
```

## Usage

### 1. Start Your Backend

Make sure your application is running on `http://localhost:9999`:

```bash
# From the project root
cd democoroutines
./mvnw spring-boot:run
# or
java -jar target/democoroutines-*.jar
```

### 2. Run All Tests (Recommended)

```bash
cd k6-test
./run_payment_tests.sh
```

This runs 3 independent tests (30 seconds each) and generates:
- `relatorio_async_coroutine.json`
- `relatorio_sequential.json`
- `relatorio_blocking_thread.json`

### 3. Compare Results

```bash
./compare_reports.sh
```

Displays a comparison table with latencies, throughput, and error rates.

## Alternative: Run Individual Tests

### Test ASYNC_COROUTINE only
```bash
k6 run -e STRATEGY=ASYNC_COROUTINE payments_test.js --summary-export=relatorio_async_coroutine.json
```

### Test SEQUENTIAL only
```bash
k6 run -e STRATEGY=SEQUENTIAL payments_test.js --summary-export=relatorio_sequential.json
```

### Test BLOCKING_THREAD only
```bash
k6 run -e STRATEGY=BLOCKING_THREAD payments_test.js --summary-export=relatorio_blocking_thread.json
```

## Customizing Test Parameters

Edit `payments_test.js` and modify the scenario configuration:

```javascript
const baseScenarios = {
  ASYNC_COROUTINE: {
    executor: 'constant-vus',
    vus: 50,          // ← Change number of virtual users
    duration: '30s',  // ← Change test duration
  },
  // ... other strategies
};
```

## Key Metrics to Watch

| Metric | What it Means | Good Value |
|--------|---------------|------------|
| `http_req_duration p(99)` | 99th percentile latency | < 500ms |
| `http_reqs (rate)` | Throughput (req/s) | Higher is better |
| `errors (rate)` | Error rate | < 1% |
| `checks (rate)` | Success rate | 100% |

## Understanding Strategies

| Strategy | Best For | Expected Performance |
|----------|----------|---------------------|
| **ASYNC_COROUTINE** | Production workloads | Highest throughput, lowest latency |
| **SEQUENTIAL** | Debugging, baseline | Medium performance |
| **BLOCKING_THREAD** | Legacy code comparison | Lowest performance, may have errors under load |

## Troubleshooting

### Backend not responding
```bash
# Check if backend is up
curl http://localhost:9999/payments-summary

# If not, start it:
cd democoroutines && ./mvnw spring-boot:run
```

### Permission denied on scripts
```bash
chmod +x run_payment_tests.sh compare_reports.sh
```

### k6 command not found
Install k6 following the instructions at the top of this guide.

### Reports not found when comparing
Run the tests first: `./run_payment_tests.sh`

## Next Steps

1. Run tests: `./run_payment_tests.sh`
2. Compare results: `./compare_reports.sh`
3. Read detailed docs: `PAYMENT_TESTS_README.md`
4. See example output: `EXAMPLE_OUTPUT.md`

## Test Execution Flow

```
run_payment_tests.sh
  │
  ├─> Run ASYNC_COROUTINE test
  │     └─> Generate relatorio_async_coroutine.json
  │
  ├─> Run SEQUENTIAL test
  │     └─> Generate relatorio_sequential.json
  │
  └─> Run BLOCKING_THREAD test
        └─> Generate relatorio_blocking_thread.json

Then:
compare_reports.sh
  │
  └─> Analyze all 3 reports
        └─> Display comparison table
```

## Advanced: Web Dashboard

For real-time monitoring during tests:

```bash
export K6_WEB_DASHBOARD=true
export K6_WEB_DASHBOARD_PORT=5665
export K6_WEB_DASHBOARD_OPEN=true

k6 run -e STRATEGY=ASYNC_COROUTINE payments_test.js
```

Browser will open at `http://localhost:5665`

---

**For full documentation, see:** [PAYMENT_TESTS_README.md](PAYMENT_TESTS_README.md)
