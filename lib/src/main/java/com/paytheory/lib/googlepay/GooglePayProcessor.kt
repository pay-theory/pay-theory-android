package com.paytheory.lib.googlepay

import android.app.Activity
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.requests.ActionRequest
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import com.paytheory.lib.model.PaymentViewModel
import timber.log.Timber
import java.util.Date
import java.util.HashMap
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Google Pay payment processor for the Pay Theory SDK
 * Handles Google Pay payment flows and token processing
 *
 * @param payable The payable interface for callbacks
 * @param activity The activity hosting the payment flow
 * @param configuration The Pay Theory configuration
 * @param viewModel The payment view model for state management
 * @param googlePayUtil The Google Pay utility interface (for easier testing)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GooglePayProcessor(
    override val payable: Payable,
    private val activity: Activity,
    override val configuration: PayTheoryConfiguration,
    override val viewModel: PaymentViewModel,
    private val googlePayUtil: GooglePayUtilInterface = GooglePayUtil.getInstance()
) : PaymentMethodProcessor(payable, HashMap(), configuration, viewModel), GooglePayProcessorInterface {

    /**
     * Constructor for testing purposes
     */
    constructor(
        payable: Payable,
        activity: Activity,
        configuration: PayTheoryConfiguration,
        viewModel: PaymentViewModel,
        googlePayUtil: GooglePayUtilInterface,
        testMode: Boolean
    ) : this(payable, activity, configuration, viewModel, googlePayUtil) {
        // The testMode parameter is used to ensure configuration.isTestMode is true
        // headerMap is now a lazy property in the parent class that will initialize
        // correctly based on configuration.isTestMode
        // No need to override it here
    }

    private var paymentToken: String? = null
    private var connected = false
    
    /**
     * Check if Google Pay is available on the device
     *
     * @return Task<Boolean> that will be updated with Google Pay availability
     */
    override fun isGooglePayAvailable(): Task<Boolean> {
        return googlePayUtil.isGooglePayAvailable(
            activity,
            configuration.googlePayEnvironment,
            configuration.googlePayBillingAddressRequired
        )
    }
    
    /**
     * Initiate a Google Pay payment
     * Shows the Google Pay payment sheet and processes the result
     */
    override fun initiateGooglePayPayment() {
        if (!connected) {
            ptTokenApiCall(payable)
            return
        }
        
        try {
            val task = googlePayUtil.requestGooglePayment(
                activity,
                configuration.amount.toBigDecimal(),
                configuration.googlePayMerchantName ?: "Pay Theory",
                configuration.googlePayEnvironment,
                configuration.googlePayBillingAddressRequired,
                configuration.googlePayBillingAddressFormat,
                configuration.googlePayShippingAddressRequired,
                configuration.googlePayPhoneNumberRequired,
                configuration.googlePayAllowPrepaidCards,
                configuration.googlePayAllowCreditCards
            )
            
            task.addOnCompleteListener { completedTask ->
                try {
                    if (completedTask.isSuccessful) {
                        val paymentData = completedTask.result
                        paymentToken = googlePayUtil.extractPaymentToken(paymentData)
                        processGooglePayToken()
                    }
                } catch (e: ApiException) {
                    // Handle user cancellation
                    if (e.statusCode == 10) {
                        payable.handleError(PTError(ErrorCode.GooglePayCancelled, "User cancelled Google Pay payment"))
                    } else {
                        payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay error: ${e.message}"))
                    }
                    viewModel._paymentState.value = PaymentViewModel.PaymentState.Idle
                    viewModel.validateInputs()
                } catch (e: Exception) {
                    payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay error: ${e.message}"))
                    viewModel._paymentState.value = PaymentViewModel.PaymentState.Idle
                    viewModel.validateInputs()
                }
            }
        } catch (e: Exception) {
            payable.handleError(PTError(ErrorCode.GooglePayError, "Failed to initiate Google Pay: ${e.message}"))
            viewModel._paymentState.value = PaymentViewModel.PaymentState.Idle
            viewModel.validateInputs()
        }
    }
    
    /**
     * Process the Google Pay token by creating and sending a payment request
     */
    private fun processGooglePayToken() {
        paymentToken?.let { token ->
            viewModel._paymentState.value = PaymentViewModel.PaymentState.Processing
            val paymentDetail = constructGooglePayPayment(token)
            sendEncryptedActionRequest("host:wallet_transaction", paymentDetail)
        }
    }
    
    /**
     * Construct a PaymentDetail object for Google Pay
     *
     * @param token The payment token from Google Pay
     * @return A PaymentDetail with Google Pay token information
     */
    override fun constructGooglePayPayment(token: String): PaymentDetail {
        return PaymentDetail(
            type = "wallet",
            timing = Date().time,
            amount = configuration.amount,
            currency = "USD",
            walletType = "GOOGLE_PAY",
            digitalWalletPayload = token,
            fee_mode = configuration.feeMode,
            payorInfo = configuration.payorInfo,
            merchant = "",
            service_fee = configuration.serviceFee?.toString()
        )
    }
    
    /**
     * Implementation of the process method from PaymentMethodProcessor
     * For Google Pay, the payment is already processed by initiateGooglePayPayment
     */
    override fun process(payment: PaymentDetail) {
        // Google Pay processing is handled by initiateGooglePayPayment
        // This method is required by the PaymentMethodProcessor abstract class
        Timber.d("Google Pay process method called - processing is handled separately")
    }
    
    /**
     * Creates an initial action request for the Google Pay payment
     *
     * @param payment The payment details
     * @return An ActionRequest with the appropriate action
     */
    override fun createInitialActionRequest(payment: PaymentDetail): ActionRequest {
        // This is used for sending the wallet transaction request
        return ActionRequest(
            action = "host:wallet_transaction",
            encoded = "", // Will be encoded by sendEncryptedActionRequest
            publicKey = publicKey ?: "",
            sessionKey = sessionKey ?: ""
        )
    }
    
    /**
     * Required override for message handling
     */
    override fun receiveMessage(message: String) {
        // Handle Google Pay specific messages
        if (message.contains("google_pay_complete")) {
            // Process Google Pay success
            val result = parseTransactionResult(message)
            if (result != null) {
                payable.handleSuccess(result)
            }
        } else {
            // Use default message handling, but don't call super directly
            // since it's abstract
            // Instead, implement similar behavior here or call a helper method
        }
    }
    
    /**
     * Establishes the view model and WebSocket connection
     *
     * @param ptTokenResponse The response from the PT token API
     * @param attestationResult The result of the attestation, if applicable
     */
    override fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?) {
        if (messageReactors != null) {
            messageReactors?.establishConnection(ptTokenResponse, attestationResult ?: "", this)
        }
        connected = true
        payable.handleReady(true)
    }
    
    /**
     * Parse a transaction result from a JSON message string
     * 
     * @param message The JSON message from the server
     * @return A SuccessfulTransactionResult object or null if parsing failed
     */
    private fun parseTransactionResult(message: String): SuccessfulTransactionResult? {
        // Implement parsing logic here
        // For now, return null to avoid compilation error
        return null
    }
    
    /**
     * Sends an encrypted action request to the server
     * 
     * @param action The action to perform
     * @param paymentDetail The payment details to include
     */
    private fun sendEncryptedActionRequest(action: String, paymentDetail: PaymentDetail) {
        // Implement encryption and sending logic
        // This is a placeholder
    }
    
    /**
     * Disconnect from the server and clean up resources
     */
    override fun disconnect() {
        connected = false
        // Do not call super.disconnect() directly as it's an abstract method
        // Instead, implement the disconnect logic here
        
        // Clean up any resources specific to Google Pay
        paymentToken = null
    }
} 