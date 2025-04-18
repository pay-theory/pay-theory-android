package com.paytheory.lib.googlepay

import android.app.Activity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayConstants
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import timber.log.Timber

/**
 * A utility class providing simplified access to common Google Pay operations.
 *
 * This class acts as a facade over the [GooglePayClientInterface], offering higher-level methods
 * for checking Google Pay readiness, initiating the payment flow, and extracting results.
 * It manages a singleton instance to ensure consistent configuration and potentially share
 * the underlying [GooglePayClientInterface] instance.
 *
 * The singleton instance can be accessed via [getInstance]. For testing purposes, a custom
 * [GooglePayClientInterface] (like a mock) can be injected using [getInstance(client)],
 * and the instance can be reset via [resetInstance].
 *
 * @property googlePayClient The underlying [GooglePayClientInterface] used for actual API interactions.
 */
class GooglePayUtil private constructor(
    private val googlePayClient: GooglePayClientInterface
) : GooglePayUtilInterface {
    
    companion object {
        @Volatile // Ensures visibility of instance changes across threads
        private var instance: GooglePayUtil? = null
        
        /**
         * Gets the singleton instance of [GooglePayUtil].
         *
         * If an instance doesn't exist, it creates one using the default [GooglePayClient].
         * This method is thread-safe.
         *
         * @return The singleton [GooglePayUtil] instance.
         */
        fun getInstance(): GooglePayUtil {
            return instance ?: synchronized(this) {
                // Double-checked locking for thread safety
                instance ?: GooglePayUtil(GooglePayClient()).also { instance = it }
            }
        }
        
        /**
         * Gets the singleton instance of [GooglePayUtil], potentially replacing it with one
         * configured with a custom [GooglePayClientInterface].
         *
         * ** Primarily intended for testing:** Allows injecting mock or fake clients.
         * Subsequent calls to [getInstance] (without arguments) will return this test-configured instance
         * until [resetInstance] is called.
         *
         * @param client The [GooglePayClientInterface] implementation to use (e.g., a mock).
         * @return The singleton [GooglePayUtil] instance, configured with the provided client.
         */
        fun getInstance(client: GooglePayClientInterface): GooglePayUtil {
            // Synchronized to ensure atomic replacement of the instance
            return synchronized(this) {
                GooglePayUtil(client).also { instance = it }
            }
        }
        
        /**
         * **For Testing Only:** Resets the singleton instance to `null`.
         *
         * This ensures that the next call to [getInstance] will create a fresh instance
         * with the default [GooglePayClient], cleaning up any test configurations.
         * Should be called in test teardown (`@After`) if [getInstance(client)] was used.
         */
        fun resetInstance() {
            synchronized(this) { // Ensure thread-safe reset
                instance = null
            }
        }
    }
    
    /**
     * Checks if the user's device is ready to make Google Pay payments.
     *
     * Delegates the call to [GooglePayClientInterface.isReadyToPay], using default
     * supported networks and methods defined in [GooglePayConstants].
     *
     * @param activity The host [Activity].
     * @param environment The target Google Pay environment ([GooglePayEnvironment.TEST] or [GooglePayEnvironment.PRODUCTION]).
     * @param billingAddressRequired Whether the payment configuration requires a billing address.
     * @param allowedCardNetworks The list of card networks allowed by the configuration.
     * @param allowedAuthMethods The list of authentication methods allowed by the configuration.
     * @return A [Task<Boolean>] indicating readiness. Handle the result asynchronously.
     */
    override fun isGooglePayAvailable(
        activity: Activity,
        environment: GooglePayEnvironment,
        billingAddressRequired: Boolean,
        allowedCardNetworks: List<String>, // Added params to match interface/usage
        allowedAuthMethods: List<String>   // Added params to match interface/usage
    ): Task<Boolean> {
        Timber.d("isGooglePayAvailable called with env: $environment, billingRequired: $billingAddressRequired")
        return googlePayClient.isReadyToPay(
            activity,
            environment,
            billingAddressRequired,
            allowedCardNetworks, // Use passed-in value
            allowedAuthMethods   // Use passed-in value
        )
    }
    
    /**
     * Initiates the Google Pay payment flow by requesting payment data from the Google Pay API.
     *
     * This involves:
     * 1. Creating the JSON payment data request using [GooglePayClientInterface.createPaymentDataRequest].
     * 2. Calling [GooglePayClientInterface.loadPaymentData] to display the Google Pay sheet.
     *
     * @param activity The host [Activity] required to launch the Google Pay sheet.
     * @param amount The transaction amount as a [BigDecimal].
     * @param merchantName The merchant name configured in [PayTheoryConfiguration].
     * @param environment The target Google Pay environment.
     * @param billingAddressRequired Whether billing address is required by the configuration.
     * @param billingAddressFormat The required format for the billing address.
     * @param shippingAddressRequired Whether shipping address is required by the configuration.
     * @param phoneNumberRequired Whether phone number is required by the configuration.
     * @param allowPrepaidCards Whether prepaid cards are allowed by the configuration.
     * @param allowCreditCards Whether credit cards are allowed by the configuration.
     * @param allowedCardNetworks The list of card networks allowed by the configuration.
     * @param allowedAuthMethods The list of authentication methods allowed by the configuration.
     * @return A [Task<PaymentData>] that will resolve with the payment data on success, or fail with an exception
     *         (potentially a [com.google.android.gms.common.api.ResolvableApiException] handled by the caller).
     * @throws Exception Rethrows exceptions occurring during request creation or loading.
     */
    override fun requestGooglePayment(
        activity: Activity,
        amount: BigDecimal,
        merchantName: String,
        environment: GooglePayEnvironment,
        billingAddressRequired: Boolean,
        billingAddressFormat: GooglePayBillingAddressFormat,
        shippingAddressRequired: Boolean,
        phoneNumberRequired: Boolean,
        allowPrepaidCards: Boolean,
        allowCreditCards: Boolean,
        allowedCardNetworks: List<String>, // Added params to match interface/usage
        allowedAuthMethods: List<String>   // Added params to match interface/usage
    ): Task<PaymentData> {
        try {
            // Ensure amount is formatted correctly for Google Pay API (string with two decimal places)
            val priceString = String.format("%.2f", amount) // Use String.format for reliable formatting
            Timber.d("Requesting Google Pay with amount: $priceString")

            val paymentDataRequestJson = googlePayClient.createPaymentDataRequest(
                price = priceString,
                merchantName = merchantName,
                billingAddressRequired = billingAddressRequired,
                billingAddressFormat = billingAddressFormat,
                shippingAddressRequired = shippingAddressRequired,
                phoneNumberRequired = phoneNumberRequired,
                environment = environment,
                allowPrepaidCards = allowPrepaidCards,
                allowCreditCards = allowCreditCards,
                allowedCardNetworks = allowedCardNetworks, // Use passed-in value
                allowedAuthMethods = allowedAuthMethods   // Use passed-in value
            )

            Timber.v("PaymentDataRequest JSON: $paymentDataRequestJson") // Verbose log for request details
            // Pass the environment to loadPaymentData
            return googlePayClient.loadPaymentData(activity, environment, paymentDataRequestJson)
        } catch (e: Exception) {
            Timber.e(e, "Error requesting Google Pay payment")
            // Rethrow to allow the caller (GooglePayProcessor) to handle it
            throw e
        }
    }
    
    /**
     * Extracts the encrypted payment token from the [PaymentData] object returned by Google Pay.
     *
     * Simply delegates the call to [GooglePayClientInterface.extractPaymentToken].
     *
     * @param paymentData The [PaymentData] object received from a successful Google Pay transaction.
     * @return The extracted payment token string.
     * @throws RuntimeException or [IllegalArgumentException] if extraction fails (propagated from client).
     */
    override fun extractPaymentToken(paymentData: PaymentData): String {
        Timber.d("Extracting payment token...")
        return googlePayClient.extractPaymentToken(paymentData)
    }
    
    /**
     * Generates a JSON array describing the allowed payment methods (specifically CARD),
     * suitable for configuring the Google Pay Button view.
     *
     * This uses the default supported networks and auth methods defined in [GooglePayConstants].
     * It constructs the basic structure required by the Google Pay Button's `allowedPaymentMethods` property.
     *
     * @return A [JSONArray] containing a single CARD payment method object with default parameters.
     */
    override fun getAllowedPaymentMethodsJson(): JSONArray {
        // Reuses logic similar to baseCardPaymentMethod in GooglePayClient, but simplified for button config.
        val cardNetworks = JSONArray(GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS)
        val authMethods = JSONArray(GooglePayConstants.DEFAULT_SUPPORTED_METHODS)

        val cardPaymentMethod = JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", authMethods)
                put("allowedCardNetworks", cardNetworks)
                // Billing address requirement details are not typically needed for the button itself.
            })
            // Tokenization spec is not needed for the button config.
        }

        return JSONArray().put(cardPaymentMethod)
    }
} 