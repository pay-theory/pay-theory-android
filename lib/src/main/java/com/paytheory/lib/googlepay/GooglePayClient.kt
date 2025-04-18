package com.paytheory.lib.googlepay

import android.app.Activity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayConstants
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

/**
 * A client class responsible for direct interaction with the Google Pay API via Google's [PaymentsClient].
 *
 * This class encapsulates the logic for:
 * - Creating and configuring the [PaymentsClient] for the specified environment (Test/Production).
 * - Building JSON request objects required by the Google Pay API (e.g., for checking readiness, loading payment data).
 * - Calling the core Google Pay API methods: [PaymentsClient.isReadyToPay] and [PaymentsClient.loadPaymentData].
 * - Extracting the crucial payment token from the successful [PaymentData] response.
 *
 * It acts as a lower-level interface to Google Pay, used internally by components like [GooglePayUtil] and [GooglePayProcessor].
 */
class GooglePayClient : GooglePayClientInterface {

    /**
     * Creates and configures an instance of Google's [PaymentsClient].
     *
     * The [PaymentsClient] is the primary entry point for interacting with the Google Pay API.
     * This method sets the appropriate environment (Test or Production) based on the provided configuration.
     *
     * @param activity The host [Activity] required by the Google Pay SDK.
     * @param environment The target Google Pay environment ([GooglePayEnvironment.TEST] or [GooglePayEnvironment.PRODUCTION]).
     * @return A configured [PaymentsClient] instance ready for use.
     */
    override fun createPaymentsClient(
        activity: Activity,
        environment: GooglePayEnvironment
    ): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(getEnvironmentConstant(environment))
            .build()
        
        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    /**
     * Converts the internal [GooglePayEnvironment] enum to the corresponding [WalletConstants] integer value.
     *
     * @param environment The [GooglePayEnvironment] enum value.
     * @return The matching integer constant defined in [WalletConstants] (e.g., [WalletConstants.ENVIRONMENT_TEST]).
     */
    private fun getEnvironmentConstant(environment: GooglePayEnvironment): Int {
        return when (environment) {
            GooglePayEnvironment.TEST -> WalletConstants.ENVIRONMENT_TEST
            GooglePayEnvironment.PRODUCTION -> WalletConstants.ENVIRONMENT_PRODUCTION
        }
    }

    /**
     * Checks if the current device and user are ready to make payments via Google Pay.
     *
     * This involves verifying Google Play Services availability, checking if the user has an eligible
     * payment method registered, and ensuring the configuration (allowed networks, auth methods) is supported.
     *
     * @param activity The host [Activity].
     * @param environment The target Google Pay environment.
     * @param billingAddressRequired Whether the payment configuration requires a billing address.
     * @param allowedCardNetworks A list of card networks (e.g., "VISA", "MASTERCARD") configured in [PayTheoryConfiguration].
     * @param allowedAuthMethods A list of authentication methods (e.g., "CRYPTOGRAM_3DS") configured in [PayTheoryConfiguration].
     * @return A [Task<Boolean>] that asynchronously resolves to `true` if the user is ready to pay, `false` otherwise. Errors during the check also result in `false`.
     */
    override fun isReadyToPay(
        activity: Activity,
        environment: GooglePayEnvironment,
        billingAddressRequired: Boolean,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): Task<Boolean> {
        val taskCompletionSource = TaskCompletionSource<Boolean>()
        
        try {
            val isReadyToPayJson = JSONObject().apply {
                put("apiVersion", GooglePayConstants.API_VERSION)
                put("apiVersionMinor", GooglePayConstants.API_VERSION_MINOR)
                put("allowedPaymentMethods", JSONArray().put(
                    baseCardPaymentMethod(
                        billingAddressRequired,
                        allowedCardNetworks,
                        allowedAuthMethods
                    )
                ))
            }
            
            val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
            val paymentsClient = createPaymentsClient(activity, environment)
            
            paymentsClient.isReadyToPay(request)
                .addOnCompleteListener { task ->
                    Timber.d("isReadyToPay addOnCompleteListener invoked. Success: ${task.isSuccessful}")
                    if (task.isSuccessful) {
                        taskCompletionSource.setResult(task.result ?: false)
                    } else {
                        val exception = task.exception
                        if (exception is ApiException) {
                             Timber.e(exception, "isReadyToPay failed with status: ${exception.status}")
                        } else {
                             Timber.e(exception, "isReadyToPay failed")
                        }
                        taskCompletionSource.setResult(false)
                    }
                }
        } catch (exception: JSONException) {
            Timber.e(exception, "Failed to create isReadyToPay JSON request")
            taskCompletionSource.setResult(false)
        } catch (exception: Exception) {
            Timber.e(exception, "Unexpected error during isReadyToPay setup")
            taskCompletionSource.setResult(false)
        }
        
        return taskCompletionSource.task
    }

