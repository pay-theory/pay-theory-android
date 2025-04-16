package com.paytheory.lib.googlepay

import android.app.Activity
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
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
    private lateinit var mockGooglePayClient: GooglePayClientInterface
    private lateinit var mockGooglePayUtil: GooglePayUtilInterface
    
    @Before
    fun setup() {
        // Create mocks for all dependencies
        mockActivity = mockk(relaxed = true)
        mockPayable = mockk(relaxed = true)
        mockViewModel = mockk(relaxed = true)
        mockConfiguration = mockk(relaxed = true)
        mockGooglePayClient = mockk(relaxed = true)
        mockGooglePayUtil = mockk(relaxed = true)
        
        // Configure mocks with minimal properties
        every { mockConfiguration.apiKey } returns "test-paytheory-apikey"
        every { mockPayable.getContext() } returns null
        
        // Reset GooglePayFactory to default implementations before each test
        GooglePayFactory.resetToDefaultImplementations()
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
    fun `createProcessor should use custom GooglePayUtil when provided`() {
        // When
        val processor = GooglePayFactory.createProcessor(
            configuration = mockConfiguration,
            activity = mockActivity,
            payable = mockPayable,
            viewModel = mockViewModel,
            googlePayUtil = mockGooglePayUtil
        )
        
        // Then
        assertNotNull(processor)
        assertTrue(processor is GooglePayProcessorInterface)
        
        // Verify the processor was created with our mock util
        // This is an indirect verification, since we can't directly access private fields
        
        // Try to use the processor to test if our mock is being used
        processor.isGooglePayAvailable()
        verify { mockGooglePayUtil.isGooglePayAvailable(any(), any(), any()) }
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
    fun `setTestImplementations should use provided implementations`() {
        // Given
        GooglePayFactory.setTestImplementations(
            client = mockGooglePayClient,
            util = mockGooglePayUtil
        )
        
        // When we create a processor, it should use our mocked util
        val processor = GooglePayFactory.createProcessor(
            configuration = mockConfiguration,
            activity = mockActivity,
            payable = mockPayable,
            viewModel = mockViewModel
        )
        
        // Then
        assertNotNull(processor)
        
        // Test if the processor uses our mocked util
        processor.isGooglePayAvailable()
        verify { mockGooglePayUtil.isGooglePayAvailable(any(), any(), any()) }
    }
    
    @Test
    fun `resetToDefaultImplementations should restore default implementations`() {
        // Given - set test implementations first
        GooglePayFactory.setTestImplementations(
            client = mockGooglePayClient,
            util = mockGooglePayUtil
        )
        
        // When - reset to defaults
        GooglePayFactory.resetToDefaultImplementations()
        
        // Then - verify processor behavior (no mocks should be used)
        val processor = GooglePayFactory.createProcessor(
            configuration = mockConfiguration,
            activity = mockActivity,
            payable = mockPayable,
            viewModel = mockViewModel
        )
        
        assertNotNull(processor)
        assertTrue(processor is GooglePayProcessorInterface)
        
        // When we call isGooglePayAvailable, our mock should NOT be called
        processor.isGooglePayAvailable()
        verify(exactly = 0) { mockGooglePayUtil.isGooglePayAvailable(any(), any(), any()) }
    }
} 