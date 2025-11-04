# Example Output from Payment Strategy Load Tests

This document shows example output from running the k6 load tests for the three payment processing strategies.

## Running All Tests

```bash
$ ./run_payment_tests.sh
==========================================
  Running k6 Load Tests
  for Payment Processing Strategies
==========================================

[1/3] Running ASYNC_COROUTINE test...

          /\      |‾‾| /‾‾/   /‾‾/   
     /\  /  \     |  |/  /   /  /    
    /  \/    \    |     (   /   ‾‾\  
   /          \   |  |\  \ |  (‾)  | 
  / __________ \  |__| \__\ \_____/ .io

  execution: local
     script: payments_test.js
     output: -

  scenarios: (100.00%) 1 scenario, 50 max VUs, 1m0s max duration (incl. graceful stop):
           * ASYNC_COROUTINE: 50 looping VUs for 30s (gracefulStop: 30s)

     ✓ status is 2xx

     checks.........................: 100.00% ✓ 1482      ✗ 0   
     data_received..................: 192 kB  6.4 kB/s
     data_sent......................: 297 kB  9.9 kB/s
     errors.........................: 0.00%   ✓ 0         ✗ 1482
     http_req_blocked...............: avg=22.45µs  min=1.2µs    med=3.1µs    max=2.46ms   p(90)=5.2µs    p(95)=8.3µs   
     http_req_connecting............: avg=15.33µs  min=0s       med=0s       max=2.33ms   p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=50.89ms  min=25.12ms  med=48.34ms  max=142.56ms p(90)=65.23ms  p(95)=72.88ms 
       { expected_response:true }...: avg=50.89ms  min=25.12ms  med=48.34ms  max=142.56ms p(90)=65.23ms  p(95)=72.88ms 
     http_req_failed................: 0.00%   ✓ 0         ✗ 1482
     http_req_receiving.............: avg=34.67µs  min=10.2µs   med=29.4µs   max=453.7µs  p(90)=51.8µs   p(95)=63.2µs  
     http_req_sending...............: avg=18.23µs  min=4.8µs    med=14.2µs   max=1.12ms   p(90)=27.6µs   p(95)=35.4µs  
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=50.84ms  min=25.09ms  med=48.29ms  max=142.51ms p(90)=65.18ms  p(95)=72.82ms 
     http_reqs......................: 1482    49.388658/s
     iteration_duration.............: avg=1.05s    min=1.02s    med=1.04s    max=1.14s    p(90)=1.06s    p(95)=1.07s   
     iterations.....................: 1482    49.388658/s
     vus............................: 50      min=50      max=50
     vus_max........................: 50      min=50      max=50

✓ ASYNC_COROUTINE test completed

[2/3] Running SEQUENTIAL test...

  (Similar output for SEQUENTIAL strategy...)

✓ SEQUENTIAL test completed

[3/3] Running BLOCKING_THREAD test...

  (Similar output for BLOCKING_THREAD strategy...)

✓ BLOCKING_THREAD test completed

==========================================
  All tests completed successfully!
==========================================

Generated reports:
-rw-rw-r-- 1 user user 5.2K Nov  4 02:15 relatorio_async_coroutine.json
-rw-rw-r-- 1 user user 5.1K Nov  4 02:16 relatorio_sequential.json
-rw-rw-r-- 1 user user 5.3K Nov  4 02:17 relatorio_blocking_thread.json

To view a report summary, use:
  cat relatorio_async_coroutine.json | jq
```

## Comparing Results

```bash
$ ./compare_reports.sh
==========================================
  Payment Strategy Performance Comparison
==========================================

=== HTTP Request Duration (ms) ===

Strategy                    Min        Avg     Median        P90        P95        P99
-----------------------------------------------------------------------------------------
async_coroutine           25.12      50.89      48.34      65.23      72.88      89.45
sequential                42.35      78.21      76.12      95.67     103.22     125.78
blocking_thread           58.91     112.45     108.23     142.89     158.34     189.67

=== Throughput & Error Rate ===

Strategy             Total Requests         Req/sec    Error Rate %
-----------------------------------------------------------------------------------------
async_coroutine                1482           49.39            0.00
sequential                      768           25.60            0.00
blocking_thread                 267            8.90            1.12

=== Test Duration & Data Transfer ===

Strategy             Duration (s)   Data Sent (KB)  Data Recv (KB)
-----------------------------------------------------------------------------------------
async_coroutine             30.01          297.45          192.34
sequential                  30.02          153.87          99.12
blocking_thread             30.05           53.67          34.21

=== Check Success Rate ===

Strategy             Check Pass %
-----------------------------------------------
async_coroutine             100.00
sequential                  100.00
blocking_thread              98.88

==========================================
  Report files analyzed:
    - relatorio_async_coroutine.json
    - relatorio_sequential.json
    - relatorio_blocking_thread.json
==========================================
```

## Key Insights from Example Results

Based on the example output above:

1. **ASYNC_COROUTINE** (Non-blocking strategy)
   - ✅ Highest throughput: ~49 req/s
   - ✅ Lowest latency: p99 = 89.45ms
   - ✅ Best overall performance
   - ✅ 100% success rate

2. **SEQUENTIAL** (Baseline)
   - ⚠️ Medium throughput: ~26 req/s (47% slower than async)
   - ⚠️ Medium latency: p99 = 125.78ms (41% slower than async)
   - ✅ 100% success rate
   - Good for comparison baseline

3. **BLOCKING_THREAD** (Thread-blocking strategy)
   - ❌ Lowest throughput: ~9 req/s (82% slower than async)
   - ❌ Highest latency: p99 = 189.67ms (112% slower than async)
   - ⚠️ 1.12% error rate (resource exhaustion under load)
   - Not recommended for high-load scenarios

## Recommendations

- **For production**: Use `ASYNC_COROUTINE` for optimal performance
- **For debugging**: Use `SEQUENTIAL` for simpler execution flow
- **Avoid**: `BLOCKING_THREAD` in high-concurrency scenarios

## Report Structure

Each JSON report contains the following sections:

```json
{
  "metrics": {
    "checks": { "values": { "rate": 1.0, "passes": 1482, "fails": 0 } },
    "data_received": { "values": { "count": 196608, "rate": 6400 } },
    "data_sent": { "values": { "count": 304128, "rate": 9900 } },
    "errors": { "values": { "count": 0, "rate": 0 } },
    "http_req_blocked": { "values": { "min": 0.0012, "avg": 0.022, "max": 2.46 } },
    "http_req_duration": {
      "values": {
        "min": 25.12,
        "avg": 50.89,
        "med": 48.34,
        "max": 142.56,
        "p(90)": 65.23,
        "p(95)": 72.88,
        "p(99)": 89.45
      }
    },
    "http_req_failed": { "values": { "rate": 0, "passes": 1482, "fails": 0 } },
    "http_reqs": { "values": { "count": 1482, "rate": 49.39 } },
    "iterations": { "values": { "count": 1482, "rate": 49.39 } },
    "vus": { "values": { "min": 50, "max": 50, "value": 50 } }
  },
  "root_group": {
    "checks": [ ... ],
    "groups": [ ... ]
  },
  "state": {
    "isStdOutTTY": true,
    "isStdErrTTY": true,
    "testRunDurationMs": 30010
  }
}
```
