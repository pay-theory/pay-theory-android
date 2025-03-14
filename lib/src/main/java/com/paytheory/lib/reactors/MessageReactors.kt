package com.paytheory.lib.reactors
import com.google.gson.Gson
import com.paytheory.lib.BarcodeResult
import com.paytheory.lib.EncryptedMessage
import com.paytheory.lib.EncryptedPaymentToken
import com.paytheory.lib.ErrorCode
import com.paytheory.lib.FailedTransactionResult
import com.paytheory.lib.PTError
import com.paytheory.lib.Payment
import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.PaymentMethodToken
import com.paytheory.lib.PaymentMethodTokenResults
import com.paytheory.lib.SuccessfulTransactionResult
import com.paytheory.lib.TransactionResult
import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.data.BarcodeMessage
import com.paytheory.lib.data.HostTokenMessage
import com.paytheory.lib.data.PaymentDetail
import com.paytheory.lib.data.PaymentMethodTokenData
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.nacl.decryptBox
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Creates reactions based on WebSocket messages
 * @param viewModel view model of WebSocket
 */
@ExperimentalCoroutinesApi
class MessageReactors(private val viewModel: PaymentViewModel) {
    var activePaymentDetail: PaymentDetail? = null
    var activePaymentToken: PaymentMethodTokenData? = null
    private var hostToken = ""
    var sessionKey = ""
    var socketPublicKey = ""
    private val mapUrl = "https://pay.vanilladirect.com/pages/locations"

    /**
     * Called when host token message received from websocket
     * @param
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
     * Called when host token message received from websocket
     * @param
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
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onError(message: String, payment: PaymentMethodProcessor? = null) {
        /* fail if unknown websocket message */
        payment?.viewModel?.disconnect()
        payment?.payable?.handleError(PTError(ErrorCode.SocketError,message))
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onTokenError(message: String, paymentMethodToken: PaymentMethodToken? = null) {
        /* fail if unknown websocket message */
        paymentMethodToken?.payable?.handleError(PTError(ErrorCode.TokenFailed,message))
    }

    /**
     * Function that handles incoming transfer response
     * @param message message to be sent
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
     * Function that handles incoming barcode response
     * @param message message to be sent
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
     * Handle tokenize success
     * @param
     */
    fun onCompleteToken(message: String, paymentMethodToken: PaymentMethodToken){
        //decrypt message
        val encryptedPaymentToken = Gson().fromJson(message, EncryptedPaymentToken::class.java)
        val decryptedMessage = decryptBox(encryptedPaymentToken.body, encryptedPaymentToken.publicKey)
        val paymentMethodTokenResult = Gson().fromJson(decryptedMessage, PaymentMethodTokenResults::class.java)

        paymentMethodToken.payable.handleTokenizeSuccess(paymentMethodTokenResult)
        PaymentMethodProcessor.sessionIsDirty = true
        paymentMethodToken.resetSocket()
    }
}
