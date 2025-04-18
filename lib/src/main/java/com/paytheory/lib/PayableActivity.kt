package com.paytheory.lib

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.model.FieldState
import com.paytheory.lib.model.PaymentField


/**
 * An abstract [ComponentActivity] that provides a foundational implementation of the [Payable] interface.
 *
 * This class simplifies the integration of Pay Theory SDK features into an Android Activity by:
 * 1. Implementing the [Payable] interface.
 * 2. Providing a concrete implementation for [Payable.clientContext] returning the Activity context.
 * 3. Setting up an [ActivityResultLauncher] ([startIntentSenderForResult]) specifically configured
 *    to handle results from `IntentSender` actions, such as the Google Pay payment sheet.
 *
 * Activities extending `PayableActivity` must provide concrete implementations for the abstract
 * callback methods defined in the [Payable] interface (e.g., [handleSuccess], [handleFailure], etc.).
 *
 * The built-in `ActivityResultLauncher` handles the basic success/error flow for `IntentSender` results,
 * automatically calling [handleError] with a [PTError.ErrorCode.GooglePayError] on failure.
 * **Note:** The success path currently only prints the intent data; specific handling (like parsing Google Pay results)
 * needs to be implemented in the concrete subclass or potentially integrated here in the future.
 */
abstract class PayableActivity : ComponentActivity(), Payable {

    /**
     * An [ActivityResultLauncher] specifically configured to handle results launched via an [IntentSenderRequest].
     *
     * This launcher is automatically registered in [onCreate]. It is primarily intended for handling the
     * result of the Google Pay payment sheet activity. On receiving a successful result (`RESULT_OK`),
     * it currently logs the intent data. On failure (any other `resultCode`), it invokes [handleError]
     * with a generic [PTError.ErrorCode.GooglePayError].
     *
     * Subclasses might override or supplement the result handling logic if needed, but the basic
     * setup is provided here.
     */
    lateinit var startIntentSenderForResult: ActivityResultLauncher<IntentSenderRequest>

    /**
     * Initializes the activity and registers the [startIntentSenderForResult] launcher.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down, then this Bundle contains the data it most
     *     recently supplied in `onSaveInstanceState`. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startIntentSenderForResult = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    println(intent)
//                    PaymentData.getFromIntent(intent)?.let { paymentData ->
//                        handlePaymentSuccess(paymentData)
//                    }
                }
            } else {
                handleError(PTError(ErrorCode.GooglePayError, "Google Pay error"))
            }
        }
    }

    /** @see Payable.handleReady */
    abstract override fun handleReady(isReady: Boolean)

    /** @see Payable.handlePaymentStart */
    abstract override fun handlePaymentStart(paymentType: String)

    /** @see Payable.handleTokenStart */
    abstract override fun handleTokenStart(paymentType: String)

    /** @see Payable.handleSuccess */
    abstract override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult)

    /** @see Payable.handleFailure */
    abstract override fun handleFailure(failedTransactionResult: FailedTransactionResult)

    /** @see Payable.handleBarcodeSuccess */
    abstract override fun handleBarcodeSuccess(barcodeResult: BarcodeResult)

    /** @see Payable.handleTokenizeSuccess */
    abstract override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults)

    /** @see Payable.handleStateChange */
    abstract override fun handleStateChange(fieldState: Pair<PaymentField, FieldState>)

    /** @see Payable.handleError */
    abstract override fun handleError(error: PTError)

    /**
     * Provides the Activity context.
     * Implements [Payable.clientContext].
     *
     * @return This Activity instance as the [Context].
     */
    override fun clientContext(): Context {
        return this
    }
}