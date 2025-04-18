package com.paytheory.lib.googlepay

import android.app.Activity
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.model.PaymentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber


/**
 * Orchestrates the Google Pay payment process within the Pay Theory SDK.
 *
 * This class acts as the high-level manager for Google Pay interactions. It utilizes [GooglePayUtil]
 * (obtained via [GooglePayUtil.getInstance]) to interact with the underlying Google Pay API via [GooglePayClient].
 *
 * Its main responsibilities include:
 * 1. Checking if Google Pay is available and configured correctly on the device using [isGooglePayAvailable].
 * 2. Initiating the Google Pay payment flow using [initiateGooglePayPayment].
 * 3. Handling the asynchronous results from the Google Pay API, including success, failure, and resolution flows.
 * 4. Managing the launch of the Google Pay payment sheet when required (via a provided [ActivityResultLauncher]).
 * 5. Coordinating with the [Payable] interface to report final outcomes (success, failure, errors).
 *
 * @property payable The [Payable] interface implementation for receiving callbacks about the payment process status.
 * @property activity The host [Activity] required for launching Google Pay UI elements.
 * @property configuration The active [PayTheoryConfiguration] containing essential Google Pay settings.
 * @property viewModel The [PaymentViewModel] used for accessing shared configuration and potentially payment state (though state seems less used here directly).
 */
