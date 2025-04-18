package com.paytheory.lib.googlepay.interfaces

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import org.json.JSONArray
import java.math.BigDecimal

/**
 * Defines the contract for a utility class that simplifies common Google Pay operations.
 *
 * This interface provides a higher-level abstraction over the potentially more complex interactions
 * handled by [GooglePayClientInterface]. It offers convenience methods for checking readiness,
 * initiating the payment flow with configuration parameters, extracting the payment token from the result,
 * and generating configuration needed for UI components like the Google Pay button.
 *
 * Implementations (like [GooglePayUtil]) typically delegate the core API calls to an instance
 * of [GooglePayClientInterface]. This interface is useful for dependency injection and testing,
 * allowing mock utilities to be provided (e.g., via [GooglePayFactory]).
 */
interface GooglePayUtilInterface {
    /**
     * Checks if the user's device is ready to make Google Pay payments with the specified configuration.
     *
     * @param activity The host [Activity].
     * @param environment The target Google Pay environment. Defaults to TEST.
     * @param billingAddressRequired Whether billing address is required. Defaults to false.
     * @param allowedCardNetworks A list of allowed card networks for the transaction.
     * @param allowedAuthMethods A list of allowed authentication methods for the transaction.
     * @return A [Task<Boolean>] indicating readiness. Handle the result asynchronously.
     */
    fun isGooglePayAvailable(
        activity: Activity,
        environment: GooglePayEnvironment = GooglePayEnvironment.TEST,
        billingAddressRequired: Boolean = false,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): Task<Boolean>
    
    /**
     * Initiates the Google Pay payment flow by requesting payment data from the Google Pay API.
     *
     * @param activity The host [Activity] required to launch the Google Pay sheet.
     * @param amount The transaction amount as a [BigDecimal].
     * @param merchantName The merchant name to display in the payment sheet.
     * @param environment The target Google Pay environment. Defaults to TEST.
     * @param billingAddressRequired Whether billing address is required. Defaults to false.
     * @param billingAddressFormat The required format for the billing address if required. Defaults to MINIMAL.
     * @param shippingAddressRequired Whether shipping address is required. Defaults to false.
     * @param phoneNumberRequired Whether phone number is required (usually linked to shipping). Defaults to false.
     * @param allowPrepaidCards Whether prepaid cards are allowed. Defaults to true.
     * @param allowCreditCards Whether credit cards are allowed. Defaults to true.
     * @param allowedCardNetworks A list of allowed card networks for the transaction.
     * @param allowedAuthMethods A list of allowed authentication methods for the transaction.
     * @return A [Task<PaymentData>] that will resolve with the payment data on success, or fail with an exception.
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
        allowCreditCards: Boolean = true,
        allowedCardNetworks: List<String>,
        allowedAuthMethods: List<String>
    ): Task<PaymentData>
    
    /**
     * Extracts the encrypted payment token from the [PaymentData] object returned by Google Pay.
     *
     * @param paymentData The [PaymentData] object received from a successful Google Pay transaction.
     * @return The extracted payment token string.
     * @throws RuntimeException or [IllegalArgumentException] if extraction fails.
     */
    fun extractPaymentToken(paymentData: PaymentData): String
    
    /**
     * Generates a JSON array describing the allowed payment methods (specifically CARD),
     * suitable for configuring the Google Pay Button view based on default SDK settings.
     *
     * @return A [JSONArray] containing a CARD payment method object with default network/auth parameters.
     */
    fun getAllowedPaymentMethodsJson(): JSONArray
} 