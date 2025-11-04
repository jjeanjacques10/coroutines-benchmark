#!/bin/bash -e
# Script to compare performance metrics from the three strategy reports

# Note: -e flag in shebang ensures script fails fast on any command errors
# set -e below is redundant but kept for clarity

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
    
    # Extract all metrics in a single jq call for efficiency
    read -r min avg med p90 p95 p99 <<< $(jq -r '
        .metrics.http_req_duration.values |
        [(.min // 0), (.avg // 0), (.med // 0), (.["p(90)"] // 0), (.["p(95)"] // 0), (.["p(99)"] // 0)] |
        map(. * 1 | tostring) | join(" ")
    ' "$report")
    
    min=$(printf "%.2f" "$min")
    avg=$(printf "%.2f" "$avg")
    med=$(printf "%.2f" "$med")
    p90=$(printf "%.2f" "$p90")
    p95=$(printf "%.2f" "$p95")
    p99=$(printf "%.2f" "$p99")
    
    printf "%-20s %10s %10s %10s %10s %10s %10s\n" "$strategy" "$min" "$avg" "$med" "$p90" "$p95" "$p99"
done

echo ""
echo "=== Throughput & Error Rate ==="
echo ""
printf "%-20s %15s %15s %15s\n" "Strategy" "Total Requests" "Req/sec" "Error Rate %"
echo "-----------------------------------------------------------------------------------------"

for report in "${reports[@]}"; do
    strategy=$(basename "$report" .json | sed 's/relatorio_//')
    
    # Extract all metrics in a single jq call for efficiency
    read -r count rate error_rate <<< $(jq -r '
        [
            (.metrics.http_reqs.values.count // 0),
            (.metrics.http_reqs.values.rate // 0),
            ((.metrics.errors.values.rate // 0) * 100)
        ] | map(. * 1 | tostring) | join(" ")
    ' "$report")
    
    rate=$(printf "%.2f" "$rate")
    error_rate=$(printf "%.2f" "$error_rate")
    
    printf "%-20s %15s %15s %15s\n" "$strategy" "$count" "$rate" "$error_rate"
done

echo ""
echo "=== Test Duration & Data Transfer ==="
echo ""
printf "%-20s %15s %15s %15s\n" "Strategy" "Duration (s)" "Data Sent (KB)" "Data Recv (KB)"
echo "-----------------------------------------------------------------------------------------"

for report in "${reports[@]}"; do
    strategy=$(basename "$report" .json | sed 's/relatorio_//')
    
    # Extract all metrics in a single jq call for efficiency
    read -r root_time data_sent data_recv <<< $(jq -r '
        [
            ((.state.testRunDurationMs // 0) / 1000),
            ((.metrics.data_sent.values.count // 0) / 1024),
            ((.metrics.data_received.values.count // 0) / 1024)
        ] | map(. * 1 | tostring) | join(" ")
    ' "$report")
    
    root_time=$(printf "%.2f" "$root_time")
    data_sent=$(printf "%.2f" "$data_sent")
    data_recv=$(printf "%.2f" "$data_recv")
    
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
    check_rate=$(jq -r '((.metrics.checks.values.rate // 1) * 100) | tostring' "$report")
    check_rate=$(printf "%.2f" "$check_rate")
    
    printf "%-20s %15s\n" "$strategy" "$check_rate"
done

echo ""
echo "=========================================="
echo "  Report files analyzed:"
for report in "${reports[@]}"; do
    echo "    - $(basename "$report")"
done
echo "=========================================="
