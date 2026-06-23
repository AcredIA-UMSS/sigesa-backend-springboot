#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "Bringing up PostgreSQL (db) via docker-compose..."
docker-compose up -d db

# wait for db healthy
echo "Waiting for Postgres to become available..."
DB_CONTAINER="$(docker-compose ps -q db)"
if [ -z "$DB_CONTAINER" ]; then
  echo "Could not determine db container id" >&2
  exit 1
fi

until docker exec "$DB_CONTAINER" pg_isready -U sigesa >/dev/null 2>&1; do
  printf '.'; sleep 1
done

echo "\nPostgres is ready. Building and starting app image..."
# build app image (this will run mvn package in builder stage)
docker-compose build --no-cache app

docker-compose up -d app

# wait for app to respond on /actuator/health or port 8080
echo "Waiting for application to be healthy (http://localhost:8080) ..."
RETRIES=120
for i in $(seq 1 $RETRIES); do
  if curl -sS http://localhost:8080/actuator/health 2>/dev/null | grep -q "UP"; then
    echo "App is healthy"
    break
  fi
  if curl -sS http://localhost:8080/ 2>/dev/null; then
    echo "App responded on root (no actuator). Continuing"
    break
  fi
  printf '.'; sleep 2
done

# find report definition id by codigo using psql inside db container
DEF_ID=$(docker exec "$DB_CONTAINER" psql -U sigesa -d sigesa -t -c "select id from report_definition where codigo='E2E-KPIS' limit 1;" | tr -d '[:space:]')
if [ -z "$DEF_ID" ]; then
  echo "Could not find report definition 'E2E-KPIS' in DB" >&2
  exit 1
fi

echo "Found report definition id: $DEF_ID"

# submit export
echo "Submitting export request..."
RESP=$(curl -s -X POST "http://localhost:8080/api/v1/reports/${DEF_ID}/export" -H "Content-Type: application/json" -d '{}')

echo "Response: $RESP"
# extract run id from JSON (body is ReportRun JSON)
RUN_ID=$(echo "$RESP" | jq -r '.id // empty')
if [ -z "$RUN_ID" ]; then
  echo "Could not extract run id from response. Raw response:" >&2
  echo "$RESP" >&2
  exit 1
fi

echo "Export started with run id: $RUN_ID. Polling for completion..."

for i in $(seq 1 60); do
  sleep 2
  STATUS_JSON=$(curl -s "http://localhost:8080/api/v1/reports/runs/${RUN_ID}")
  STATUS=$(echo "$STATUS_JSON" | jq -r '.status // empty')
  echo "Status: $STATUS"
  if [ "$STATUS" = "COMPLETED" ]; then
    DOWNLOAD_URL=$(echo "$STATUS_JSON" | jq -r '.downloadUrl // empty')
    echo "Export completed. downloadUrl=$DOWNLOAD_URL"
    exit 0
  fi
  if [ "$STATUS" = "FAILED" ]; then
    echo "Export FAILED. metadata: $STATUS_JSON" >&2
    exit 1
  fi
done

echo "Export did not complete in time" >&2
exit 2
