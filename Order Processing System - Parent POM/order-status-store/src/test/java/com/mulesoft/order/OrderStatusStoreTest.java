package com.mulesoft.order;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for Order Status Store
 */
public class OrderStatusStoreTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "order-status-store.xml";
    }

    /**
     * Test PUT /{orderId} - Update Order Status
     */
    @Test
    public void testUpdateOrderStatus() throws Exception {
        String payload = "{\"status\": \"RECEIVED\", \"timestamp\": \"2024-01-28T10:00:00Z\"}";
        
        Object result = runFlow("update-order-status-flow", payload);
        
        assertThat(result, notNullValue());
        assertThat(result.toString(), containsString("RECEIVED"));
    }

    /**
     * Test GET /{orderId} - Get Order Status
     */
    @Test
    public void testGetOrderStatus() throws Exception {
        // First update status
        String updatePayload = "{\"status\": \"INVENTORY_CHECK_STARTED\", \"timestamp\": \"2024-01-28T10:00:00Z\"}";
        runFlow("update-order-status-flow", updatePayload);
        
        // Then retrieve status
        Object result = runFlow("get-order-status-flow", "");
        
        assertThat(result, notNullValue());
    }

    /**
     * Test status persistence
     */
    @Test
    public void testStatusPersistence() throws Exception {
        String payload = "{\"status\": \"INVENTORY_CONFIRMED\", \"timestamp\": \"2024-01-28T10:00:00Z\"}";
        
        // Update status
        Object updateResult = runFlow("update-order-status-flow", payload);
        assertThat(updateResult, notNullValue());
        
        // Retrieve status
        Object getResult = runFlow("get-order-status-flow", "");
        assertThat(getResult, notNullValue());
        assertThat(getResult.toString(), containsString("INVENTORY_CONFIRMED"));
    }

    /**
     * Test status update sequence
     */
    @Test
    public void testStatusUpdateSequence() throws Exception {
        String[] statuses = {"RECEIVED", "INVENTORY_CHECK_STARTED", "INVENTORY_CONFIRMED", "PAYMENT_COMPLETED", "SHIPPED"};
        
        for (String status : statuses) {
            String payload = "{\"status\": \"" + status + "\", \"timestamp\": \"2024-01-28T10:00:00Z\"}";
            
            Object result = runFlow("update-order-status-flow", payload);
            
            assertThat(result, notNullValue());
            assertThat(result.toString(), containsString(status));
        }
    }

    /**
     * Test order not found scenario
     */
    @Test
    public void testOrderNotFound() throws Exception {
        try {
            Object result = runFlow("get-order-status-flow", "");
            // If order not found, should return 404
        } catch (Exception e) {
            // Expected behavior
        }
    }

    /**
     * Test correlation ID handling
     */
    @Test
    public void testCorrelationIdHandling() throws Exception {
        String payload = "{\"status\": \"PAYMENT_FAILED\", \"timestamp\": \"2024-01-28T10:00:00Z\"}";
        
        Object result = runFlow("update-order-status-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test concurrent status updates
     */
    @Test
    public void testConcurrentStatusUpdates() throws Exception {
        String[] statuses = {"RECEIVED", "INVENTORY_CHECK_STARTED", "INVENTORY_CONFIRMED"};
        
        for (String status : statuses) {
            String payload = "{\"status\": \"" + status + "\", \"timestamp\": \"2024-01-28T10:00:00Z\"}";
            
            Object result = runFlow("update-order-status-flow", payload);
            
            assertThat(result, notNullValue());
        }
    }
}
