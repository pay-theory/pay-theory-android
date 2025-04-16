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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentProcessViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
        // Given
        val transactionResult = SuccessfulTransactionResult(
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
        
        // When
        viewModel.updateState(PaymentResultState.Success(transactionResult))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.Success)
        
        val successState = currentState as PaymentResultState.Success
        assertEquals(transactionResult.receiptNumber, successState.result.receiptNumber)
        assertEquals(transactionResult.lastFour, successState.result.lastFour)
        assertEquals(transactionResult.amount, successState.result.amount)
    }

    @Test
    fun `updateState should update to Failure state with error data`() = runTest {
        // Given
        val failureResult = FailedTransactionResult(
            state = "failed",
            brand = "mastercard",
            lastFour = "5678",
            receiptNumber = "REC-FAIL-12345",
            paymentMethodId = "",
            payorId = "cus_456"
        )
        
        // When
        viewModel.updateState(PaymentResultState.Failure(failureResult))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.Failure)
        
        val failureState = currentState as PaymentResultState.Failure
        assertEquals(failureResult.receiptNumber, failureState.result.receiptNumber)
        assertEquals(failureResult.state, failureState.result.state)
    }

    @Test
    fun `updateState should update to Error state with PTError`() = runTest {
        // Given
        val error = PTError(ErrorCode.NotValid, "Invalid credit card number")
        
        // When
        viewModel.updateState(PaymentResultState.Error(error))
        
        // Then
        val currentState = viewModel.paymentState.first()
        assertTrue(currentState is PaymentResultState.Error)
        
        val errorState = currentState as PaymentResultState.Error
        assertEquals(error.code, errorState.error.code)
        assertEquals(error.error, errorState.error.error)
    }

    @Test
    fun `updateState should update to TokenSuccess state`() = runTest {
        // Given
        val tokenResults = PaymentMethodTokenResults(
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
        // Given
        val barcodeResult = BarcodeResult(
            barcodeId = "BARCODE123",
            barcodeUrl = "https://example.com/barcode/123",
            barcode = "123456789",
            barcodeFee = "1.50",
            merchant = "merchant123",
            mapUrl = "https://maps.example.com/nearby"
        )
        
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
} 