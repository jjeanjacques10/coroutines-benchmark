#!/bin/bash

echo "Starting the test script..."

mvn clean install -f democoroutines/pom.xml -DskipTests

docker build -t jjeanjacques10/democoroutines ./democoroutines --no-cache

echo "Stopping any existing Coroutines..."
docker-compose -f local_env/mockserver/docker-compose.yml down --remove-orphans
docker-compose -f local_env/redis/docker-compose.yml down --remove-orphans
docker-compose -f democoroutines/docker-compose.yml down --remove-orphans
docker-compose -f payment-processor/docker-compose.yml down --remove-orphans

echo "Building the Payment Processor service..."
docker-compose -f payment-processor/docker-compose.yml up -d --build

echo "Starting Redis and Demo Coroutines services..."
docker-compose -f local_env/observability/docker-compose.yml up -d --build
docker-compose -f local_env/mockserver/docker-compose.yml up -d --build
docker-compose -f local_env/redis/docker-compose.yml up -d --build
docker-compose -f democoroutines/docker-compose.yml up -d --build

echo "Setting up environment variables for k6..."
export K6_WEB_DASHBOARD=true
export K6_WEB_DASHBOARD_PORT=5665
export K6_WEB_DASHBOARD_PERIOD=2s
export K6_WEB_DASHBOARD_OPEN=true
export K6_WEB_DASHBOARD_EXPORT='report.html'

## Await for the services to be up and running
echo "Waiting for the services to be ready..."
sleep 15

echo "Running k6 tests..."
./k6-test/run_payment_tests.sh