package com.paytheory.android.test.mocks

import com.paytheory.android.sdk.model.FieldState
import com.paytheory.android.sdk.model.PTError
import com.paytheory.android.sdk.model.PTResult
import com.paytheory.android.sdk.model.PaymentField
import com.paytheory.android.sdk.view.Payable

/**
 * Test implementation of the Payable interface
 * 
 * Captures callback results for verification in tests
 */
class TestPayable : Payable {
    
    // Result tracking for test assertions
    var lastResult: PTResult? = null
    var lastError: PTError? = null
    val fieldStates = mutableMapOf<PaymentField, FieldState>()
    
    // Counters for callback invocations
    var successCallCount = 0
    var errorCallCount = 0
    var stateChangeCallCount = 0
    
    // Transaction history for test validation
    val transactionHistory = mutableListOf<PTResult>()
    val errorHistory = mutableListOf<PTError>()
    
    /**
     * Receives successful payment/tokenization result
     */
    override fun handleSuccess(result: PTResult) {
        lastResult = result
        transactionHistory.add(result)
        successCallCount++
    }
    
    /**
     * Receives error details
     */
    override fun handleError(error: PTError) {
        lastError = error
        errorHistory.add(error)
        errorCallCount++
    }
    
    /**
     * Receives field state changes
     */
    override fun handleStateChange(fieldState: Pair<PaymentField, FieldState>) {
        fieldStates[fieldState.first] = fieldState.second
        stateChangeCallCount++
    }
    
    /**
     * Reset all tracking for a fresh test
     */
    fun reset() {
        lastResult = null
        lastError = null
        fieldStates.clear()
        successCallCount = 0
        errorCallCount = 0
        stateChangeCallCount = 0
        transactionHistory.clear()
        errorHistory.clear()
    }
} 