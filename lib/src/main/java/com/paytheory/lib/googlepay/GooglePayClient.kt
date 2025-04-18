package com.paytheory.lib.googlepay

import android.app.Activity
import android.util.Log
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
 * Handles interactions with the Google Pay API
 */
class GooglePayClient : GooglePayClientInterface {

    /**
     * Creates a PaymentsClient with the appropriate environment setting
     *
     * @param activity The activity that will host the Google Pay flow
     * @param environment The environment setting (TEST or PRODUCTION)
     * @return A configured PaymentsClient instance
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
     * Converts GooglePayEnvironment enum to WalletConstants environment value
     * 
     * @param environment The GooglePayEnvironment enum value
     * @return The corresponding WalletConstants environment value
     */
    private fun getEnvironmentConstant(environment: GooglePayEnvironment): Int {
        return when (environment) {
            GooglePayEnvironment.TEST -> WalletConstants.ENVIRONMENT_TEST
            GooglePayEnvironment.PRODUCTION -> WalletConstants.ENVIRONMENT_PRODUCTION
        }
    }

    /**
     * Determines if the user's device supports Google Pay and if they have a payment method available
     *
     * @param activity The activity that will host the Google Pay flow
     * @param environment The environment setting (TEST or PRODUCTION)
     * @param billingAddressRequired Whether to require a billing address
     * @param allowedCardNetworks List of allowed card networks
     * @param allowedAuthMethods List of allowed authentication methods
     * @return Task<Boolean> that will be updated with Google Pay availability
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
                put("apiVersion", 2)
                put("apiVersionMinor", 0)
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
                    Log.d("GPC_LISTENER_CALLED", "isReadyToPay addOnCompleteListener invoked. Success: ${task.isSuccessful}")
                    if (task.isSuccessful) {
                        taskCompletionSource.setResult(task.result)
                    } else {
                        Log.e("GPC_ISREADY_FAIL", "isReadyToPay task failed", task.exception)
                        Timber.e(task.exception, "isReadyToPay failed")
                        taskCompletionSource.setResult(false)
                    }
                }
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to create isReadyToPay request")
            Log.e("GooglePayClient", "Failed to create isReadyToPay request", exception)
            taskCompletionSource.setResult(false)
        }
        
        return taskCompletionSource.task
    }

    /**
     * Creates a payment data request to show the Google Pay payment sheet
     *
     * @param price The transaction amount (string)
     * @param merchantName The merchant name to display
     * @param billingAddressRequired Whether to require a billing address
     * @param billingAddressFormat Format of the billing address (MINIMAL or FULL)
     * @param shippingAddressRequired Whether to require a shipping address
     * @param phoneNumberRequired Whether to require a phone number
     * @param environment The environment setting (TEST or PRODUCTION)
     * @param allowPrepaidCards Whether to allow prepaid cards
     * @param allowCreditCards Whether to allow credit cards
     * @param allowedCardNetworks List of allowed card networks
     * @param allowedAuthMethods List of allowed authentication methods
     * @return A JSON string containing the payment data request
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
                put("apiVersion", 2)
                put("apiVersionMinor", 0)
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
                    put("totalPriceStatus", "FINAL")
                    put("currencyCode", GooglePayConstants.CURRENCY_CODE)
                    put("countryCode", GooglePayConstants.COUNTRY_CODE)
                })
                put("merchantInfo", JSONObject().apply {
                    put("merchantName", merchantName)
                })
                if (shippingAddressRequired) {
                    put("shippingAddressRequired", true)
                    put("shippingAddressParameters", JSONObject().apply {
                        put("phoneNumberRequired", phoneNumberRequired)
                    })
                }
            }.toString()
        } catch (e: JSONException) {
            Timber.e(e, "Failed to create PaymentDataRequest")
            "{}"
        }
    }

    /**
     * Load the payment data to show the Google Pay payment sheet
     *
     * @param activity The activity that will host the Google Pay payment sheet
     * @param paymentDataRequestJson The payment data request JSON string
     * @return Task with PaymentData result
     */
    override fun loadPaymentData(
        activity: Activity,
        paymentDataRequestJson: String
    ): Task<PaymentData> {
        val paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson)
        val paymentsClient = createPaymentsClient(activity)
        return paymentsClient.loadPaymentData(paymentDataRequest)
    }

    /**
     * Extracts the payment token from the Google Pay PaymentData object.
     *
     * @param paymentData The PaymentData object received from Google Pay.
     * @return The payment token string.
     * @throws IllegalArgumentException If the PaymentData JSON is null.
     * @throws RuntimeException If there's an error parsing the JSON or the token is missing.
     */
    override fun extractPaymentToken(paymentData: PaymentData): String {
        val paymentDataJsonString = paymentData.toJson() ?: throw IllegalArgumentException("PaymentData JSON is null")
        Log.d("GooglePayClient", "PaymentData JSON: $paymentDataJsonString") // Log for debugging

        try {
            val paymentDataJson = JSONObject(paymentDataJsonString)
            val paymentMethodData = paymentDataJson.getJSONObject("paymentMethodData")
            val tokenizationData = paymentMethodData.getJSONObject("tokenizationData")
            val token = tokenizationData.getString("token")
            if (token.isBlank()) {
                throw RuntimeException("Extracted payment token is blank")
            }
            return token
        } catch (e: JSONException) {
            Log.e("GooglePayClient", "Error parsing payment data JSON", e)
            throw RuntimeException("Failed to extract payment token from PaymentData due to JSON parsing error", e)
        } catch (e: Exception) { // Catch other potential errors
            Log.e("GooglePayClient", "Unexpected error extracting payment token", e)
            throw RuntimeException("Failed to extract payment token from PaymentData", e)
        }
    }

    /**
     * Creates a base card payment method for Google Pay API requests
     */
    private fun baseCardPaymentMethod(
        billingAddressRequired: Boolean = false,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): JSONObject {
        val cardNetworks = JSONArray()
        allowedCardNetworks.forEach { cardNetworks.put(it) }
        
        val authMethods = JSONArray()
        allowedAuthMethods.forEach { authMethods.put(it) }
        
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", authMethods)
                put("allowedCardNetworks", cardNetworks)
                put("billingAddressRequired", billingAddressRequired)
                
                if (billingAddressRequired) {
                    put("billingAddressParameters", JSONObject().apply {
                        put("format", "MIN")
                    })
                }
            })
        }
    }

    /**
     * Creates a complete card payment method including tokenization
     */
    private fun cardPaymentMethod(
        billingAddressRequired: Boolean = false,
        billingAddressFormat: GooglePayBillingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>,
        allowPrepaidCards: Boolean = true,
        allowCreditCards: Boolean = true
    ): JSONObject {
        val baseMethod = baseCardPaymentMethod(billingAddressRequired, allowedCardNetworks, allowedAuthMethods)
        val parameters = baseMethod.getJSONObject("parameters")
        
        // Update billing address parameters if required
        if (billingAddressRequired) {
            val format = when (billingAddressFormat) {
                GooglePayBillingAddressFormat.MINIMAL -> "MIN"
                GooglePayBillingAddressFormat.FULL -> "FULL"
            }
            
            parameters.put("billingAddressParameters", JSONObject().apply {
                put("format", format)
            })
        }
        
        // Add card restrictions
        parameters.put("allowPrepaidCards", allowPrepaidCards)
        parameters.put("allowCreditCards", allowCreditCards)
        
        // Add tokenization specification
        baseMethod.put("tokenizationSpecification", JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject().apply {
                put("gateway", GooglePayConstants.GATEWAY_NAME)
                put("gatewayMerchantId", GooglePayConstants.GATEWAY_MERCHANT_ID)
            })
        })
        
        return baseMethod
    }
} 