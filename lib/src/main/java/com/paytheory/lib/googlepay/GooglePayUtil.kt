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
 * Utility class for Google Pay operations
 */
class GooglePayUtil private constructor(
    private val googlePayClient: GooglePayClientInterface
) : GooglePayUtilInterface {
    
    companion object {
        @Volatile
        private var instance: GooglePayUtil? = null
        
        /**
         * Get instance of GooglePayUtil with default implementation of GooglePayClient
         */
        fun getInstance(): GooglePayUtil {
            return instance ?: synchronized(this) {
                instance ?: GooglePayUtil(GooglePayClient()).also { instance = it }
            }
        }
        
        /**
         * Get instance of GooglePayUtil with custom implementation of GooglePayClient
         * This is primarily used for testing to inject mock clients
         */
        fun getInstance(client: GooglePayClientInterface): GooglePayUtil {
            return synchronized(this) {
                GooglePayUtil(client).also { instance = it }
            }
        }
        
        /**
         * Reset the singleton instance (useful for testing)
         */
        fun resetInstance() {
            instance = null
        }
    }
    
    /**
     * Checks if Google Pay is available on the device
     * 
     * @param activity Host activity
     * @param environment Google Pay environment (TEST or PRODUCTION)
     * @param billingAddressRequired Whether billing address is required
     * @return Task<Boolean> with Google Pay availability result
     */
    override fun isGooglePayAvailable(
        activity: Activity,
        environment: GooglePayEnvironment,
        billingAddressRequired: Boolean
    ): Task<Boolean> {
        return googlePayClient.isReadyToPay(
            activity,
            environment,
            billingAddressRequired,
            GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS,
            GooglePayConstants.DEFAULT_SUPPORTED_METHODS
        )
    }
    
    /**
     * Initiates Google Pay payment flow
     * 
     * @param activity Host activity
     * @param amount Payment amount (in dollars)
     * @param merchantName Merchant name to display in payment sheet
     * @param environment Google Pay environment (TEST or PRODUCTION)
     * @param billingAddressRequired Whether billing address is required
     * @param billingAddressFormat Format of billing address (MINIMAL or FULL)
     * @param shippingAddressRequired Whether shipping address is required
     * @param phoneNumberRequired Whether phone number is required
     * @param allowPrepaidCards Whether prepaid cards are allowed
     * @param allowCreditCards Whether credit cards are allowed
     * @return Task<PaymentData> Google Pay payment result
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
        allowCreditCards: Boolean
    ): Task<PaymentData> {
        try {
            val priceString = amount.toString()
            
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
                allowedCardNetworks = GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS,
                allowedAuthMethods = GooglePayConstants.DEFAULT_SUPPORTED_METHODS
            )
            
            return googlePayClient.loadPaymentData(activity, paymentDataRequestJson)
        } catch (e: Exception) {
            Timber.e(e, "Error requesting Google Pay payment")
            throw e
        }
    }
    
    /**
     * Extracts payment token from Google Pay response
     * 
     * @param paymentData PaymentData from Google Pay response
     * @return Token string for payment processing
     */
    override fun extractPaymentToken(paymentData: PaymentData): String {
        return googlePayClient.extractPaymentToken(paymentData)
    }
    
    /**
     * Gets the allowed payment methods JSON array for Google Pay
     * This is needed for the Google Pay button component
     * 
     * @return JSONArray containing the allowed payment methods
     */
    override fun getAllowedPaymentMethodsJson(): JSONArray {
        val cardNetworks = JSONArray()
        GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS.forEach { cardNetworks.put(it) }
        
        val authMethods = JSONArray()
        GooglePayConstants.DEFAULT_SUPPORTED_METHODS.forEach { authMethods.put(it) }
        
        val baseMethod = JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", authMethods)
                put("allowedCardNetworks", cardNetworks)
            })
        }
        
        return JSONArray().put(baseMethod)
    }
} 