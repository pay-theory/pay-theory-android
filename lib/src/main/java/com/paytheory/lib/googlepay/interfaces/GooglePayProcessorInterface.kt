package com.paytheory.lib.googlepay.interfaces

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.payable.SuccessfulTransactionResult

/**
 * Interface for Google Pay processor operations
 * This interface enables easier testing by allowing mock implementations
 */
interface GooglePayProcessorInterface {
    /**
     * Check if Google Pay is available on the device
     *
     * @return Task<Boolean> that will be updated with Google Pay availability
     */
    fun isGooglePayAvailable(): Task<Boolean>
    
    /**
     * Initiate a Google Pay payment
     * Shows the Google Pay payment sheet and processes the result
     */
    fun initiateGooglePayPayment()
    
    /**
     * Process the specified payment
     *
     * @param payment The payment details
     */
    fun process(payment: PaymentDetail)
    
    /**
     * Receives and processes a message from the WebSocket
     *
     * @param message The message string to process
     */
    fun receiveMessage(message: String)
    
    /**
     * Establishes the view model and WebSocket connection
     *
     * @param ptTokenResponse The response from the PT token API
     * @param attestationResult The result of the attestation, if applicable
     */
    fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?)
    
    /**
     * Disconnect from the server and clean up resources
     */
    fun disconnect()
    
    /**
     * Construct a PaymentDetail object for Google Pay
     *
     * @param token The payment token from Google Pay
     * @return A PaymentDetail with Google Pay token information
     */
    fun constructGooglePayPayment(token: String): PaymentDetail
} 