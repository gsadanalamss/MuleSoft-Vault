package com.mulesoft.order;

import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for Payment System API Mock
 */
public class PaymentSysApiTest extends MuleArtifactFunctionalTestCase {

    @Override
    protected String getConfigFile() {
        return "payment-sys-api.xml";
    }

    /**
     * Test POST /payment/charge - Mock success response
     */
    @Test
    public void testPaymentChargeSuccess() throws Exception {
        String payload = "{\"orderId\": \"test-order-001\", \"amount\": 100.00, \"currency\": \"USD\"}";
        
        Object result = runFlow("payment-charge-flow", payload);
        
        assertThat(result, notNullValue());
    }

    /**
     * Test random success/failure distribution
     */
    @Test
    public void testRandomSuccessFailureDistribution() throws Exception {
        String payload = "{\"orderId\": \"test-order-002\", \"amount\": 50.00, \"currency\": \"USD\"}";
        
        int successCount = 0;
        int failureCount = 0;
        int iterations = 100;
        
        for (int i = 0; i < iterations; i++) {
            try {
                Object result = runFlow("payment-charge-flow", payload);
                if (result.toString().contains("SUCCESS")) {
                    successCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }
        
        // Verify approximately 60% success rate
        double successRate = (double) successCount / iterations;
        assertThat(successRate, greaterThan(0.5));
        assertThat(successRate, lessThan(0.7));
    }

    /**
     * Test payment charge with various amounts
     */
    @Test
    public void testPaymentChargeVariousAmounts() throws Exception {
        String[] amounts = {"10.00", "50.00", "100.00", "500.00", "1000.00"};
        
        for (String amount : amounts) {
            String payload = "{\"orderId\": \"test-order-003\", \"amount\": " + amount + ", \"currency\": \"USD\"}";
            
            Object result = runFlow("payment-charge-flow", payload);
            
            assertThat(result, notNullValue());
        }
    }

    /**
     * Test transaction ID generation
     */
    @Test
    public void testTransactionIdGeneration() throws Exception {
        String payload = "{\"orderId\": \"test-order-004\", \"amount\": 75.00, \"currency\": \"USD\"}";
        
        Object result = runFlow("payment-charge-flow", payload);
        
        assertThat(result, notNullValue());
        assertThat(result.toString(), containsString("transactionId"));
    }

    /**
     * Test correlation ID handling
     */
    @Test
    public void testCorrelationIdHandling() throws Exception {
        String payload = "{\"orderId\": \"test-order-005\", \"amount\": 200.00, \"currency\": \"USD\"}";
        
        Object result = runFlow("payment-charge-flow", payload);
        
        assertThat(result, notNullValue());
    }
}
