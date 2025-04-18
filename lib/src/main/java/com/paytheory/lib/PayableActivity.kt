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
 * Abstract class that provides a base implementation of the Payable interface.
 * It provides a method to register an ActivityResultLauncher for handling
 * activities that need to return a result.
 */
 abstract class PayableActivity : ComponentActivity(), Payable {

    /**
     * ActivityResultLauncher for handling activities that need to return a result.
     */
    lateinit var startIntentSenderForResult: ActivityResultLauncher<IntentSenderRequest>

    /**
     * Called when the activity is created. Registers the ActivityResultLauncher.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down, then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
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

    abstract override fun handleReady(isReady: Boolean)

    abstract override fun handlePaymentStart(paymentType: String)

    abstract override fun handleTokenStart(paymentType: String)

    abstract override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult)

    abstract override fun handleFailure(failedTransactionResult: FailedTransactionResult)

    abstract override fun handleBarcodeSuccess(barcodeResult: BarcodeResult)


    abstract override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults)

    abstract override fun handleStateChange(fieldState: Pair<PaymentField, FieldState>)

    abstract override fun handleError(error: PTError)

    override fun clientContext(): Context {
        return this
    }


}