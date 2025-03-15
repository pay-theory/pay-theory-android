package com.paytheory.lib.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paytheory.lib.data.BarcodeResult
import com.paytheory.lib.data.FailedTransactionResult
import com.paytheory.lib.data.PTError
import com.paytheory.lib.data.PaymentMethodTokenResults
import com.paytheory.lib.data.SuccessfulTransactionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the possible states of a payment transaction process.
 *
 * This sealed class encapsulates all possible states that a payment transaction can be in:
 * - [Loading]: Initial state when the payment process is starting
 * - [Idle]: Default state when no payment is being processed
 * - [Success]: Payment was successfully processed
 * - [Failure]: Payment failed to process
 * - [TokenSuccess]: Payment method was successfully tokenized
 * - [BarcodeSuccess]: Barcode was successfully generated
 * - [Error]: An error occurred during processing
 */
sealed class PaymentResultState {

    /** Indicates the payment process is initializing or loading */
    object Loading : PaymentResultState()

    /** Indicates no payment is currently being processed */
    object Idle : PaymentResultState()

    /** Indicates payment is currently being processed */
    object Processing : PaymentResultState()
    
    /** Represents a successful payment transaction with its result */
    data class Success(val result: SuccessfulTransactionResult) : PaymentResultState()
    
    /** Represents a failed payment transaction with failure details */
    data class Failure(val result: FailedTransactionResult) : PaymentResultState()
    
    /** Represents successful tokenization of a payment method */
    data class TokenSuccess(val token: PaymentMethodTokenResults) : PaymentResultState()
    
    /** Represents successful generation of a barcode */
    data class BarcodeSuccess(val barcode: BarcodeResult) : PaymentResultState()
    
    /** Represents an error that occurred during payment processing */
    data class Error(val error: PTError) : PaymentResultState()
}

/**
 * ViewModel responsible for managing the payment processing state.
 *
 * This ViewModel maintains the state of a payment transaction and provides
 * functionality to update the state as the payment process progresses.
 *
 * @property paymentState The current state of the payment process, exposed as an immutable StateFlow
 */
class PaymentProcessViewModel : ViewModel() {
    private val _paymentState = MutableStateFlow<PaymentResultState>(PaymentResultState.Idle)
    val paymentState = _paymentState.asStateFlow()

    /**
     * Updates the payment process state.
     *
     * This function safely updates the payment state within a coroutine scope.
     * It ensures that state updates are properly handled even when the ViewModel
     * is being cleared or when multiple updates are requested simultaneously.
     *
     * @param newState The new state to update to
     */
    fun updateState(newState: PaymentResultState) {
        viewModelScope.launch {
            _paymentState.emit(newState)
            Log.d("PaymentProcessViewModel", "updateState: $newState")
        }
    }
}

