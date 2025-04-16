package com.paytheory.lib.configuration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PayorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PayTheoryConfigurationTest {

    private val validApiKey = "test-paytheory-apikey"
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `builder creates valid configuration with required parameters`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .build()
            
        // Then
        assertEquals(validApiKey, config.apiKey)
        assertEquals(1000, config.amount)
        assertEquals(PaymentMethodType.CARD, config.paymentMethodType)
        assertEquals(PaymentMethodAction.PAYMENT, config.paymentMethodAction)
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
        assertFalse(config.googlePayEnabled)
        assertTrue(config.isTestMode)
    }
    
    @Test
    fun `builder sets payment method type correctly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setPaymentMethodType(PaymentMethodType.ACH)
            .build()
            
        // Then
        assertEquals(PaymentMethodType.ACH, config.paymentMethodType)
    }
    
    @Test
    fun `builder sets payment method action correctly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(0)
            .setPaymentMethodAction(PaymentMethodAction.TOKEN)
            .build()
            
        // Then
        assertEquals(PaymentMethodAction.TOKEN, config.paymentMethodAction)
    }
    
    @Test
    fun `builder sets fee mode correctly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setFeeMode(FeeMode.MERCHANT_FEE)
            .build()
            
        // Then
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
    }
    
    @Test
    fun `builder sets service fee amount correctly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setFeeMode(FeeMode.MERCHANT_FEE)
            .setServiceFee(200)
            .build()
            
        // Then
        assertEquals(200, config.serviceFee)
    }
    
    @Test
    fun `builder sets payor info correctly`() {
        // Given
        val address = Address(
            line1 = "123 Main St",
            city = "Columbus",
            region = "OH",
            postal_code = "43215"
        )
        
        val payorInfo = PayorInfo(
            first_name = "Test",
            last_name = "User",
            email = "test@example.com",
            phone = "1234567890",
            address = address
        )
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setPayorInfo(payorInfo)
            .build()
            
        // Then
        assertEquals(payorInfo, config.payorInfo)
        assertEquals("Test", config.payorInfo?.first_name)
        assertEquals("User", config.payorInfo?.last_name)
        assertEquals("test@example.com", config.payorInfo?.email)
    }
    
    @Test
    fun `builder sets metadata correctly`() {
        // Given
        val metadata = hashMapOf<Any, Any>(
            "transaction_id" to "tx123",
            "customer_id" to "cust456",
            "department" to "science"
        )
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setMetadata(metadata)
            .build()
            
        // Then
        assertEquals(metadata, config.metadata)
        assertEquals("tx123", config.metadata["transaction_id"])
        assertEquals("science", config.metadata["department"])
    }
    
    @Test
    fun `builder sets isTestMode correctly`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setTestMode(false)
            .build()
            
        // Then
        assertFalse(config.isTestMode)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `builder throws exception with empty API key`() {
        // When - should throw exception
        PayTheoryConfiguration.Builder()
            .setApiKey("")
            .setAmount(1000)
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `builder throws exception with invalid API key format`() {
        // When - should throw exception
        PayTheoryConfiguration.Builder()
            .setApiKey("invalid-key-format")
            .setAmount(1000)
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `builder throws exception with negative amount for payment action`() {
        // When - should throw exception
        PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(-100)
            .setPaymentMethodAction(PaymentMethodAction.PAYMENT)
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `builder throws exception with invalid fee mode`() {
        // When - should throw exception
        PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setFeeMode("INVALID_FEE_MODE")
            .build()
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `builder throws exception with negative service fee`() {
        // When - should throw exception
        PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .setFeeMode(FeeMode.MERCHANT_FEE)
            .setServiceFee(-100)
            .build()
    }
    
    @Test
    fun `builder allows zero amount for token action`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(0)
            .setPaymentMethodAction(PaymentMethodAction.TOKEN)
            .build()
            
        // Then
        assertEquals(0, config.amount)
        assertEquals(PaymentMethodAction.TOKEN, config.paymentMethodAction)
    }
    
    @Test
    fun `builder uses default values when not specified`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .build()
            
        // Then
        assertEquals(PaymentMethodType.CARD, config.paymentMethodType)
        assertEquals(PaymentMethodAction.PAYMENT, config.paymentMethodAction)
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
        assertEquals(0, config.serviceFee)
        assertTrue(config.isTestMode)
        assertNull(config.payorInfo)
        assertTrue(config.metadata.isEmpty())
        assertFalse(config.googlePayEnabled)
    }
    
    @Test
    fun `builder correctly handles various configurations together`() {
        // Given
        val address = Address(
            line1 = "123 Main St",
            city = "Columbus",
            region = "OH",
            postal_code = "43215"
        )
        
        val payorInfo = PayorInfo(
            first_name = "Test",
            last_name = "User",
            email = "test@example.com",
            phone = null,
            address = address
        )
        
        val metadata = hashMapOf<Any, Any>("order_id" to "order789")
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1500)
            .setPaymentMethodType(PaymentMethodType.ACH)
            .setPaymentMethodAction(PaymentMethodAction.PAYMENT)
            .setFeeMode(FeeMode.MERCHANT_FEE)
            .setServiceFee(150)
            .setPayorInfo(payorInfo)
            .setMetadata(metadata)
            .setTestMode(false)
            .enableGooglePay("Test Merchant")
            .build()
            
        // Then
        assertEquals(validApiKey, config.apiKey)
        assertEquals(1500, config.amount)
        assertEquals(PaymentMethodType.ACH, config.paymentMethodType)
        assertEquals(PaymentMethodAction.PAYMENT, config.paymentMethodAction)
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
        assertEquals(150, config.serviceFee)
        assertEquals(payorInfo, config.payorInfo)
        assertEquals(metadata, config.metadata)
        assertFalse(config.isTestMode)
        assertTrue(config.googlePayEnabled)
        assertEquals("Test Merchant", config.googlePayMerchantName)
    }

    @Test
    fun `enableGooglePay sets merchant name and enables Google Pay`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .build()
            
        // Then
        assertTrue(config.googlePayEnabled)
        assertEquals("Test Merchant", config.googlePayMerchantName)
    }

    @Test
    fun `enableGooglePay allows empty merchant name with test API key`() {
        // When using test API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey) // This is the test API key in setup
            .setAmount(1000)
            .enableGooglePay("")
            .build()
            
        // Then empty name should be allowed
        assertTrue(config.googlePayEnabled)
        assertEquals("", config.googlePayMerchantName)
    }

    @Test
    fun `setGooglePaySupportedMethods sets the supported methods correctly`() {
        // When
        val methods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .setGooglePaySupportedMethods(methods)
            .build()
            
        // Then
        assertEquals(methods, config.googlePaySupportedMethods)
        assertTrue(config.googlePaySupportedMethods.contains("CRYPTOGRAM_3DS"))
    }

    @Test
    fun `Google Pay supports methods without CRYPTOGRAM_3DS in test mode`() {
        // When using test API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey) // This is the test API key in setup
            .setAmount(1000)
            .enableGooglePay("Test Merchant")
            .setGooglePaySupportedMethods(listOf("PAN_ONLY")) // Missing CRYPTOGRAM_3DS
            .build()
            
        // Then the methods should be set without error
        assertEquals(listOf("PAN_ONLY"), config.googlePaySupportedMethods)
        assertFalse(config.googlePaySupportedMethods.contains("CRYPTOGRAM_3DS"))
    }
} 