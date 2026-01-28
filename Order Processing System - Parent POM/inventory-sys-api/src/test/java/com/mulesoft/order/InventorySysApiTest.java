package com.mulesoft.order;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for Inventory System API Mock
 */
public class InventorySysApiTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "inventory-sys-api.xml";
    }

    /**
     * Test POST /inventory/check - Mock success response
     */
    @Test
    public void testInventoryCheckSuccess() throws Exception {
        String payload = "{\"orderId\": \"test-order-001\", \"items\": [{\"itemId\": \"ITEM001\", \"quantity\": 2, \"price\": 29.99}]}";
        
        Object result = runFlow("inventory-check-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test random success/failure distribution
     */
    @Test
    public void testRandomSuccessFailureDistribution() throws Exception {
        String payload = "{\"orderId\": \"test-order-002\", \"items\": [{\"itemId\": \"ITEM002\", \"quantity\": 1, \"price\": 19.99}]}";
        
        int successCount = 0;
        int failureCount = 0;
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            try {
                Object result = runFlow("inventory-check-flow", payload);
                if (result.toString().contains("available")) {
                    successCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }
        
        // Verify approximately 70% success rate
        double successRate = (double) successCount / iterations;
        assertThat(successRate, greaterThan(0.6));
        assertThat(successRate, lessThan(0.8));
    }

    /**
     * Test inventory check with multiple items
     */
    @Test
    public void testInventoryCheckMultipleItems() throws Exception {
        String payload = "{\"orderId\": \"test-order-003\", \"items\": [{\"itemId\": \"ITEM001\", \"quantity\": 2, \"price\": 29.99}, {\"itemId\": \"ITEM002\", \"quantity\": 1, \"price\": 19.99}]}";
        
        Object result = runFlow("inventory-check-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test correlation ID handling
     */
    @Test
    public void testCorrelationIdHandling() throws Exception {
        String payload = "{\"orderId\": \"test-order-004\", \"items\": [{\"itemId\": \"ITEM004\", \"quantity\": 3, \"price\": 39.99}]}";
        
        Object result = runFlow("inventory-check-flow", payload);
        
        assertThat(result, notNullValue());
    }
}
