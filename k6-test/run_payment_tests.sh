#!/bin/bash -e
# Wrapper script to run k6 tests for three payment-processing strategies
# Generates separate JSON reports for each strategy

# Note: -e flag in shebang ensures script fails fast on any command errors
# set -e below is redundant but kept for clarity

set -e

K6_SCRIPT="$(dirname "$0")/payments_test.js"
OUTPUT_DIR="$(dirname "$0")"

echo "=========================================="
echo "  Running k6 Load Tests"
echo "  for Payment Processing Strategies"
echo "=========================================="
echo ""

# Test 1: ASYNC_COROUTINE
echo "[1/3] Running ASYNC_COROUTINE test..."
k6 run -e STRATEGY=ASYNC_COROUTINE "$K6_SCRIPT" --summary-export="$OUTPUT_DIR/relatorio_async_coroutine.json"
echo "✓ ASYNC_COROUTINE test completed"
echo ""

# Test 2: SEQUENTIAL
echo "[2/3] Running SEQUENTIAL test..."
k6 run -e STRATEGY=SEQUENTIAL "$K6_SCRIPT" --summary-export="$OUTPUT_DIR/relatorio_sequential.json"
echo "✓ SEQUENTIAL test completed"
echo ""

# Test 3: BLOCKING_THREAD
echo "[3/3] Running BLOCKING_THREAD test..."
k6 run -e STRATEGY=BLOCKING_THREAD "$K6_SCRIPT" --summary-export="$OUTPUT_DIR/relatorio_blocking_thread.json"
echo "✓ BLOCKING_THREAD test completed"
echo ""

echo "=========================================="
echo "  All tests completed successfully!"
echo "=========================================="
echo ""
echo "Generated reports:"
ls -lh "$OUTPUT_DIR"/relatorio_*.json
echo ""
echo "To view a report summary, use:"
echo "  cat $OUTPUT_DIR/relatorio_async_coroutine.json | jq"
