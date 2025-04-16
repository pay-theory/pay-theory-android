package com.paytheory.lib.googlepay.interfaces

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import org.json.JSONArray
import java.math.BigDecimal

/**
 * Interface for Google Pay utility operations
 * This interface enables easier testing by allowing mock implementations
 */
interface GooglePayUtilInterface {
    /**
     * Checks if Google Pay is available on the device
     * 
     * @param activity Host activity
     * @param environment Google Pay environment (TEST or PRODUCTION)
     * @param billingAddressRequired Whether billing address is required
     * @return Task<Boolean> with Google Pay availability result
     */
    fun isGooglePayAvailable(
        activity: Activity,
        environment: GooglePayEnvironment = GooglePayEnvironment.TEST,
        billingAddressRequired: Boolean = false
    ): Task<Boolean>
    
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
    ): Task<PaymentData>
    
    /**
     * Extracts payment token from Google Pay response
     * 
     * @param paymentData PaymentData from Google Pay response
     * @return Token string for payment processing
     */
    fun extractPaymentToken(paymentData: PaymentData): String
    
    /**
     * Gets the allowed payment methods JSON array for Google Pay
     * This is needed for the Google Pay button component
     * 
     * @return JSONArray containing the allowed payment methods
     */
    fun getAllowedPaymentMethodsJson(): JSONArray
} 