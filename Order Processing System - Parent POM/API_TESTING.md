# MuleSoft Order Processing System - API Testing Guide

Comprehensive guide for testing all APIs in the Order Processing System.

## Quick Start

### 1. Verify All Applications Running

```bash
# Check all ports are listening
netstat -tuln | grep -E '808[1-6]'

# Or use curl to check each endpoint
for port in 8081 8082 8083 8084 8085 8086; do
  echo "Checking port $port..."
  curl -s -I http://localhost:$port/api/v1/ || echo "Port $port: NOT RUNNING"
done
```

## API Testing Scenarios

### Scenario 1: Successful Order Processing

#### Step 1: Create Order

```bash
# Request
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "items": [
      {
        "itemId": "ITEM001",
        "quantity": 2,
        "price": 29.99
      },
      {
        "itemId": "ITEM002",
        "quantity": 1,
        "price": 49.99
      }
    ]
  }'

# Expected Response (202 Accepted)
# {
#   "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
#   "status": "RECEIVED",
#   "createdAt": "2024-01-28T10:00:00Z"
# }
```

#### Step 2: Poll Order Status

```bash
# Save orderId from previous response
ORDER_ID="a1b2c3d4-e5f6-7890-abcd-ef1234567890"

# Poll status multiple times to see state transitions
for i in {1..10}; do
  echo "Poll #$i:"
  curl -s -X GET http://localhost:8081/api/v1/orders/$ORDER_ID/status \
    -H "X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000" | jq .
  sleep 2
done

# Expected status progression:
# RECEIVED → INVENTORY_CHECK_STARTED → INVENTORY_CONFIRMED → 
# PAYMENT_COMPLETED → SHIPPING_REQUESTED → SHIPPED
```

### Scenario 2: Inventory Check Failure

```bash
# Create order that will fail inventory check (30% failure rate)
# Run multiple times until inventory check fails

for i in {1..5}; do
  echo "Attempt $i:"
  
  RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/orders \
    -H "Content-Type: application/json" \
    -H "X-Correlation-ID: test-inv-fail-$i" \
    -d '{
      "items": [{"itemId": "ITEM_FAIL", "quantity": 1, "price": 99.99}]
    }')
  
  ORDER_ID=$(echo $RESPONSE | jq -r '.orderId')
  
  # Wait and check status
  sleep 3
  
  STATUS=$(curl -s -X GET http://localhost:8081/api/v1/orders/$ORDER_ID/status \
    -H "X-Correlation-ID: test-inv-fail-$i" | jq -r '.status')
  
  echo "Order $ORDER_ID status: $STATUS"
  
  if [ "$STATUS" == "INVENTORY_FAILED" ]; then
    echo "✓ Inventory failure scenario captured"
    break
  fi
done
```

### Scenario 3: Payment Processing Failure

```bash
# Create order that will fail payment check (40% failure rate)
# Run multiple times until payment fails

for i in {1..5}; do
  echo "Attempt $i:"
  
  RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/orders \
    -H "Content-Type: application/json" \
    -H "X-Correlation-ID: test-pay-fail-$i" \
    -d '{
      "items": [{"itemId": "ITEM_PAY", "quantity": 1, "price": 199.99}]
    }')
  
  ORDER_ID=$(echo $RESPONSE | jq -r '.orderId')
  
  # Wait and check status
  sleep 5
  
  STATUS=$(curl -s -X GET http://localhost:8081/api/v1/orders/$ORDER_ID/status \
    -H "X-Correlation-ID: test-pay-fail-$i" | jq -r '.status')
  
  echo "Order $ORDER_ID status: $STATUS"
  
  if [ "$STATUS" == "PAYMENT_FAILED" ]; then
    echo "✓ Payment failure scenario captured"
    break
  fi
done
```

### Scenario 4: Shipping Failure (Async)

```bash
# Create order that will fail shipping (20% failure rate)
# Run multiple times until shipping fails

for i in {1..10}; do
  echo "Attempt $i:"
  
  RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/orders \
    -H "Content-Type: application/json" \
    -H "X-Correlation-ID: test-ship-fail-$i" \
    -d '{
      "items": [{"itemId": "ITEM_SHIP", "quantity": 1, "price": 299.99}]
    }')
  
  ORDER_ID=$(echo $RESPONSE | jq -r '.orderId')
  
  # Wait longer for shipping (async)
  sleep 8
  
  STATUS=$(curl -s -X GET http://localhost:8081/api/v1/orders/$ORDER_ID/status \
    -H "X-Correlation-ID: test-ship-fail-$i" | jq -r '.status')
  
  echo "Order $ORDER_ID status: $STATUS"
  
  if [ "$STATUS" == "SHIPPING_FAILED" ]; then
    echo "✓ Shipping failure scenario captured"
    break
  fi
done
```