@OptIn(ExperimentalCoroutinesApi::class) // Likely related to coroutine usage within dependencies or planned implementation
class GooglePayProcessor(
    private val payable: Payable,
    private val activity: Activity,
    private val configuration: PayTheoryConfiguration,
    private val viewModel: PaymentViewModel
): GooglePayProcessorInterface {

    // Obtains the singleton instance of GooglePayUtil, potentially configured by GooglePayFactory
    private val googlePayUtil = GooglePayUtil.getInstance()
    // Launcher required to start the Google Pay payment sheet activity for result.
    // Must be set via setActivityLauncher before calling initiateGooglePayPayment.
    private lateinit var activityLauncher: ActivityResultLauncher<IntentSenderRequest>

    // Configuration properties extracted for local use
    // Note: Using viewModel.configuration might be preferable if config can change, but local copies are fine if config is immutable per processor instance.
    private val allowedCardNetworks = configuration.googlePayAllowedCardNetworks // Defaulted in config if not set
    private val allowedAuthMethods = configuration.googlePaySupportedMethods   // Defaulted in config if not set
    private val billingAddressRequired = configuration.googlePayBillingAddressRequired
    private val billingAddressFormat = configuration.googlePayBillingAddressFormat
    private val shippingAddressRequired = configuration.googlePayShippingAddressRequired
    private val phoneNumberRequired = configuration.googlePayPhoneNumberRequired
    private val allowPrepaidCards = configuration.googlePayAllowPrepaidCards
    private val allowCreditCards = configuration.googlePayAllowCreditCards
    private val environment = configuration.googlePayEnvironment
    private val merchantName = configuration.googlePayMerchantName // Assumed to be validated by PayTheoryConfiguration

    /**
     * Checks if the user's device is set up and ready for Google Pay transactions with the current configuration.
     *
     * Delegates the check to [GooglePayUtil.isGooglePayAvailable]. Logs potential API errors.
     *
     * @return A [Task<Boolean>] that indicates readiness. The caller should use `addOnCompleteListener` or
     *         `addOnSuccessListener`/`addOnFailureListener` to handle the asynchronous result.
     */
    override fun isGooglePayAvailable(): Task<Boolean> {
        Timber.d("Checking Google Pay availability...")
        val availabilityTask = googlePayUtil.isGooglePayAvailable(
            activity,
            environment,
            billingAddressRequired,
            allowedCardNetworks, // Pass the configured networks
            allowedAuthMethods   // Pass the configured auth methods
        )
        availabilityTask.addOnFailureListener { exception ->
            // Log details if the readiness check itself fails
            if (exception is ApiException) {
                Timber.e(exception, "Google Pay API Error during readiness check. Status Code: ${exception.statusCode}")
            } else {
                Timber.e(exception, "Non-API Error during Google Pay readiness check: ${exception.message}")
            }
            // The failure is implicitly propagated through the returned Task.
        }
        return availabilityTask
    }

    /**
     * Initiates the Google Pay payment process by requesting payment data.
     *
     * This method calls [GooglePayUtil.requestGooglePayment] which, in turn, uses [GooglePayClient]
     * to call Google's `loadPaymentData`. It handles the resulting [Task]:
     * - **Success (Rare for initial call):** If the task succeeds directly (meaning Google Pay didn't need UI),
     *   it attempts to extract the token and process it (currently marked as TODO).
     * - **Failure (ResolvableApiException):** If Google Pay needs user interaction (e.g., selecting a card),
     *   it receives a [ResolvableApiException]. The code extracts the `PendingIntent` and uses the
     *   [activityLauncher] (set via [setActivityLauncher]) to start the Google Pay payment sheet activity.
     *   The result of this activity launch needs to be handled where the launcher was registered (typically in the host Activity).
     * - **Failure (Other ApiExceptions):** Reports an error via [Payable.handleError].
     * - **Failure (Other Exceptions):** Reports an error via [Payable.handleError].
     *
     * **Precondition:** [setActivityLauncher] must be called before invoking this method.
     */
    override fun initiateGooglePayPayment() {
        // Ensure merchantName is not null, as it's required by requestGooglePayment
        if (merchantName.isNullOrBlank()) {
            Timber.e("Google Pay merchant name is missing in configuration.")
            payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay merchant name not configured"))
            return
        }

        // Ensure the launcher has been set
        if (!::activityLauncher.isInitialized) {
            Timber.e("ActivityResultLauncher was not set before calling initiateGooglePayPayment.")
            payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay ActivityResultLauncher not set"))
            return
        }

        Timber.d("Initiating Google Pay payment request...")
        payable.handlePaymentStart("GOOGLE_PAY") // Notify host that Google Pay process is starting

        val task = googlePayUtil.requestGooglePayment(
            activity,
            configuration.amount.toBigDecimal(), // Use amount from configuration
            merchantName, // Use validated merchant name
            environment,
            billingAddressRequired,
            billingAddressFormat,
            shippingAddressRequired,
            phoneNumberRequired,
            allowPrepaidCards,
            allowCreditCards,
            allowedCardNetworks, // Pass configured networks
            allowedAuthMethods   // Pass configured auth methods
        )

        task.addOnCompleteListener { completedTask ->
            if (completedTask.isSuccessful) {
                // This path is unlikely for the initial loadPaymentData call, but handle defensively.
                Timber.d("Google Pay task completed successfully without needing resolution.")
                completedTask.result?.let { paymentData ->
                    Timber.d("Received PaymentData directly: ${paymentData.toJson()}")
                    // Process the payment data immediately
                    handleSuccessfulPaymentData(paymentData)
                } ?: run {
                    Timber.e("Google Pay task successful but PaymentData is null.")
                    payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay returned null PaymentData"))
                }
            } else {
                // Handle failures, checking specifically for resolvable errors
                when (val exception = completedTask.exception) {
                    is ResolvableApiException -> {
                        // Common case: Google Pay needs user interaction.
                        Timber.i("ResolvableApiException received (Status code: ${exception.statusCode}). Launching Google Pay sheet.")
                        try {
                            val resolutionIntentSender = exception.resolution.intentSender
                            val intentSenderRequest = IntentSenderRequest.Builder(resolutionIntentSender).build()
                            activityLauncher.launch(intentSenderRequest)
                            // The result will be delivered to the ActivityResultCallback where the launcher was registered.
                        } catch (e: IntentSender.SendIntentException) {
                            Timber.e(e, "Failed to launch Google Pay sheet intent.")
                            payable.handleError(PTError(ErrorCode.GooglePayError, "Could not launch Google Pay UI: ${e.message}"))
                        }
                    }
                    is ApiException -> {
                        // Other non-resolvable Google Pay API errors.
                        Timber.e(exception, "Google Pay API error: Status Code: ${exception.statusCode}, Message: ${exception.message}")
                        payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay API error: ${exception.statusCode} - ${exception.message}"))
                    }
                    else -> {
                        // Unexpected errors.
                        Timber.e(exception, "Unexpected error during Google Pay request.")
                        payable.handleError(PTError(ErrorCode.GooglePayError, "Unknown Google Pay error: ${exception?.message}"))
                    }
                }
            }
        }
    }

    /**
     * Processes the [PaymentData] received after a successful Google Pay interaction
     * (typically from the ActivityResultCallback).
     *
     * @param paymentData The successful [PaymentData] result from Google Pay.
     */
    private fun handleSuccessfulPaymentData(paymentData: PaymentData) {
        try {
            val token = googlePayUtil.extractPaymentToken(paymentData)
            Timber.i("Successfully extracted Google Pay token.") // Don't log the token itself
            // TODO: Implement logic to send this token to the Pay Theory backend for processing.
            // This might involve calling another service or method within the SDK.
            // Based on the backend response, call:
            // payable.handleSuccess(...) with transaction details, or
            // payable.handleFailure(...) / payable.handleError(...) if backend processing fails.
            println("Google Pay Token (for demo): $token") // Replace with actual processing
            // Example placeholder for error:
             payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay token processing not yet implemented."))

        } catch (e: Exception) {
            Timber.e(e, "Failed to process successful Google Pay PaymentData.")
            payable.handleError(PTError(ErrorCode.GooglePayError, "Error processing Google Pay result: ${e.message}"))
        }
    }

    /**
     * Sets the [ActivityResultLauncher] required to launch the Google Pay payment sheet.
     *
     * This launcher must be registered in the host Activity (e.g., using `registerForActivityResult`) and
     * its callback is responsible for handling the `Activity.RESULT_OK` / `Activity.RESULT_CANCELED`
     * results from the Google Pay sheet, potentially calling [handleSuccessfulPaymentData] on success.
     *
     * @param launcher The [ActivityResultLauncher] configured for [IntentSenderRequest].
     */
    fun setActivityLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        Timber.d("Setting ActivityResultLauncher for GooglePayProcessor.")
        this.activityLauncher = launcher
    }

    // --- Methods below are part of PaymentProcessorInterface but not implemented for Google Pay --- 
    // TODO: Review if these methods should throw an error, log a warning, or be removed if 
    // GooglePayProcessor shouldn't fully implement PaymentProcessorInterface.

    /** This method is not applicable to the Google Pay flow. */
    override fun process(payment: PaymentDetail) {
        Timber.w("process() called on GooglePayProcessor - Not Implemented for Google Pay flow.")
        // Consider throwing UnsupportedOperationException or logging?
        // TODO("Not yet implemented for Google Pay")
    }

    /** This method is not applicable to the Google Pay flow. */
    override fun receiveMessage(message: String) {
        Timber.w("receiveMessage() called on GooglePayProcessor - Not Implemented for Google Pay flow.")
        // TODO("Not yet implemented for Google Pay")
    }

    /** This method is not applicable to the standard Google Pay flow. */
    override fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?) {
        Timber.w("establishViewModel() called on GooglePayProcessor - Not Implemented for Google Pay flow.")
        // TODO("Not yet implemented for Google Pay")
    }

    /** This method is not applicable to the Google Pay flow. */
    override fun disconnect() {
        Timber.w("disconnect() called on GooglePayProcessor - Not Implemented for Google Pay flow.")
        // TODO("Not yet implemented for Google Pay")
    }

    /**
     * Constructs a [PaymentDetail] object containing the Google Pay token.
     * This might be used if the standard SDK flow involves wrapping the token
     * before sending it to a unified backend endpoint.
     *
     * @param token The Google Pay payment token obtained from [extractPaymentToken].
     * @return A [PaymentDetail] object encapsulating the Google Pay token.
     */
    override fun constructGooglePayPayment(token: String): PaymentDetail {
        Timber.d("Constructing PaymentDetail for Google Pay token.")
        // TODO: Confirm the exact structure expected by the backend/standard flow.
        // This is a plausible implementation, but might need adjustment.
        return PaymentDetail(
            digitalWalletPayload = token, // Place the Google Pay token here
            type = "GOOGLE_PAY",
            timing = 1234567890L,
            amount = 1000,
            currency = "USD"
        )

    }
