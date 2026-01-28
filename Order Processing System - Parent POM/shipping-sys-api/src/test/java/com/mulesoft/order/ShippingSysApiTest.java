package com.mulesoft.order;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for Shipping System API Mock
 */
public class ShippingSysApiTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "shipping-sys-api.xml";
    }

    /**
     * Test POST /shipping/create - Mock success response
     */
    @Test
    public void testShippingCreateSuccess() throws Exception {
        String payload = "{\"orderId\": \"test-order-001\", \"shippingAddress\": \"123 Main St\", \"items\": [{\"itemId\": \"ITEM001\", \"quantity\": 2, \"price\": 29.99}]}";
        
        Object result = runFlow("shipping-create-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test async response with 202 status
     */
    @Test
    public void testAsyncResponse() throws Exception {
        String payload = "{\"orderId\": \"test-order-002\", \"shippingAddress\": \"456 Oak Ave\", \"items\": [{\"itemId\": \"ITEM002\", \"quantity\": 1, \"price\": 19.99}]}";
        
        Object result = runFlow("shipping-create-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test shipment ID generation
     */
    @Test
    public void testShipmentIdGeneration() throws Exception {
        String payload = "{\"orderId\": \"test-order-003\", \"shippingAddress\": \"789 Pine Rd\", \"items\": [{\"itemId\": \"ITEM003\", \"quantity\": 3, \"price\": 39.99}]}";
        
        Object result = runFlow("shipping-create-flow", payload);
        
        assertThat(result, notNullValue());
        assertThat(result.toString(), containsString("shippingId"));
    }

    /**
     * Test estimated delivery calculation
     */
    @Test
    public void testEstimatedDeliveryCalculation() throws Exception {
        String payload = "{\"orderId\": \"test-order-004\", \"shippingAddress\": \"321 Elm St\", \"items\": [{\"itemId\": \"ITEM004\", \"quantity\": 1, \"price\": 49.99}]}";
        
        Object result = runFlow("shipping-create-flow", payload);
        
        assertThat(result, notNullValue());
        assertThat(result.toString(), containsString("estimatedDelivery"));
    }

    /**
     * Test random failure handling
     */
    @Test
    public void testRandomFailureHandling() throws Exception {
        String payload = "{\"orderId\": \"test-order-005\", \"shippingAddress\": \"654 Maple Dr\", \"items\": [{\"itemId\": \"ITEM005\", \"quantity\": 2, \"price\": 59.99}]}";
        
        Object result = runFlow("shipping-create-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test correlation ID handling
     */
    @Test
    public void testCorrelationIdHandling() throws Exception {
        String payload = "{\"orderId\": \"test-order-006\", \"shippingAddress\": \"987 Birch Ln\", \"items\": [{\"itemId\": \"ITEM006\", \"quantity\": 1, \"price\": 69.99}]}";
        
        Object result = runFlow("shipping-create-flow", payload);
        
        assertThat(result, notNullValue());
    }
}
