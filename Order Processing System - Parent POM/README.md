# MuleSoft Order Processing System

Complete MuleSoft implementation of a 6-application order processing system with state persistence, retry logic, and correlation ID propagation.

## Project Structure

```
mulesoft-project/
├── pom.xml                          # Parent POM with module definitions
├── raml-library/                    # Shared RAML types and traits
│   ├── pom.xml
│   └── src/main/resources/raml/
│       ├── types/common-types.raml  # Shared data types
│       └── traits/common-traits.raml # Shared traits
├── order-exp-api/                   # Experience API (Client Interface)
│   ├── pom.xml
│   ├── src/main/mule/order-exp-api.xml
│   ├── src/main/resources/api/order-exp-api.raml
│   └── src/test/java/...
├── order-proc-api/                  # Process API (Orchestrator)
│   ├── pom.xml
│   ├── src/main/mule/order-proc-api.xml
│   ├── src/main/resources/api/order-proc-api.raml
│   └── src/test/java/...
├── inventory-sys-api/               # Inventory System API (Mock)
│   ├── pom.xml
│   ├── src/main/mule/inventory-sys-api.xml
│   ├── src/main/resources/api/inventory-sys-api.raml
│   └── src/test/java/...
├── payment-sys-api/                 # Payment System API (Mock)
│   ├── pom.xml
│   ├── src/main/mule/payment-sys-api.xml
│   ├── src/main/resources/api/payment-sys-api.raml
│   └── src/test/java/...
├── shipping-sys-api/                # Shipping System API (Mock)
│   ├── pom.xml
│   ├── src/main/mule/shipping-sys-api.xml
│   ├── src/main/resources/api/shipping-sys-api.raml
│   └── src/test/java/...
└── order-status-store/              # Order Status Store (State Persistence)
    ├── pom.xml
    ├── src/main/mule/order-status-store.xml
    ├── src/main/resources/api/order-status-store.raml
    └── src/test/java/...
```

## Applications Overview

### 1. **order-exp-api** (Experience API)
- **Port:** 8081
- **Purpose:** Client-facing interface for order submission and status polling
- **Endpoints:**
  - `POST /api/v1/orders` - Submit new order
  - `GET /api/v1/orders/{orderId}/status` - Get order status
- **Behavior:** Receives orders asynchronously, delegates to Process API

### 2. **order-proc-api** (Process API - Orchestrator)
- **Port:** 8082
- **Purpose:** Core orchestration layer managing order processing workflow
- **Endpoints:**
  - `POST /api/v1/process` - Process order through inventory, payment, and shipping
- **Retry Logic:**
  - Inventory Check: 3 attempts, 1s interval, 3s timeout
  - Payment Charge: 2 attempts, 2s interval, 5s timeout
  - Shipping Create: 5 attempts, 2s interval, 10s timeout (async)
- **State Management:** Updates status store after each step

### 3. **inventory-sys-api** (System API - Mock)
- **Port:** 8083
- **Purpose:** Mock inventory management system
- **Endpoints:**
  - `POST /api/v1/inventory/check` - Check item availability
- **Mock Behavior:** 70% success, 30% failure, 0.5-2s latency

### 4. **payment-sys-api** (System API - Mock)
- **Port:** 8084
- **Purpose:** Mock payment processing system
- **Endpoints:**
  - `POST /api/v1/payment/charge` - Process payment
- **Mock Behavior:** 60% success, 40% failure, occasional timeout

### 5. **shipping-sys-api** (System API - Mock)
- **Port:** 8085
- **Purpose:** Mock shipping management system
- **Endpoints:**
  - `POST /api/v1/shipping/create` - Create shipment
- **Mock Behavior:** Async response (202), 1-4s latency, 80% success

### 6. **order-status-store** (System API - State Persistence)
- **Port:** 8086
- **Purpose:** Centralized order status persistence
- **Endpoints:**
  - `PUT /api/v1/status/{orderId}` - Update order status
  - `GET /api/v1/status/{orderId}` - Retrieve order status
- **Storage:** In-memory Object Store (local testing)

## Order Processing Flow

```
RECEIVED
  ↓
INVENTORY_CHECK_STARTED
  ├─ Success → INVENTORY_CONFIRMED
  └─ Failure → INVENTORY_FAILED (Stop)
  ↓
PAYMENT_COMPLETED
  ├─ Success → PAYMENT_COMPLETED
  └─ Failure → PAYMENT_FAILED (Stop)
  ↓
SHIPPING_REQUESTED
  ├─ Success → SHIPPED
  └─ Failure → SHIPPING_FAILED (Async, continues retrying)
```

