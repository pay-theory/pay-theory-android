package com.paytheory.lib.configuration

import com.paytheory.lib.PayTheoryConfiguration
import com.google.android.gms.wallet.WalletConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayConfigurationTest {

    // Use the correct test API key matching the constant in PayTheoryConfiguration
    private val validApiKey = "test-paytheory-apikey"

    @Test
    fun `builder should enable Google Pay with merchant name`() {
        // Given
        val merchantName = "Test Merchant"
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .build()
            
        // Then
        assertTrue(config.googlePayEnabled)
        assertEquals(merchantName, config.googlePayMerchantName)
    }
    
    @Test
    fun `builder should set Google Pay billing address options`() {
        // Given
        val merchantName = "Test Merchant"
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .setGooglePayBillingAddressRequired(true)
            .setGooglePayBillingAddressFormat(GooglePayBillingAddressFormat.FULL)
            .build()
            
        // Then
        assertTrue(config.googlePayBillingAddressRequired)
        assertEquals(GooglePayBillingAddressFormat.FULL, config.googlePayBillingAddressFormat)
    }
    
    @Test
    fun `builder should set Google Pay shipping address options`() {
        // Given
        val merchantName = "Test Merchant"
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .setGooglePayShippingAddressRequired(true)
            .setGooglePayPhoneNumberRequired(true)
            .build()
            
        // Then
        assertTrue(config.googlePayShippingAddressRequired)
        assertTrue(config.googlePayPhoneNumberRequired)
    }
    
    @Test
    fun `builder should set Google Pay card options`() {
        // Given
        val merchantName = "Test Merchant"
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .setGooglePayAllowPrepaidCards(false)
            .setGooglePayAllowCreditCards(false)
            .build()
            
        // Then
        assertFalse(config.googlePayAllowPrepaidCards)
        assertFalse(config.googlePayAllowCreditCards)
    }
    
    @Test
    fun `builder should set Google Pay button options`() {
        // Given
        val merchantName = "Test Merchant"
        val buttonType = GooglePayButtonType.CHECKOUT
        val buttonColor = GooglePayButtonColor.WHITE
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .setGooglePayButtonType(buttonType)
            .setGooglePayButtonColor(buttonColor)
            .build()
            
        // Then
        assertEquals(buttonType, config.googlePayButtonType)
        assertEquals(buttonColor, config.googlePayButtonColor)
    }
    
    @Test
    fun `builder should set Google Pay environment`() {
        // Given
        val merchantName = "Test Merchant"
        val environment = GooglePayEnvironment.PRODUCTION
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .setGooglePayEnvironment(environment)
            .build()
            
        // Then
        assertEquals(environment, config.googlePayEnvironment)
    }
    
    @Test
    fun `builder should set Google Pay allowed card networks`() {
        // Given
        val merchantName = "Test Merchant"
        val networks = listOf("VISA", "MASTERCARD")
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .setGooglePayAllowedCardNetworks(networks)
            .build()
            
        // Then
        assertEquals(networks, config.googlePayAllowedCardNetworks)
    }
    
    @Test
    fun `should use default values when only required Google Pay options are set`() {
        // Given
        val merchantName = "Test Merchant"
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .build()
            
        // Then
        assertEquals(GooglePayEnvironment.TEST, config.googlePayEnvironment)
        assertEquals(GooglePayButtonType.PAY, config.googlePayButtonType)
        assertEquals(GooglePayButtonColor.BLACK, config.googlePayButtonColor)
        assertEquals(GooglePayBillingAddressFormat.MINIMAL, config.googlePayBillingAddressFormat)
        assertFalse(config.googlePayBillingAddressRequired)
        assertFalse(config.googlePayShippingAddressRequired)
        assertFalse(config.googlePayPhoneNumberRequired)
        assertTrue(config.googlePayAllowPrepaidCards)
        assertTrue(config.googlePayAllowCreditCards)
        assertEquals(GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS, config.googlePayAllowedCardNetworks)
        assertEquals(GooglePayConstants.DEFAULT_SUPPORTED_METHODS, config.googlePaySupportedMethods)
    }
    
    @Test
    fun `Google Pay should be disabled by default`() {
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .build()
            
        // Then
        assertFalse(config.googlePayEnabled)
        assertEquals(null, config.googlePayMerchantName)
    }
    
    @Test
    fun `should correctly set all Google Pay button types`() {
        // Given
        val merchantName = "Test Merchant"
        val buttonTypes = listOf(
            GooglePayButtonType.PAY,
            GooglePayButtonType.CHECKOUT,
            GooglePayButtonType.ORDER,
            GooglePayButtonType.SUBSCRIBE,
            GooglePayButtonType.BOOK,
            GooglePayButtonType.BUY
        )
        
        // When/Then - Test each button type
        buttonTypes.forEach { buttonType ->
            val config = PayTheoryConfiguration.Builder()
                .setApiKey(validApiKey)
                .setAmount(1099)
                .enableGooglePay(merchantName)
                .setGooglePayButtonType(buttonType)
                .build()
                
            assertEquals(buttonType, config.googlePayButtonType)
        }
    }
    
    @Test
    fun `should correctly set Google Pay supported methods`() {
        // Given
        val merchantName = "Test Merchant"
        val supportedMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        
        // When
        val config = PayTheoryConfiguration.Builder()
            .setApiKey(validApiKey)
            .setAmount(1099)
            .enableGooglePay(merchantName)
            .setGooglePaySupportedMethods(supportedMethods)
            .build()
            
        // Then
        assertEquals(supportedMethods, config.googlePaySupportedMethods)
    }
} 