package com.paytheory.lib.reactors
import com.google.gson.Gson

import com.paytheory.lib.Payment
import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.PaymentMethodToken

import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.data.BarcodeMessage
import com.paytheory.lib.data.BarcodeResult
import com.paytheory.lib.data.EncryptedMessage
import com.paytheory.lib.data.ErrorCode
import com.paytheory.lib.data.FailedTransactionResult
import com.paytheory.lib.data.HostTokenMessage
import com.paytheory.lib.data.PTError
import com.paytheory.lib.data.PaymentDetail
import com.paytheory.lib.data.PaymentMethodTokenResults
import com.paytheory.lib.data.SuccessfulTransactionResult
import com.paytheory.lib.data.TransactionResult
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.nacl.decryptBox
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Handles and processes various types of WebSocket messages in the Pay Theory payment system.
 *
 * This class is responsible for managing different types of message reactions including:
 * - Host token processing
 * - Payment transaction completion
 * - Barcode generation and handling
 * - Payment method tokenization
 * - Error handling
 *
 * @property viewModel The PaymentViewModel instance managing the payment state
 * @property activePaymentDetail Current payment details being processed
 * @property activePaymentToken Current payment token being processed
 * @property hostToken The current host token for authentication
 * @property sessionKey The current session key for secure communication
 * @property socketPublicKey The public key used for WebSocket encryption
 */
@ExperimentalCoroutinesApi
class MessageReactors(private val viewModel: PaymentViewModel) {
    var activePaymentDetail: PaymentDetail? = null
    var activePaymentToken: PaymentDetail? = null
    private var hostToken = ""
    var sessionKey = ""
    var socketPublicKey = ""
    private val mapUrl = "https://pay.vanilladirect.com/pages/locations"

