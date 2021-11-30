package com.paytheory.android.sdk.reactors

import BarcodeMessage
import HostTokenMessage
import Payment
import TransferMessage
import TransferPartTwoMessage
import com.google.gson.Gson
import com.paytheory.android.sdk.*
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import java.util.*

/**
 * Creates reactions based on WebSocket messages
 * @param viewModel view model of WebSocket
 * @param webSocketInteractor interactor for WebSocket
 */
class MessageReactors(private val viewModel: WebSocketViewModel, private val webSocketInteractor: WebsocketInteractor) {
    var activePayment: Payment? = null
    var hostToken = ""
    var sessionKey = ""
    var socketPublicKey = ""

    /**
     * Handles incoming host token message
     * @param message message received from host token call
     */
    @ExperimentalCoroutinesApi
    fun transferPartOne(message: String, transaction: Transaction? = null): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.publicKey
        sessionKey = hostTokenMessage.sessionKey
        hostToken = hostTokenMessage.hostToken

        if (transaction?.queuedRequest != null) {
            transaction.queuedRequest!!.sessionKey = hostTokenMessage.sessionKey
            val actionRequest = transaction.transferPartOne(transaction.queuedRequest!!, sessionKey)
            transaction.viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Transfer Part One Requested")
        }
        return hostTokenMessage
    }

    /**
     * Confirmation of payment
     * @param
     */
    @ExperimentalCoroutinesApi
    fun confirmPayment(message: String, transaction: Transaction? = null){

        val transferPartTwoMessage = Gson().fromJson(message, TransferPartTwoMessage::class.java)

        //get user confirmation of payment
        if (transaction != null) {
            if (transaction.context is Payable){
                transaction.context.confirmation(message, transaction)
            }
        }

    }

    /**
     * Creates transfer part two request
     * @param message message received from transfer part one
     */
    @ExperimentalCoroutinesApi
    fun transferPartTwo(message: String, transaction: Transaction? = null): TransferPartTwoMessage? {
        val transferPartTwoMessage = Gson().fromJson(message, TransferPartTwoMessage::class.java)

//        //get user confirmation of payment
//        transaction.context.paymentConfirmation()


        if (transaction?.queuedRequest != null) {
            val actionRequest = transaction.transferPartTwo(transferPartTwoMessage)
            transaction.viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Transfer Part Two Requested")
        }

        return transferPartTwoMessage
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onUnknown(message: String): Any {
        return Gson().fromJson(message, Any::class.java)
    }

    /**
     * Function that instrument message and creates idempotency request
     * @param message message to be sent
     * @param apiKey api-key used for transaction
     */
//    @ExperimentalCoroutinesApi
//    fun onInstrument(message:String, apiKey:String): InstrumentMessage {
//        println("Pay Theory Instrument Token")
//        val instrumentMessage = Gson().fromJson(message, InstrumentMessage::class.java)
//        activePayment!!.ptInstrument = instrumentMessage.ptInstrument
//
//        val keyPair = generateLocalKeyPair()
//        val idempotencyRequest = IdempotencyRequest(apiKey,
//            activePayment!!,
//            hostToken,
//            sessionKey,
//            System.currentTimeMillis())
//        val localPublicKey = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
//        } else {
//            android.util.Base64.encodeToString(keyPair.publicKey.asBytes,android.util.Base64.DEFAULT)
//        }
//
//        val boxed = encryptBox(Gson().toJson(idempotencyRequest), Key.fromBase64String(socketPublicKey))
//
//        val actionRequest = ActionRequest(
//            "host:idempotency",
//            boxed,
//            localPublicKey)
//        viewModel.sendSocketMessage(Gson().toJson(actionRequest))
//
//        return instrumentMessage
//    }

    /**
     * Function that idempotency message and creates transfer request
     * @param message message to be sent
     */
//    @ExperimentalCoroutinesApi
//    fun onIdempotency(message: String, tags: HashMap<String, String>?): IdempotencyMessage {
//        println("Pay Theory Idempotency")
//        val idempotencyMessage = Gson().fromJson(message, IdempotencyMessage::class.java)
//
//        tags!!["pt-number"] = idempotencyMessage.idempotency
//        val keyPair = generateLocalKeyPair()
//        val localPublicKey = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
//        } else {
//            android.util.Base64.encodeToString(keyPair.publicKey.asBytes,android.util.Base64.DEFAULT)
//        }
//        val transferRequest = TransferRequest(Transfer(idempotencyMessage.paymentToken, idempotencyMessage.idempotency), System.currentTimeMillis(),
//            tags
//        )
//
//
//        val boxed = encryptBox(Gson().toJson(transferRequest), Key.fromBase64String(socketPublicKey))
//
//        val actionRequest = ActionRequest(
//            "host:transfer",
//            boxed,
//            localPublicKey)
//
//        viewModel.sendSocketMessage(Gson().toJson(actionRequest))
//
//        return idempotencyMessage
//    }

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
                val paymentResponse = PaymentResult(transferResponse.tags["pt-number"].toString(),
                    transferResponse.lastFour, transferResponse.cardBrand, transferResponse.state,
                    transferResponse.amount, transferResponse.serviceFee, transferResponse.tags,
                    transferResponse.createdAt, transferResponse.updatedAt, "Card")
                transaction.context.paymentComplete(paymentResponse)
            }
            "PENDING" -> {
                val transferResponse = Gson().fromJson(message, TransferMessage::class.java)
                val paymentResponse = PaymentResult(transferResponse.tags["pt-number"].toString(),
                    transferResponse.lastFour, transferResponse.cardBrand, transferResponse.state,
                    transferResponse.amount, transferResponse.serviceFee, transferResponse.tags,
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