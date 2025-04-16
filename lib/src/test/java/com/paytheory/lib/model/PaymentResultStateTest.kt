package com.paytheory.lib.model

import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.HashMap

/**
 * Tests for the PaymentResultState sealed class
 */
class PaymentResultStateTest {

    @Test
    fun `Loading state should be equal to another Loading state`() {
        val state1 = PaymentResultState.Loading
        val state2 = PaymentResultState.Loading
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Idle state should be equal to another Idle state`() {
        val state1 = PaymentResultState.Idle
        val state2 = PaymentResultState.Idle
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Processing state should be equal to another Processing state`() {
        val state1 = PaymentResultState.Processing
        val state2 = PaymentResultState.Processing
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Success state should be equal to another Success state with same transaction result`() {
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
        
        val state1 = PaymentResultState.Success(transactionResult)
        val state2 = PaymentResultState.Success(transactionResult)
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Success state should not be equal to another Success state with different transaction result`() {
        val transactionResult1 = SuccessfulTransactionResult(
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
        
        val transactionResult2 = SuccessfulTransactionResult(
            state = "completed",
            amount = "2000",
            brand = "mastercard",
            lastFour = "5678",
            serviceFee = "0",
            currency = "USD",
            metadata = HashMap<Any, Any>(),
            receiptNumber = "REC-54321",
            createdAt = "1234567891",
            paymentMethodId = "",
            payorId = "cus_456"
        )
        
        val state1 = PaymentResultState.Success(transactionResult1)
        val state2 = PaymentResultState.Success(transactionResult2)
        
        assertNotEquals(state1, state2)
        assertNotEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Failure state should be equal to another Failure state with same failed transaction result`() {
        val failureResult = FailedTransactionResult(
            state = "failed",
            brand = "mastercard",
            lastFour = "5678",
            receiptNumber = "REC-FAIL-12345",
            paymentMethodId = "",
            payorId = "cus_456"
        )
        
        val state1 = PaymentResultState.Failure(failureResult)
        val state2 = PaymentResultState.Failure(failureResult)
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Failure state should not be equal to another Failure state with different failed transaction result`() {
        val failureResult1 = FailedTransactionResult(
            state = "failed",
            brand = "mastercard",
            lastFour = "5678",
            receiptNumber = "REC-FAIL-12345",
            paymentMethodId = "",
            payorId = "cus_456"
        )
        
        val failureResult2 = FailedTransactionResult(
            state = "failed",
            brand = "visa",
            lastFour = "1234",
            receiptNumber = "REC-FAIL-54321",
            paymentMethodId = "",
            payorId = "cus_123"
        )
        
        val state1 = PaymentResultState.Failure(failureResult1)
        val state2 = PaymentResultState.Failure(failureResult2)
        
        assertNotEquals(state1, state2)
        assertNotEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `TokenSuccess state should be equal to another TokenSuccess state with same token result`() {
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
        
        val state1 = PaymentResultState.TokenSuccess(tokenResults)
        val state2 = PaymentResultState.TokenSuccess(tokenResults)
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `TokenSuccess state should not be equal to another TokenSuccess state with different token result`() {
        val tokenResults1 = PaymentMethodTokenResults(
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
        
        val tokenResults2 = PaymentMethodTokenResults(
            state = "active",
            paymentMethodId = "pm_987654321",
            metadata = HashMap<Any, Any>(),
            payor_id = "cus_123",
            lastFour = "4321",
            firstSix = "654321",
            brand = "visa",
            expiration = "1226",
            paymentType = "card"
        )
        
        val state1 = PaymentResultState.TokenSuccess(tokenResults1)
        val state2 = PaymentResultState.TokenSuccess(tokenResults2)
        
        assertNotEquals(state1, state2)
        assertNotEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `BarcodeSuccess state should be equal to another BarcodeSuccess state with same barcode result`() {
        val barcodeResult = BarcodeResult(
            barcodeId = "BARCODE123",
            barcodeUrl = "https://example.com/barcode/123",
            barcode = "123456789",
            barcodeFee = "1.50",
            merchant = "merchant123",
            mapUrl = "https://maps.example.com/nearby"
        )
        
        val state1 = PaymentResultState.BarcodeSuccess(barcodeResult)
        val state2 = PaymentResultState.BarcodeSuccess(barcodeResult)
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `BarcodeSuccess state should not be equal to another BarcodeSuccess state with different barcode result`() {
        val barcodeResult1 = BarcodeResult(
            barcodeId = "BARCODE123",
            barcodeUrl = "https://example.com/barcode/123",
            barcode = "123456789",
            barcodeFee = "1.50",
            merchant = "merchant123",
            mapUrl = "https://maps.example.com/nearby"
        )
        
        val barcodeResult2 = BarcodeResult(
            barcodeId = "BARCODE456",
            barcodeUrl = "https://example.com/barcode/456",
            barcode = "987654321",
            barcodeFee = "2.00",
            merchant = "merchant456",
            mapUrl = "https://maps.example.com/other"
        )
        
        val state1 = PaymentResultState.BarcodeSuccess(barcodeResult1)
        val state2 = PaymentResultState.BarcodeSuccess(barcodeResult2)
        
        assertNotEquals(state1, state2)
        assertNotEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Error state should be equal to another Error state with same error`() {
        val error = PTError(ErrorCode.NotValid, "Invalid credit card number")
        
        val state1 = PaymentResultState.Error(error)
        val state2 = PaymentResultState.Error(error)
        
        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Error state should not be equal to another Error state with different error`() {
        val error1 = PTError(ErrorCode.NotValid, "Invalid credit card number")
        val error2 = PTError(ErrorCode.InProgress, "No internet connection")
        
        val state1 = PaymentResultState.Error(error1)
        val state2 = PaymentResultState.Error(error2)
        
        assertNotEquals(state1, state2)
        assertNotEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `Loading state should not be equal to Idle state`() {
        val loadingState = PaymentResultState.Loading
        val idleState = PaymentResultState.Idle
        
        assertNotEquals(loadingState, idleState)
        assertNotEquals(loadingState.hashCode(), idleState.hashCode())
    }

    @Test
    fun `Success state should not be equal to Failure state`() {
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
        
        val failureResult = FailedTransactionResult(
            state = "failed",
            brand = "mastercard",
            lastFour = "5678",
            receiptNumber = "REC-FAIL-12345",
            paymentMethodId = "",
            payorId = "cus_456"
        )
        
        val successState = PaymentResultState.Success(transactionResult)
        val failureState = PaymentResultState.Failure(failureResult)
        
        assertNotEquals(successState, failureState)
        assertNotEquals(successState.hashCode(), failureState.hashCode())
    }

    @Test
    fun `Success state should hold the correct transaction result`() {
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
        
        val successState = PaymentResultState.Success(transactionResult)
        
        assertEquals(transactionResult, successState.result)
    }

    @Test
    fun `Failure state should hold the correct failed transaction result`() {
        val failureResult = FailedTransactionResult(
            state = "failed",
            brand = "mastercard",
            lastFour = "5678",
            receiptNumber = "REC-FAIL-12345",
            paymentMethodId = "",
            payorId = "cus_456"
        )
        
        val failureState = PaymentResultState.Failure(failureResult)
        
        assertEquals(failureResult, failureState.result)
    }

    @Test
    fun `TokenSuccess state should hold the correct token result`() {
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
        
        val tokenSuccessState = PaymentResultState.TokenSuccess(tokenResults)
        
        assertEquals(tokenResults, tokenSuccessState.token)
    }

    @Test
    fun `BarcodeSuccess state should hold the correct barcode result`() {
        val barcodeResult = BarcodeResult(
            barcodeId = "BARCODE123",
            barcodeUrl = "https://example.com/barcode/123",
            barcode = "123456789",
            barcodeFee = "1.50",
            merchant = "merchant123",
            mapUrl = "https://maps.example.com/nearby"
        )
        
        val barcodeSuccessState = PaymentResultState.BarcodeSuccess(barcodeResult)
        
        assertEquals(barcodeResult, barcodeSuccessState.barcode)
    }

    @Test
    fun `Error state should hold the correct error`() {
        val error = PTError(ErrorCode.NotValid, "Invalid credit card number")
        
        val errorState = PaymentResultState.Error(error)
        
        assertEquals(error, errorState.error)
    }
} 