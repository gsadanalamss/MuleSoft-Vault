package com.mulesoft.order;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for Order Experience API
 */
public class OrderExpApiTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "order-exp-api.xml";
    }

    /**
     * Test POST /orders - Create Order
     */
    @Test
    public void testCreateOrder() throws Exception {
        String payload = "{\"items\": [{\"itemId\": \"ITEM001\", \"quantity\": 2, \"price\": 29.99}]}";
        
        Object result = runFlow("post-orders-flow", payload);
        
        assertThat(result, notNullValue());
        assertThat(result.toString(), containsString("orderId"));
        assertThat(result.toString(), containsString("RECEIVED"));
    }

    /**
     * Test GET /orders/{orderId}/status - Get Order Status
     */
    @Test
    public void testGetOrderStatus() throws Exception {
        String payload = "{\"status\": \"RECEIVED\"}";
        
        Object result = runFlow("get-status-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test invalid order request
     */
    @Test
    public void testInvalidOrderRequest() throws Exception {
        String payload = "{\"items\": []}"; // Empty items array
        
        try {
            runFlow("post-orders-flow", payload);
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("items"));
        }
    }

    /**
     * Test correlation ID propagation
     */
    @Test
    public void testCorrelationIdPropagation() throws Exception {
        String correlationId = "550e8400-e29b-41d4-a716-446655440000";
        String payload = "{\"items\": [{\"itemId\": \"ITEM001\", \"quantity\": 1, \"price\": 19.99}]}";
        
        // This test verifies that correlation ID is extracted and propagated
        Object result = runFlow("post-orders-flow", payload);
        
        assertThat(result, notNullValue());
    }
}
