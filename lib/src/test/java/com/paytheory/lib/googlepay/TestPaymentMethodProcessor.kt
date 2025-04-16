package com.paytheory.lib.googlepay

import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.requests.ActionRequest
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.model.PaymentViewModel

/**
 * Test implementation of PaymentMethodProcessor that can be used in unit tests
 * This version doesn't rely on API keys during initialization
 */
class TestPaymentMethodProcessor(
    override val payable: Payable,
    override val configuration: PayTheoryConfiguration,
    override val viewModel: PaymentViewModel
) : PaymentMethodProcessor(payable, hashMapOf(), configuration, viewModel) {

    init {
        // No need to override headerMap - it's now a lazy property that will initialize based on configuration.isTestMode
        // The configuration used in tests should have isTestMode = true
    }

    override fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?) {
        // No-op implementation for testing
    }

    override fun process(payment: PaymentDetail) {
        // No-op implementation for testing
    }

    override fun createInitialActionRequest(payment: PaymentDetail): ActionRequest {
        return ActionRequest(
            action = "test-action",
            encoded = "",
            publicKey = "",
            sessionKey = ""
        )
    }

    override fun receiveMessage(message: String) {
        // No-op implementation for testing
    }

    override fun disconnect() {
        // No-op implementation for testing
    }
} 