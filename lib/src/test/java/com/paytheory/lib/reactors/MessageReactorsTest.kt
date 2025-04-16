package com.paytheory.lib.reactors

import com.google.gson.Gson
import com.paytheory.lib.Payment
import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.PaymentMethodToken
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.data.payable.EncryptedMessage
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.data.payable.TransactionResult
import com.paytheory.lib.data.responses.BarcodeMessage
import com.paytheory.lib.googlepay.TestReflectionUtils
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class MessageReactorsTest {

    // Mock dependencies
    private lateinit var mockViewModel: PaymentViewModel
    private lateinit var mockPayment: Payment
    private lateinit var mockPaymentMethodToken: PaymentMethodToken
    private lateinit var mockProcessor: PaymentMethodProcessor
    
    // System under test
    private lateinit var messageReactors: MessageReactors
    
    @Before
    fun setup() {
        // Initialize mocks
        mockViewModel = mockk(relaxed = true)
        mockPayment = mockk(relaxed = true)
        mockPaymentMethodToken = mockk(relaxed = true)
        mockProcessor = mockk(relaxed = true)
        
        // Initialize the system under test
        messageReactors = MessageReactors(mockViewModel)
    }
    
    // Helper methods for creating test data
    private fun createHostTokenMessage(): String {
        val bodyJson = """
            {
                "publicKey": "test-public-key",
                "sessionKey": "test-session-key",
                "hostToken": "test-host-token"
            }
        """.trimIndent()
        
        return """
            {
                "action": "host:hostToken",
                "body": $bodyJson
            }
        """.trimIndent()
    }
    
    // CREDENTIAL MANAGEMENT TESTS
    
    @Test
    fun `onHostToken_storesCredentials`() {
        // Arrange
        val hostTokenJson = createHostTokenMessage()
        
        // Act
        val result = messageReactors.onHostToken(hostTokenJson, mockPayment)
        
        // Assert
        assertEquals("test-public-key", messageReactors.socketPublicKey)
        assertEquals("test-session-key", messageReactors.sessionKey)
        assertEquals("test-host-token", MessageReactors.globalHostToken)
        
        // Verify the credentials were set on the payment object
        verify {
            mockPayment.setConnectionCredentials(
                "test-public-key",
                "test-session-key",
                "test-host-token"
            )
        }
    }
    
    @Test
    fun `onTokenizeHostToken_storesCredentials`() {
        // Arrange
        val hostTokenJson = createHostTokenMessage()
        
        // Act
        val result = messageReactors.onTokenizeHostToken(hostTokenJson, mockPaymentMethodToken)
        
        // Assert
        assertEquals("test-public-key", messageReactors.socketPublicKey)
        assertEquals("test-session-key", messageReactors.sessionKey)
        assertEquals("test-host-token", MessageReactors.globalHostToken)
        
        // Verify the credentials were set on the payment method token object
        verify {
            mockPaymentMethodToken.setConnectionCredentials(
                "test-public-key",
                "test-session-key",
                "test-host-token"
            )
        }
    }
    
    @Test
    fun `establishConnection_storesCredentials`() {
        // Arrange
        val ptTokenResponse = mockk<PTTokenResponse>()
        val attestationResult = "test-attestation"
        
        every { ptTokenResponse.ptToken } returns "test-pt-token"
        
        // Act
        messageReactors.establishConnection(ptTokenResponse, attestationResult, mockProcessor)
        
        // Assert
        // The method uses the ptToken for all credentials
        assertEquals("test-pt-token", messageReactors.socketPublicKey)
        assertEquals("test-pt-token", messageReactors.sessionKey)
        assertEquals("test-pt-token", MessageReactors.globalHostToken)
        
        // Verify credentials were set on the processor
        verify {
            mockProcessor.setConnectionCredentials(
                "test-pt-token",
                "test-pt-token",
                "test-pt-token"
            )
        }
    }
    
    // ERROR HANDLING TESTS
    
    @Test
    fun `onError_callsDisconnect`() {
        // Arrange
        val errorMessage = "Test error message"
        
        // Configure mockProcessor to return mockViewModel when viewModel is accessed
        every { mockProcessor.viewModel } returns mockViewModel
        
        // Act
        messageReactors.onError(errorMessage, mockProcessor)
        
        // Assert - verify the processor's view model disconnect was called
        verify { mockViewModel.disconnect() }
    }
    
    @Test
    fun `onError_whenPaymentNull_doesNotThrow`() {
        // Arrange
        val errorMessage = "Error with null payment"
        
        // Act - this should not throw
        messageReactors.onError(errorMessage, null)
        
        // No assertions needed - we're just verifying it doesn't throw
    }
    
    @Test
    fun `onTokenError_whenPaymentMethodTokenNull_doesNotThrow`() {
        // Arrange
        val errorMessage = "Error with null token"
        
        // Act - this should not throw
        messageReactors.onTokenError(errorMessage, null)
        
        // No assertions needed - we're just verifying it doesn't throw
    }
    
    @Test
    fun `completeTransaction_handlesSuccessfulTransaction`() {
        // Skip testing decryptAndParse directly
        // Instead, test the success path by setting up test mode
        
        // Mock payment for the test
        val mockPayment = mockk<Payment>(relaxed = true) {
            every { configuration.isTestMode } returns true
            every { configuration.feeMode } returns FeeMode.MERCHANT_FEE
            every { viewModel } returns mockViewModel
            every { resetSocket() } just runs
        }
        
        // When
        messageReactors.completeTransaction("{test-message}", mockViewModel, mockPayment)
        
        // Then
        verify { mockPayment.payable.handleSuccess(any()) }
        verify { mockPayment.resetSocket() }
    }
    
    @Test
    fun `onWalletTransaction_handlesSuccessfulTransaction`() {
        // Skip testing decryptAndParse directly
        // Instead, test the success path by setting up test mode
        
        // Mock processor for the test
        val mockProcessor = mockk<PaymentMethodProcessor>(relaxed = true) {
            every { configuration.isTestMode } returns true
            every { configuration.feeMode } returns FeeMode.MERCHANT_FEE
            every { viewModel } returns mockViewModel
            every { resetSocket() } just runs
        }
        
        // When
        messageReactors.onWalletTransaction("{test-message}", mockViewModel, mockProcessor)
        
        // Then
        verify { mockProcessor.payable.handleSuccess(any()) }
        verify { mockProcessor.resetSocket() }
    }
    
    @Test
    fun `onBarcode_handlesValidBarcode`() {
        // Skip testing decryptAndParse directly
        // Instead, test the success path by setting up test mode
        
        // Mock payment for the test
        val mockPayment = mockk<Payment>(relaxed = true) {
            every { configuration.isTestMode } returns true
            every { resetSocket() } just runs
        }
        
        // When
        messageReactors.onBarcode("{test-message}", mockViewModel, mockPayment)
        
        // Then
        verify { mockPayment.payable.handleBarcodeSuccess(any()) }
        verify { mockPayment.resetSocket() }
    }
    
    @Test
    fun `onCompleteToken_handlesSuccessfulTokenization`() {
        // Skip testing decryptAndParse directly
        // Instead, test the success path by setting up test mode
        
        // Mock payment method token for the test
        val mockToken = mockk<PaymentMethodToken>(relaxed = true) {
            every { configuration.isTestMode } returns true
            every { viewModel } returns mockViewModel
            every { resetSocket() } just runs
        }
        
        // When
        messageReactors.onCompleteToken("{test-message}", mockToken)
        
        // Then
        verify { mockToken.payable.handleTokenizeSuccess(any()) }
        verify { mockToken.resetSocket() }
    }
    
    @Test
    fun `completeTransaction_inTestMode_handlesSuccessfulTransaction`() {
        // Arrange
        val mockPaymentInTestMode = mockk<Payment>(relaxed = true)
        
        // Configure the payment to use test mode
        every { mockPaymentInTestMode.configuration.isTestMode } returns true
        every { mockPaymentInTestMode.configuration.feeMode } returns FeeMode.MERCHANT_FEE
        every { mockPaymentInTestMode.viewModel } returns mockViewModel
        every { mockPaymentInTestMode.resetSocket() } just runs
        
        // Act
        // The content of the message doesn't matter in test mode since our mock data will be used
        messageReactors.completeTransaction("{test-message}", mockViewModel, mockPaymentInTestMode)
        
        // Assert/Verify
        // In test mode it should still successfully call handleSuccess with the mock data
        verify { mockPaymentInTestMode.payable.handleSuccess(any()) }
        verify { mockPaymentInTestMode.resetSocket() }
    }
} 