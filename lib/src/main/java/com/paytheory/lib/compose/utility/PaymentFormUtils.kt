package com.paytheory.lib.compose.utility

import android.content.Context
import android.icu.util.TimeZone
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.model.PaymentViewModel

/**
 * Utility class for payment form operations
 *
 * This class contains business logic for payment form processing
 * following the testing strategy of isolating business logic from UI components.
 */
object PaymentFormUtils {
    
    /**
     * Validates network connectivity for payment processing
     *
     * @param context The application or activity context
     * @param payable The Payable implementation for callbacks
     * @return True if network is available, false otherwise
     */
    fun validateNetworkConnectivity(context: Context, payable: Payable): Boolean {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            payable.handleError(PTError(ErrorCode.TokenFailed, NetworkUtils.NO_NETWORK_CONNECTION))
            return false
        }
        return true
    }
    
    /**
     * Creates payTheoryData object for transfer requests
     * 
     * @param configuration PayTheoryConfiguration object
     * @return HashMap<Any, Any> of pay theory data for the request
     */
    fun createPayTheoryData(configuration: PayTheoryConfiguration): HashMap<Any, Any> {
        //create pay_theory_data object for host:transfer_part1 action request
        val payTheoryData = hashMapOf<Any, Any>()

        payTheoryData["skip_validation"] = configuration.skipTokenizeValidation

        payTheoryData["send_receipt"] = configuration.sendReceipt
        if (configuration.sendReceipt == true) {
            if (configuration.receiptDescription.isNotBlank()){
                payTheoryData["receipt_description"] = configuration.receiptDescription
            }
        }
        // if paymentParameters is given add to pay_theory_data
        if (configuration.paymentParameters.isNotBlank()) {
            payTheoryData["payment_parameters"] = configuration.paymentParameters
        }
        // if payorId is given add to pay_theory_data
        if (configuration.payorId.isNotBlank()) {
            payTheoryData["payorId"] = configuration.payorId
        }
        // if invoiceId is given add to pay_theory_data
        if (configuration.invoiceId.isNotBlank()) {
            payTheoryData["invoice_id"] = configuration.invoiceId
        }
        // if account_code is given add to pay_theory_data
        if (configuration.accountCode.isNotBlank()) {
            payTheoryData["account_code"] = configuration.accountCode
        }
        // if reference is given add to pay_theory_data
        if (configuration.reference.isNotBlank()) {
            payTheoryData["reference"] = configuration.reference
        }

        payTheoryData["fee"] = configuration.serviceFee as Any

        if (configuration.isTestMode) {
            payTheoryData["timezone"] = "test"
        } else {
            payTheoryData["timezone"] = TimeZone.getDefault().id
        }

        return payTheoryData
    }
    
    /**
     * Triggers a payment submission based on the current validation state
     *
     * @param viewModel The payment view model
     * @return True if payment submission was triggered, false otherwise
     */
    fun submitPaymentIfValid(viewModel: PaymentViewModel): Boolean {
        if (viewModel.isValidAndReady) {
            viewModel.submitPayment()
            return true
        }
        return false
    }
} 