package com.paytheory.lib.googlepay

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.PayableActivity
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.configuration.GooglePayConstants
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.model.PaymentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.google.android.gms.common.api.ResolvableApiException


/**
 * Google Pay payment processor for the Pay Theory SDK
 * Handles Google Pay payment flows and token processing
 *
 * @param payable The payable interface for callbacks
 * @param activity The activity hosting the payment flow
 * @param configuration The Pay Theory configuration
 * @param viewModel The payment view model for state management
 * @param googlePayUtil The Google Pay utility interface (for easier testing)
 */
@OptIn(ExperimentalCoroutinesApi::class)
/**
 * Manages the Google Pay flow.
 *
 * @param payable The Payable interface implementation
 * @param activity The activity being used for Google Pay
 * @param configuration Pay Theory configuration with Google Pay settings
 * @param viewModel The PaymentViewModel for managing payment data
 */
class GooglePayProcessor(
    private val payable: Payable,
    private val activity: Activity,
    private val configuration: PayTheoryConfiguration,
    private val viewModel: PaymentViewModel
): GooglePayProcessorInterface {

    private val googlePayUtil = GooglePayUtil.getInstance()
    private lateinit var activityLauncher: ActivityResultLauncher<IntentSenderRequest>

    private val allowedCardNetworks = GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS
    private val allowedAuthMethods = GooglePayConstants.DEFAULT_SUPPORTED_METHODS
    private val billingAddressRequired = configuration.googlePayBillingAddressRequired
    private val billingAddressFormat = configuration.googlePayBillingAddressFormat
    private val shippingAddressRequired = configuration.googlePayShippingAddressRequired
    private val phoneNumberRequired = configuration.googlePayPhoneNumberRequired
    private val allowPrepaidCards = configuration.googlePayAllowPrepaidCards
    private val allowCreditCards = configuration.googlePayAllowCreditCards
    private val environment = configuration.googlePayEnvironment
    private val merchantName = configuration.googlePayMerchantName

    /**
     * Checks if Google Pay is available on the device.
     *
     * @param onAvailabilityChecked Callback to handle the result of availability check
     */
    override fun isGooglePayAvailable(): Task<Boolean> {
        val availabilityTask = googlePayUtil.isGooglePayAvailable(
            activity,
            environment,
            billingAddressRequired
        )
        availabilityTask.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e("isReadyToPay", "API Error code: ${exception.statusCode}")
                Log.e("isReadyToPay", "API Error: ${exception.message}")
            } else {
                // Log non-API exceptions as well
                Log.e("isReadyToPay", "Non-API Error: ${exception.message}", exception)
            }
        }
        // Return the task itself so the caller can handle completion/failure
        return availabilityTask
    }

    /**
     * Loads the payment data from the Google Pay API.
     */
    override fun initiateGooglePayPayment() {
        val task = googlePayUtil.requestGooglePayment(
            activity,
            viewModel.configuration.amount.toBigDecimal(),
            viewModel.configuration.googlePayMerchantName!!,
            environment,
            billingAddressRequired,
            billingAddressFormat,
            shippingAddressRequired,
            phoneNumberRequired,
            allowPrepaidCards,
            allowCreditCards
        )

        task.addOnCompleteListener { completedTask ->
            if (completedTask.isSuccessful) {
                // This case is less common with loadPaymentData.
                // It might happen if Google Pay determines no user interaction is needed.
                Log.d("requestGooglePayment", "Task successful, attempting to get PaymentData directly.")
                completedTask.result?.let { paymentData ->
                     Log.d("PaymentData", paymentData.toString())
                     // Process the payment data (e.g., extract token)
                     val token = googlePayUtil.extractPaymentToken(paymentData)
                     Log.d("PaymentToken", token)
                     // TODO: Send token to your backend for processing
                     // TODO: Call payable.handleSuccess(...) or payable.handleError(...) based on backend result
                } ?: run {
                     Log.e("requestGooglePayment", "Task successful but payment data is null")
                     payable.handleError(PTError(ErrorCode.GooglePayError, "Failed to retrieve payment data"))
                }
            } else {
                when (val exception = completedTask.exception) {
                    is com.google.android.gms.common.api.ResolvableApiException -> {
                        // Display the Google Pay sheet to resolve the error
                        Log.d("requestGooglePayment", "ResolvableApiException received, launching Google Pay sheet.")
                        try {
                            val resolutionIntentSender = exception.resolution.intentSender
                            val intentSenderRequest = IntentSenderRequest.Builder(resolutionIntentSender).build()
                            activityLauncher.launch(intentSenderRequest)
                        } catch (e: IntentSender.SendIntentException) {
                             Log.e("requestGooglePayment", "Error launching Google Pay sheet", e)
                             payable.handleError(PTError(ErrorCode.GooglePayError, "Could not launch Google Pay UI"))
                        }
                    }
                    is ApiException -> {
                        // Handle other API exceptions
                        Log.e("requestGooglePayment", "ApiException: Status Code: ${exception.statusCode}, Message: ${exception.message}")
                        // You might want to map specific status codes to different PTError codes
                        payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay API error: ${exception.message}"))
                    }
                    else -> {
                        // Handle other exceptions
                        Log.e("requestGooglePayment", "Unexpected error during Google Pay request", exception)
                        payable.handleError(PTError(ErrorCode.GooglePayError, "Unknown error during Google Pay setup: ${exception?.message}"))
                    }
                }
            }
        }
    }

    fun setActivityLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.activityLauncher = launcher
    }

    override fun process(payment: PaymentDetail) {
        TODO("Not yet implemented")
    }

    override fun receiveMessage(message: String) {
        TODO("Not yet implemented")
    }

    override fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?) {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun constructGooglePayPayment(token: String): PaymentDetail {
        TODO("Not yet implemented")
    }
}