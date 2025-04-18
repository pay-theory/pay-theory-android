package com.paytheory.lib.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.HashMap

@ExperimentalCoroutinesApi
class PaymentProcessViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PaymentProcessViewModel

    // Test data for reuse across tests
    private lateinit var successfulTransaction: SuccessfulTransactionResult
    private lateinit var failedTransaction: FailedTransactionResult
    private lateinit var tokenResults: PaymentMethodTokenResults
    private lateinit var barcodeResult: BarcodeResult
    private lateinit var errorResult: PTError
    private lateinit var networkErrorResult: PTError

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentProcessViewModel()
        
        // Initialize test data
        successfulTransaction = SuccessfulTransactionResult(
            state = "completed",
            amount = "1000",
            brand = "visa",
            lastFour = "1234",
            serviceFee = "0",
            currency = "USD",
            metadata = HashMap<Any, Any>(),
            receiptNumber = "REC-12345",
            createdAt = "1234567890",
            paymentMethodId = "",
            payorId = "cus_123"
        )
        
        failedTransaction = FailedTransactionResult(
            state = "failed",
            brand = "mastercard",
            lastFour = "5678",
            receiptNumber = "REC-FAIL-12345",
            paymentMethodId = "",
            payorId = "cus_456"
        )
        
        tokenResults = PaymentMethodTokenResults(
            state = "active",
            paymentMethodId = "pm_123456789",
            metadata = HashMap<Any, Any>(),
            payor_id = "cus_789",
            lastFour = "9876",
            firstSix = "123456",
            brand = "amex",
            expiration = "1225",
            paymentType = "card"
        )
        
        barcodeResult = BarcodeResult(
            barcodeId = "BARCODE123",
            barcodeUrl = "https://example.com/barcode/123",
            barcode = "123456789",
            barcodeFee = "1.50",
            merchant = "merchant123",
            mapUrl = "https://maps.example.com/nearby"
        )
        
        errorResult = PTError(ErrorCode.NotValid, "Invalid credit card number")
        networkErrorResult = PTError(ErrorCode.SocketError, "Connection timeout")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // ENHANCED FLOW TESTING
    
    @Test
    fun `test full payment flow state transitions`() = runTest {
        // Initial state
        assertEquals(PaymentResultState.Idle, viewModel.paymentState.first())
        
        // Transition to Loading
        viewModel.updateState(PaymentResultState.Loading)
        assertEquals(PaymentResultState.Loading, viewModel.paymentState.first())
        
        // Transition to Processing
        viewModel.updateState(PaymentResultState.Processing)
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
        
        // Transition to Success
        viewModel.updateState(PaymentResultState.Success(successfulTransaction))
        val successState = viewModel.paymentState.first() as PaymentResultState.Success
        assertEquals(successfulTransaction.receiptNumber, successState.result.receiptNumber)
        
        // Reset to Idle
        viewModel.updateState(PaymentResultState.Idle)
        assertEquals(PaymentResultState.Idle, viewModel.paymentState.first())
    }
    
    @Test
    fun `test error handling flow`() = runTest {
        // Initial state is Idle
        assertEquals(PaymentResultState.Idle, viewModel.paymentState.first())
        
        // Transition to Processing
        viewModel.updateState(PaymentResultState.Processing)
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
        
        // Transition to Error
        viewModel.updateState(PaymentResultState.Error(errorResult))
        val errorState = viewModel.paymentState.first() as PaymentResultState.Error
        assertEquals(ErrorCode.NotValid, errorState.error.code)
        assertEquals("Invalid credit card number", errorState.error.error)
    }
    
    @Test
    fun `test multiple state transitions in sequence`() = runTest {
        // Initial state is Idle
        assertEquals(PaymentResultState.Idle, viewModel.paymentState.first())
        
        // Test a complete payment flow sequence with multiple transitions
        
        // 1. Start with Loading
        viewModel.updateState(PaymentResultState.Loading)
        assertEquals(PaymentResultState.Loading, viewModel.paymentState.first())
        
        // 2. Move to Processing
        viewModel.updateState(PaymentResultState.Processing)
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
        
        // 3. First attempt fails with network error
        viewModel.updateState(PaymentResultState.Error(networkErrorResult))
        val firstError = viewModel.paymentState.first() as PaymentResultState.Error
        assertEquals(ErrorCode.SocketError, firstError.error.code)
        
        // 4. Retry - back to Processing
        viewModel.updateState(PaymentResultState.Processing)
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
        
        // 5. Second attempt fails differently
        viewModel.updateState(PaymentResultState.Failure(failedTransaction))
        val failureState = viewModel.paymentState.first() as PaymentResultState.Failure
        assertEquals(failedTransaction.receiptNumber, failureState.result.receiptNumber)
        
        // 6. Retry again - back to Processing
        viewModel.updateState(PaymentResultState.Processing)
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
        
        // 7. Finally succeeds
        viewModel.updateState(PaymentResultState.Success(successfulTransaction))
        val successState = viewModel.paymentState.first() as PaymentResultState.Success
        assertEquals(successfulTransaction.receiptNumber, successState.result.receiptNumber)
        
        // 8. Reset to Idle
        viewModel.updateState(PaymentResultState.Idle)
        assertEquals(PaymentResultState.Idle, viewModel.paymentState.first())
    }

    // ORIGINAL TESTS

    @Test
    fun `initial state should be Idle`() = runTest {
        assertEquals(PaymentResultState.Idle, viewModel.paymentState.first())
    }

    @Test
    fun `updateState should update to Loading state`() = runTest {
        // When
        viewModel.updateState(PaymentResultState.Loading)
        
        // Then
        assertEquals(PaymentResultState.Loading, viewModel.paymentState.first())
    }

    @Test
    fun `updateState should update to Processing state`() = runTest {
        // When
        viewModel.updateState(PaymentResultState.Processing)
        
        // Then
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
    }

    @Test
    fun `updateState should update to Success state with transaction data`() = runTest {
        // When
        viewModel.updateState(PaymentResultState.Success(successfulTransaction))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.Success)
        
        val successState = currentState as PaymentResultState.Success
        assertEquals(successfulTransaction.receiptNumber, successState.result.receiptNumber)
        assertEquals(successfulTransaction.lastFour, successState.result.lastFour)
        assertEquals(successfulTransaction.amount, successState.result.amount)
    }

    @Test
    fun `updateState should update to Failure state with error data`() = runTest {
        // When
        viewModel.updateState(PaymentResultState.Failure(failedTransaction))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.Failure)
        
        val failureState = currentState as PaymentResultState.Failure
        assertEquals(failedTransaction.receiptNumber, failureState.result.receiptNumber)
        assertEquals(failedTransaction.state, failureState.result.state)
    }

    @Test
    fun `updateState should update to Error state with PTError`() = runTest {
        // When
        viewModel.updateState(PaymentResultState.Error(errorResult))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.Error)
        
        val errorState = currentState as PaymentResultState.Error
        assertEquals(errorResult.code, errorState.error.code)
        assertEquals(errorResult.error, errorState.error.error)
    }

    @Test
    fun `updateState should update to TokenSuccess state`() = runTest {
        // When
        viewModel.updateState(PaymentResultState.TokenSuccess(tokenResults))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.TokenSuccess)
        
        val tokenState = currentState as PaymentResultState.TokenSuccess
        assertEquals(tokenResults.paymentMethodId, tokenState.token.paymentMethodId)
        assertEquals(tokenResults.lastFour, tokenState.token.lastFour)
        assertEquals(tokenResults.brand, tokenState.token.brand)
        assertEquals(tokenResults.paymentType, tokenState.token.paymentType)
    }

    @Test
    fun `updateState should update to BarcodeSuccess state`() = runTest {
        // When
        viewModel.updateState(PaymentResultState.BarcodeSuccess(barcodeResult))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.BarcodeSuccess)
        
        val barcodeState = currentState as PaymentResultState.BarcodeSuccess
        assertEquals(barcodeResult.barcodeUrl, barcodeState.barcode.barcodeUrl)
        assertEquals(barcodeResult.barcode, barcodeState.barcode.barcode)
        assertEquals(barcodeResult.barcodeId, barcodeState.barcode.barcodeId)
    }
    
    // EDGE CASE TESTS
    
    @Test
    fun `test switching back and forth between same states`() = runTest {
        // Initial state is Idle
        assertEquals(PaymentResultState.Idle, viewModel.paymentState.first())
        
        // Update to Loading
        viewModel.updateState(PaymentResultState.Loading)
        assertEquals(PaymentResultState.Loading, viewModel.paymentState.first())
        
        // Update to Processing
        viewModel.updateState(PaymentResultState.Processing)
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
        
        // Back to Loading
        viewModel.updateState(PaymentResultState.Loading)
        assertEquals(PaymentResultState.Loading, viewModel.paymentState.first())
        
        // Back to Processing
        viewModel.updateState(PaymentResultState.Processing)
        assertEquals(PaymentResultState.Processing, viewModel.paymentState.first())
    }
    
    @Test
    fun `test all possible state transitions`() = runTest {
        // Define all possible states
        val allStates = listOf(
            PaymentResultState.Idle,
            PaymentResultState.Loading,
            PaymentResultState.Processing,
            PaymentResultState.Success(successfulTransaction),
            PaymentResultState.Failure(failedTransaction),
            PaymentResultState.Error(errorResult),
            PaymentResultState.TokenSuccess(tokenResults),
            PaymentResultState.BarcodeSuccess(barcodeResult)
        )
        
        // Test transitions from each state to every other state
        for (fromState in allStates) {
            for (toState in allStates) {
                // Reset to fromState
                viewModel.updateState(fromState)
                assertEquals(fromState, viewModel.paymentState.first())
                
                // Transition to toState
                viewModel.updateState(toState)
                assertEquals(toState, viewModel.paymentState.first())
            }
        }
    }
    
    @Test
    fun `test state equality with matching data`() = runTest {
        // Create two identical success states with the same transaction data
        val state1 = PaymentResultState.Success(successfulTransaction)
        val state2 = PaymentResultState.Success(
            SuccessfulTransactionResult(
                state = "completed",
                amount = "1000",
                brand = "visa",
                lastFour = "1234",
                serviceFee = "0",
                currency = "USD",
                metadata = HashMap<Any, Any>(),
                receiptNumber = "REC-12345",
                createdAt = "1234567890",
                paymentMethodId = "",
                payorId = "cus_123"
            )
        )
        
        // Update to first state
        viewModel.updateState(state1)
        assertEquals(state1, viewModel.paymentState.first())
        
        // Update to identical state - should still update flow values
        viewModel.updateState(state2)
        assertEquals(state2, viewModel.paymentState.first())
    }
} 