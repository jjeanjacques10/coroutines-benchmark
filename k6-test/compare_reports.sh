#!/bin/bash
# Script to compare performance metrics from the three strategy reports

set -e

SCRIPT_DIR="$(dirname "$0")"

echo "=========================================="
echo "  Payment Strategy Performance Comparison"
echo "=========================================="
echo ""

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: jq is required but not installed."
    echo "Install it with: sudo apt-get install jq"
    exit 1
fi

# Check if all report files exist
reports=("$SCRIPT_DIR/relatorio_async_coroutine.json" "$SCRIPT_DIR/relatorio_sequential.json" "$SCRIPT_DIR/relatorio_blocking_thread.json")
missing=false

for report in "${reports[@]}"; do
    if [ ! -f "$report" ]; then
        echo "Error: Report file not found: $report"
        missing=true
    fi
done

if [ "$missing" = true ]; then
    echo ""
    echo "Please run the tests first: ./run_payment_tests.sh"
    exit 1
fi

echo "=== HTTP Request Duration (ms) ==="
echo ""
printf "%-20s %10s %10s %10s %10s %10s %10s\n" "Strategy" "Min" "Avg" "Median" "P90" "P95" "P99"
echo "-----------------------------------------------------------------------------------------"

for report in "${reports[@]}"; do
    strategy=$(basename "$report" .json | sed 's/relatorio_//')
    
    # Extract metrics with fallback to 0 if not found
    min=$(jq -r '.metrics.http_req_duration.values.min // 0' "$report" | awk '{printf "%.2f", $1}')
    avg=$(jq -r '.metrics.http_req_duration.values.avg // 0' "$report" | awk '{printf "%.2f", $1}')
    med=$(jq -r '.metrics.http_req_duration.values.med // 0' "$report" | awk '{printf "%.2f", $1}')
    p90=$(jq -r '.metrics.http_req_duration.values["p(90)"] // 0' "$report" | awk '{printf "%.2f", $1}')
    p95=$(jq -r '.metrics.http_req_duration.values["p(95)"] // 0' "$report" | awk '{printf "%.2f", $1}')
    p99=$(jq -r '.metrics.http_req_duration.values["p(99)"] // 0' "$report" | awk '{printf "%.2f", $1}')
    
    printf "%-20s %10s %10s %10s %10s %10s %10s\n" "$strategy" "$min" "$avg" "$med" "$p90" "$p95" "$p99"
done

echo ""
echo "=== Throughput & Error Rate ==="
echo ""
printf "%-20s %15s %15s %15s\n" "Strategy" "Total Requests" "Req/sec" "Error Rate %"
echo "-----------------------------------------------------------------------------------------"

for report in "${reports[@]}"; do
    strategy=$(basename "$report" .json | sed 's/relatorio_//')
    
    # Extract metrics
    count=$(jq -r '.metrics.http_reqs.values.count // 0' "$report")
    rate=$(jq -r '.metrics.http_reqs.values.rate // 0' "$report" | awk '{printf "%.2f", $1}')
    error_rate=$(jq -r '.metrics.errors.values.rate // 0' "$report" | awk '{printf "%.2f", $1 * 100}')
    
    printf "%-20s %15s %15s %15s\n" "$strategy" "$count" "$rate" "$error_rate"
done

echo ""
echo "=== Test Duration & Data Transfer ==="
echo ""
printf "%-20s %15s %15s %15s\n" "Strategy" "Duration (s)" "Data Sent (KB)" "Data Recv (KB)"
echo "-----------------------------------------------------------------------------------------"

for report in "${reports[@]}"; do
    strategy=$(basename "$report" .json | sed 's/relatorio_//')
    
    # Extract root metrics
    root_time=$(jq -r '.state.testRunDurationMs // 0' "$report" | awk '{printf "%.2f", $1/1000}')
    data_sent=$(jq -r '.metrics.data_sent.values.count // 0' "$report" | awk '{printf "%.2f", $1/1024}')
    data_recv=$(jq -r '.metrics.data_received.values.count // 0' "$report" | awk '{printf "%.2f", $1/1024}')
    
    printf "%-20s %15s %15s %15s\n" "$strategy" "$root_time" "$data_sent" "$data_recv"
done

echo ""
echo "=== Check Success Rate ==="
echo ""
printf "%-20s %15s\n" "Strategy" "Check Pass %"
echo "-----------------------------------------------"

for report in "${reports[@]}"; do
    strategy=$(basename "$report" .json | sed 's/relatorio_//')
    
    # Extract checks metric
    check_rate=$(jq -r '.metrics.checks.values.rate // 1' "$report" | awk '{printf "%.2f", $1 * 100}')
    
    printf "%-20s %15s\n" "$strategy" "$check_rate"
done

echo ""
echo "=========================================="
echo "  Report files analyzed:"
for report in "${reports[@]}"; do
    echo "    - $(basename "$report")"
done
echo "=========================================="
