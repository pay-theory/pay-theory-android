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
 * Interface that defines the contract for handling payment and tokenization events within the Pay Theory SDK.
 *
 * This interface must be implemented by the Activity or Fragment that hosts the Pay Theory UI components.
 * It provides the necessary callbacks for the SDK to communicate the status and results of various operations,
 * such as payment transactions, tokenization, and field state changes. The implementation also grants the SDK
 * access to the application context via the [ContextProvider] interface.
 *
 * Implementing this interface allows your application to react to these events and manage the
 * payment flow accordingly, updating the UI or triggering other application logic based on the results.
 *
 * Key responsibilities include handling:
 * - SDK readiness status ([handleReady])
 * - Start indications for payment and tokenization processes ([handlePaymentStart], [handleTokenStart])
 * - Results of successful and failed transactions ([handleSuccess], [handleFailure])
 * - Results of successful barcode generation ([handleBarcodeSuccess])
 * - Results of successful payment method tokenization ([handleTokenizeSuccess])
 * - State changes of individual payment input fields ([handleStateChange])
 * - Errors encountered during SDK operations ([handleError])
 */
interface Payable : ContextProvider {
    /**
     * Called when the SDK's initialization status changes.
     *
     * This function notifies the host application whether the Pay Theory SDK components are ready
     * to accept user input and initiate operations like payments or tokenization. It's typically
     * used to enable/disable UI elements related to payment actions.
     *
     * @param isReady `true` if the SDK components are initialized and ready; `false` otherwise.
     */
    fun handleReady(isReady: Boolean)

    /**
     * Called immediately before the SDK initiates a payment transaction (e.g., charging a card or bank account).
     *
     * This callback signals that a payment attempt is about to start. It can be used to display
     * loading indicators or disable further user interaction until the transaction completes.
     *
     * @param paymentType A string identifier for the type of payment being initiated (e.g., "CARD", "ACH", "CASH", "GOOGLE_PAY").
     */
    fun handlePaymentStart(paymentType: String)

    /**
     * Called immediately before the SDK initiates a payment method tokenization process.
     *
     * This callback signals that a request to tokenize a payment method (like a card or bank account)
     * is about to start. It can be used similarly to [handlePaymentStart] for UI feedback.
     *
     * @param paymentType A string identifier for the type of payment method being tokenized (e.g., "CARD", "ACH").
     */
    fun handleTokenStart(paymentType: String)

    /**
     * Called when a payment transaction completes successfully.
     *
     * This function delivers the results of a successful payment, including transaction details.
     * The host application should use this callback to confirm the payment to the user and proceed
     * with the next steps in their flow (e.g., showing an order confirmation).
     *
     * @param successfulTransactionResult An object containing detailed information about the successful transaction.
     */
    fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult)

    /**
     * Called when a payment transaction fails or is declined.
     *
     * This function delivers the results of a failed payment attempt. The host application should
     * use this callback to inform the user about the failure and potentially allow them to retry
     * or use a different payment method.
     *
     * @param failedTransactionResult An object containing details about the failed transaction, including error messages or decline reasons.
     */
    fun handleFailure(failedTransactionResult: FailedTransactionResult)

    /**
     * Called when a cash barcode generation request completes successfully.
     *
     * This function delivers the barcode details necessary for the user to complete a cash payment
     * at a physical location.
     *
     * @param barcodeResult An object containing the generated barcode information.
     */
    fun handleBarcodeSuccess(barcodeResult: BarcodeResult)

    /**
     * Called when a payment method tokenization request completes successfully.
     *
     * This function delivers the tokenized payment method details. The host application can store
     * this token securely for future transactions or other purposes.
     *
     * @param paymentMethodToken An object containing the results of the tokenization, including the payment method token.
     */
    fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults)

    /**
     * Called whenever the input state (validity, focus, etc.) of a Pay Theory payment field changes.
     *
     * This allows the host application to react to changes in individual fields, for example,
     * by displaying validation errors or updating UI elements based on field focus.
     *
     * @param fieldState A [Pair] containing the specific [PaymentField] that changed and its new [FieldState].
     */
    fun handleStateChange(fieldState: Pair<PaymentField, FieldState>)

    /**
     * Called when an unexpected error occurs within the SDK.
     *
     * This function is used to report errors that are not related to specific transaction failures
     * (handled by [handleFailure]) but rather to issues like network problems, configuration errors,
     * or internal SDK exceptions.
     *
     * @param error A [PTError] object describing the error condition.
     */
    fun handleError(error: PTError)

    /**
     * Provides the Android [Context] to the SDK.
     *
     * This method must be implemented to return the application or activity context.
     * It is used internally by the SDK for various Android-specific operations.
     *
     * @return The Android [Context] of the host application component.
     */
    fun clientContext(): Context

    /**
     * Implementation of the [ContextProvider] interface.
     * Retrieves the Android [Context] via [clientContext].
     *
     * @return The Android [Context] provided by [clientContext], or null if unavailable (e.g., in certain test environments).
     */
    override fun getContext(): Context? = clientContext()
}