## Retry Strategy

| System | Attempts | Interval | Timeout | Stop Condition |
|--------|----------|----------|---------|----------------|
| Inventory | 3 | 1s | 3s | Exhaustion or 400-level error |
| Payment | 2 | 2s | 5s | Exhaustion or 402/403 error |
| Shipping | 5 | 2s | 10s | Exhaustion (async) |

## Correlation ID Propagation

All requests include `X-Correlation-ID` header for distributed tracing:
- Generated if not provided in request
- Propagated through all system calls
- Enables end-to-end request tracking

## Getting Started

### Prerequisites
- Java 11+
- Maven 3.8+
- Anypoint Studio 7.x or Mule Runtime 4.5.0+

### Build All Applications

```bash
cd mulesoft-project
mvn clean install
```

### Build Individual Application

```bash
cd order-exp-api
mvn clean install
```

### Run Unit Tests

```bash
# Run all tests
mvn test

# Run specific application tests
cd order-proc-api
mvn test
```

### Deploy to Anypoint Studio

1. Open Anypoint Studio
2. File → Import → Existing Maven Projects
3. Select `mulesoft-project` directory
4. Select all 6 applications + raml-library
5. Click Finish
6. Right-click each application → Run As → Mule Application

### Local Testing

Once all applications are running:

```bash
# Create order
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "items": [
      {"itemId": "ITEM001", "quantity": 2, "price": 29.99}
    ]
  }'

# Check order status
curl -X GET http://localhost:8081/api/v1/orders/{orderId}/status \
  -H "X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000"
```

## RAML Specifications

All RAML files are located in `src/main/resources/api/` for each application:

- **order-exp-api.raml** - Experience API specification
- **order-proc-api.raml** - Process API specification
- **inventory-sys-api.raml** - Inventory System API specification
- **payment-sys-api.raml** - Payment System API specification
- **shipping-sys-api.raml** - Shipping System API specification
- **order-status-store.raml** - Status Store API specification

Shared types and traits are defined in:
- **raml-library/src/main/resources/raml/types/common-types.raml**
- **raml-library/src/main/resources/raml/traits/common-traits.raml**

## Error Handling

All applications implement comprehensive error handling:
- **Try-Catch blocks** for system API calls
- **On-Error-Retry** for transient failures
- **On-Error-Continue** for non-recoverable errors
- **Proper HTTP status codes** (200, 202, 400, 402, 404, 500, 503)

## Testing

Each application includes unit tests using Mule Testing Framework:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=OrderExpApiTest
```

Test coverage includes:
- Happy path scenarios
- Error handling and retries
- Correlation ID propagation
- State persistence
- Async behavior validation

## Key Features

✅ **State Persistence** - Every state transition is persisted in Object Store
✅ **Retry Logic** - Configurable retry attempts with exponential backoff
✅ **Correlation ID** - End-to-end request tracing
✅ **Async Processing** - Non-blocking shipping operations
✅ **Error Handling** - Comprehensive error handling with proper status codes
✅ **RAML Specifications** - Complete API documentation
✅ **Unit Tests** - Comprehensive test coverage
✅ **Mock Systems** - Realistic failure simulation for testing

## Configuration

### Object Store Configuration
- **Type:** In-memory (local testing)
- **Name:** `orderStatusStore`
- **Scope:** Application
- **TTL:** Not set (persists for application lifetime)

### HTTP Timeouts
- Experience API: 30s (default)
- Process API: 30s (default)
- System APIs: 3-10s (per retry configuration)

## Deployment Notes

For production deployment to CloudHub:
1. Replace in-memory Object Store with persistent store (Redis, Database)
2. Configure external HTTP endpoints instead of localhost
3. Add authentication/authorization
4. Implement proper logging and monitoring
5. Configure API versioning and backwards compatibility
6. Set up CI/CD pipeline with Maven

## Support

For issues or questions:
1. Check Mule Runtime logs in `.mule/logs/`
2. Verify all applications are running on correct ports
3. Check correlation IDs in logs for request tracing
4. Review RAML specifications for API contract compliance

## License

MIT License - See LICENSE file for details
