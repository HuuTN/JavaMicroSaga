# 🔧 CURL COMMANDS - Quick Test Reference

> **15+ curl commands để test nhanh từ terminal**

---

## 📌 PREREQUISITES

```bash
# Set base URLs as environment variables (Optional)
export SAGA_URL="http://localhost:8070"
export ORDER_URL="http://localhost:8071"
export PAYMENT_URL="http://localhost:8072"
export INVENTORY_URL="http://localhost:8073"
export NOTIFICATION_URL="http://localhost:8074"
export DEBEZIUM_URL="http://localhost:8085"
```

---

## ✅ 1. HEALTH CHECKS

### Check All Services Health

```bash
# Saga Orchestrator
curl http://localhost:8070/actuator/health

# Order Service
curl http://localhost:8071/actuator/health

# Payment Service
curl http://localhost:8072/actuator/health

# Inventory Service
curl http://localhost:8073/actuator/health

# Notification Service
curl http://localhost:8074/actuator/health
```

**One-liner to check all:**
```bash
for port in 8070 8071 8072 8073 8074; do 
  echo "Port $port: $(curl -s http://localhost:$port/actuator/health | jq -r '.status')"; 
done
```

---

## 🎯 2. HAPPY PATH - CREATE ORDERS

### Scenario 1: Simple Order (Single Item)

```bash
curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST001",
    "items": [
      {
        "productId": "PROD001",
        "productName": "Laptop Dell XPS 15",
        "quantity": 1,
        "price": 1500.00
      }
    ],
    "totalAmount": 1500.00,
    "shippingAddress": {
      "street": "123 Nguyen Hue",
      "city": "Ho Chi Minh",
      "country": "Vietnam",
      "zipCode": "700000"
    },
    "paymentMethod": "CREDIT_CARD"
  }' | jq
```

**Expected Response:**
```json
{
  "sagaId": "saga-abc123",
  "orderId": "order-xyz789",
  "status": "COMPLETED",
  "transactionId": "txn-456"
}
```

---

### Scenario 2: Multiple Items Order

```bash
curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST002",
    "items": [
      {
        "productId": "PROD003",
        "productName": "iPhone 15 Pro",
        "quantity": 1,
        "price": 1200.00
      },
      {
        "productId": "PROD004",
        "productName": "AirPods Pro",
        "quantity": 1,
        "price": 249.00
      },
      {
        "productId": "PROD005",
        "productName": "iPhone Case",
        "quantity": 2,
        "price": 29.00
      }
    ],
    "totalAmount": 1507.00,
    "shippingAddress": {
      "street": "456 Le Loi",
      "city": "Da Nang",
      "country": "Vietnam",
      "zipCode": "550000"
    },
    "paymentMethod": "DEBIT_CARD"
  }' | jq
```

---

### Scenario 3: High-Value Order

```bash
curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST003",
    "items": [
      {
        "productId": "PROD006",
        "productName": "MacBook Pro 16 M3",
        "quantity": 2,
        "price": 2999.00
      }
    ],
    "totalAmount": 5998.00,
    "shippingAddress": {
      "street": "789 Tran Hung Dao",
      "city": "Hanoi",
      "country": "Vietnam",
      "zipCode": "100000"
    },
    "paymentMethod": "CREDIT_CARD"
  }' | jq
```

---

## ❌ 3. FAILURE SCENARIOS

### Scenario 1: Payment Failed (Trigger Compensation)

```bash
curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST_PAYMENT_FAIL",
    "items": [
      {
        "productId": "PROD007",
        "productName": "Gaming Laptop",
        "quantity": 1,
        "price": 2500.00
      }
    ],
    "totalAmount": 2500.00,
    "shippingAddress": {
      "street": "100 Test Street",
      "city": "Test City",
      "country": "Vietnam",
      "zipCode": "000000"
    },
    "paymentMethod": "INVALID_CARD"
  }' | jq
```

**Expected:** Order created, payment fails, compensation triggered

---

### Scenario 2: Inventory Insufficient

