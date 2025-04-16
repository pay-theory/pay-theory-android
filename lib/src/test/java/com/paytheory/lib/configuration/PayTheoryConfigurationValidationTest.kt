package com.paytheory.lib.configuration

import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.configuration.PaymentMethodAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for validating PayTheoryConfiguration validation logic.
 * This test class specifically focuses on the validation logic
 * in the validatePaymentConfigAndExtractDetails method.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PayTheoryConfigurationValidationTest {

    // Constants matching those in PayTheoryConfiguration
    private val TEST_API_KEY = "test-paytheory-apikey"
    private val VALID_PRODUCTION_API_KEY = "partner-paytheory-apikey"
    private val VALID_LAB_API_KEY = "partner-paytheorylab-apikey"
    private val VALID_STUDY_API_KEY = "partner-paytheorystudy-apikey"
    private val INVALID_API_KEY = "partner-invalidstage-apikey"

    @Test
    fun `test API key parsing extracts correct partner name and stage`() {
        // Create configuration with valid production API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(VALID_PRODUCTION_API_KEY)
            .setAmount(1000)
            .build()
        
        // Verify partner name and stage extracted correctly
        assertEquals("partner", config.partnerName)
        assertEquals("paytheory", config.stageName)
        assertEquals("https://partner.paytheory.com/", config.apiBasePath)
    }
    
    @Test
    fun `test API key parsing with paytheorylab stage`() {
        // Create configuration with valid lab API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(VALID_LAB_API_KEY)
            .setAmount(1000)
            .build()
        
        // Verify partner name and stage extracted correctly
        assertEquals("partner", config.partnerName)
        assertEquals("paytheorylab", config.stageName)
        assertEquals("https://partner.paytheorylab.com/", config.apiBasePath)
    }
    
    @Test
    fun `test API key parsing with paytheorystudy stage`() {
        // Create configuration with valid study API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(VALID_STUDY_API_KEY)
            .setAmount(1000)
            .build()
        
        // Verify partner name and stage extracted correctly
        assertEquals("partner", config.partnerName)
        assertEquals("paytheorystudy", config.stageName)
        assertEquals("https://partner.paytheorystudy.com/", config.apiBasePath)
    }
    
    @Test
    fun `test test API key bypasses validation`() {
        // Create configuration with test API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .build()
        
        // Verify test values set without validation
        assertEquals("test", config.partnerName)
        assertEquals("paytheory", config.stageName)
        assertEquals("https://api.paytheory.com/", config.apiBasePath)
        assertTrue(config.isTestMode)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test invalid API key stage throws exception`() {
        // Should throw IllegalArgumentException
        PayTheoryConfiguration.Builder()
            .setApiKey(INVALID_API_KEY)
            .setAmount(1000)
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test amount less than 10 with payment action throws exception`() {
        // Should throw IllegalArgumentException for insufficient amount
        PayTheoryConfiguration.Builder()
            .setApiKey(VALID_PRODUCTION_API_KEY)
            .setAmount(5)  // Less than 10 cents
            .setPaymentMethodAction(PaymentMethodAction.PAYMENT)
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test non-zero amount with token action throws exception`() {
        // Should throw IllegalArgumentException for amount with token action
        PayTheoryConfiguration.Builder()
            .setApiKey(VALID_PRODUCTION_API_KEY)
            .setAmount(1000)  // Non-zero amount
            .setPaymentMethodAction(PaymentMethodAction.TOKEN)
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test Google Pay enabled without merchant name throws exception`() {
        // Should throw IllegalArgumentException for missing merchant name
        PayTheoryConfiguration.Builder()
            .setApiKey(VALID_PRODUCTION_API_KEY)
            .setAmount(1000)
            .enableGooglePay("")  // Empty merchant name
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `test Google Pay without CRYPTOGRAM_3DS method throws exception`() {
        // Should throw IllegalArgumentException for missing required auth method
        PayTheoryConfiguration.Builder()
            .setApiKey(VALID_PRODUCTION_API_KEY)
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .setGooglePaySupportedMethods(listOf("PAN_ONLY"))  // Missing CRYPTOGRAM_3DS
            .build()
    }
    
    @Test
    fun `test Google Pay with required auth method succeeds`() {
        // Create valid Google Pay configuration
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(VALID_PRODUCTION_API_KEY)
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .setGooglePaySupportedMethods(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))
            .build()
        
        // Verify configuration created successfully
        assertTrue(config.googlePayEnabled)
        assertEquals("Test Merchant", config.googlePayMerchantName)
        assertTrue(config.googlePaySupportedMethods.contains("CRYPTOGRAM_3DS"))
    }
    
    @Test
    fun `test zero amount with token action is valid`() {
        // Create configuration with token action and zero amount
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(VALID_PRODUCTION_API_KEY)
            .setAmount(0)
            .setPaymentMethodAction(PaymentMethodAction.TOKEN)
            .build()
        
        // Verify configuration created successfully
        assertEquals(0, config.amount)
        assertEquals(PaymentMethodAction.TOKEN, config.paymentMethodAction)
    }
    
    @Test
    fun `test Google Pay validation is bypassed with test API key`() {
        // Create configuration with test API key and Google Pay enabled but no merchant name
        // This would normally throw an exception, but should be bypassed with test API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .enableGooglePay("")  // Empty merchant name, but test mode bypasses validation
            .build()
        
        // Verify configuration created successfully despite invalid Google Pay settings
        assertTrue(config.isTestMode)
        assertTrue(config.googlePayEnabled)
        assertEquals("", config.googlePayMerchantName)
    }
    
    @Test
    fun `test Google Pay methods validation is bypassed with test API key`() {
        // Create configuration with test API key and Google Pay enabled with invalid methods
        // This would normally throw an exception, but should be bypassed with test API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .enableGooglePay("Test Merchant")
            .setGooglePaySupportedMethods(listOf("PAN_ONLY"))  // Missing CRYPTOGRAM_3DS
            .build()
        
        // Verify configuration created successfully despite invalid Google Pay settings
        assertTrue(config.isTestMode)
        assertTrue(config.googlePayEnabled)
        assertEquals(listOf("PAN_ONLY"), config.googlePaySupportedMethods)
    }
}