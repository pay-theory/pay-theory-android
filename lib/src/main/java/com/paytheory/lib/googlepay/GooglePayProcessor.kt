package com.paytheory.lib.googlepay

import android.app.Activity
import androidx.lifecycle.LiveData
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.ActionRequest
import com.paytheory.lib.data.ErrorCode
import com.paytheory.lib.data.PTError
import com.paytheory.lib.data.PaymentDetail
import com.paytheory.lib.model.PaymentState
import com.paytheory.lib.model.PaymentViewModel
import timber.log.Timber
import java.util.Date

/**
 * Google Pay payment processor for the Pay Theory SDK
 * Handles Google Pay payment flows and token processing
 *
 * @param configuration The Pay Theory configuration
 * @param activity The activity hosting the payment flow
 * @param payable The payable interface for callbacks
 * @param viewModel The payment view model for state management
 */
class GooglePayProcessor(
    private val configuration: PayTheoryConfiguration,
    private val activity: Activity,
    private val payable: Payable,
    viewModel: PaymentViewModel
) : PaymentMethodProcessor(payable, null, configuration, viewModel) {

    private val googlePayUtil = GooglePayUtil
    private var paymentToken: String? = null
    
    /**
     * Check if Google Pay is available on the device
     *
     * @return LiveData<Boolean> that will be updated with Google Pay availability
     */
    fun isGooglePayAvailable(): LiveData<Boolean> {
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
    fun initiateGooglePayPayment() {
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
                    _paymentState.value = PaymentState.Ready
                } catch (e: Exception) {
                    payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay error: ${e.message}"))
                    _paymentState.value = PaymentState.Ready
                }
            }
        } catch (e: Exception) {
            payable.handleError(PTError(ErrorCode.GooglePayError, "Failed to initiate Google Pay: ${e.message}"))
            _paymentState.value = PaymentState.Ready
        }
    }
    
    /**
     * Process the Google Pay token by creating and sending a payment request
     */
    private fun processGooglePayToken() {
        paymentToken?.let { token ->
            _paymentState.value = PaymentState.Processing
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
    private fun constructGooglePayPayment(token: String): PaymentDetail {
        return PaymentDetail(
            type = "wallet",
            timing = Date().time,
            amount = configuration.amount,
            currency = "USD",
            walletType = "GOOGLE_PAY",
            digitalWalletPayload = token,
            fee_mode = configuration.feeMode,
            payorInfo = configuration.payorInfo,
            merchant = configuration.merchantId,
            service_fee = configuration.serviceFee?.toString(),
            metadata = configuration.metadata
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
            publicKey = publicKey,
            sessionKey = sessionKey
        )
    }
    
    /**
     * Establishes the view model and WebSocket connection
     *
     * @param ptTokenResponse The response from the PT token API
     * @param attestationResult The result of the attestation, if applicable
     */
    override fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String?) {
        super.messageReactors?.let { messageReactors ->
            messageReactors.establishConnection(ptTokenResponse, attestationResult, this)
        }
        connected = true
        updatePayableReadyState(true)
    }
} 