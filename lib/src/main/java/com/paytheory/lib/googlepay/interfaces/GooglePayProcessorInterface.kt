package com.paytheory.lib.googlepay.interfaces

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.payable.SuccessfulTransactionResult

/**
 * Defines the contract for a processor component responsible for managing the Google Pay payment flow
 * within the Pay Theory SDK.
 *
 * This interface outlines the high-level operations needed to integrate Google Pay, such as checking
 * device readiness, initiating the payment process (which typically involves displaying the Google Pay sheet),
 * and constructing payment details from the Google Pay result.
 *
 * It also includes methods (`process`, `receiveMessage`, `establishViewModel`, `disconnect`) that appear
 * to be inherited from a more general payment processing interface (likely related to WebSocket communication
 * for other payment types). Implementations specifically for Google Pay (like [GooglePayProcessor])
 * might provide no-ops or specific logging for these inherited methods if they aren't directly used
 * in the Google Pay flow.
 *
 * Defining this interface allows for polymorphism and easier testing by enabling the injection of
 * mock processors (e.g., via [GooglePayFactory]).
 */
interface GooglePayProcessorInterface {
    /**
     * Checks if the user's device is set up and ready for Google Pay transactions based on the SDK's configuration.
     *
     * @return A [Task<Boolean>] that asynchronously resolves to `true` if Google Pay is available and ready, `false` otherwise.
     *         Handle the result asynchronously using listeners.
     */
    fun isGooglePayAvailable(): Task<Boolean>
    
    /**
     * Initiates the Google Pay payment process.
     *
     * This typically involves:
     * 1. Requesting payment data from Google using `loadPaymentData`.
     * 2. Handling the result, which might involve launching the Google Pay payment sheet via an [ActivityResultLauncher]
     *    (if a [com.google.android.gms.common.api.ResolvableApiException] is received).
     * 3. Coordinating callbacks to the [Payable] interface based on the final outcome.
     *
     * Requires setup, including providing an [ActivityResultLauncher] to the implementing class.
     */
    fun initiateGooglePayPayment()
    
    /**
     * Processes a payment using standard Pay Theory mechanisms (e.g., WebSocket communication).
     * **Note:** This method might not be directly applicable or implemented for the standard Google Pay flow,
     * which typically involves sending the Google Pay token to a backend endpoint rather than through
     * the SDK's WebSocket channel.
     *
     * @param payment The [PaymentDetail] object containing payment information (likely not a Google Pay token here).
     */
    fun process(payment: PaymentDetail)
    
    /**
     * Handles incoming messages, typically from a WebSocket connection used for other payment methods.
     * **Note:** This method is likely not used in the standard Google Pay flow.
     *
     * @param message The message string received.
     */
    fun receiveMessage(message: String)
    
    /**
     * Establishes necessary view models and potentially communication channels (like WebSockets).
     * **Note:** While view models might be relevant, the WebSocket establishment part might not be
     * directly used for the standard Google Pay flow.
     *
     * @param ptTokenResponse The response containing the necessary host token for communication.
     * @param attestationResult Optional attestation result string (e.g., from SafetyNet/Play Integrity).
     */
    fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?)
    
    /**
     * Disconnects from any active communication channels (like WebSockets) and cleans up resources.
     * **Note:** The applicability of this depends on whether the Google Pay processor implementation
     * utilizes resources that require explicit disconnection.
     */
    fun disconnect()
    
    /**
     * Constructs a [PaymentDetail] object specifically formatted for a Google Pay transaction,
     * typically wrapping the Google Pay token.
     *
     * This might be used to standardize the data structure before sending payment information
     * to a unified backend processing endpoint.
     *
     * @param token The encrypted payment token obtained from the Google Pay API ([PaymentData]).
     * @return A [PaymentDetail] object containing the `google_pay_token` and relevant type information.
     */
    fun constructGooglePayPayment(token: String): PaymentDetail
} 