    /**
     * Processes a host token message for payment processing.
     *
     * Extracts and stores necessary security credentials from the host token message
     * and updates the payment instance with these credentials.
     *
     * @param message The raw message containing the host token information
     * @param payment The Payment instance to be updated with the credentials
     * @return The parsed [HostTokenMessage]
     */
    @ExperimentalCoroutinesApi
    fun onHostToken(message: String, payment: Payment): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.body.publicKey
        sessionKey = hostTokenMessage.body.sessionKey
        hostToken = hostTokenMessage.body.hostToken
        payment.publicKey = hostTokenMessage.body.publicKey
        payment.sessionKey = hostTokenMessage.body.sessionKey
        payment.hostToken = hostTokenMessage.body.hostToken
        return hostTokenMessage
    }

    /**
     * Processes a host token message for payment method tokenization.
     *
     * Similar to [onHostToken], but specifically for tokenization operations.
     *
     * @param message The raw message containing the host token information
     * @param paymentMethodToken The PaymentMethodToken instance to be updated
     * @return The parsed [HostTokenMessage]
     */
    @ExperimentalCoroutinesApi
    fun onTokenizeHostToken(message: String, paymentMethodToken: PaymentMethodToken): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.body.publicKey
        sessionKey = hostTokenMessage.body.sessionKey
        hostToken = hostTokenMessage.body.hostToken
        paymentMethodToken.publicKey = hostTokenMessage.body.publicKey
        paymentMethodToken.sessionKey = hostTokenMessage.body.sessionKey
        paymentMethodToken.hostToken = hostTokenMessage.body.hostToken
        return hostTokenMessage
    }

    /**
     * Handles error messages for payment processing.
     *
     * @param message The error message
     * @param payment Optional payment processor instance
     */
    fun onError(message: String, payment: PaymentMethodProcessor? = null) {
        /* fail if unknown websocket message */
        payment?.viewModel?.disconnect()
        payment?.payable?.handleError(PTError(ErrorCode.SocketError,message))
    }

    /**
     * Handles error messages for tokenization.
     *
     * @param message The error message
     * @param paymentMethodToken Optional payment method token instance
     */
    fun onTokenError(message: String, paymentMethodToken: PaymentMethodToken? = null) {
        /* fail if unknown websocket message */
        paymentMethodToken?.payable?.handleError(PTError(ErrorCode.TokenFailed,message))
    }

    /**
     * Completes a payment transaction by processing the encrypted response.
     *
     * This method:
     * 1. Decrypts the transaction message
     * 2. Processes the transaction result based on its state (SUCCEEDED, PENDING, FAILURE)
     * 3. Updates the payment state and notifies appropriate handlers
     *
     * @param message The encrypted transaction message
     * @param viewModel The payment view model instance
     * @param payment The payment instance being processed
     */
    @ExperimentalCoroutinesApi
    fun completeTransaction(message: String, viewModel: PaymentViewModel, payment: Payment) {
//        viewModel.disconnect()

        try {
            val encryptedTransferMessage = Gson().fromJson(message, EncryptedMessage::class.java)
            //decrypt message
            val decryptedMessage = decryptBox(encryptedTransferMessage.body, encryptedTransferMessage.publicKey)

            val transactionResult = Gson().fromJson(decryptedMessage, TransactionResult::class.java)

            //Remove service_fee for any merchant_fee transaction
            if (payment.configuration.feeMode == FeeMode.MERCHANT_FEE) {
                transactionResult.serviceFee = "0"
            }

            when (transactionResult.state) {
                "SUCCEEDED" -> {
                    val successfulTransactionResult = Gson().fromJson(decryptedMessage, SuccessfulTransactionResult::class.java)
                    payment.viewModel.paymentSuccess(successfulTransactionResult)
                    payment.payable.handleSuccess(successfulTransactionResult)
                    PaymentMethodProcessor.sessionIsDirty = true
                    payment.resetSocket()
                }
                "PENDING" -> {
                    val successfulTransactionResult = Gson().fromJson(decryptedMessage, SuccessfulTransactionResult::class.java)
                    payment.viewModel.paymentSuccess(successfulTransactionResult)
                    payment.payable.handleSuccess(successfulTransactionResult)
                    PaymentMethodProcessor.sessionIsDirty = true
                    payment.resetSocket()
                }
                "FAILURE" -> {
                    val failedTransactionResult = Gson().fromJson(decryptedMessage, FailedTransactionResult::class.java)
                    payment.payable.handleFailure(failedTransactionResult)
                    payment.resetSocket()
                }
            }
        } catch (e: Exception) {
            payment.payable.handleError(PTError(ErrorCode.SocketError,e.message ?: "Unknown error"))
        }

    }

    /**
     * Processes a barcode generation response.
     *
     * This method:
     * 1. Decrypts the barcode message
     * 2. Creates a barcode result with necessary information
     * 3. Handles success or failure of barcode generation
     *
     * @param message The encrypted barcode message
     * @param viewModel The payment view model instance
     * @param payment The payment instance being processed
     */
    @ExperimentalCoroutinesApi
    fun onBarcode(message: String, viewModel: PaymentViewModel, payment: Payment) {
        println("Pay Theory Barcode Result")

        viewModel.disconnect()
        val encryptedBarcodeMessage = Gson().fromJson(message, EncryptedMessage::class.java)
        val decryptedMessage = decryptBox(encryptedBarcodeMessage.body, encryptedBarcodeMessage.publicKey)
        val barcodeMessageResult = Gson().fromJson(decryptedMessage, BarcodeMessage::class.java)

        if (barcodeMessageResult.barcode.isNotBlank() && barcodeMessageResult.barcodeUrl.isNotBlank()) {
            val barcodeResult = BarcodeResult(
                barcodeId = barcodeMessageResult.barcodeId,
                barcodeUrl = barcodeMessageResult.barcodeUrl,
                barcode = barcodeMessageResult.barcode,
                barcodeFee = barcodeMessageResult.barcodeFee,
                merchant = barcodeMessageResult.merchant,
                mapUrl = mapUrl
            )

            payment.payable.handleBarcodeSuccess(barcodeResult)
            PaymentMethodProcessor.sessionIsDirty = true
            payment.resetSocket()

        } else {
            payment.payable.handleError(PTError(ErrorCode.SocketError,"Failed to Create Barcode"))
        }
    }

    /**
     * Completes the payment method tokenization process.
     *
     * This method:
     * 1. Decrypts the tokenization message
     * 2. Processes the token results
     * 3. Handles success or failure of tokenization
     *
     * @param message The encrypted tokenization message
     * @param paymentMethodToken The payment method token instance
     */
    fun onCompleteToken(message: String, paymentMethodToken: PaymentMethodToken){
        try {
            val encryptedTransferMessage = Gson().fromJson(message, EncryptedMessage::class.java)
            //decrypt message
            val decryptedMessage = decryptBox(encryptedTransferMessage.body, encryptedTransferMessage.publicKey)

            val tokenResults = Gson().fromJson(decryptedMessage, PaymentMethodTokenResults::class.java)

            when (tokenResults.paymentMethodId.isEmpty()) {
                false -> {
                    val successfulTokenResult = Gson().fromJson(decryptedMessage,
                        PaymentMethodTokenResults::class.java)
                    paymentMethodToken.viewModel.tokenSuccess(successfulTokenResult)
//                        .paymentSuccess(successfulTransactionResult)
                    paymentMethodToken.payable.handleTokenizeSuccess(successfulTokenResult)
                        //.handleSuccess(successfulTransactionResult)
                    PaymentMethodProcessor.sessionIsDirty = true
                    paymentMethodToken.resetSocket()
                }
                true -> {
                    val failedTransactionResult = Gson().fromJson(decryptedMessage, FailedTransactionResult::class.java)
                    paymentMethodToken.payable.handleFailure(failedTransactionResult)
                    paymentMethodToken.resetSocket()
                }
            }
        } catch (e: Exception) {
            paymentMethodToken.payable.handleError(PTError(ErrorCode.SocketError,e.message ?: "Unknown error"))
        }
    }
}
