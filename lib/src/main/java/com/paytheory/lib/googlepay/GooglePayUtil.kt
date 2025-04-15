package com.paytheory.lib.googlepay

import android.app.Activity
import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayConstants
import com.paytheory.lib.configuration.GooglePayEnvironment
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal

/**
 * Utility class for Google Pay operations
 */
object GooglePayUtil {
    private val googlePayClient = GooglePayClient()
    
    /**
     * Checks if Google Pay is available on the device
     * 
     * @param activity Host activity
     * @param environment Google Pay environment (TEST or PRODUCTION)
     * @param billingAddressRequired Whether billing address is required
     * @return LiveData<Boolean> with Google Pay availability result
     */
    fun isGooglePayAvailable(
        activity: Activity,
        environment: GooglePayEnvironment = GooglePayEnvironment.TEST,
        billingAddressRequired: Boolean = false
    ): LiveData<Boolean> {
        return googlePayClient.isReadyToPay(
            activity,
            environment,
            billingAddressRequired
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
    fun requestGooglePayment(
        activity: Activity,
        amount: BigDecimal,
        merchantName: String,
        environment: GooglePayEnvironment = GooglePayEnvironment.TEST,
        billingAddressRequired: Boolean = false,
        billingAddressFormat: GooglePayBillingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
        shippingAddressRequired: Boolean = false,
        phoneNumberRequired: Boolean = false,
        allowPrepaidCards: Boolean = true,
        allowCreditCards: Boolean = true
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
                allowCreditCards = allowCreditCards
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
    fun extractPaymentToken(paymentData: PaymentData): String {
        return googlePayClient.extractPaymentToken(paymentData)
    }
    
    /**
     * Gets the allowed payment methods JSON array for Google Pay
     * This is needed for the Google Pay button component
     * 
     * @return JSONArray containing the allowed payment methods
     */
    fun getAllowedPaymentMethodsJson(): JSONArray {
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