    /**
     * Constructs the JSON string representation of a [PaymentDataRequest] required by Google Pay's `loadPaymentData` method.
     *
     * This JSON object defines the transaction details (amount, currency), allowed payment methods,
     * merchant information, and requirements for billing/shipping addresses and phone numbers, based
     * on the provided [PayTheoryConfiguration] parameters.
     *
     * @param price The transaction amount as a string (e.g., "10.00").
     * @param merchantName The merchant name to display in the Google Pay sheet.
     * @param billingAddressRequired Whether billing address is required.
     * @param billingAddressFormat The required format ([GooglePayBillingAddressFormat.MINIMAL] or [GooglePayBillingAddressFormat.FULL]).
     * @param shippingAddressRequired Whether shipping address is required.
     * @param phoneNumberRequired Whether phone number is required (if shipping address is required).
     * @param environment The target Google Pay environment (unused in this specific JSON structure, but kept for consistency).
     * @param allowPrepaidCards Whether prepaid cards are allowed.
     * @param allowCreditCards Whether credit cards are allowed.
     * @param allowedCardNetworks A list of allowed card networks.
     * @param allowedAuthMethods A list of allowed authentication methods.
     * @return A JSON [String] representing the [PaymentDataRequest], or "{}" if a JSONException occurs.
     */
    override fun createPaymentDataRequest(
        price: String,
        merchantName: String,
        billingAddressRequired: Boolean,
        billingAddressFormat: GooglePayBillingAddressFormat,
        shippingAddressRequired: Boolean,
        phoneNumberRequired: Boolean,
        environment: GooglePayEnvironment,
        allowPrepaidCards: Boolean,
        allowCreditCards: Boolean,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): String {
        return try {
            JSONObject().apply {
                put("apiVersion", GooglePayConstants.API_VERSION)
                put("apiVersionMinor", GooglePayConstants.API_VERSION_MINOR)
                put("allowedPaymentMethods", JSONArray().put(
                    cardPaymentMethod(
                        billingAddressRequired,
                        billingAddressFormat,
                        allowedCardNetworks,
                        allowedAuthMethods,
                        allowPrepaidCards,
                        allowCreditCards
                    )
                ))
                put("transactionInfo", JSONObject().apply {
                    put("totalPrice", price)
                    put("totalPriceStatus", GooglePayConstants.TOTAL_PRICE_STATUS_FINAL)
                    put("currencyCode", GooglePayConstants.CURRENCY_CODE)
                    put("countryCode", GooglePayConstants.COUNTRY_CODE)
                })
                put("merchantInfo", JSONObject().apply {
                    put("merchantName", merchantName.ifBlank { "Default Merchant" })
                })
                if (shippingAddressRequired) {
                    put("shippingAddressRequired", true)
                    put("shippingAddressParameters", JSONObject().apply {
                        put("phoneNumberRequired", phoneNumberRequired)
                    })
                }
            }.toString()
        } catch (e: JSONException) {
            Timber.e(e, "Failed to create PaymentDataRequest JSON")
            "{}"
        }
    }

