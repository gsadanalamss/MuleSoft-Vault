# MuleSoft Order Processing System - Project Structure

Complete documentation of all files and their purposes.

## Directory Tree

```
mulesoft-project/
├── pom.xml                                          # Parent POM - Module definitions
├── README.md                                        # Project overview and features
├── SETUP.md                                         # Setup and installation guide
├── API_TESTING.md                                   # API testing scenarios
├── PROJECT_STRUCTURE.md                             # This file
├── .gitignore                                       # Git ignore rules
│
├── raml-library/                                    # Shared RAML Specifications
│   ├── pom.xml                                      # RAML Library POM
│   └── src/main/resources/raml/
│       ├── types/
│       │   └── common-types.raml                    # Shared data types (OrderRequest, OrderResponse, etc.)
│       └── traits/
│           └── common-traits.raml                   # Shared traits (correlationIdHeader, errorResponses, etc.)
│
├── order-exp-api/                                   # Experience API (Client Interface)
│   ├── pom.xml                                      # Application POM
│   ├── src/main/
│   │   ├── mule/
│   │   │   └── order-exp-api.xml                    # Main Mule flows (POST /orders, GET /status)
│   │   └── resources/api/
│   │       └── order-exp-api.raml                   # RAML specification
│   └── src/test/java/com/mulesoft/order/
│       └── OrderExpApiTest.java                     # Unit tests
│
├── order-proc-api/                                  # Process API (Orchestrator)
│   ├── pom.xml                                      # Application POM
│   ├── src/main/
│   │   ├── mule/
│   │   │   └── order-proc-api.xml                   # Orchestration flows with retry logic
│   │   └── resources/api/
│   │       └── order-proc-api.raml                  # RAML specification
│   └── src/test/java/com/mulesoft/order/
│       └── OrderProcApiTest.java                    # Unit tests
│
├── inventory-sys-api/                               # Inventory System API (Mock)
│   ├── pom.xml                                      # Application POM
│   ├── src/main/
│   │   ├── mule/
│   │   │   └── inventory-sys-api.xml                # Mock inventory check flow (70% success)
│   │   └── resources/api/
│   │       └── inventory-sys-api.raml               # RAML specification
│   └── src/test/java/com/mulesoft/order/
│       └── InventorySysApiTest.java                 # Unit tests
│
├── payment-sys-api/                                 # Payment System API (Mock)
│   ├── pom.xml                                      # Application POM
│   ├── src/main/
│   │   ├── mule/
│   │   │   └── payment-sys-api.xml                  # Mock payment charge flow (60% success)
│   │   └── resources/api/
│   │       └── payment-sys-api.raml                 # RAML specification
│   └── src/test/java/com/mulesoft/order/
│       └── PaymentSysApiTest.java                   # Unit tests
│
├── shipping-sys-api/                                # Shipping System API (Mock)
│   ├── pom.xml                                      # Application POM
│   ├── src/main/
│   │   ├── mule/
│   │   │   └── shipping-sys-api.xml                 # Mock shipping create flow (async, 1-4s latency)
│   │   └── resources/api/
│   │       └── shipping-sys-api.raml                # RAML specification
│   └── src/test/java/com/mulesoft/order/
│       └── ShippingSysApiTest.java                  # Unit tests
│
└── order-status-store/                              # Order Status Store (State Persistence)
    ├── pom.xml                                      # Application POM
    ├── src/main/
    │   ├── mule/
    │   │   └── order-status-store.xml               # Status persistence flows (PUT/GET)
    │   └── resources/api/
    │       └── order-status-store.raml              # RAML specification
    └── src/test/java/com/mulesoft/order/
        └── OrderStatusStoreTest.java                # Unit tests
```

## File Descriptions

### Root Level Files

#### pom.xml
**Purpose:** Parent POM for the entire project
**Key Content:**
- Module definitions for all 7 modules
- Dependency management (Mule Runtime, connectors, testing)
- Build plugin configuration
- Properties for versions

#### README.md
**Purpose:** Project overview and quick reference
**Sections:**
- Project structure overview
- Applications description
- Order processing flow diagram
- Retry strategy table
- Getting started instructions
- Features list

