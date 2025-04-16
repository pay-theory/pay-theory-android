package com.paytheory.lib

import com.paytheory.lib.configuration.*
import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PayorInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PayTheoryConfigurationTest {
    
    private val TEST_API_KEY = "test-paytheory-apikey"
    
    @Test
    fun `constructor with test API key should default to test mode`() {
        // When
        val config = PayTheoryConfiguration(apiKey = TEST_API_KEY)
        
        // Then
        assertTrue(config.isTestMode)
        assertEquals("test", config.partnerName)
        assertEquals("paytheory", config.stageName)
        assertEquals("https://api.paytheory.com/", config.apiBasePath)
    }
    
    @Test
    fun `constructor should set default values correctly`() {
        // When
        val config = PayTheoryConfiguration(apiKey = TEST_API_KEY)
        
        // Then
        assertEquals(0, config.amount)
        assertEquals(PaymentMethodType.CARD, config.paymentMethodType)
        assertEquals(PaymentMethodAction.PAYMENT, config.paymentMethodAction)
        assertFalse(config.requireAccountName)
        assertFalse(config.requireBillingAddress)
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
        assertTrue(config.metadata.isEmpty())
        assertEquals(null, config.payorInfo)
    }
    
    @Test
    fun `constructor should validate amount for payment action with test key`() {
        // No exception should be thrown for test key
        PayTheoryConfiguration(
            apiKey = TEST_API_KEY,
            amount = 5,
            paymentMethodAction = PaymentMethodAction.PAYMENT
        )
    }
    
    @Test
    fun `constructor should validate amount for token action with test key`() {
        // No exception should be thrown for test key
        PayTheoryConfiguration(
            apiKey = TEST_API_KEY,
            amount = 100,
            paymentMethodAction = PaymentMethodAction.TOKEN
        )
    }
    
    @Test
    fun `builder should create valid configuration with test api key`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .setAmount(1099)
            .setPaymentMethodType(PaymentMethodType.CARD)
            .setPaymentMethodAction(PaymentMethodAction.PAYMENT)
            .setRequireAccountName(true)
            .setRequireBillingAddress(true)
            .setFeeMode(FeeMode.MERCHANT_FEE)
            .build()
        
        // Then
        assertEquals(TEST_API_KEY, config.apiKey)
        assertEquals(1099, config.amount)
        assertEquals(PaymentMethodType.CARD, config.paymentMethodType)
        assertEquals(PaymentMethodAction.PAYMENT, config.paymentMethodAction)
        assertTrue(config.requireAccountName)
        assertTrue(config.requireBillingAddress)
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
    }
    
    @Test
    fun `builder should validate inputs`() {
        // Then
        assertFailsWith<IllegalArgumentException> {
            PayTheoryConfiguration.Builder()
                .setApiKey("")  // Empty API key
                .build()
        }
        
        assertFailsWith<IllegalArgumentException> {
            PayTheoryConfiguration.Builder()
                .setApiKey(TEST_API_KEY)
                .setAmount(-100)  // Negative amount
                .build()
        }
        
        assertFailsWith<IllegalArgumentException> {
            PayTheoryConfiguration.Builder()
                .setApiKey(TEST_API_KEY)
                .setServiceFee(-50)  // Negative service fee
                .build()
        }
    }
    
    @Test
    fun `builder should configure Google Pay settings correctly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .setAmount(1099)
            .enableGooglePay("Test Merchant")
            .setGooglePayBillingAddressRequired(true)
            .setGooglePayBillingAddressFormat(GooglePayBillingAddressFormat.FULL)
            .setGooglePayEnvironment(GooglePayEnvironment.PRODUCTION)
            .setGooglePayButtonType(GooglePayButtonType.CHECKOUT)
            .setGooglePayButtonColor(GooglePayButtonColor.WHITE)
            .build()
        
        // Then
        assertTrue(config.googlePayEnabled)
        assertEquals("Test Merchant", config.googlePayMerchantName)
        assertTrue(config.googlePayBillingAddressRequired)
        assertEquals(GooglePayBillingAddressFormat.FULL, config.googlePayBillingAddressFormat)
        assertEquals(GooglePayEnvironment.PRODUCTION, config.googlePayEnvironment)
        assertEquals(GooglePayButtonType.CHECKOUT, config.googlePayButtonType)
        assertEquals(GooglePayButtonColor.WHITE, config.googlePayButtonColor)
    }
    
    @Test
    fun `builder should set metadata and payor info correctly`() {
        // Given
        val metadata = hashMapOf<Any, Any>("customerId" to "12345", "source" to "mobile")
        val payorInfo = PayorInfo(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            phone = "123-456-7890",
            address = Address(
                line1 = "123 Main St",
                city = "Anytown",
                region = "OH",
                postal_code = "12345"
            )
        )
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .setMetadata(metadata)
            .setPayorInfo(payorInfo)
            .build()
        
        // Then
        assertEquals(metadata, config.metadata)
        assertEquals(payorInfo, config.payorInfo)
    }
    
    @Test
    fun `builder should set test mode explicitly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .setTestMode(true)  // Explicitly set test mode
            .build()
        
        // Then
        assertTrue(config.isTestMode)
    }
    
    @Test
    fun `builder should set receipt options correctly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(TEST_API_KEY)
            .setSendReceipt(true)
            .setReceiptDescription("Custom Receipt")
            .build()
        
        // Then
        assertTrue(config.sendReceipt)
        assertEquals("Custom Receipt", config.receiptDescription)
    }
} 