    /**
     * Initiates the Google Pay payment sheet to collect payment information from the user.
     *
     * This method takes the JSON request string generated by [createPaymentDataRequest] and passes
     * it to the [PaymentsClient.loadPaymentData] method, which displays the Google Pay UI.
     *
     * @param activity The host [Activity] required to launch the Google Pay sheet.
     * @param environment The target Google Pay environment to create the client for.
     * @param paymentDataRequestJson The JSON [String] generated by [createPaymentDataRequest].
     * @return A [Task<PaymentData>] that asynchronously resolves with the user's selected [PaymentData] on success, or fails if the user cancels or an error occurs.
     */
    override fun loadPaymentData(
        activity: Activity,
        environment: GooglePayEnvironment,
        paymentDataRequestJson: String
    ): Task<PaymentData> {
        val paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson)
        val paymentsClient = createPaymentsClient(activity, environment)
        return paymentsClient.loadPaymentData(paymentDataRequest)
    }

    /**
     * Extracts the encrypted Google Pay payment token from the [PaymentData] result.
     *
     * This token is the essential piece of information needed by the Pay Theory backend
     * to process the transaction via the appropriate gateway.
     *
     * @param paymentData The [PaymentData] object returned by a successful `loadPaymentData` call.
     * @return The base64 encoded payment token string.
     * @throws IllegalArgumentException If the input [paymentData] or its JSON representation is null.
     * @throws RuntimeException If the JSON structure is unexpected, or the token itself is missing or blank, indicating an error in the Google Pay response or parsing logic.
     */
    override fun extractPaymentToken(paymentData: PaymentData): String {
        val paymentDataJsonString = paymentData.toJson() ?: run {
            Timber.e("PaymentData.toJson() returned null")
            throw IllegalArgumentException("PaymentData JSON is null")
        }
        Timber.d("PaymentData JSON: $paymentDataJsonString")

        try {
            val paymentDataJson = JSONObject(paymentDataJsonString)
            val paymentMethodData = paymentDataJson.getJSONObject("paymentMethodData")

            if (!paymentMethodData.has("tokenizationData")) {
                 throw RuntimeException("Missing 'tokenizationData' in paymentMethodData")
            }
            val tokenizationData = paymentMethodData.getJSONObject("tokenizationData")

            if (!tokenizationData.has("token")) {
                throw RuntimeException("Missing 'token' in tokenizationData")
            }
            val token = tokenizationData.getString("token")

            if (token.isBlank()) {
                throw RuntimeException("Extracted payment token is blank")
            }
            return token
        } catch (e: JSONException) {
            Timber.e(e, "Error parsing payment data JSON")
            throw RuntimeException("Failed to extract payment token from PaymentData due to JSON parsing error", e)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error extracting payment token")
            throw RuntimeException("Failed to extract payment token from PaymentData", e)
        }
    }

    /**
     * Constructs the base JSON structure for a CARD payment method, used in both `isReadyToPay` and `loadPaymentData` requests.
     *
     * Defines the payment method type, allowed authentication methods, allowed card networks,
     * and basic billing address requirements.
     *
     * @param billingAddressRequired Whether billing address is required.
     * @param allowedCardNetworks List of allowed card networks (e.g., ["VISA", "MASTERCARD"]).
     * @param allowedAuthMethods List of allowed authentication methods (e.g., ["PAN_ONLY", "CRYPTOGRAM_3DS"]).
     * @return A [JSONObject] representing the base card payment method.
     * @throws JSONException If there's an error constructing the JSON objects.
     */
    @Throws(JSONException::class)
    private fun baseCardPaymentMethod(
        billingAddressRequired: Boolean = false,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): JSONObject {
        val cardNetworksJson = JSONArray(allowedCardNetworks)
        val authMethodsJson = JSONArray(allowedAuthMethods)
        
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", authMethodsJson)
                put("allowedCardNetworks", cardNetworksJson)
                put("billingAddressRequired", billingAddressRequired)
                
                if (billingAddressRequired) {
                    put("billingAddressParameters", JSONObject().apply {
                        put("format", GooglePayConstants.BILLING_ADDRESS_FORMAT_MIN)
                    })
                }
            })
        }
    }

    /**
     * Constructs the complete JSON structure for a CARD payment method, including tokenization parameters,
     * intended for the `loadPaymentData` request.
     *
     * Builds upon the [baseCardPaymentMethod] and adds:
     * - Detailed billing address format.
     * - Restrictions on prepaid/credit cards.
     * - The required `tokenizationSpecification` for Pay Theory's gateway.
     *
     * @param billingAddressRequired Whether billing address is required.
     * @param billingAddressFormat The required format ([GooglePayBillingAddressFormat.MINIMAL] or [GooglePayBillingAddressFormat.FULL]).
     * @param allowedCardNetworks List of allowed card networks.
     * @param allowedAuthMethods List of allowed authentication methods.
     * @param allowPrepaidCards Whether prepaid cards are permitted.
     * @param allowCreditCards Whether credit cards are permitted.
     * @return A [JSONObject] representing the detailed card payment method with tokenization.
     * @throws JSONException If there's an error constructing the JSON objects.
     */
    @Throws(JSONException::class)
    private fun cardPaymentMethod(
        billingAddressRequired: Boolean = false,
        billingAddressFormat: GooglePayBillingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>,
        allowPrepaidCards: Boolean = true,
        allowCreditCards: Boolean = true
    ): JSONObject {
        val paymentMethod = baseCardPaymentMethod(billingAddressRequired, allowedCardNetworks, allowedAuthMethods)
        val parameters = paymentMethod.getJSONObject("parameters")
        
        if (billingAddressRequired) {
            val formatString = when (billingAddressFormat) {
                GooglePayBillingAddressFormat.MINIMAL -> GooglePayConstants.BILLING_ADDRESS_FORMAT_MIN
                GooglePayBillingAddressFormat.FULL -> GooglePayConstants.BILLING_ADDRESS_FORMAT_FULL
            }
            
            if (!parameters.has("billingAddressParameters")) {
                 parameters.put("billingAddressParameters", JSONObject())
            }
            parameters.getJSONObject("billingAddressParameters").put("format", formatString)
        }
        
        parameters.put("allowPrepaidCards", allowPrepaidCards)
        parameters.put("allowCreditCards", allowCreditCards)
        
        paymentMethod.put("tokenizationSpecification", JSONObject().apply {
            put("type", GooglePayConstants.TOKENIZATION_TYPE_PAYMENT_GATEWAY)
            put("parameters", JSONObject().apply {
                put("gateway", GooglePayConstants.GATEWAY_NAME)
                put("gatewayMerchantId", GooglePayConstants.GATEWAY_MERCHANT_ID)
            })
        })
        
        return paymentMethod
    }
} 