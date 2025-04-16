package com.paytheory.lib

import android.content.Context
import android.content.res.Resources
import com.paytheory.lib.api.ChallengeOptions
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.googlepay.TestReflectionUtils
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.reactors.ConnectionReactors
import com.paytheory.lib.reactors.MessageReactors
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Field
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test class for PaymentMethodToken focusing on testable aspects
 * that don't require encryption or native libraries
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PaymentMethodTokenTest {

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
    
    private lateinit var paymentMethodToken: PaymentMethodToken
    private val TEST_API_KEY = "test-paytheory-apikey"
    private val PACKAGE_NAME = "com.test.app"
    
    // Reflection utility methods
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
        every { mockPayable.handleTokenStart(any()) } returns Unit
        every { mockPayable.handleTokenizeSuccess(any()) } returns Unit
        
        // Mock configuration - CRITICAL TO SET isTestMode to true
        every { mockConfig.apiKey } returns TEST_API_KEY
        every { mockConfig.isTestMode } returns true
        every { mockConfig.apiBasePath } returns "https://api.paytheory.com/"
        
        // Mock ViewModel
        every { mockViewModel.connected } returns false
        every { mockViewModel.disconnect() } returns Unit
        every { mockViewModel.sendSocketMessage(any()) } returns Unit
        
        // Create PaymentMethodToken instance
        paymentMethodToken = PaymentMethodToken(
            PACKAGE_NAME,
            mockPayable,
            hashMapOf(),
            mockConfig,
            mockViewModel
        )
    }
    
    @Test
    fun `packageName should be set during initialization`() {
        // Then
        assertEquals(PACKAGE_NAME, paymentMethodToken.packageName)
    }
    
    @Test
    fun `disconnect should call disconnect on viewModel`() {
        // When
        paymentMethodToken.disconnect()
        
        // Then
        verify { mockViewModel.disconnect() }
    }
    
    @Test
    fun `receiveMessage should handle connection messages correctly`() {
        // Create a mock ConnectionReactors instance
        val mockConnectionReactors = mockk<ConnectionReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(paymentMethodToken, "connectionReactors", mockConnectionReactors)
        
        // When - connected message
        paymentMethodToken.receiveMessage(PaymentMethodProcessor.CONNECTED)
        
        // Then
        verify { mockConnectionReactors.onConnected() }
        
        // When - disconnected message
        paymentMethodToken.receiveMessage(PaymentMethodProcessor.DISCONNECTED)
        
        // Then
        verify { mockConnectionReactors.onDisconnected() }
    }
    
    @Test
    fun `receiveMessage should handle host token message`() {
        // Create a mock MessageReactors instance
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(paymentMethodToken, "messageReactors", mockMessageReactors)
        
        // When
        val hostTokenMessage = """{"message": "host_token"}"""
        paymentMethodToken.receiveMessage(hostTokenMessage)
        
        // Then
        verify { mockMessageReactors.onTokenizeHostToken(hostTokenMessage, paymentMethodToken) }
    }
    
    @Test
    fun `receiveMessage should handle tokenize result message`() {
        // Create a mock MessageReactors instance
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(paymentMethodToken, "messageReactors", mockMessageReactors)
        
        // When
        val tokenizeMessage = """{"message": "tokenize_complete"}"""
        paymentMethodToken.receiveMessage(tokenizeMessage)
        
        // Then
        verify { mockMessageReactors.onCompleteToken(tokenizeMessage, paymentMethodToken) }
    }
    
    @Test
    fun `receiveMessage should handle wallet transaction result message`() {
        // Create a mock MessageReactors instance
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(paymentMethodToken, "messageReactors", mockMessageReactors)
        
        // When
        val walletMessage = """{"message": "wallet_transaction_complete"}"""
        paymentMethodToken.receiveMessage(walletMessage)
        
        // Then
        verify { mockMessageReactors.onWalletTransaction(walletMessage, mockViewModel, paymentMethodToken) }
    }
    
    @Test
    fun `receiveMessage should handle error message`() {
        // Create a mock MessageReactors instance
        val mockMessageReactors = mockk<MessageReactors>(relaxed = true)
        TestReflectionUtils.setPrivateProperty(paymentMethodToken, "messageReactors", mockMessageReactors)
        
        // When
        val errorMessage = """{"error": "some error"}"""
        paymentMethodToken.receiveMessage(errorMessage)
        
        // Then
        verify { mockMessageReactors.onTokenError(errorMessage, paymentMethodToken) }
    }
    
    @Test
    fun `getWebSocketMessageType should identify different message types`() {
        // Using TestReflectionUtils to access private method
        
        // When - tokenize result
        val tokenizeResult = TestReflectionUtils.invokePrivateMethod<String>(
            paymentMethodToken,
            "getWebSocketMessageType",
            """{"message": "tokenize_complete"}"""
        )
        
        // Then
        assertEquals(PaymentMethodProcessor.TOKENIZE_RESULT, tokenizeResult)
        
        // When - host token
        val hostTokenResult = TestReflectionUtils.invokePrivateMethod<String>(
            paymentMethodToken,
            "getWebSocketMessageType",
            """{"message": "host_token"}"""
        )
        
        // Then
        assertEquals(PaymentMethodProcessor.HOST_TOKEN_RESULT, hostTokenResult)
        
        // When - wallet transaction
        val walletResult = TestReflectionUtils.invokePrivateMethod<String>(
            paymentMethodToken,
            "getWebSocketMessageType",
            """{"message": "wallet_transaction_complete"}"""
        )
        
        // Then
        assertEquals(PaymentMethodProcessor.WALLET_TRANSACTION_RESULT, walletResult)
        
        // When - unknown
        val unknownResult = TestReflectionUtils.invokePrivateMethod<String>(
            paymentMethodToken,
            "getWebSocketMessageType",
            """{"message": "unknown_message"}"""
        )
        
        // Then
        assertEquals(PaymentMethodProcessor.UNKNOWN, unknownResult)
    }
} 