#### SETUP.md
**Purpose:** Complete setup and installation guide
**Sections:**
- Prerequisites and software installation
- Project setup steps
- Running applications (Anypoint Studio and CLI)
- Verification procedures
- Troubleshooting guide
- Development workflow
- Production deployment notes

#### API_TESTING.md
**Purpose:** Comprehensive API testing scenarios
**Sections:**
- Quick start verification
- Successful order processing scenario
- Failure scenarios (inventory, payment, shipping)
- Direct API testing for each system
- Retry logic testing
- Correlation ID testing
- Load testing
- Error scenarios
- Batch testing scripts
- Monitoring and debugging

#### PROJECT_STRUCTURE.md
**Purpose:** This file - Complete documentation of all files

#### .gitignore
**Purpose:** Git ignore rules for version control

### RAML Library Module

#### raml-library/pom.xml
**Purpose:** POM for shared RAML library
**Key Content:**
- Parent reference
- No dependencies (pure RAML)

#### raml-library/src/main/resources/raml/types/common-types.raml
**Purpose:** Shared data types used across all APIs
**Key Types:**
- `CorrelationId` - UUID format for request tracing
- `OrderStatus` - Enum of all order states
- `OrderItem` - Item in an order
- `OrderRequest` - Request payload for order creation
- `OrderResponse` - Response payload for order creation
- `StatusCheckResponse` - Order status response
- `InventoryCheckRequest/Response` - Inventory API payloads
- `PaymentChargeRequest/Response` - Payment API payloads
- `ShippingCreateRequest/Response` - Shipping API payloads
- `ErrorResponse` - Standard error response

#### raml-library/src/main/resources/raml/traits/common-traits.raml
**Purpose:** Shared traits for API consistency
**Key Traits:**
- `correlationIdHeader` - X-Correlation-ID header
- `errorResponses` - Standard error response definitions
- `rateLimited` - Rate limit headers
- `paged` - Pagination parameters
- `timestamped` - Server timestamp header
- `asyncResponse` - 202 Accepted response

### Order Experience API Module

#### order-exp-api/pom.xml
**Purpose:** Application POM with dependencies
**Key Dependencies:**
- Mule Core
- HTTP Connector
- Java Module
- Testing Framework

#### order-exp-api/src/main/mule/order-exp-api.xml
**Purpose:** Main Mule flows for Experience API
**Flows:**
- `post-orders-flow` - POST /api/v1/orders
  - Generates order ID
  - Extracts/generates correlation ID
  - Stores initial status (RECEIVED)
  - Invokes Process API asynchronously
  - Returns 202 Accepted
- `get-status-flow` - GET /api/v1/orders/{orderId}/status
  - Retrieves status from Status Store
  - Returns current order status

#### order-exp-api/src/main/resources/api/order-exp-api.raml
**Purpose:** RAML specification for Experience API
**Endpoints:**
- POST /orders - Create order (202 Accepted)
- GET /orders/{orderId}/status - Get order status (200 OK)

#### order-exp-api/src/test/java/com/mulesoft/order/OrderExpApiTest.java
**Purpose:** Unit tests for Experience API
**Test Cases:**
- `testCreateOrder` - Verify order creation
- `testGetOrderStatus` - Verify status retrieval
- `testInvalidOrderRequest` - Verify validation
- `testCorrelationIdPropagation` - Verify correlation ID handling

### Order Process API Module

#### order-proc-api/pom.xml
**Purpose:** Application POM with dependencies

#### order-proc-api/src/main/mule/order-proc-api.xml
**Purpose:** Main Mule flows for Process API (Orchestrator)
**Flows:**
- `process-order-flow` - POST /api/v1/process
  - Extracts order ID and items
  - Updates status: INVENTORY_CHECK_STARTED
  - Calls Inventory API with retry (3x, 1s, 3s timeout)
  - On success: Updates status INVENTORY_CONFIRMED
  - Calls Payment API with retry (2x, 2s, 5s timeout)
  - On success: Updates status PAYMENT_COMPLETED
  - Calls Shipping API asynchronously with retry (5x, 2s, 10s timeout)
  - Updates status: SHIPPED or SHIPPING_FAILED
  - Proper error handling at each step

