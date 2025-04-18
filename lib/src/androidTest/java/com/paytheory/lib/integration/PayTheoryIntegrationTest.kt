package com.paytheory.lib.integration

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.configuration.GooglePayEnvironment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Basic integration test for Pay Theory SDK
 * 
 * This test verifies that we can construct valid configuration
 * objects with various settings, which is the first step for any integration.
 */
@RunWith(AndroidJUnit4::class)
class PayTheoryIntegrationTest {

    private lateinit var context: Context
    
    @Before
    fun setup() {
        // Get the application context for testing
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }
    
    /**
     * Test that we can create a valid configuration with the test API key
     */
    @Test
    fun testConfiguration_withTestApiKey_shouldSucceed() {
        // Create a configuration with the test API key
        val config = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setAmount(1099) // $10.99
            .build()
            
        // Verify the configuration was created successfully
        assertNotNull(config)
        assertEquals("test-paytheory-apikey", config.apiKey)
        assertEquals(1099, config.amount)
        
        // Verify test mode is enabled for test API key
        assertTrue(config.isTestMode)
        
        // Log success
        android.util.Log.d("PayTheoryIntegrationTest", "Successfully created test configuration")
    }
    
    /**
     * Test that we can create a configuration with Google Pay enabled
     */
    @Test
    fun testConfiguration_withGooglePay_shouldSucceed() {
        // Create a configuration with Google Pay enabled
        val config = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setAmount(1099) // $10.99
            .enableGooglePay("Test Merchant")
            .setGooglePayEnvironment(GooglePayEnvironment.TEST)
            .setGooglePayButtonType(GooglePayButtonType.BUY)
            .setGooglePayButtonColor(GooglePayButtonColor.BLACK)
            .build()
            
        // Verify the configuration was created successfully
        assertNotNull(config)
        
        // Verify Google Pay settings
        assertTrue(config.googlePayEnabled)
        assertEquals("Test Merchant", config.googlePayMerchantName)
        assertEquals(GooglePayEnvironment.TEST, config.googlePayEnvironment)
        assertEquals(GooglePayButtonType.BUY, config.googlePayButtonType)
        assertEquals(GooglePayButtonColor.BLACK, config.googlePayButtonColor)
        
        // Log success
        android.util.Log.d("PayTheoryIntegrationTest", "Successfully created configuration with Google Pay")
    }
    
    /**
     * Test creation of configuration with various settings
     */
    @Test
    fun testConfiguration_withVariousSettings_shouldSucceed() {
        // Create a configuration with different settings
        val config = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setAmount(2500) // $25.00
            .setRequireAccountName(true)
            .setRequireBillingAddress(true)
            .setServiceFee(199) // $1.99 service fee
            .setSendReceipt(true)
            .setReceiptDescription("Test Receipt")
            .build()
            
        // Verify the configuration was created successfully
        assertNotNull(config)
        
        // Verify specific settings
        assertEquals(2500, config.amount)
        assertTrue(config.requireAccountName)
        assertTrue(config.requireBillingAddress)
        assertEquals(199, config.serviceFee)
        assertTrue(config.sendReceipt)
        assertEquals("Test Receipt", config.receiptDescription)
        
        // Log success
        android.util.Log.d("PayTheoryIntegrationTest", "Successfully created configuration with various settings")
    }
} 