## Direct API Testing

### Test Inventory System API

```bash
# Successful check
curl -X POST http://localhost:8083/api/v1/inventory/check \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-inv-001" \
  -d '{
    "orderId": "test-order-001",
    "items": [
      {"itemId": "ITEM001", "quantity": 2, "price": 29.99}
    ]
  }'

# Expected (70% success):
# {"available": true, "message": "All items available in inventory"}

# Or (30% failure):
# {"error": "Inventory check failed", "code": "INVENTORY_ERROR"}
```

### Test Payment System API

```bash
# Successful charge
curl -X POST http://localhost:8084/api/v1/payment/charge \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-pay-001" \
  -d '{
    "orderId": "test-order-001",
    "amount": 100.00,
    "currency": "USD"
  }'

# Expected (60% success):
# {"transactionId": "uuid", "status": "SUCCESS", "timestamp": "2024-01-28T10:00:00Z"}

# Or (40% failure):
# {"error": "Payment declined", "code": "PAYMENT_DECLINED"}
```

### Test Shipping System API

```bash
# Create shipment
curl -X POST http://localhost:8085/api/v1/shipping/create \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-ship-001" \
  -d '{
    "orderId": "test-order-001",
    "shippingAddress": "123 Main St, City, State 12345",
    "items": [
      {"itemId": "ITEM001", "quantity": 2, "price": 29.99}
    ]
  }'

# Expected (202 Accepted):
# {"shippingId": "uuid", "status": "CREATED", "estimatedDelivery": "2024-02-02"}

# Or (500 Error):
# {"error": "Shipping creation failed", "code": "SHIPPING_ERROR"}
```

### Test Status Store API

```bash
# Update status
curl -X PUT http://localhost:8086/api/v1/status/test-order-001 \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-store-001" \
  -d '{
    "status": "INVENTORY_CONFIRMED",
    "timestamp": "2024-01-28T10:00:05Z"
  }'

# Expected (200 OK):
# {"orderId": "test-order-001", "status": "INVENTORY_CONFIRMED", "lastUpdated": "2024-01-28T10:00:05Z"}

# Retrieve status
curl -X GET http://localhost:8086/api/v1/status/test-order-001 \
  -H "X-Correlation-ID: test-store-001"

# Expected (200 OK):
# {"orderId": "test-order-001", "status": "INVENTORY_CONFIRMED", "lastUpdated": "2024-01-28T10:00:05Z"}
```

## Retry Logic Testing

### Test Inventory Retry (3 attempts)

```bash
# Monitor retry behavior by checking logs
# Inventory API will retry up to 3 times on failure

# Create order that triggers inventory check
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-retry-inv" \
  -d '{"items": [{"itemId": "ITEM_RETRY", "quantity": 1, "price": 99.99}]}'

# Check Anypoint Studio console for retry messages:
# "Retrying inventory check for order..."
```

### Test Payment Retry (2 attempts)

```bash
# Monitor retry behavior for payment failures
# Payment API will retry up to 2 times on failure

curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-retry-pay" \
  -d '{"items": [{"itemId": "ITEM_RETRY", "quantity": 1, "price": 199.99}]}'

# Check logs for:
# "Retrying payment for order..."
```

### Test Shipping Retry (5 attempts, async)

```bash
# Monitor retry behavior for shipping failures
# Shipping API will retry up to 5 times asynchronously

curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: test-retry-ship" \
  -d '{"items": [{"itemId": "ITEM_RETRY", "quantity": 1, "price": 299.99}]}'

# Check logs for:
# "Retrying shipping for order..."
```

## Correlation ID Testing

```bash
# Test with custom correlation ID
CORRELATION_ID="custom-trace-$(date +%s)"

echo "Creating order with Correlation ID: $CORRELATION_ID"

curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: $CORRELATION_ID" \
  -d '{
    "items": [{"itemId": "ITEM001", "quantity": 1, "price": 29.99}]
  }'

# Verify correlation ID is propagated through all system calls
# Check logs for correlation ID in all components
```

