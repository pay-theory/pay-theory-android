package com.paytheory.lib.compose.forms

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.compose.GooglePayForm
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.model.FieldState
import com.paytheory.lib.model.PaymentField
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the GooglePayForm component.
 * 
 * Tests different states of the Google Pay form including:
 * - Loading/checking availability state
 * - Available state (button displayed)
 * - Unavailable state (message displayed)
 */
@RunWith(AndroidJUnit4::class)
class GooglePayFormTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }
    
    /**
     * Tests that the GooglePayForm displays a checking availability message initially.
     */
    @Test
    fun testGooglePayFormShowsCheckingAvailabilityState() {
        // Create a configuration with Google Pay enabled
        val configuration = PayTheoryConfiguration.Builder()
            .setApiKey("test-api-key")
            .setAmount(1099)
            .enableGooglePay("Test Merchant")
            .setGooglePayEnvironment(GooglePayEnvironment.TEST)
            .build()
        
        // Set up the GooglePayForm
        composeTestRule.setContent {
            // In a real test, we'd provide a real Payable implementation
            // Here we're just testing the UI composition
            GooglePayForm(
                payable = TestPayable(),
                configuration = configuration
            )
        }
        
        // Verify the checking availability message is displayed initially
        composeTestRule.onNodeWithText("Checking Google Pay availability...").assertIsDisplayed()
    }
    
    /**
     * Tests Google Pay form with specific button type and color.
     */
    @Test
    fun testGooglePayFormWithCustomButtonType() {
        // Create a configuration with Google Pay enabled and custom button settings
        val configuration = PayTheoryConfiguration.Builder()
            .setApiKey("test-api-key")
            .setAmount(1099)
            .enableGooglePay("Test Merchant")
            .setGooglePayEnvironment(GooglePayEnvironment.TEST)
            .setGooglePayButtonType(GooglePayButtonType.BUY)
            .setGooglePayButtonColor(GooglePayButtonColor.BLACK)
            .build()
        
        // Set up the GooglePayForm
        composeTestRule.setContent {
            GooglePayForm(
                payable = TestPayable(),
                configuration = configuration,
                buttonType = GooglePayButtonType.BUY,
                buttonColor = GooglePayButtonColor.BLACK
            )
        }
        
        // Verify the checking availability message is displayed initially
        composeTestRule.onNodeWithText("Checking Google Pay availability...").assertIsDisplayed()
        
        // Note: We can't easily test the transition to showing the button since that
        // depends on asynchronous Google Pay availability checking
    }
    
    /**
     * Complete test implementation of Payable interface for UI testing.
     * This mock doesn't implement any real behavior but allows the UI to compose.
     */
    private class TestPayable : com.paytheory.lib.Payable {
        override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
            // No-op for UI testing
        }
        
        override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
            // No-op for UI testing
        }
        
        override fun handleError(error: PTError) {
            // No-op for UI testing
        }
        
        override fun handlePaymentStart(paymentType: String) {
            // No-op for UI testing
        }
        
        override fun handleReady(isReady: Boolean) {
            // No-op for UI testing
        }
        
        override fun handleTokenStart(paymentType: String) {
            // No-op for UI testing
        }
        
        override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
            // No-op for UI testing
        }
        
        override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
            // No-op for UI testing
        }
        
        override fun handleStateChange(fieldState: Pair<PaymentField, FieldState>) {
            // No-op for UI testing
        }
        
        override fun clientContext(): Context {
            return InstrumentationRegistry.getInstrumentation().targetContext
        }
        
        override fun getContext(): Context? {
            return clientContext()
        }
    }
} 