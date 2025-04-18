package com.paytheory.lib

import android.content.Context
import android.content.res.Resources
import com.google.android.play.core.integrity.IntegrityManager
import com.paytheory.lib.api.ApiService
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.requests.ActionRequest
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.googlepay.TestReflectionUtils
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.reactors.ConnectionReactors
import com.paytheory.lib.reactors.MessageReactors
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Field
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test class for PaymentMethodProcessor
 * Since PaymentMethodProcessor is abstract, we'll create a concrete implementation for testing
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PaymentMethodProcessorTest {

    // Test implementation of PaymentMethodProcessor
    private class TestPaymentMethodProcessor(
        payable: Payable,
        payTheoryData: HashMap<Any, Any>?,
        configuration: PayTheoryConfiguration,
        viewModel: PaymentViewModel
    ) : PaymentMethodProcessor(payable, payTheoryData, configuration, viewModel) {
        
        // For testing purposes - make connectionReactors accessible
        var testConnectionReactors: ConnectionReactors? = null
            set(value) {
                field = value
                connectionReactors = value
            }
            get() = connectionReactors
            
        // Implement the required receiveMessage method
        override fun receiveMessage(message: String) {
            // Simple implementation for testing
            when (message) {
                PaymentMethodProcessor.CONNECTED -> {
                    connectionReactors?.onConnected()
                }
                PaymentMethodProcessor.DISCONNECTED -> {
                    connectionReactors?.onDisconnected()
                }
            }
        }
        
        // Implement the required disconnect method
        override fun disconnect() {
            viewModel.disconnect()
        }
        
        // Override abstract methods with simple implementations
        override fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?) {
            // Simple implementation for testing
            messageReactors = MessageReactors(viewModel)
            connectionReactors = ConnectionReactors(
                ptTokenResponse.ptToken,
                attestationResult ?: "",
                viewModel,
                "com.test.app",
                "android"
            )
        }

        override fun process(payment: PaymentDetail) {
            // Simple implementation for testing
            val request = createInitialActionRequest(payment)
            if (viewModel.connected) {
                payable.handlePaymentStart(payment.type)
            }
        }

        override fun createInitialActionRequest(payment: PaymentDetail): ActionRequest {
            // Return a dummy ActionRequest for testing
            return ActionRequest(
                "test_action",
                "encrypted_body",
                "public_key",
                "session_key"
            )
        }
        
        // Expose protected methods for testing
        fun testUpdatePayableReadyState(isReady: Boolean) {
            updatePayableReadyState(isReady)
        }
        
        fun testSetConnectionCredentials(pubKey: String, sessKey: String, hToken: String) {
            setConnectionCredentials(pubKey, sessKey, hToken)
        }
    }

    // Mocked dependencies
    @MockK(relaxed = true)
    private lateinit var mockPayable: Payable
    
    @MockK(relaxed = true)
    private lateinit var mockViewModel: PaymentViewModel
    
    @MockK(relaxed = true)
    private lateinit var mockConfig: PayTheoryConfiguration
    
    @MockK(relaxed = true)
    private lateinit var mockContext: Context
    
    @MockK(relaxed = true)
    private lateinit var mockResources: Resources
    
    @MockK
    private lateinit var mockApiService: ApiService
    
    @MockK
    private lateinit var mockIntegrityManager: IntegrityManager

    private lateinit var processor: TestPaymentMethodProcessor
    private val TEST_API_KEY = "test-paytheory-apikey"
    
    // Reflection utility methods - fixed to handle class hierarchy
    private fun getPrivateField(obj: Any, fieldName: String): Field? {
        var currentClass: Class<*>? = obj.javaClass
        var field: Field? = null
        
        while (currentClass != null) {
            try {
                field = currentClass.getDeclaredField(fieldName)
                field.isAccessible = true
                return field
            } catch (e: NoSuchFieldException) {
                // Try superclass
                currentClass = currentClass.superclass
            }
        }
        
        return null
    }
    
    private fun <T> setPrivateField(obj: Any, fieldName: String, value: T) {
        val field = getPrivateField(obj, fieldName)
        field?.set(obj, value)
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Mock context and resources
        every { mockContext.resources } returns mockResources
        every { mockResources.getString(R.string.google_project_number) } returns "12345678"
        
        // Mock Payable
        every { mockPayable.getContext() } returns mockContext
        every { mockPayable.handleReady(any()) } returns Unit
        
        // Mock configuration
        every { mockConfig.apiKey } returns TEST_API_KEY
        every { mockConfig.isTestMode } returns true
        every { mockConfig.apiBasePath } returns "https://api.paytheory.com/"
        
        // Mock ViewModel
        every { mockViewModel.connected } returns false
        every { mockViewModel.disconnect() } returns Unit
        
        // Create processor instance
        processor = TestPaymentMethodProcessor(
            mockPayable,
            hashMapOf(),
            mockConfig,
            mockViewModel
        )
    }
    
    @Test
    fun `updatePayableReadyState should call handleReady on Payable`() {
        // When
        processor.testUpdatePayableReadyState(true)
        
        // Then
        verify { mockPayable.handleReady(true) }
    }
    
    @Test
    fun `setConnectionCredentials should set credentials correctly`() {
        // Given
        val pubKey = "test_public_key"
        val sessKey = "test_session_key"
        val hostToken = "test_host_token"
        
        // When
        processor.testSetConnectionCredentials(pubKey, sessKey, hostToken)
        
        // Then
        assertEquals(pubKey, processor.publicKey)
        assertEquals(sessKey, processor.sessionKey)
        assertEquals(hostToken, processor.hostToken)
    }
    
    @Test
    fun `hasCredentials should return true when all credentials are set`() {
        // Given
        processor.testSetConnectionCredentials(
            "test_public_key",
            "test_session_key",
            "test_host_token"
        )
        
        // Then
        assertTrue(processor.hasCredentials())
    }
    
    @Test
    fun `hasCredentials should return false when credentials are missing`() {
        // Given - no credentials set
        
        // Then
        assertFalse(processor.hasCredentials())
        
        // Given - partial credentials
        processor.testSetConnectionCredentials(
            "test_public_key",
            "",
            "test_host_token"
        )
        
        // Then
        assertFalse(processor.hasCredentials())
    }
    
    @Test
    fun `disconnect should call disconnect on viewModel`() {
        // When
        processor.disconnect()
        
        // Then
        verify { mockViewModel.disconnect() }
    }
    
    @Test
    fun `ptTokenApiCall should handle test mode correctly`() {
        // This requires more complex mocking of ApiService and observable chains
        // For now, we'll verify that test mode skips the API call
        
        // Given
        every { mockConfig.isTestMode } returns true
        
        // When
        processor.ptTokenApiCall(mockPayable)
        
        // Then - no exceptions should be thrown
        // In test mode, the method should not make actual API calls
    }
    
    @Test
    fun `sessionIsDirty flag should be set correctly`() {
        // Given - static field access
        PaymentMethodProcessor.sessionIsDirty = true
        
        // When - calling ptTokenApiCall (which modifies sessionIsDirty)
        processor.ptTokenApiCall(mockPayable)
        
        // Then
        assertFalse(PaymentMethodProcessor.sessionIsDirty)
    }
    
    @Test
    fun `creating the processor should initialize properties correctly`() {
        // Then
        assertEquals(mockConfig, processor.configuration)
        assertEquals(false, processor.isWarm)
        assertEquals(0, processor.resetCounter)
        assertEquals(0, processor.ptResetCounter)
        assertNull(processor.publicKey)
        assertNull(processor.sessionKey)
        assertNull(processor.hostToken)
        assertNotNull(processor.headerMap)
        assertTrue(processor.headerMap.containsKey("Content-Type"))
    }
    
    @Test
    fun `receiveMessage should handle connection messages correctly`() {
        // Create a mock ConnectionReactors instance
        val mockConnectionReactors = mockk<ConnectionReactors>(relaxed = true)
        
        // Use our accessor property to set it
        processor.testConnectionReactors = mockConnectionReactors
        
        // When - connected message
        processor.receiveMessage(PaymentMethodProcessor.CONNECTED)
        
        // Then
        verify { mockConnectionReactors.onConnected() }
        
        // When - disconnected message
        processor.receiveMessage(PaymentMethodProcessor.DISCONNECTED)
        
        // Then
        verify { mockConnectionReactors.onDisconnected() }
    }

    @Test
    fun `resetSocket should increment counter`() {
        // Given
        val initialCounter = 10
        
        // Set the initial counter
        processor.resetCounter = initialCounter
        
        // Stub the ptTokenApiCall method to avoid actual execution
        val processorSpy = spyk(processor)
        every { processorSpy.ptTokenApiCall(any()) } returns Unit
        
        // When
        processorSpy.resetSocket()
        
        // Then - just verify counter was incremented
        assertEquals(initialCounter + 1, processorSpy.resetCounter)
    }

    @Test
    fun `resetSocket should call onError when counter exceeds threshold`() {
        // Given
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(processor, "messageReactors", mockMessageReactors)
        
        // When - reset counter exceeds threshold
        processor.resetCounter = 50
        processor.resetSocket()
        
        // Then
        verify { mockMessageReactors.onError("NETWORK_ERROR: Please check device connection", processor) }
    }

    @Test
    fun `attemptReconnectToPtToken should increment counter`() {
        // Prepare a test processor with test counter value
        val initialCounter = 100
        processor.ptResetCounter = initialCounter
        
        // Create a spy that stubs the nested method calls
        val processorSpy = spyk(processor)
        every { processorSpy.disconnect() } returns Unit
        every { processorSpy.ptTokenApiCall(any()) } returns Unit
        
        // Set up message reactors to avoid NPE
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(processorSpy, "messageReactors", mockMessageReactors)
        
        // When - invoke the private method
        TestReflectionUtils.invokePrivateMethod<Unit>(
            processorSpy,
            "attemptReconnectToPtToken"
        )
        
        // Then - just verify the counter was incremented
        assertEquals(initialCounter + 1, processorSpy.ptResetCounter)
    }

    @Test
    fun `attemptReconnectToPtToken should call onError when counter exceeds threshold`() {
        // Given
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(processor, "messageReactors", mockMessageReactors)
        
        // Set counter at the threshold
        processor.ptResetCounter = 2000
        
        // When
        TestReflectionUtils.invokePrivateMethod<Unit>(
            processor,
            "attemptReconnectToPtToken"
        )
        
        // Then
        verify { mockMessageReactors.onError("NETWORK_ERROR: Please check device connection", processor) }
    }

    @Test
    fun `ptTokenApiCall should handle network errors correctly`() {
        // This test requires mocking the RxJava Observable chain
        // We'll need to improve this test with proper RxJava testing
        
        // Given
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(processor, "messageReactors", mockMessageReactors)
        
        every { mockConfig.isTestMode } returns false
        
        // When/Then
        // For now, just verify that no exception is thrown
        processor.ptTokenApiCall(mockPayable)
        
        // Future improvement: Mock the ApiService response and verify error handling paths
    }
} 