## Load Testing

### Simple Load Test (10 concurrent orders)

```bash
#!/bin/bash

# Create 10 orders concurrently
for i in {1..10}; do
  (
    curl -X POST http://localhost:8081/api/v1/orders \
      -H "Content-Type: application/json" \
      -H "X-Correlation-ID: load-test-$i" \
      -d '{
        "items": [
          {"itemId": "ITEM'$i'", "quantity": '$((i % 3 + 1))', "price": '$((i * 10)).99'}
        ]
      }' &
  )
done

wait
echo "Load test completed"
```

### Monitor Performance

```bash
# Monitor memory usage
watch -n 1 'free -h'

# Monitor CPU usage
top -b -n 1 | head -20

# Monitor network connections
netstat -an | grep ESTABLISHED | wc -l
```

## Error Scenarios

### Test Invalid Order Request

```bash
# Missing required items array
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "CUST001"}'

# Expected (400 Bad Request)
# {"code": "...", "message": "..."}

# Empty items array
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"items": []}'

# Expected (400 Bad Request)
# {"code": "...", "message": "..."}
```

### Test Non-existent Order

```bash
# Query non-existent order
curl -X GET http://localhost:8081/api/v1/orders/non-existent-id/status

# Expected (404 Not Found) - after status store returns 404
```

### Test Timeout Scenarios

```bash
# Inventory timeout (3s limit)
# If inventory API doesn't respond within 3s, it will retry

# Payment timeout (5s limit)
# If payment API doesn't respond within 5s, it will retry

# Shipping timeout (10s limit)
# If shipping API doesn't respond within 10s, it will retry

# Monitor timeouts in logs
```

## Batch Testing Script

```bash
#!/bin/bash

# Create test_orders.sh
cat > test_orders.sh << 'EOF'
#!/bin/bash

ORDERS_COUNT=${1:-5}
SUCCESS_COUNT=0
FAILURE_COUNT=0

echo "Creating $ORDERS_COUNT test orders..."

for i in $(seq 1 $ORDERS_COUNT); do
  RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/orders \
    -H "Content-Type: application/json" \
    -H "X-Correlation-ID: batch-test-$i" \
    -d '{
      "items": [
        {"itemId": "ITEM'$i'", "quantity": '$((i % 5 + 1))', "price": '$((i * 20)).99'}
      ]
    }')
  
  ORDER_ID=$(echo $RESPONSE | jq -r '.orderId // empty')
  
  if [ -n "$ORDER_ID" ]; then
    echo "✓ Order $i created: $ORDER_ID"
    SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
  else
    echo "✗ Order $i failed"
    FAILURE_COUNT=$((FAILURE_COUNT + 1))
  fi
done

echo ""
echo "Summary:"
echo "  Successful: $SUCCESS_COUNT"
echo "  Failed: $FAILURE_COUNT"
EOF

chmod +x test_orders.sh
./test_orders.sh 10
```

## Monitoring and Debugging

### View Application Logs

```bash
# In Anypoint Studio Console tab
# Or check .mule/logs/mule.log

tail -f .mule/logs/mule.log | grep -i "order\|error\|correlation"
```

### Enable Debug Logging

Add to application configuration:
```xml
<logger message="DEBUG: #[payload]" level="DEBUG" />
```

### Monitor Object Store

```bash
# Check stored orders in status store
curl -X GET http://localhost:8086/api/v1/status/test-order-001

# List all stored orders (if API supports it)
curl -X GET http://localhost:8086/api/v1/status
```

## Performance Benchmarks

Expected response times:
- Order Creation: 100-200ms
- Status Retrieval: 50-100ms
- Inventory Check: 500-2000ms (with retries)
- Payment Charge: 1000-5000ms (with retries)
- Shipping Create: 1000-4000ms (async)

## Troubleshooting

### Orders Not Processing

1. Check all applications running: `netstat -tuln | grep 808`
2. Check logs for errors
3. Verify correlation IDs in logs
4. Check Object Store status

### High Failure Rate

1. Adjust mock success rates in system API XML files
2. Check timeout values
3. Monitor system resources (CPU, memory)
4. Check network connectivity

### Slow Response Times

1. Increase heap memory: `export MULE_OPTS="-Xmx1024m"`
2. Check for deadlocks in logs
3. Monitor Object Store size
4. Check for retry storms

