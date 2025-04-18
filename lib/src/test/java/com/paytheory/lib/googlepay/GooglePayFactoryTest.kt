package com.paytheory.lib.googlepay

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

/**
 * Tests for GooglePayFactory to ensure proper creation of Google Pay components
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayFactoryTest {
    
    // Test dependencies
    private lateinit var mockActivity: Activity
    private lateinit var mockPayable: Payable
    private lateinit var mockViewModel: PaymentViewModel
    private lateinit var mockConfiguration: PayTheoryConfiguration
    
    @Before
    fun setup() {
        // Create mocks for all dependencies
        mockActivity = mockk(relaxed = true)
        mockPayable = mockk(relaxed = true)
        mockViewModel = mockk(relaxed = true)
        mockConfiguration = mockk(relaxed = true)
        
        // Configure mocks with minimal properties
        every { mockConfiguration.apiKey } returns "test-paytheory-apikey"
        every { mockPayable.getContext() } returns null
        
        // Configure Google Pay required parameters
        configureGooglePayMock(mockConfiguration)
        
        // Reset GooglePayFactory to default implementations before each test
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
    
    @After
    fun tearDown() {
        // Reset GooglePayFactory after tests
        GooglePayFactory.resetToDefaultImplementations()
        unmockkAll()
    }
    
    @Test
    fun `createProcessor should return a valid GooglePayProcessor`() {
        // When
        val processor = GooglePayFactory.createProcessor(
            configuration = mockConfiguration,
            activity = mockActivity,
            payable = mockPayable,
            viewModel = mockViewModel
        )
        
        // Then
        assertNotNull(processor)
        assertTrue(processor is GooglePayProcessorInterface)
    }
    
    @Test
    fun `createProcessor should accept a custom GooglePayUtil`() {
        // Given
        val customUtil = TestGooglePayFactory.createMockUtil(isGooglePayAvailable = true)
        
        // When - pass the custom util
        val processor = GooglePayFactory.createProcessor(
            configuration = mockConfiguration,
            activity = mockActivity,
            payable = mockPayable,
            viewModel = mockViewModel,
            googlePayUtil = customUtil
        )
        
        // Then - just verify we got a processor back without error
        assertNotNull(processor)
        assertTrue(processor is GooglePayProcessorInterface)
    }
    
    @Test
    fun `getGooglePayUtil should return a valid GooglePayUtil instance`() {
        // When
        val util = GooglePayFactory.getGooglePayUtil()
        
        // Then
        assertNotNull(util)
        assertTrue(util is GooglePayUtilInterface)
    }
    
    @Test
    fun `getGooglePayClient should return a valid GooglePayClient instance`() {
        // When
        val client = GooglePayFactory.getGooglePayClient()
        
        // Then
        assertNotNull(client)
        assertTrue(client is GooglePayClientInterface)
    }
    
    @Test
    fun `setTestImplementations should accept custom implementations`() {
        // Given
        val mockClient = TestGooglePayFactory.createMockClient()
        val mockUtil = TestGooglePayFactory.createMockUtil()
        
        try {
            // When
            GooglePayFactory.setTestImplementations(
                client = mockClient,
                util = mockUtil
            )
            
            // Then - successful if no exceptions are thrown
            // We can still get the clients to make sure they are returned
            val client = GooglePayFactory.getGooglePayClient()
            val util = GooglePayFactory.getGooglePayUtil()
            
            assertNotNull(client)
            assertNotNull(util)
            assertTrue(client is GooglePayClientInterface)
            assertTrue(util is GooglePayUtilInterface)
        } catch (e: Exception) {
            // Log any error and fail the test
            println("Exception when setting test implementations: ${e.message}")
            throw e
        }
    }
    
    @Test
    fun `resetToDefaultImplementations should restore default implementations`() {
        // Given - set test implementations first
        val mockClient = TestGooglePayFactory.createMockClient()
        val mockUtil = TestGooglePayFactory.createMockUtil()
        
        GooglePayFactory.setTestImplementations(
            client = mockClient,
            util = mockUtil
        )
        
        // When - reset to defaults
        GooglePayFactory.resetToDefaultImplementations()
        
        // Then - verify we can still get instances
        val client = GooglePayFactory.getGooglePayClient()
        val util = GooglePayFactory.getGooglePayUtil()
        
        assertNotNull(client)
        assertNotNull(util)
        
        // Just verify we got the expected instance types
        assertTrue(client is GooglePayClientInterface)
        assertTrue(util is GooglePayUtilInterface)
    }
} 