```bash
curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST004",
    "items": [
      {
        "productId": "OUT_OF_STOCK",
        "productName": "Limited Edition Product",
        "quantity": 999999,
        "price": 500.00
      }
    ],
    "totalAmount": 500.00,
    "shippingAddress": {
      "street": "200 Test Avenue",
      "city": "Test City",
      "country": "Vietnam",
      "zipCode": "000000"
    },
    "paymentMethod": "CREDIT_CARD"
  }' | jq
```

**Expected:** Payment successful, inventory fails, compensation chain executed

---

### Scenario 3: Service Timeout

```bash
curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -H "X-Simulate-Timeout: true" \
  -d '{
    "customerId": "CUST_TIMEOUT",
    "items": [
      {
        "productId": "PROD008",
        "productName": "Test Product",
        "quantity": 1,
        "price": 100.00
      }
    ],
    "totalAmount": 100.00,
    "shippingAddress": {
      "street": "300 Timeout Road",
      "city": "Test City",
      "country": "Vietnam",
      "zipCode": "000000"
    },
    "paymentMethod": "CREDIT_CARD"
  }' | jq
```

---

## 🔍 4. MONITORING & STATUS

### Get Saga Status by ID

```bash
# Replace {saga_id} with actual saga ID from previous response
SAGA_ID="saga-abc123"
curl http://localhost:8070/api/saga/$SAGA_ID/status | jq
```

---

### List All Active Sagas

```bash
curl http://localhost:8070/api/saga/active | jq
```

---

### Get Saga Execution History

```bash
SAGA_ID="saga-abc123"
curl http://localhost:8070/api/saga/$SAGA_ID/history | jq
```

---

### Get Order Details

```bash
ORDER_ID="order-xyz789"
curl http://localhost:8071/api/orders/$ORDER_ID | jq
```

---

### Check Payment Status

```bash
ORDER_ID="order-xyz789"
curl http://localhost:8072/api/payments?orderId=$ORDER_ID | jq
```

---

### Check Inventory Reservation

```bash
ORDER_ID="order-xyz789"
curl http://localhost:8073/api/inventory/reservations/$ORDER_ID | jq
```

---

## 📊 5. CDC & KAFKA VERIFICATION

### List All Debezium Connectors

```bash
curl http://localhost:8085/connectors | jq
```

**Expected:**
```json
[
  "order-outbox-connector",
  "payment-outbox-connector",
  "inventory-outbox-connector",
  "notification-outbox-connector"
]
```

---

### Get Connector Status

```bash
# Order Connector
curl http://localhost:8085/connectors/order-outbox-connector/status | jq

# Payment Connector
curl http://localhost:8085/connectors/payment-outbox-connector/status | jq

# Inventory Connector
curl http://localhost:8085/connectors/inventory-outbox-connector/status | jq

# Notification Connector
curl http://localhost:8085/connectors/notification-outbox-connector/status | jq
```

---

### Check All Connectors Status (One-liner)

```bash
for connector in order-outbox-connector payment-outbox-connector inventory-outbox-connector notification-outbox-connector; do
  echo "=== $connector ==="
  curl -s http://localhost:8085/connectors/$connector/status | jq '.connector.state, .tasks[0].state'
  echo ""
done
```

---

### Restart Failed Connector

```bash
# Restart specific connector
curl -X POST http://localhost:8085/connectors/order-outbox-connector/restart

# Restart connector task
curl -X POST http://localhost:8085/connectors/order-outbox-connector/tasks/0/restart
```

---

## 🚀 6. BULK OPERATIONS

### Create 5 Orders Sequentially