//    data class PaymentDetail (
//        @SerializedName("type") val type: String,
//        @SerializedName("timing") val timing: Long,
//        @SerializedName("amount") val amount: Int,
//        @SerializedName("currency") val currency: String = "USD",
//        @SerializedName("name") val name: String? = "",
//        @SerializedName("merchant") val merchant: String? = null,
//        @SerializedName("service_fee") val service_fee: String? = null,
//        @SerializedName("account_number") val account_number: String? = null,
//        @SerializedName("account_type") val account_type: String? = null,
//        @SerializedName("bank_code") val bank_code: String? = null,
//        @SerializedName("number") val number: String? = null,
//        @SerializedName("security_code") val security_code: String? = null,
//        @SerializedName("expiration_year") val expiration_year: String? = null,
//        @SerializedName("expiration_month") val expiration_month: String? = null,
//        @SerializedName("address") val address: Address? = null,
//        @SerializedName("fee_mode") var fee_mode: String? = FeeMode.MERCHANT_FEE,
//        @SerializedName("payor_info") var payorInfo: PayorInfo? = null,
//        @SerializedName("buyer") val buyer: String? = null,
//        @SerializedName("buyer_contact") val buyerContact: String? = null,
//        @SerializedName("sessionKey") var sessionKey: String? = null,
//        @SerializedName("wallet_type") val walletType: String? = null,
//        @SerializedName("digital_wallet_payload") val digitalWalletPayload: String? = null
//    )
}