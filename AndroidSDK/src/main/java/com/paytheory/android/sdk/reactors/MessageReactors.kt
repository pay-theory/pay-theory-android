package com.paytheory.android.sdk.reactors

import BarcodeMessage
import HostTokenMessage
import Payment
import PaymentMethodTokenData
import com.google.gson.Gson
import com.paytheory.android.sdk.*
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.nacl.decryptBox
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject

/**
 * Creates reactions based on WebSocket messages
 * @param viewModel view model of WebSocket
 * @param webSocketInteractor interactor for WebSocket
 */
@ExperimentalCoroutinesApi
class MessageReactors(private val viewModel: WebSocketViewModel, private val webSocketInteractor: WebsocketInteractor) {
    var activePayment: Payment? = null
    var activePaymentToken: PaymentMethodTokenData? = null
    var hostToken = ""
    var sessionKey = ""
    var socketPublicKey = ""
    private val mapUrl = "https://pay.vanilladirect.com/pages/locations"

    /**
     * Called when host token message received from websocket
     * @param
     */
    @ExperimentalCoroutinesApi
    fun onHostToken(message: String, transaction: Transaction): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.body.publicKey
        sessionKey = hostTokenMessage.body.sessionKey
        hostToken = hostTokenMessage.body.hostToken
        transaction.publicKey = hostTokenMessage.body.publicKey
        transaction.sessionKey = hostTokenMessage.body.sessionKey
        transaction.hostToken = hostTokenMessage.body.hostToken
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
    fun confirmPayment(message: String, transaction: Transaction? = null){
        //decrypt message
        val encryptedPaymentConfirmation = Gson().fromJson(message, EncryptedMessage::class.java)
        val decryptedMessage = decryptBox(encryptedPaymentConfirmation.body, encryptedPaymentConfirmation.publicKey)
        val confirmationMessage = Gson().fromJson(decryptedMessage, ConfirmationMessage::class.java)
        //set original confirmation message from Pay Theory
        transaction!!.setConfirmation(confirmationMessage)
        //Remove service_fee for any interchange transaction
        if (transaction.feeMode == FeeMode.INTERCHANGE) {
            confirmationMessage.fee = "0"
        }
        //send user confirmation of payment
        if (transaction.context is Payable){
            transaction.context.confirmation(confirmationMessage, transaction)
        }
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onError(message: String, transaction: Transaction? = null) {
        print("Error with WebSocket: $message")
        /* fail if unknown websocket message */
        if (transaction != null) {
            if (transaction.context is Payable){
                transaction.context.transactionError(Error("Error processing payment"))
            }
        }
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onTokenError(message: String, paymentMethodToken: PaymentMethodToken? = null) {
        print("Error with WebSocket: $message")
        /* fail if unknown websocket message */
        if (paymentMethodToken != null) {
            if (paymentMethodToken.context is Payable){
                paymentMethodToken.context.transactionError(Error("Error tokenizing payment method"))
            }
        }
    }

    /**
     * Function that handles incoming transfer response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun completeTransaction(message: String, viewModel: WebSocketViewModel, transaction: Transaction) {
        viewModel.disconnect()
        val encryptedTransferMessage = Gson().fromJson(message, EncryptedMessage::class.java)
        //decrypt message
        val decryptedMessage = decryptBox(encryptedTransferMessage.body, encryptedTransferMessage.publicKey)

        val transactionResult = Gson().fromJson(decryptedMessage, TransactionResult::class.java)

        //Remove service_fee for any interchange transaction
        if (transaction.feeMode == FeeMode.INTERCHANGE) {
            transactionResult.serviceFee = "0"
        }

        if (transaction.context is Payable) when (transactionResult.state) {
            "SUCCEEDED" -> {
                val completedTransactionResult = Gson().fromJson(decryptedMessage, CompletedTransactionResult::class.java)
                transaction.context.paymentSuccess(completedTransactionResult)
            }
            "PENDING" -> {
                val completedTransactionResult = Gson().fromJson(decryptedMessage, CompletedTransactionResult::class.java)
                transaction.context.paymentSuccess(completedTransactionResult)
            }
            "FAILURE" -> {
                val failedTransactionResult = Gson().fromJson(decryptedMessage, FailedTransactionResult::class.java)
                transaction.context.paymentFailed(failedTransactionResult)
            }

        else -> {
            val errorResponse = Error("Error retrieving payment confirmation")
            transaction.context.transactionError(errorResponse)
        }
        }
    }

    /**
     * Function that handles incoming barcode response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun onBarcode(message: String, viewModel: WebSocketViewModel, transaction: Transaction) {
        println("Pay Theory Barcode Result")
        viewModel.disconnect()
        val encryptedBarcodeMessage = Gson().fromJson(message, EncryptedMessage::class.java)
        val decryptedMessage = decryptBox(encryptedBarcodeMessage.body, encryptedBarcodeMessage.publicKey)
        val barcodeMessageResult = Gson().fromJson(decryptedMessage, BarcodeMessage::class.java)

        if (transaction.context is Payable && barcodeMessageResult.barcode.isNotBlank() && barcodeMessageResult.barcodeUrl.isNotBlank()) {
            val barcodeResult = BarcodeResult(barcodeMessageResult.barcodeUid, barcodeMessageResult.barcodeUrl,
                barcodeMessageResult.barcode, barcodeMessageResult.barcodeFee, barcodeMessageResult.merchant, mapUrl)
            transaction.context.barcodeSuccess(barcodeResult)

        } else if (transaction.context is Payable) {
            val errorResponse = Error("Failed to Create Barcode")
            transaction.context.transactionError(errorResponse)
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
        if (paymentMethodToken.context is Payable) {
            paymentMethodToken.context.tokenizedSuccess(paymentMethodTokenResult)
        }
    }
}