```bash
for i in {1..5}; do
  echo "Creating order #$i..."
  curl -X POST http://localhost:8070/api/saga/orders \
    -H "Content-Type: application/json" \
    -d "{
      \"customerId\": \"CUST_BULK_$i\",
      \"items\": [
        {
          \"productId\": \"PROD_BULK\",
          \"productName\": \"Bulk Test Product\",
          \"quantity\": 1,
          \"price\": 100.00
        }
      ],
      \"totalAmount\": 100.00,
      \"shippingAddress\": {
        \"street\": \"$i Test Street\",
        \"city\": \"Test City\",
        \"country\": \"Vietnam\",
        \"zipCode\": \"000000\"
      },
      \"paymentMethod\": \"CREDIT_CARD\"
    }" | jq -r '.orderId'
  sleep 2
done
```

---

### Create Orders Concurrently (Background Jobs)

```bash
# Create 10 orders in parallel
for i in {1..10}; do
  (
    curl -X POST http://localhost:8070/api/saga/orders \
      -H "Content-Type: application/json" \
      -d "{
        \"customerId\": \"CUST_CONCURRENT_$i\",
        \"items\": [{
          \"productId\": \"PROD_CONCURRENT\",
          \"productName\": \"Concurrent Test\",
          \"quantity\": 1,
          \"price\": 50.00
        }],
        \"totalAmount\": 50.00,
        \"shippingAddress\": {
          \"street\": \"$i Concurrent St\",
          \"city\": \"Test City\",
          \"country\": \"Vietnam\",
          \"zipCode\": \"000000\"
        },
        \"paymentMethod\": \"CREDIT_CARD\"
      }" > /dev/null 2>&1
  ) &
done

# Wait for all background jobs to complete
wait
echo "All concurrent orders created!"
```

---

## 📈 7. PERFORMANCE TESTING

### Measure Response Time

```bash
# Single request with timing
time curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST_PERF",
    "items": [{
      "productId": "PROD_PERF",
      "productName": "Performance Test",
      "quantity": 1,
      "price": 100.00
    }],
    "totalAmount": 100.00,
    "shippingAddress": {
      "street": "Perf Street",
      "city": "Test City",
      "country": "Vietnam",
      "zipCode": "000000"
    },
    "paymentMethod": "CREDIT_CARD"
  }' | jq
```

---

### Benchmark with Apache Bench

```bash
# Install Apache Bench (if not installed)
# Ubuntu/Debian: sudo apt-get install apache2-utils
# macOS: brew install httpd (includes ab)

# Create test-order.json file first
cat > test-order.json << 'EOF'
{
  "customerId": "CUST_AB",
  "items": [{
    "productId": "PROD_AB",
    "productName": "AB Test Product",
    "quantity": 1,
    "price": 100.00
  }],
  "totalAmount": 100.00,
  "shippingAddress": {
    "street": "AB Test Street",
    "city": "Test City",
    "country": "Vietnam",
    "zipCode": "000000"
  },
  "paymentMethod": "CREDIT_CARD"
}
EOF

# Run 100 requests with 10 concurrent connections
ab -n 100 -c 10 -p test-order.json -T application/json \
  http://localhost:8070/api/saga/orders
```

---

## 🛠️ 8. TROUBLESHOOTING COMMANDS

### Check Kafka Topics

```bash
# List all topics
docker exec kafka-day5 kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# Get topic details
docker exec kafka-day5 kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic order-events
```

---

### Consume Kafka Messages

```bash
# Consume order events from beginning
docker exec kafka-day5 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  --max-messages 10

# Consume with timestamp
docker exec kafka-day5 kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  --property print.timestamp=true \
  --property print.key=true
```

---

### Check Database Records

```bash
# Order Service Database
docker exec mysql-order mysql -uroot -pfpt@123 order_service_db \
  -e "SELECT order_id, customer_id, status, created_at FROM orders ORDER BY created_at DESC LIMIT 5;"

# Check outbox events
docker exec mysql-order mysql -uroot -pfpt@123 order_service_db \
  -e "SELECT id, aggregate_id, event_type, processed, created_at FROM outbox_events ORDER BY created_at DESC LIMIT 10;"

# Payment records
docker exec mysql-payment mysql -uroot -pfpt@123 payment_service_db \
  -e "SELECT payment_id, order_id, amount, status FROM payments ORDER BY created_at DESC LIMIT 5;"

# Inventory reservations
docker exec mysql-inventory mysql -uroot -pfpt@123 inventory_service_db \
  -e "SELECT reservation_id, order_id, product_id, quantity, status FROM reservations ORDER BY created_at DESC LIMIT 5;"
```

