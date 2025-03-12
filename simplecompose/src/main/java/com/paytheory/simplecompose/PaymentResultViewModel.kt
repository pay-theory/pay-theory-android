package com.paytheory.simplecompose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paytheory.lib.BarcodeResult
import com.paytheory.lib.FailedTransactionResult
import com.paytheory.lib.PTError
import com.paytheory.lib.PaymentMethodTokenResults
import com.paytheory.lib.SuccessfulTransactionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PaymentResultState {
    object Idle : PaymentResultState()
    data class Success(val result: SuccessfulTransactionResult) : PaymentResultState()
    data class Failure(val result: FailedTransactionResult) : PaymentResultState()
    data class TokenSuccess(val token: PaymentMethodTokenResults) : PaymentResultState()
    data class BarcodeSuccess(val barcode: BarcodeResult) : PaymentResultState()
    data class Error(val error: PTError) : PaymentResultState()
}

class PaymentResultViewModel : ViewModel() {
    private val _paymentState = MutableStateFlow<PaymentResultState>(PaymentResultState.Idle)
    val paymentState = _paymentState.asStateFlow()

    fun updateState(newState: PaymentResultState) {
        viewModelScope.launch {
            _paymentState.emit(newState)
        }
    }

    fun resetState() {
        _paymentState.value = PaymentResultState.Idle
    }
}