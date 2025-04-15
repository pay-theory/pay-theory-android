package com.paytheory.lib.configuration

import com.paytheory.lib.PayTheoryConfiguration
import org.junit.Assert.*
import org.junit.Test

class GooglePayConfigurationTest {

    private val validApiKey = "test-paytheorylab-test"
    
    @Test
    fun testDefaultGooglePaySettings() {
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .build()
            
        // Verify default values
        assertFalse(config.googlePayEnabled)
        assertNull(config.googlePayMerchantName)
        assertTrue(config.googlePayAllowPrepaidCards)
        assertTrue(config.googlePayAllowCreditCards)
        assertFalse(config.googlePayBillingAddressRequired)
        assertEquals(GooglePayBillingAddressFormat.MINIMAL, config.googlePayBillingAddressFormat)
        assertFalse(config.googlePayShippingAddressRequired)
        assertFalse(config.googlePayPhoneNumberRequired)
        assertEquals(GooglePayButtonType.PAY, config.googlePayButtonType)
        assertEquals(GooglePayButtonColor.BLACK, config.googlePayButtonColor)
        assertEquals(GooglePayEnvironment.TEST, config.googlePayEnvironment)
        assertEquals(GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS, config.googlePayAllowedCardNetworks)
        assertEquals(GooglePayConstants.DEFAULT_SUPPORTED_METHODS, config.googlePaySupportedMethods)
    }
    
    @Test
    fun testEnableGooglePay() {
        val merchantName = "Test Merchant"
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .enableGooglePay(merchantName)
            .build()
            
        // Verify Google Pay is enabled with the right merchant name
        assertTrue(config.googlePayEnabled)
        assertEquals(merchantName, config.googlePayMerchantName)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testGooglePayEnabledWithoutMerchantName() {
        // This should throw an exception because merchant name is required when Google Pay is enabled
        PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .apply { 
                // Manually set googlePayEnabled without setting merchantName
                this.googlePayEnabled = true 
            }
            .build()
    }
    
    @Test
    fun testCustomGooglePaySettings() {
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .setGooglePayAllowPrepaidCards(false)
            .setGooglePayAllowCreditCards(false)
            .setGooglePayBillingAddressRequired(true)
            .setGooglePayBillingAddressFormat(GooglePayBillingAddressFormat.FULL)
            .setGooglePayShippingAddressRequired(true)
            .setGooglePayPhoneNumberRequired(true)
            .setGooglePayButtonType(GooglePayButtonType.CHECKOUT)
            .setGooglePayButtonColor(GooglePayButtonColor.WHITE)
            .setGooglePayEnvironment(GooglePayEnvironment.PRODUCTION)
            .build()
        
        // Verify custom settings
        assertTrue(config.googlePayEnabled)
        assertEquals("Test Merchant", config.googlePayMerchantName)
        assertFalse(config.googlePayAllowPrepaidCards)
        assertFalse(config.googlePayAllowCreditCards)
        assertTrue(config.googlePayBillingAddressRequired)
        assertEquals(GooglePayBillingAddressFormat.FULL, config.googlePayBillingAddressFormat)
        assertTrue(config.googlePayShippingAddressRequired)
        assertTrue(config.googlePayPhoneNumberRequired)
        assertEquals(GooglePayButtonType.CHECKOUT, config.googlePayButtonType)
        assertEquals(GooglePayButtonColor.WHITE, config.googlePayButtonColor)
        assertEquals(GooglePayEnvironment.PRODUCTION, config.googlePayEnvironment)
    }
    
    @Test
    fun testIsReadyToPayRequestBuilding() {
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .build()
            
        val request = GooglePayRequestBuilder.buildIsReadyToPayRequest(config)
        
        // Verify request structure
        assertEquals(2, request.getInt("apiVersion"))
        assertEquals(0, request.getInt("apiVersionMinor"))
        assertTrue(request.has("allowedPaymentMethods"))
        
        val allowedMethods = request.getJSONArray("allowedPaymentMethods")
        assertEquals(1, allowedMethods.length())
        
        val cardMethod = allowedMethods.getJSONObject(0)
        assertEquals("CARD", cardMethod.getString("type"))
        
        val parameters = cardMethod.getJSONObject("parameters")
        assertTrue(parameters.has("allowedAuthMethods"))
        assertTrue(parameters.has("allowedCardNetworks"))
        assertTrue(parameters.getBoolean("allowPrepaidCards"))
        assertTrue(parameters.getBoolean("allowCreditCards"))
    }
    
    @Test
    fun testPaymentDataRequestBuilding() {
        val merchantName = "Test Merchant"
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .enableGooglePay(merchantName)
            .setGooglePayBillingAddressRequired(true)
            .setGooglePayShippingAddressRequired(true)
            .setGooglePayPhoneNumberRequired(true)
            .build()
            
        val price = "10.00"
        val request = GooglePayRequestBuilder.buildPaymentDataRequest(config, price)
        
        // Verify request structure
        assertEquals(2, request.getInt("apiVersion"))
        assertEquals(0, request.getInt("apiVersionMinor"))
        
        // Verify transaction info
        val transactionInfo = request.getJSONObject("transactionInfo")
        assertEquals(price, transactionInfo.getString("totalPrice"))
        assertEquals("FINAL", transactionInfo.getString("totalPriceStatus"))
        assertEquals(GooglePayConstants.CURRENCY_CODE, transactionInfo.getString("currencyCode"))
        assertEquals(GooglePayConstants.COUNTRY_CODE, transactionInfo.getString("countryCode"))
        
        // Verify merchant info
        val merchantInfo = request.getJSONObject("merchantInfo")
        assertEquals(merchantName, merchantInfo.getString("merchantName"))
        
        // Verify shipping address requirements
        assertTrue(request.getBoolean("shippingAddressRequired"))
        val shippingParams = request.getJSONObject("shippingAddressParameters")
        assertTrue(shippingParams.getBoolean("phoneNumberRequired"))
        
        // Verify payment method with tokenization
        val allowedMethods = request.getJSONArray("allowedPaymentMethods")
        val cardMethod = allowedMethods.getJSONObject(0)
        val tokenSpec = cardMethod.getJSONObject("tokenizationSpecification")
        assertEquals("PAYMENT_GATEWAY", tokenSpec.getString("type"))
        
        val tokenParams = tokenSpec.getJSONObject("parameters")
        assertEquals(GooglePayConstants.GATEWAY_NAME, tokenParams.getString("gateway"))
        assertEquals(GooglePayConstants.GATEWAY_MERCHANT_ID, tokenParams.getString("gatewayMerchantId"))
        
        // Verify billing address parameters
        val parameters = cardMethod.getJSONObject("parameters")
        assertTrue(parameters.getBoolean("billingAddressRequired"))
        val billingParams = parameters.getJSONObject("billingAddressParameters")
        assertEquals("MIN", billingParams.getString("format"))
    }
} 