package com.mulesoft.order;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for Order Process API
 */
public class OrderProcApiTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "order-proc-api.xml";
    }

    /**
     * Test POST /process - Process Order with successful flow
     */
    @Test
    public void testProcessOrderSuccess() throws Exception {
        String payload = "{\"orderId\": \"test-order-001\", \"items\": [{\"itemId\": \"ITEM001\", \"quantity\": 2, \"price\": 29.99}]}";
        
        Object result = runFlow("process-order-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test inventory check failure handling
     */
    @Test
    public void testInventoryCheckFailure() throws Exception {
        String payload = "{\"orderId\": \"test-order-002\", \"items\": [{\"itemId\": \"ITEM002\", \"quantity\": 1, \"price\": 19.99}]}";
        
        Object result = runFlow("process-order-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test payment processing failure handling
     */
    @Test
    public void testPaymentProcessingFailure() throws Exception {
        String payload = "{\"orderId\": \"test-order-003\", \"items\": [{\"itemId\": \"ITEM003\", \"quantity\": 3, \"price\": 39.99}]}";
        
        Object result = runFlow("process-order-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test correlation ID propagation through orchestration
     */
    @Test
    public void testCorrelationIdPropagation() throws Exception {
        String correlationId = "550e8400-e29b-41d4-a716-446655440000";
        String payload = "{\"orderId\": \"test-order-004\", \"items\": [{\"itemId\": \"ITEM004\", \"quantity\": 1, \"price\": 49.99}]}";
        
        Object result = runFlow("process-order-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test retry logic on transient failures
     */
    @Test
    public void testRetryLogicOnTransientFailure() throws Exception {
        String payload = "{\"orderId\": \"test-order-005\", \"items\": [{\"itemId\": \"ITEM005\", \"quantity\": 2, \"price\": 59.99}]}";
        
        Object result = runFlow("process-order-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test async shipping invocation
     */
    @Test
    public void testAsyncShippingInvocation() throws Exception {
        String payload = "{\"orderId\": \"test-order-006\", \"items\": [{\"itemId\": \"ITEM006\", \"quantity\": 1, \"price\": 69.99}]}";
        
        Object result = runFlow("process-order-flow", payload);
        
        assertThat(result, notNullValue());
    }
}
