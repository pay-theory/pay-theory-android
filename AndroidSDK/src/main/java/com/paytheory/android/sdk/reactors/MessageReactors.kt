package com.paytheory.android.sdk.reactors
import com.google.gson.Gson
import com.paytheory.android.sdk.BarcodeResult
import com.paytheory.android.sdk.ConfirmationMessage
import com.paytheory.android.sdk.EncryptedMessage
import com.paytheory.android.sdk.EncryptedPaymentToken
import com.paytheory.android.sdk.ErrorCode
import com.paytheory.android.sdk.FailedTransactionResult
import com.paytheory.android.sdk.PTError
import com.paytheory.android.sdk.PayTheoryMerchantActivity
import com.paytheory.android.sdk.Payment
import com.paytheory.android.sdk.PaymentMethodProcessor
import com.paytheory.android.sdk.PaymentMethodToken
import com.paytheory.android.sdk.PaymentMethodTokenResults
import com.paytheory.android.sdk.SuccessfulTransactionResult
import com.paytheory.android.sdk.TransactionResult
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.data.BarcodeMessage
import com.paytheory.android.sdk.data.HostTokenMessage
import com.paytheory.android.sdk.data.PaymentDetail
import com.paytheory.android.sdk.data.PaymentMethodTokenData
import com.paytheory.android.sdk.nacl.decryptBox
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi

/*
* Modernization
* Errors have been updated to PTError
* supporting error code enum
* */

/**
 * Creates reactions based on WebSocket messages
 * @param viewModel view model of WebSocket
 * @param webSocketInteractor interactor for WebSocket
 */
@ExperimentalCoroutinesApi
class MessageReactors(private val viewModel: WebSocketViewModel, private val webSocketInteractor: WebsocketInteractor) {
    var activePaymentDetail: PaymentDetail? = null
    var activePaymentToken: PaymentMethodTokenData? = null
    private var hostToken = ""
    private var sessionKey = ""
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
     * Sends payment confirmation to paymentConfirmation override method
     * @param
     */
    fun confirmPayment(message: String, payment: Payment? = null){
        //decrypt message
        val encryptedPaymentConfirmation = Gson().fromJson(message, EncryptedMessage::class.java)
        val decryptedMessage = decryptBox(encryptedPaymentConfirmation.body, encryptedPaymentConfirmation.publicKey)
        val confirmationMessage = Gson().fromJson(decryptedMessage, ConfirmationMessage::class.java)
        //set original confirmation/transaction with correct fee from Pay Theory
        payment!!.setConfirmation(confirmationMessage)
        //Remove service_fee for any merchant_fee transaction
        if (payment.configuration.feeMode == FeeMode.MERCHANT_FEE) {
            confirmationMessage.fee = "0"
        }
        //send user confirmation of payment

        payment.context.confirmation(confirmationMessage, payment)
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onError(message: String, payment: PaymentMethodProcessor? = null) {
        /* fail if unknown websocket message */
        if (payment != null) {
            payment.context.handleError(PTError(ErrorCode.SocketError,message))
        }
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onTokenError(message: String, paymentMethodToken: PaymentMethodToken? = null) {
        /* fail if unknown websocket message */
        paymentMethodToken?.context?.handleError(PTError(ErrorCode.SocketError,message))
    }

    /**
     * Function that handles incoming transfer response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun completeTransaction(message: String, viewModel: WebSocketViewModel, payment: Payment) {
        viewModel.disconnect()
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
                (payment.context as PayTheoryMerchantActivity).clearFields()
                payment.context.handleSuccess(successfulTransactionResult)
                PaymentMethodProcessor.sessionIsDirty = true
                payment.resetSocket()
            }
            "PENDING" -> {
                val successfulTransactionResult = Gson().fromJson(decryptedMessage, SuccessfulTransactionResult::class.java)
                (payment.context as PayTheoryMerchantActivity).clearFields()
                payment.context.handleSuccess(successfulTransactionResult)
                PaymentMethodProcessor.sessionIsDirty = true
                payment.resetSocket()
            }
            "FAILURE" -> {
                val failedTransactionResult = Gson().fromJson(decryptedMessage, FailedTransactionResult::class.java)
                payment.context.handleFailure(failedTransactionResult)
                payment.resetSocket()
            }
        }
    }

    /**
     * Function that handles incoming barcode response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun onBarcode(message: String, viewModel: WebSocketViewModel, payment: Payment) {
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



            payment.context.handleBarcodeSuccess(barcodeResult)
            PaymentMethodProcessor.sessionIsDirty = true

        } else {
            payment.context.handleError(PTError(ErrorCode.SocketError,"Failed to Create Barcode"))
        }
    }

    /**
     * Sends payment confirmation to paymentConfirmation override method
     * @param
     */
    fun onCompleteToken(message: String, paymentMethodToken: PaymentMethodToken){
        //decrypt message
        val encryptedPaymentToken = Gson().fromJson(message, EncryptedPaymentToken::class.java)
        val decryptedMessage = decryptBox(encryptedPaymentToken.body, encryptedPaymentToken.publicKey)
        val paymentMethodTokenResult = Gson().fromJson(decryptedMessage, PaymentMethodTokenResults::class.java)
        (paymentMethodToken.context as PayTheoryMerchantActivity).clearFields()
        paymentMethodToken.context.handleTokenizeSuccess(paymentMethodTokenResult)
        PaymentMethodProcessor.sessionIsDirty = true
        PaymentMethodProcessor.sessionIsDirty = true
        paymentMethodToken.resetSocket()
    }
}
