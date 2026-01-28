# MuleSoft Order Processing System - Setup Guide

Complete step-by-step guide to set up and run the MuleSoft Order Processing System locally.

## Prerequisites

### System Requirements
- **OS:** Windows, macOS, or Linux
- **Java:** JDK 11 or higher
- **Maven:** 3.8.0 or higher
- **Memory:** Minimum 4GB RAM (8GB recommended)
- **Disk Space:** 2GB for dependencies and applications

### Software Installation

#### 1. Install Java 11+
```bash
# Verify Java installation
java -version

# Should output Java 11 or higher
```

#### 2. Install Maven
```bash
# Verify Maven installation
mvn -version

# Should output Maven 3.8.0 or higher
```

#### 3. Install Anypoint Studio (Optional but Recommended)
- Download from: https://www.mulesoft.com/lp/dl/studio
- Unzip and run `AnypointStudio` executable
- Complete initial setup wizard

## Project Setup

### Step 1: Clone or Extract Project

```bash
# Navigate to project directory
cd mulesoft-project
```

### Step 2: Build All Applications

```bash
# Clean and build all modules
mvn clean install

# Expected output:
# [INFO] Building Order Processing System - Parent POM 1.0.0
# [INFO] Building RAML Library - Shared Types and Traits 1.0.0
# [INFO] Building Order Experience API 1.0.0
# [INFO] Building Order Process API 1.0.0
# [INFO] Building Inventory System API 1.0.0
# [INFO] Building Payment System API 1.0.0
# [INFO] Building Shipping System API 1.0.0
# [INFO] Building Order Status Store 1.0.0
# [INFO] BUILD SUCCESS
```

### Step 3: Verify Build

```bash
# Check if all modules built successfully
ls -la */target/*.jar

# Should show 6 JAR files (one for each application)
```

## Running Applications

### Option A: Using Anypoint Studio

#### 1. Import Project into Anypoint Studio

```
File → Import → Existing Maven Projects
```

1. Click "Browse" and select `mulesoft-project` directory
2. Click "Refresh" to discover modules
3. Select all 7 modules (including raml-library)
4. Click "Finish"

#### 2. Run Each Application

For each application (order-exp-api, order-proc-api, etc.):

```
Right-click application → Run As → Mule Application
```

Wait for console message: `Started app 'application-name'`

### Option B: Using Command Line

#### 1. Install Mule Runtime (if not using Anypoint Studio)

```bash
# Download Mule 4.5.0 from MuleSoft
# Or use Maven to download and run

# Run each application using Maven
cd order-exp-api
mvn mule:run

# In separate terminals, run other applications:
cd order-proc-api
mvn mule:run

cd inventory-sys-api
mvn mule:run

cd payment-sys-api
mvn mule:run

cd shipping-sys-api
mvn mule:run

cd order-status-store
mvn mule:run
```

## Verification

### Step 1: Verify All Applications Running

Check that all 6 applications are running on their respective ports:

```bash
# Check port 8081 (order-exp-api)
curl -I http://localhost:8081/api/v1/orders

# Check port 8082 (order-proc-api)
curl -I http://localhost:8082/api/v1/process

# Check port 8083 (inventory-sys-api)
curl -I http://localhost:8083/api/v1/inventory/check

# Check port 8084 (payment-sys-api)
curl -I http://localhost:8084/api/v1/payment/charge

# Check port 8085 (shipping-sys-api)
curl -I http://localhost:8085/api/v1/shipping/create

# Check port 8086 (order-status-store)
curl -I http://localhost:8086/api/v1/status/test-order
```

### Step 2: Test Order Creation

```bash
# Create a test order
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000" \
  -d '{
    "items": [
      {
        "itemId": "ITEM001",
        "quantity": 2,
        "price": 29.99
      }
    ]
  }'

# Expected response (202 Accepted):
# {
#   "orderId": "uuid-string",
#   "status": "RECEIVED",
#   "createdAt": "2024-01-28T10:00:00Z"
# }
```

### Step 3: Check Order Status

```bash
# Replace {orderId} with the ID from previous response
curl -X GET http://localhost:8081/api/v1/orders/{orderId}/status \
  -H "X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000"

# Expected response (200 OK):
# {
#   "orderId": "uuid-string",
#   "status": "SHIPPED",  # or other status
#   "lastUpdated": "2024-01-28T10:00:05Z"
# }
```

## Running Tests

### Run All Tests

