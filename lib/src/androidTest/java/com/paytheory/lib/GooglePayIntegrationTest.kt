package com.paytheory.lib

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.wallet.PaymentsClient
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.googlepay.GooglePayClient
import com.paytheory.lib.googlepay.GooglePayUtil
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore

/**
 * Integration tests for Google Pay functionality
 */
@RunWith(AndroidJUnit4::class)
class GooglePayIntegrationTest {

    private lateinit var context: Context
    private lateinit var googlePayClient: GooglePayClient
    private lateinit var googlePayUtil: GooglePayUtil
    
    // Standard test card networks and auth methods for Google Pay
    private val allowedCardNetworks = listOf("VISA", "MASTERCARD", "AMEX", "DISCOVER")
    private val allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")

    @Before
    fun setup() {
        // Get the context of the app under test
        context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Initialize Google Pay client and util
        googlePayClient = GooglePayClient()
        googlePayUtil = GooglePayUtil.getInstance()
    }

    @Test
    fun googlePayClientCanBeInitialized() {
        // Verify the client was successfully created
        assertNotNull("Google Pay client should not be null", googlePayClient)
        
        // Log test execution
        android.util.Log.d("GooglePayIntegrationTest", "Google Pay client successfully initialized")
    }
    
    /**
     * Tests Google Pay availability in the current environment.
     * 
     * Note: This test cannot properly test Google Pay availability
     * since we don't have an Activity context in this test.
     * It is marked with @Ignore to prevent failures but kept for reference.
     */
    @Test
    @Ignore("This test requires a real Activity context")
    fun testGooglePayAvailability() {
        // This test would require a real activity
        android.util.Log.d("GooglePayIntegrationTest", "Google Pay availability test skipped - requires real Activity")
    }
    
    /**
     * Tests the creation of payment data request JSON with minimum required parameters.
     */
    @Test
    fun testCreatePaymentDataRequestMinimumParameters() {
        val requestJson = googlePayClient.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            allowedCardNetworks = allowedCardNetworks,
            allowedAuthMethods = allowedAuthMethods
        )
        
        // Verify JSON is not empty
        assertNotNull("Payment data request JSON should not be null", requestJson)
        assertTrue("Payment data request JSON should not be empty", requestJson.isNotEmpty())
        
        // Log the generated JSON for debugging
        android.util.Log.d("GooglePayIntegrationTest", "Generated payment request JSON: $requestJson")
        
        // Verify it's valid JSON by parsing it
        try {
            val jsonObject = JSONObject(requestJson)
            assertTrue("JSON should have apiVersion field", jsonObject.has("apiVersion"))
            assertTrue("JSON should have allowedPaymentMethods field", jsonObject.has("allowedPaymentMethods"))
            assertTrue("JSON should have transactionInfo field", jsonObject.has("transactionInfo"))
        } catch (e: Exception) {
            assertFalse("JSON parsing should not throw exception: ${e.message}", true)
        }
    }
    
    /**
     * Tests the creation of payment data request JSON with all parameters provided.
     */
    @Test
    fun testCreatePaymentDataRequestAllParameters() {
        val requestJson = googlePayClient.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            billingAddressRequired = true,
            billingAddressFormat = com.paytheory.lib.configuration.GooglePayBillingAddressFormat.FULL,
            shippingAddressRequired = true,
            phoneNumberRequired = true,
            environment = GooglePayEnvironment.TEST,
            allowPrepaidCards = true,
            allowCreditCards = true,
            allowedCardNetworks = allowedCardNetworks,
            allowedAuthMethods = allowedAuthMethods
        )
        
        // Verify JSON is not empty
        assertNotNull("Payment data request JSON should not be null", requestJson)
        assertTrue("Payment data request JSON should not be empty", requestJson.isNotEmpty())
        
        // Log the generated JSON for debugging
        android.util.Log.d("GooglePayIntegrationTest", "Generated full payment request JSON: $requestJson")
        
        // Verify it's valid JSON by parsing it
        try {
            val jsonObject = JSONObject(requestJson)
            assertTrue("JSON should have apiVersion field", jsonObject.has("apiVersion"))
            assertTrue("JSON should have shippingAddressRequired field", jsonObject.has("shippingAddressRequired"))
        } catch (e: Exception) {
            assertFalse("JSON parsing should not throw exception: ${e.message}", true)
        }
    }
    
    /**
     * Verifies the integration test environment is working properly.
     */
    @Test
    fun verifyIntegrationTestSetup() {       
        // Do a basic assertion to verify test framework is working
        assertNotNull("Context should not be null", context)
        
        // Log success
        android.util.Log.d("GooglePayIntegrationTest", "Integration test environment is working properly")
    }
} 