package com.paytheory.lib

import android.content.Context
import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.model.FieldState
import com.paytheory.lib.model.PaymentField


/**
 * Interface that defines the contract for handling payment and tokenization events.
 *
 * This interface is designed to be implemented by the consuming application's Activity or Fragment,
 * providing access to the application context for the SDK. It acts as a callback mechanism for
 * the SDK to communicate the status and results of various operations.
 *
 * The `Payable` interface provides methods to handle:
 * - SDK readiness status
 * - Start of payment and tokenization processes
 * - Results of successful and failed transactions
 * - Results of barcode requests
 * - Results of tokenization requests
 * - State changes of payment fields
 * - Errors encountered during the process
 *
 * Implementing this interface allows your application to react to these events and manage the
 * payment flow accordingly.
 */
interface Payable : ContextProvider {
    /**
     * Handles changes to the SDK readiness status.
     *
     * This function is called by the SDK to notify the consuming application whether the SDK
     * is ready to interact. When the SDK is ready, it can accept payment and tokenization
     * requests.
     *
     * @param isReady `true` if the SDK is ready to interact; `false` otherwise.
     */
    fun handleReady(isReady: Boolean)

    /**
     * Indicates the start of a payment process.
     *
     * This function is called by the SDK when a payment process is initiated. It provides
     * information about the type of payment being started.
     *
     * @param paymentType A string indicating the type of payment started (e.g., "CARD", "ACH", "CASH").
     */
    fun handlePaymentStart(paymentType: String)

    /**
     * Indicates the start of a tokenization process.
     *
     * This function is called by the SDK when a tokenization process is initiated for a
     * payment method.
     *
     * @param paymentType A string indicating the type of payment method being tokenized (e.g., "CARD", "ACH").
     */
    fun handleTokenStart(paymentType: String)

    /**
     * Handles successful payment results.
     *
     * This function is called by the SDK when a payment transaction has completed successfully.
     * It provides detailed results about the successful transaction.
     *
     * @param successfulTransactionResult The result object containing data for a successful transaction.
     */
    fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult)

    /**
     * Handles declined payment results.
     *
     * This function is called by the SDK when a payment transaction has failed or been declined.
     * It provides information about the failed transaction.
     *
     * @param failedTransactionResult The result object containing data for a failed transaction.
     */
    fun handleFailure(failedTransactionResult: FailedTransactionResult)

    /**
     * Handles successful barcode results.
     *
     * This function is called by the SDK when a barcode request has completed successfully.
     * It provides the results of the barcode request.
     *
     * @param barcodeResult The result object containing data for a successful barcode request.
     */
    fun handleBarcodeSuccess(barcodeResult: BarcodeResult)

    /**
     * Handles successful tokenization results.
     *
     * This function is called by the SDK when a tokenization request has completed successfully.
     * It provides the results of the tokenization process.
     *
     * @param paymentMethodToken The result object containing data for a payment method token request.
     */
    fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults)

    /**
     * Handles state changes of payment fields.
     *
     * This function is called by the SDK when the state of a payment field changes.
     * It provides information about the specific field and its new state.
     *
     * @param fieldState A pair where the first element is the `PaymentField` that changed, and the second element is the `FieldState` of the field.
     */
    fun handleStateChange(fieldState: Pair<PaymentField, FieldState>)

    /**
     * Handles errors encountered by the SDK.
     *
     * This function is called by the SDK to communicate any system errors that have occurred,
     * either from the user's device or from the Pay Theory platform.
     *
     * @param error The `PTError` object containing the reason for the failure.
     */
    fun handleError(error: PTError)

    fun clientContext(): Context
    
    /**
     * Returns the Android Context from the implementing class.
     * Default implementation returns null for test environments.
     *
     * @return The Android Context or null
     */
    override fun getContext(): Context? = clientContext()
}