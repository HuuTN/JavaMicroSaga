#!/bin/sh
# Register all connectors with Debezium Connect (expects connect at http://localhost:8083)
set -e
CONNECT_URL=http://localhost:8083
for f in /connectors/*.json; do
  echo "Registering connector $f"
  curl -s -X POST -H "Content-Type: application/json" --data-binary @$f ${CONNECT_URL}/connectors || true
done
echo "Done"
