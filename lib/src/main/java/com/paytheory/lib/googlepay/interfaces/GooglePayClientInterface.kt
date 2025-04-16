package com.paytheory.lib.googlepay.interfaces

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment

/**
 * Interface for Google Pay client operations
 * This interface enables easier testing by allowing mock implementations
 */
interface GooglePayClientInterface {
    /**
     * Creates a PaymentsClient with the appropriate environment setting
     *
     * @param activity The activity that will host the Google Pay flow
     * @param environment The environment setting (TEST or PRODUCTION)
     * @return A configured PaymentsClient instance
     */
    fun createPaymentsClient(
        activity: Activity,
        environment: GooglePayEnvironment = GooglePayEnvironment.TEST
    ): PaymentsClient
    
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
    fun isReadyToPay(
        activity: Activity,
        environment: GooglePayEnvironment = GooglePayEnvironment.TEST,
        billingAddressRequired: Boolean = false,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): Task<Boolean>
    
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
    fun createPaymentDataRequest(
        price: String,
        merchantName: String,
        billingAddressRequired: Boolean = false,
        billingAddressFormat: GooglePayBillingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
        shippingAddressRequired: Boolean = false,
        phoneNumberRequired: Boolean = false,
        environment: GooglePayEnvironment = GooglePayEnvironment.TEST,
        allowPrepaidCards: Boolean = true,
        allowCreditCards: Boolean = true,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): String
    
    /**
     * Load the payment data to show the Google Pay payment sheet
     *
     * @param activity The activity that will host the Google Pay payment sheet
     * @param paymentDataRequestJson The payment data request JSON string
     * @return Task with PaymentData result
     */
    fun loadPaymentData(
        activity: Activity,
        paymentDataRequestJson: String
    ): Task<PaymentData>
    
    /**
     * Extracts the payment token from the Google Pay response
     *
     * @param paymentData The PaymentData returned from the Google Pay payment sheet
     * @return The payment token as a string
     */
    fun extractPaymentToken(paymentData: PaymentData): String
} 