#### order-proc-api/src/main/resources/api/order-proc-api.raml
**Purpose:** RAML specification for Process API
**Endpoints:**
- POST /process - Process order (202 Accepted)

#### order-proc-api/src/test/java/com/mulesoft/order/OrderProcApiTest.java
**Purpose:** Unit tests for Process API
**Test Cases:**
- `testProcessOrderSuccess` - Verify successful processing
- `testInventoryCheckFailure` - Verify inventory failure handling
- `testPaymentProcessingFailure` - Verify payment failure handling
- `testCorrelationIdPropagation` - Verify correlation ID propagation
- `testRetryLogicOnTransientFailure` - Verify retry behavior
- `testAsyncShippingInvocation` - Verify async shipping

### Inventory System API Module

#### inventory-sys-api/pom.xml
**Purpose:** Application POM with dependencies

#### inventory-sys-api/src/main/mule/inventory-sys-api.xml
**Purpose:** Mock inventory system flows
**Flows:**
- `inventory-check-flow` - POST /api/v1/inventory/check
  - Simulates random latency (0.5-2s)
  - Returns 70% success, 30% failure
  - Success: `{"available": true}`
  - Failure: `{"error": "Inventory check failed"}`

#### inventory-sys-api/src/main/resources/api/inventory-sys-api.raml
**Purpose:** RAML specification for Inventory API
**Endpoints:**
- POST /check - Check inventory (200 OK or 500 Error)

#### inventory-sys-api/src/test/java/com/mulesoft/order/InventorySysApiTest.java
**Purpose:** Unit tests for Inventory API
**Test Cases:**
- `testInventoryCheckSuccess` - Verify success response
- `testRandomSuccessFailureDistribution` - Verify 70/30 distribution
- `testInventoryCheckMultipleItems` - Verify multiple items handling
- `testCorrelationIdHandling` - Verify correlation ID handling

### Payment System API Module

#### payment-sys-api/pom.xml
**Purpose:** Application POM with dependencies

#### payment-sys-api/src/main/mule/payment-sys-api.xml
**Purpose:** Mock payment system flows
**Flows:**
- `payment-charge-flow` - POST /api/v1/payment/charge
  - Returns 60% success, 40% failure
  - Success: `{"transactionId": "uuid", "status": "SUCCESS"}`
  - Failure: `{"error": "Payment declined", "code": "PAYMENT_DECLINED"}`

#### payment-sys-api/src/main/resources/api/payment-sys-api.raml
**Purpose:** RAML specification for Payment API
**Endpoints:**
- POST /charge - Charge payment (200 OK or 402 Error)

#### payment-sys-api/src/test/java/com/mulesoft/order/PaymentSysApiTest.java
**Purpose:** Unit tests for Payment API
**Test Cases:**
- `testPaymentChargeSuccess` - Verify success response
- `testRandomSuccessFailureDistribution` - Verify 60/40 distribution
- `testPaymentChargeVariousAmounts` - Verify various amounts
- `testTransactionIdGeneration` - Verify transaction ID generation
- `testCorrelationIdHandling` - Verify correlation ID handling

### Shipping System API Module

#### shipping-sys-api/pom.xml
**Purpose:** Application POM with dependencies

#### shipping-sys-api/src/main/mule/shipping-sys-api.xml
**Purpose:** Mock shipping system flows
**Flows:**
- `shipping-create-flow` - POST /api/v1/shipping/create
  - Simulates random latency (1-4s)
  - Returns 202 Accepted (async)
  - Success (80%): `{"shippingId": "uuid", "status": "CREATED"}`
  - Failure (20%): `{"error": "Shipping creation failed"}`

#### shipping-sys-api/src/main/resources/api/shipping-sys-api.raml
**Purpose:** RAML specification for Shipping API
**Endpoints:**
- POST /create - Create shipment (202 Accepted or 500 Error)