---

### Check Container Logs

```bash
# Saga Orchestrator logs (if containerized)
docker logs saga-orchestrator-service --tail 50 -f

# Order Service logs
docker logs order-service --tail 50 -f

# Debezium Connect logs
docker logs debezium-connect --tail 50 -f | grep ERROR

# Kafka logs
docker logs kafka-day5 --tail 50 -f
```

---

## 📋 9. CLEANUP COMMANDS

### Delete Test Data

```bash
# WARNING: This will delete all data!

# Clear orders
docker exec mysql-order mysql -uroot -pfpt@123 order_service_db \
  -e "DELETE FROM orders WHERE customer_id LIKE 'CUST_TEST%';"

# Clear outbox events
docker exec mysql-order mysql -uroot -pfpt@123 order_service_db \
  -e "DELETE FROM outbox_events WHERE processed = true AND created_at < DATE_SUB(NOW(), INTERVAL 1 HOUR);"

# Clear payments
docker exec mysql-payment mysql -uroot -pfpt@123 payment_service_db \
  -e "DELETE FROM payments WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 HOUR);"
```

---

### Reset Kafka Consumer Groups

```bash
# Reset consumer group offset
docker exec kafka-day5 kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group saga-orchestrator-group \
  --reset-offsets \
  --to-earliest \
  --all-topics \
  --execute
```

---

## 💡 TIPS & TRICKS

### Save Response to Variable

```bash
# Bash
RESPONSE=$(curl -s -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -d '{ ... }')

ORDER_ID=$(echo $RESPONSE | jq -r '.orderId')
SAGA_ID=$(echo $RESPONSE | jq -r '.sagaId')

echo "Order ID: $ORDER_ID"
echo "Saga ID: $SAGA_ID"
```

---

### Pretty Print JSON

```bash
# Using jq
curl http://localhost:8070/api/saga/active | jq '.'

# Using python
curl http://localhost:8070/api/saga/active | python -m json.tool

# Save to file
curl http://localhost:8070/api/saga/active | jq '.' > active-sagas.json
```

---

### Test with Custom Headers

```bash
curl -X POST http://localhost:8070/api/saga/orders \
  -H "Content-Type: application/json" \
  -H "X-Request-ID: test-request-123" \
  -H "X-Correlation-ID: correlation-456" \
  -H "X-Simulate-Timeout: true" \
  -d '{ ... }' | jq
```

---

### Retry Failed Request

```bash
# Retry up to 3 times on failure
for i in {1..3}; do
  echo "Attempt $i..."
  if curl -f -X POST http://localhost:8070/api/saga/orders \
    -H "Content-Type: application/json" \
    -d '{ ... }'; then
    echo "Success!"
    break
  else
    echo "Failed, retrying..."
    sleep 2
  fi
done
```

---

## 📚 QUICK REFERENCE CHEAT SHEET

```bash
# Health Check All Services
for port in 8070 8071 8072 8073 8074; do curl -s http://localhost:$port/actuator/health | jq; done

# Create Order
curl -X POST localhost:8070/api/saga/orders -H "Content-Type: application/json" -d @order.json | jq

# Get Saga Status
curl localhost:8070/api/saga/{saga_id}/status | jq

# List Connectors
curl localhost:8085/connectors | jq

# Check Connector Status
curl localhost:8085/connectors/order-outbox-connector/status | jq '.connector.state, .tasks[0].state'

# Consume Kafka Messages
docker exec kafka-day5 kafka-console-consumer --bootstrap-server localhost:9092 --topic order-events --from-beginning

# Check Database
docker exec mysql-order mysql -uroot -pfpt@123 order_service_db -e "SELECT * FROM orders LIMIT 5;"
```

---

🎉 **Copy-Paste Ready Commands!** 🚀
