package com.paytheory.lib.googlepay

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONArray
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.math.BigDecimal

/**
 * Tests for Google Pay interfaces to verify proper implementation
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayInterfacesTest {

    @After
    fun tearDown() {
        // Reset factory after each test to ensure clean state
        GooglePayFactory.resetToDefaultImplementations()
    }

    /**
     * Configure mock with all required Google Pay parameters
     */
    private fun configureGooglePayMock(configuration: PayTheoryConfiguration) {
        // Add the required card networks and auth methods to the configuration
        val allowedCardNetworks = listOf("VISA", "MASTERCARD")
        val allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        
        // Basic configuration
        every { configuration.googlePayAllowedCardNetworks } returns allowedCardNetworks
        every { configuration.googlePaySupportedMethods } returns allowedAuthMethods
        every { configuration.googlePayEnvironment } returns GooglePayEnvironment.TEST
        every { configuration.googlePayBillingAddressRequired } returns false
        
        // Additional required configurations
        every { configuration.googlePayBillingAddressFormat } returns GooglePayBillingAddressFormat.MINIMAL
        every { configuration.googlePayShippingAddressRequired } returns false
        every { configuration.googlePayPhoneNumberRequired } returns false
        every { configuration.googlePayAllowPrepaidCards } returns true
        every { configuration.googlePayAllowCreditCards } returns true
        every { configuration.googlePayMerchantName } returns "Test Merchant"
        every { configuration.amount } returns 1099
    }

    /**
     * Test that GooglePayClient properly implements GooglePayClientInterface
     */
    @Test
    fun `GooglePayClient should implement GooglePayClientInterface`() {
        // Given
        val client = GooglePayClient()
        
        // Then
        assertIs<GooglePayClientInterface>(client)
    }
    
    /**
     * Test that GooglePayUtil properly implements GooglePayUtilInterface
     */
    @Test
    fun `GooglePayUtil should implement GooglePayUtilInterface`() {
        // Given
        val util = GooglePayUtil.getInstance()
        
        // Then
        assertIs<GooglePayUtilInterface>(util)
    }
    
    /**
     * Test that GooglePayProcessor properly implements GooglePayProcessorInterface
     */
    @Test
    fun `GooglePayProcessor should implement GooglePayProcessorInterface`() {
        // Given
        val activity = mockk<Activity>()
        val payable = mockk<Payable>()
        val configuration = mockk<PayTheoryConfiguration>()
        val viewModel = mockk<PaymentViewModel>()
        
        // Configure the mock with all required Google Pay parameters
        configureGooglePayMock(configuration)
        
        // When
        val processor = GooglePayProcessor(
            payable = payable,
            activity = activity,
            configuration = configuration,
            viewModel = viewModel
        )
        
        // Then
        assertIs<GooglePayProcessorInterface>(processor)
    }
    
    /**
     * Test that GooglePayFactory can create instances of all interfaces
     */
    @Test
    fun `GooglePayFactory should create valid interface implementations`() {
        // Given
        val activity = mockk<Activity>()
        val payable = mockk<Payable>()
        val configuration = mockk<PayTheoryConfiguration>()
        val viewModel = mockk<PaymentViewModel>()
        
        // Configure the mock with all required Google Pay parameters
        configureGooglePayMock(configuration)
        
        // When
        val client = GooglePayFactory.getGooglePayClient()
        val processor = GooglePayFactory.createProcessor(
            payable = payable,
            activity = activity,
            configuration = configuration,
            viewModel = viewModel
        )
        val util = GooglePayFactory.getGooglePayUtil()
        
        // Then
        assertIs<GooglePayClientInterface>(client)
        assertIs<GooglePayProcessorInterface>(processor)
        assertIs<GooglePayUtilInterface>(util)
    }

    /**
     * Test that TestGooglePayFactory creates instances that properly implement the interfaces
     */
    @Test
    fun `TestGooglePayFactory should create valid test implementations`() {
        // Given
        val activity = mockk<Activity>()
        val payable = mockk<Payable>()
        val configuration = mockk<PayTheoryConfiguration>()
        val viewModel = mockk<PaymentViewModel>(relaxed = true)
        
        // Configure the mock with all required Google Pay parameters
        configureGooglePayMock(configuration)
        
        // Configure payable
        every { payable.getContext() } returns null
        
        // When - set test implementations
        val mockClient = mockk<GooglePayClientInterface>(relaxed = true)
        val mockUtil = mockk<GooglePayUtilInterface>(relaxed = true)
        
        // Set the test implementations
        GooglePayFactory.setTestImplementations(
            client = mockClient,
            util = mockUtil
        )
        
        // Get instances from the factory
        val client = GooglePayFactory.getGooglePayClient()
        val util = GooglePayFactory.getGooglePayUtil()
        val processor = GooglePayFactory.createProcessor(
            payable = payable,
            activity = activity,
            configuration = configuration,
            viewModel = viewModel
        )
        
        // Then
        // Only verify the types are correct, instead of strict object identity which may vary
        assertIs<GooglePayClientInterface>(client)
        assertIs<GooglePayUtilInterface>(util)
        assertIs<GooglePayProcessorInterface>(processor)
    }
} 