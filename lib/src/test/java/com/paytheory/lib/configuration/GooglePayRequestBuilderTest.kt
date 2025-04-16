package com.paytheory.lib.configuration

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.paytheory.lib.PayTheoryConfiguration
import org.json.JSONObject
import org.robolectric.annotation.Config

/**
 * Tests for the GooglePayRequestBuilder class
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayRequestBuilderTest {

    private lateinit var basicConfig: PayTheoryConfiguration
    private lateinit var fullConfig: PayTheoryConfiguration

    @Before
    fun setup() {
        // Basic config with minimal required settings
        basicConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .build()
            
        // Full config with all Google Pay options set
        fullConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setAmount(2000)
            .enableGooglePay("Full Test Merchant")
            .setGooglePayAllowPrepaidCards(false)
            .setGooglePayAllowCreditCards(false)
            .setGooglePayBillingAddressRequired(true)
            .setGooglePayBillingAddressFormat(GooglePayBillingAddressFormat.FULL)
            .setGooglePayShippingAddressRequired(true)
            .setGooglePayPhoneNumberRequired(true)
            .setGooglePayAllowedCardNetworks(listOf("VISA", "MASTERCARD"))
            .setGooglePaySupportedMethods(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))
            .setGooglePayEnvironment(GooglePayEnvironment.PRODUCTION)
            .build()
    }

    @Test
    fun `buildPaymentDataRequest returns valid request with basic config`() {
        // When
        val jsonObject = GooglePayRequestBuilder.buildPaymentDataRequest(basicConfig, "10.00")
        
        // Then
        // Verify top-level properties
        val transactionInfo = jsonObject.getJSONObject("transactionInfo")
        assertEquals("10.00", transactionInfo.getString("totalPrice"))
        assertEquals("FINAL", transactionInfo.getString("totalPriceStatus"))
        assertEquals("USD", transactionInfo.getString("currencyCode"))
        
        // Verify merchant info
        val merchantInfo = jsonObject.getJSONObject("merchantInfo")
        assertEquals("Test Merchant", merchantInfo.getString("merchantName"))
        
        // Verify allowed payment methods
        val allowedPaymentMethods = jsonObject.getJSONArray("allowedPaymentMethods")
        assertEquals(1, allowedPaymentMethods.length())
        val cardPaymentMethod = allowedPaymentMethods.getJSONObject(0)
        assertEquals("CARD", cardPaymentMethod.getString("type"))
        
        // Verify card parameters
        val parameters = cardPaymentMethod.getJSONObject("parameters")
        assertTrue(parameters.getBoolean("allowPrepaidCards"))
        assertTrue(parameters.getBoolean("allowCreditCards"))
        assertFalse(parameters.has("billingAddressRequired") || 
                   parameters.optBoolean("billingAddressRequired", false))
    }
    
    @Test
    fun `buildPaymentDataRequest includes all settings with full config`() {
        // When
        val jsonObject = GooglePayRequestBuilder.buildPaymentDataRequest(fullConfig, "20.00")
        
        // Then
        // Verify basic properties
        val transactionInfo = jsonObject.getJSONObject("transactionInfo")
        assertEquals("20.00", transactionInfo.getString("totalPrice"))
        assertEquals("USD", transactionInfo.getString("currencyCode"))
        
        // Verify merchant info
        val merchantInfo = jsonObject.getJSONObject("merchantInfo")
        assertEquals("Full Test Merchant", merchantInfo.getString("merchantName"))
        
        // Verify shipping address requirements
        assertTrue(jsonObject.getBoolean("shippingAddressRequired"))
        assertTrue(jsonObject.getJSONObject("shippingAddressParameters").getBoolean("phoneNumberRequired"))
        
        // Verify allowed payment methods
        val allowedPaymentMethods = jsonObject.getJSONArray("allowedPaymentMethods")
        val cardPaymentMethod = allowedPaymentMethods.getJSONObject(0)
        
        // Verify card parameters
        val parameters = cardPaymentMethod.getJSONObject("parameters")
        assertFalse(parameters.getBoolean("allowPrepaidCards"))
        assertFalse(parameters.getBoolean("allowCreditCards"))
        assertTrue(parameters.getBoolean("billingAddressRequired"))
        assertEquals("FULL", parameters.getJSONObject("billingAddressParameters").getString("format"))
        
        // Verify card networks
        val allowedCardNetworks = parameters.getJSONArray("allowedCardNetworks")
        assertEquals(2, allowedCardNetworks.length())
        assertEquals("VISA", allowedCardNetworks.getString(0))
        assertEquals("MASTERCARD", allowedCardNetworks.getString(1))
    }
    
    @Test
    fun `buildIsReadyToPayRequest returns valid request with supported card networks`() {
        // When
        val jsonObject = GooglePayRequestBuilder.buildIsReadyToPayRequest(fullConfig)
        
        // Then
        // Verify allowed payment methods
        val allowedPaymentMethods = jsonObject.getJSONArray("allowedPaymentMethods")
        assertEquals(1, allowedPaymentMethods.length())
        val cardPaymentMethod = allowedPaymentMethods.getJSONObject(0)
        assertEquals("CARD", cardPaymentMethod.getString("type"))
        
        // Verify card parameters
        val parameters = cardPaymentMethod.getJSONObject("parameters")
        val allowedCardNetworks = parameters.getJSONArray("allowedCardNetworks")
        assertEquals(2, allowedCardNetworks.length())
        assertEquals("VISA", allowedCardNetworks.getString(0))
        assertEquals("MASTERCARD", allowedCardNetworks.getString(1))
        
        // Verify auth methods
        val allowedAuthMethods = parameters.getJSONArray("allowedAuthMethods")
        assertEquals(2, allowedAuthMethods.length())
        assertTrue(allowedAuthMethods.getString(0) == "PAN_ONLY" || allowedAuthMethods.getString(0) == "CRYPTOGRAM_3DS")
        assertTrue(allowedAuthMethods.getString(1) == "PAN_ONLY" || allowedAuthMethods.getString(1) == "CRYPTOGRAM_3DS")
    }
    
    @Test
    fun `buildTokenizationSpecification creates valid specification`() {
        // When
        val tokenizationSpec = GooglePayRequestBuilder.buildTokenizationSpecification()
        
        // Then
        assertEquals("PAYMENT_GATEWAY", tokenizationSpec.getString("type"))
        val parameters = tokenizationSpec.getJSONObject("parameters")
        assertEquals(GooglePayConstants.GATEWAY_NAME, parameters.getString("gateway"))
        assertEquals(GooglePayConstants.GATEWAY_MERCHANT_ID, parameters.getString("gatewayMerchantId"))
    }
} 