#### shipping-sys-api/src/test/java/com/mulesoft/order/ShippingSysApiTest.java
**Purpose:** Unit tests for Shipping API
**Test Cases:**
- `testShippingCreateSuccess` - Verify success response
- `testAsyncResponse` - Verify 202 response
- `testShipmentIdGeneration` - Verify shipment ID generation
- `testEstimatedDeliveryCalculation` - Verify delivery date calculation
- `testRandomFailureHandling` - Verify failure handling
- `testCorrelationIdHandling` - Verify correlation ID handling

### Order Status Store Module

#### order-status-store/pom.xml
**Purpose:** Application POM with dependencies

#### order-status-store/src/main/mule/order-status-store.xml
**Purpose:** Status persistence flows
**Flows:**
- `update-order-status-flow` - PUT /api/v1/status/{orderId}
  - Stores status in Object Store
  - Returns updated status with timestamp
- `get-order-status-flow` - GET /api/v1/status/{orderId}
  - Retrieves status from Object Store
  - Returns 200 OK or 404 Not Found

#### order-status-store/src/main/resources/api/order-status-store.raml
**Purpose:** RAML specification for Status Store API
**Endpoints:**
- PUT /{orderId} - Update status (200 OK or 404 Not Found)
- GET /{orderId} - Get status (200 OK or 404 Not Found)

#### order-status-store/src/test/java/com/mulesoft/order/OrderStatusStoreTest.java
**Purpose:** Unit tests for Status Store
**Test Cases:**
- `testUpdateOrderStatus` - Verify status update
- `testGetOrderStatus` - Verify status retrieval
- `testStatusPersistence` - Verify persistence
- `testStatusUpdateSequence` - Verify state transitions
- `testOrderNotFound` - Verify 404 handling
- `testCorrelationIdHandling` - Verify correlation ID handling
- `testConcurrentStatusUpdates` - Verify concurrent updates

## Build Artifacts

After building, the following artifacts are created:

```
target/
├── order-exp-api-1.0.0-mule-application.jar
├── order-proc-api-1.0.0-mule-application.jar
├── inventory-sys-api-1.0.0-mule-application.jar
├── payment-sys-api-1.0.0-mule-application.jar
├── shipping-sys-api-1.0.0-mule-application.jar
├── order-status-store-1.0.0-mule-application.jar
└── raml-library-1.0.0.jar
```

## Configuration Files

### application.properties (Optional)
Can be added to each application's `src/main/resources/` for environment-specific configuration:

```properties
# HTTP Listener Port
http.port=8081

# Timeout values
inventory.timeout=3000
payment.timeout=5000
shipping.timeout=10000

# Retry configuration
inventory.retries=3
payment.retries=2
shipping.retries=5
```

### log4j2.xml (Optional)
Can be added to each application's `src/main/resources/` for logging configuration.

## Dependencies

### Core Dependencies
- **Mule Runtime:** 4.5.0
- **HTTP Connector:** 1.7.3
- **Java Module:** 1.2.4

### Testing Dependencies
- **Mule Tests Unit:** 4.5.0
- **JUnit:** 4.13.2

## Ports

| Application | Port | Purpose |
|-------------|------|---------|
| order-exp-api | 8081 | Experience API (Client Interface) |
| order-proc-api | 8082 | Process API (Orchestrator) |
| inventory-sys-api | 8083 | Inventory System API (Mock) |
| payment-sys-api | 8084 | Payment System API (Mock) |
| shipping-sys-api | 8085 | Shipping System API (Mock) |
| order-status-store | 8086 | Order Status Store (State Persistence) |

## File Statistics

- **Total Files:** 34
- **XML Files:** 13 (1 parent POM + 6 app POMs + 6 Mule configs)
- **RAML Files:** 8 (2 shared + 6 API specs)
- **Java Files:** 6 (1 test per application)
- **Documentation Files:** 4 (README, SETUP, API_TESTING, PROJECT_STRUCTURE)
- **Configuration Files:** 2 (pom.xml parent, .gitignore)

## Next Steps

1. **Review README.md** - Understand project overview
2. **Follow SETUP.md** - Set up local environment
3. **Read API_TESTING.md** - Test all APIs
4. **Explore Mule flows** - Open in Anypoint Studio
5. **Modify mock rates** - Adjust success/failure percentages
6. **Add persistence** - Replace Object Store with database

