package com.paytheory.android.sdk.reactors

import BarcodeMessage
import HostTokenMessage
import Payment
import TransferMessage
import android.util.Log
import com.google.gson.Gson
import com.paytheory.android.sdk.*
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
    var hostToken = ""
    var sessionKey = ""
    var socketPublicKey = ""

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

        Log.d("PT- onHostToken", "Socket Public Key: ${hostTokenMessage.body.publicKey}")
        return hostTokenMessage
    }


    /**
     * Confirmation of payment
     * @param
     */
    fun confirmPayment(message: String, transaction: Transaction? = null){
        Log.d("PT-confirmPayment", "Socket Public Key: $socketPublicKey")

        val encryptedPaymentConfirmation = Gson().fromJson(message, EncryptedPaymentConfirmation::class.java)
        Log.d("PT-confirmPayment", "encryptedPaymentConfirmation PUBLIC_KEY from socket: ${encryptedPaymentConfirmation.publicKey}")
        //decrypt message
        val decryptedBody = decryptBox(encryptedPaymentConfirmation.body, encryptedPaymentConfirmation.publicKey)

        val paymentConfirmation = Gson().fromJson(decryptedBody, PaymentConfirmation::class.java)
        //get user confirmation of payment
        if (transaction != null) {
            if (transaction.context is Payable){
                transaction.context.paymentConfirmation(paymentConfirmation, transaction)
            }
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
                transaction.context.transactionError(TransactionError("Error processing payment"))
            }
        }
    }

    /**
     * Function that handles incoming transfer response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun completeTransfer(message: String, viewModel: WebSocketViewModel, transaction: Transaction) {
        println("Pay Theory Payment Result")
        viewModel.disconnect()
        val responseJson = JSONObject(message)
        if (transaction.context is Payable) when (responseJson["state"]) {
            "SUCCEEDED" -> {
                val transferResponse = Gson().fromJson(message, TransferMessage::class.java)
                val paymentResponse = PaymentResult(transferResponse.metadata["pt-number"].toString(),
                    transferResponse.lastFour, transferResponse.cardBrand, transferResponse.state,
                    transferResponse.amount, transferResponse.serviceFee, transferResponse.metadata,
                    transferResponse.createdAt, transferResponse.updatedAt, "Card")
                transaction.context.paymentComplete(paymentResponse)
            }
            "PENDING" -> {
                val transferResponse = Gson().fromJson(message, TransferMessage::class.java)
                val paymentResponse = PaymentResult(transferResponse.metadata["pt-number"].toString(),
                    transferResponse.lastFour, transferResponse.cardBrand, transferResponse.state,
                    transferResponse.amount, transferResponse.serviceFee, transferResponse.metadata,
                    transferResponse.createdAt, transferResponse.updatedAt, "ACH")
                transaction.context.paymentComplete(paymentResponse)
            }
            "FAILURE" -> {
                val failedResponse = PaymentResultFailure(responseJson["receipt_number"] as String,
                    responseJson["last_four"] as String,
                    responseJson["brand"] as String, responseJson["state"] as String, responseJson["type"] as String
                )
                transaction.context.paymentFailed(failedResponse)
            }

            else -> {
                val json = """
                { 
                    "error": ${responseJson["error"]}, 
                 }"""

                val errorResponse = Gson().fromJson(json, TransactionError::class.java)
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

        val responseJson = JSONObject(message)
        if (transaction.context is Payable && responseJson["BarcodeUid"].toString().isNotBlank()) {
            val transferResponse = Gson().fromJson(message, BarcodeMessage::class.java)
            val mapUrl = "https://pay.vanilladirect.com/pages/locations"
            val barcodeResponse = BarcodeResult(transferResponse.barcodeUid, transferResponse.barcodeUrl,
                transferResponse.barcode, transferResponse.barcodeFee, transferResponse.merchant, mapUrl)
            transaction.context.barcodeComplete(barcodeResponse)

        } else if (transaction.context is Payable) {
            val json = """
                { 
                    "error": "Cannot Create Barcode", 
                 }"""

            val errorResponse = Gson().fromJson(json, TransactionError::class.java)
            transaction.context.transactionError(errorResponse)
        }
        }
    }