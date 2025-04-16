package com.paytheory.lib.configuration

import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.configuration.PaymentMethodAction
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PayorInfo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for PayTheoryConfiguration settings and parameters.
 * This test class focuses on settings and their default values
 * in the PayTheoryConfiguration class.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PayTheoryConfigurationSettingsTest {

    private val testApiKey = "test-paytheory-apikey"

    @Test
    fun `default settings are correctly applied`() {
        // Create default configuration
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(testApiKey)
            .setAmount(1000)
            .build()
        
        // Verify default settings
        assertEquals(testApiKey, config.apiKey)
        assertEquals(1000, config.amount)
        assertEquals(PaymentMethodType.CARD, config.paymentMethodType)
        assertEquals(PaymentMethodAction.PAYMENT, config.paymentMethodAction)
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
        assertFalse(config.requireAccountName)
        assertFalse(config.requireBillingAddress)
        assertTrue(config.metadata.isEmpty())
        assertNull(config.payorInfo)
        assertEquals("", config.payorId)
        assertFalse(config.skipTokenizeValidation)
        assertEquals("", config.accountCode)
        assertEquals("", config.reference)
        assertEquals("", config.paymentParameters)
        assertEquals("", config.invoiceId)
        assertFalse(config.sendReceipt)
        assertEquals("Payment Confirmation", config.receiptDescription)
        assertEquals(0, config.serviceFee)
        assertTrue(config.outlined)
        assertTrue(config.isTestMode)
        
        // Google Pay defaults
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
    }
    
    @Test
    fun `custom settings are correctly applied`() {
        // Create Address for PayorInfo
        val address = Address(
            line1 = "123 Main St",
            city = "Columbus",
            region = "OH",
            postal_code = "43215"
        )
        
        // Create PayorInfo object
        val payorInfo = PayorInfo(
            first_name = "John",
            last_name = "Doe",
            email = "john.doe@example.com",
            phone = "1234567890",
            address = address
        )
        
        // Create metadata
        val metadata = HashMap<Any, Any>()
        metadata["customerId"] = "cust-123"
        metadata["source"] = "android-app"
        
        // Create configuration with custom settings
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(testApiKey)
            .setAmount(2500)
            .setPaymentMethodType(PaymentMethodType.ACH)
            .setPaymentMethodAction(PaymentMethodAction.TOKEN)
            .setRequireAccountName(true)
            .setRequireBillingAddress(true)
            .setFeeMode(FeeMode.MERCHANT_FEE)
            .setMetadata(metadata)
            .setPayorInfo(payorInfo)
            .setPayorId("payor-123")
            .setSkipTokenizeValidation(true)
            .setAccountCode("ACC123")
            .setReference("REF456")
            .setPaymentParameters("additional=params")
            .setInvoiceId("INV789")
            .setSendReceipt(true)
            .setReceiptDescription("Custom Receipt")
            .setServiceFee(300) // $3.00 service fee
            .setOutlined(false)
            .setTestMode(true)
            .build()
        
        // Verify custom settings
        assertEquals(testApiKey, config.apiKey)
        assertEquals(2500, config.amount)
        assertEquals(PaymentMethodType.ACH, config.paymentMethodType)
        assertEquals(PaymentMethodAction.TOKEN, config.paymentMethodAction)
        assertEquals(FeeMode.MERCHANT_FEE, config.feeMode)
        assertTrue(config.requireAccountName)
        assertTrue(config.requireBillingAddress)
        assertEquals(metadata, config.metadata)
        assertEquals("cust-123", config.metadata["customerId"])
        assertEquals("android-app", config.metadata["source"])
        assertEquals(payorInfo, config.payorInfo)
        assertEquals("John", config.payorInfo?.first_name)
        assertEquals("Doe", config.payorInfo?.last_name)
        assertEquals("payor-123", config.payorId)
        assertTrue(config.skipTokenizeValidation)
        assertEquals("ACC123", config.accountCode)
        assertEquals("REF456", config.reference)
        assertEquals("additional=params", config.paymentParameters)
        assertEquals("INV789", config.invoiceId)
        assertTrue(config.sendReceipt)
        assertEquals("Custom Receipt", config.receiptDescription)
        assertEquals(300, config.serviceFee)
        assertFalse(config.outlined)
        assertTrue(config.isTestMode)
    }
    
    @Test
    fun `custom Google Pay settings are correctly applied`() {
        // Create configuration with custom Google Pay settings
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(testApiKey)
            .setAmount(1000)
            .enableGooglePay("Custom Merchant")
            .setGooglePayAllowPrepaidCards(false)
            .setGooglePayAllowCreditCards(false)
            .setGooglePayBillingAddressRequired(true)
            .setGooglePayBillingAddressFormat(GooglePayBillingAddressFormat.FULL)
            .setGooglePayShippingAddressRequired(true)
            .setGooglePayPhoneNumberRequired(true)
            .setGooglePayButtonType(GooglePayButtonType.CHECKOUT)
            .setGooglePayButtonColor(GooglePayButtonColor.WHITE)
            .setGooglePayEnvironment(GooglePayEnvironment.PRODUCTION)
            .setGooglePayAllowedCardNetworks(listOf("VISA", "MASTERCARD"))
            .setGooglePaySupportedMethods(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))
            .build()
        
        // Verify custom Google Pay settings
        assertTrue(config.googlePayEnabled)
        assertEquals("Custom Merchant", config.googlePayMerchantName)
        assertFalse(config.googlePayAllowPrepaidCards)
        assertFalse(config.googlePayAllowCreditCards)
        assertTrue(config.googlePayBillingAddressRequired)
        assertEquals(GooglePayBillingAddressFormat.FULL, config.googlePayBillingAddressFormat)
        assertTrue(config.googlePayShippingAddressRequired)
        assertTrue(config.googlePayPhoneNumberRequired)
        assertEquals(GooglePayButtonType.CHECKOUT, config.googlePayButtonType)
        assertEquals(GooglePayButtonColor.WHITE, config.googlePayButtonColor)
        assertEquals(GooglePayEnvironment.PRODUCTION, config.googlePayEnvironment)
        assertEquals(listOf("VISA", "MASTERCARD"), config.googlePayAllowedCardNetworks)
        assertEquals(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"), config.googlePaySupportedMethods)
    }
    
    @Test
    fun `testMode is determined automatically from apiKey if not set explicitly`() {
        // Create configuration with test API key without explicitly setting testMode
        val config1 = PayTheoryConfiguration.Builder()
            .setApiKey(testApiKey)
            .setAmount(1000)
            .build()
        
        // Create configuration with production API key without explicitly setting testMode
        val config2 = PayTheoryConfiguration.Builder()
            .setApiKey("partner-paytheory-apikey")
            .setAmount(1000)
            .build()
        
        // Verify testMode determined from API key
        assertTrue(config1.isTestMode)  // Should be true for test API key
        assertFalse(config2.isTestMode) // Should be false for production API key
        
        // Create configuration with test API key but explicitly set testMode to false
        val config3 = PayTheoryConfiguration.Builder()
            .setApiKey(testApiKey)
            .setAmount(1000)
            .setTestMode(false)
            .build()
        
        // Verify explicit testMode overrides API key determination
        assertFalse(config3.isTestMode)
    }
} 