```bash
# From project root
mvn test

# Expected output:
# [INFO] Running com.mulesoft.order.OrderExpApiTest
# [INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
# [INFO] Running com.mulesoft.order.OrderProcApiTest
# [INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
# ... (tests for all applications)
# [INFO] BUILD SUCCESS
```

### Run Tests for Specific Application

```bash
cd order-exp-api
mvn test

# Or run specific test class
mvn test -Dtest=OrderExpApiTest
```

### Run Specific Test Method

```bash
mvn test -Dtest=OrderExpApiTest#testCreateOrder
```

## Troubleshooting

### Issue: Port Already in Use

**Error:** `Address already in use`

**Solution:**
```bash
# Find process using port 8081
lsof -i :8081

# Kill process
kill -9 <PID>

# Or change port in application configuration
```

### Issue: Maven Build Fails

**Error:** `Failed to execute goal org.mule.tools.maven:mule-maven-plugin`

**Solution:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

### Issue: Application Won't Start

**Error:** `Failed to start application`

**Solution:**
1. Check Java version: `java -version` (should be 11+)
2. Check available memory: `free -h` (need at least 1GB per app)
3. Check Mule logs in `.mule/logs/`
4. Verify all dependencies installed: `mvn dependency:resolve`

### Issue: Curl Commands Fail

**Error:** `Connection refused`

**Solution:**
1. Verify application is running: Check Anypoint Studio console
2. Wait 10-15 seconds for application to fully start
3. Check firewall settings
4. Try localhost instead of 127.0.0.1

### Issue: Tests Fail

**Error:** `Test failures in OrderExpApiTest`

**Solution:**
1. Ensure all applications are running before running tests
2. Check for port conflicts
3. Clear Object Store: Restart applications
4. Check test logs: `target/surefire-reports/`

## Performance Tuning

### Increase Heap Memory

```bash
# Set MULE_OPTS environment variable
export MULE_OPTS="-Xmx1024m -Xms512m"

# Then run applications
mvn mule:run
```

### Increase Thread Pool Size

Edit application configuration to increase worker threads:
```xml
<http:listener-config name="HTTP_Listener_config">
    <http:listener-connection host="0.0.0.0" port="8081" 
                              workerThreadingProfile="default"/>
</http:listener-config>
```

## Development Workflow

### Making Changes

1. Edit Mule XML files in `src/main/mule/`
2. Edit RAML files in `src/main/resources/api/`
3. Rebuild: `mvn clean install`
4. Restart application in Anypoint Studio

### Debugging

1. Set breakpoints in Anypoint Studio
2. Run application in Debug mode: `Right-click → Debug As → Mule Application`
3. Use Mule Debugger to step through flows

### Logging

Add logging to flows:
```xml
<logger message="Order received: #[payload.orderId]" level="INFO" />
```

View logs in Anypoint Studio Console or `.mule/logs/mule.log`

## Production Deployment

### Before Deploying to Production

1. Replace in-memory Object Store with persistent store
2. Configure external database for order storage
3. Add authentication (OAuth2, API Key)
4. Implement rate limiting
5. Add comprehensive error handling
6. Set up monitoring and alerting
7. Configure SSL/TLS certificates
8. Test failover scenarios

### Deploy to CloudHub

```bash
# Configure CloudHub credentials
mvn install \
  -Danypoint.username=your-username \
  -Danypoint.password=your-password \
  -Danypoint.organization=your-org \
  -Danypoint.environment=Production
```

## Next Steps

1. Review RAML specifications: See `README.md`
2. Explore application flows: Open in Anypoint Studio
3. Modify mock success rates: Edit system API XML files
4. Add database persistence: Upgrade to use database connector
5. Implement authentication: Add security policies

## Support Resources

- **MuleSoft Documentation:** https://docs.mulesoft.com
- **Anypoint Studio Guide:** https://docs.mulesoft.com/studio/latest/
- **Mule Runtime:** https://docs.mulesoft.com/mule-runtime/latest/
- **RAML Specification:** https://raml.org/

## Cleanup

### Stop All Applications

```bash
# In Anypoint Studio: Right-click each app → Stop
# Or press Ctrl+C in terminal windows
```

### Clean Build Artifacts

```bash
mvn clean

# Remove all target directories
find . -name "target" -type d -exec rm -rf {} +
```

### Reset Object Store

```bash
# Restart all applications to clear in-memory Object Store
```
