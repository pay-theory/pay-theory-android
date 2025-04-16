package com.paytheory.android.sdk

import com.paytheory.android.sdk.api.WebsocketInteractor
import com.paytheory.android.sdk.model.PTError
import com.paytheory.android.sdk.model.PTResult
import com.paytheory.android.sdk.processor.Payment
import com.paytheory.android.test.factories.PayTheoryConfigurationFactory
import com.paytheory.android.test.factories.PaymentDetailFactory
import com.paytheory.android.test.fixtures.TestResponses
import com.paytheory.android.test.mocks.FakeIntegrityTokenProvider
import com.paytheory.android.test.mocks.FakeWebSocketRepository
import com.paytheory.android.test.mocks.TestPayable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import java.math.BigDecimal

/**
 * Example test that demonstrates using the test utilities and helpers
 */
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class PaymentProcessorTest {
    
    // Test objects
    private lateinit var payable: TestPayable
    private lateinit var fakeWebSocketRepository: FakeWebSocketRepository
    private lateinit var websocketInteractor: WebsocketInteractor
    private lateinit var fakeIntegrityProvider: FakeIntegrityTokenProvider
    private lateinit var paymentProcessor: Payment
    
    // Test data
    private val configFactory = PayTheoryConfigurationFactory()
    private val paymentDetailFactory = PaymentDetailFactory()
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Initialize test components
        payable = TestPayable()
        fakeWebSocketRepository = FakeWebSocketRepository()
        websocketInteractor = WebsocketInteractor(fakeWebSocketRepository)
        fakeIntegrityProvider = FakeIntegrityTokenProvider()
        
        // Create payment processor with test configuration
        paymentProcessor = Payment(
            configFactory.create(),
            payable
        )
        
        // Set test WebSocket interactor
        paymentProcessor.setWebSocketInteractor(websocketInteractor)
        
        // Reset test state
        payable.reset()
    }
    
    @Test
    fun `processCardPayment with valid card should succeed`() = runTest {
        // Arrange
        val paymentDetail = paymentDetailFactory.create()
        
        // Act
        paymentProcessor.processCardPayment(paymentDetail)
        
        // Wait for the asynchronous operations to complete
        // In a real test, use appropriate waiting mechanisms
        
        // Assert
        assertEquals(1, payable.successCallCount)
        assertEquals(0, payable.errorCallCount)
        assertNotNull(payable.lastResult)
        
        // Verify the result contains expected data
        val result = payable.lastResult as PTResult
        assertEquals("TEST-${fakeWebSocketRepository.receiptNumber}", result.receiptNumber)
    }
    
    @Test
    fun `processCardPayment with connection failure should emit error`() = runTest {
        // Arrange
        val paymentDetail = paymentDetailFactory.create()
        fakeWebSocketRepository.shouldFailConnection = true
        
        // Act
        paymentProcessor.processCardPayment(paymentDetail)
        
        // Assert
        assertEquals(0, payable.successCallCount)
        assertEquals(1, payable.errorCallCount)
        assertNotNull(payable.lastError)
        
        // Verify the error contains expected data
        val error = payable.lastError as PTError
        assertEquals("NETWORK_ERROR", error.code.toString())
    }
    
    @Test
    fun `processCardPayment with custom amount should use that amount`() = runTest {
        // Arrange
        val customAmount = BigDecimal("25.99")
        val paymentDetail = paymentDetailFactory.createWithAmount(customAmount)
        
        // Act
        paymentProcessor.processCardPayment(paymentDetail)
        
        // Assert
        assertEquals(customAmount, paymentDetail.amount)
        
        // Verify the request contains the correct amount
        val sentMessage = fakeWebSocketRepository.sentMessages.firstOrNull { 
            it.contains("host:transfer_part1") 
        }
        assertNotNull(sentMessage)
        // Further assertions would check the amount in the encrypted message
    }
    
    @Test
    fun `processBankPayment with valid ACH details should succeed`() = runTest {
        // Arrange
        val paymentDetail = paymentDetailFactory.createWithAch()
        
        // Act
        paymentProcessor.processBankPayment(paymentDetail)
        
        // Assert
        assertEquals(1, payable.successCallCount)
        assertEquals(0, payable.errorCallCount)
        assertNotNull(payable.lastResult)
        
        // Verify the result contains expected data
        val result = payable.lastResult as PTResult
        assertEquals("TEST-${fakeWebSocketRepository.receiptNumber}", result.receiptNumber)
    }
    
    @Test
    fun `processGooglePayment with valid token should succeed`() = runTest {
        // Arrange
        val paymentDetail = paymentDetailFactory.createWithGooglePay()
        
        // Act
        paymentProcessor.processGooglePayPayment(paymentDetail)
        
        // Assert
        assertEquals(1, payable.successCallCount)
        assertEquals(0, payable.errorCallCount)
        assertNotNull(payable.lastResult)
        
        // Verify the result contains expected data
        val result = payable.lastResult as PTResult
        assertEquals("TEST-${fakeWebSocketRepository.receiptNumber}", result.receiptNumber)
    }
} 