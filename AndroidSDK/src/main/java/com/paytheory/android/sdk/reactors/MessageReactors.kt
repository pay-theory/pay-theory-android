package com.paytheory.android.sdk.reactors

import ActionRequest
import HostTokenMessage
import IdempotencyMessage
import IdempotencyRequest
import InstrumentMessage
import Payment
import Transfer
import TransferMessage
import TransferRequest
import com.google.gson.Gson
import com.goterl.lazycode.lazysodium.utils.Key
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.PaymentError
import com.paytheory.android.sdk.PaymentResult
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.nacl.encryptBox
import com.paytheory.android.sdk.nacl.generateLocalKeyPair
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
     * Function that creates a message for a host token action
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun onHostToken(message: String, transaction: Transaction? = null): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.publicKey
        sessionKey = hostTokenMessage.sessionKey
        hostToken = hostTokenMessage.hostToken

        if (transaction?.queuedRequest != null) {

            val actionRequest = transaction.generateQueuedActionRequest(transaction.queuedRequest!!)
            transaction.viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Queued Payment Requested")
        }
        return hostTokenMessage
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
    @ExperimentalCoroutinesApi
    fun onInstrument(message:String, apiKey:String): InstrumentMessage {
        println("Pay Theory Instrument Token")
        val instrumentMessage = Gson().fromJson(message, InstrumentMessage::class.java)
        activePayment!!.ptInstrument = instrumentMessage.ptInstrument

        val keyPair = generateLocalKeyPair()
        val idempotencyRequest = IdempotencyRequest(apiKey,
            activePayment!!,
            hostToken,
            sessionKey,
            System.currentTimeMillis())
        val localPublicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)

        val boxed = encryptBox(Gson().toJson(idempotencyRequest), Key.fromBase64String(socketPublicKey))

        val actionRequest = ActionRequest(
            "host:idempotency",
            boxed,
            localPublicKey)
        viewModel.sendSocketMessage(Gson().toJson(actionRequest))

        return instrumentMessage
    }

    /**
     * Function that idempotency message and creates transfer request
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun onIdempotency(message: String): IdempotencyMessage {
        println("Pay Theory Idempotency")
        val idempotencyMessage = Gson().fromJson(message, IdempotencyMessage::class.java)

        val keyPair = generateLocalKeyPair()
        val localPublicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        val transferRequest = TransferRequest(Transfer(idempotencyMessage.paymentToken, idempotencyMessage.idempotency), System.currentTimeMillis())


        val boxed = encryptBox(Gson().toJson(transferRequest), Key.fromBase64String(socketPublicKey))

        val actionRequest = ActionRequest(
            "host:transfer",
            boxed,
            localPublicKey)

        viewModel.sendSocketMessage(Gson().toJson(actionRequest))

        return idempotencyMessage
    }

    /**
     * Function that handles incoming transfer response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun onTransfer(message: String, viewModel: WebSocketViewModel, transaction: Transaction) {
        println("Pay Theory Payment Result")
        viewModel.disconnect()
        val responseJson = JSONObject(message)
        if (transaction.context is Payable) when (responseJson["state"]) {
            "SUCCEEDED" -> {
                val transferResponse = Gson().fromJson(message, TransferMessage::class.java)
                val json = """
                { 
                    "receipt_number": ${transferResponse.tags["pt-number"]}, 
                    "last_four":  ${transferResponse.lastFour},
                    "brand":  ${transferResponse.cardBrand},
                    "state":  ${transferResponse.state},
                    "amount":  ${transferResponse.amount},
                    "service_fee":  ${transferResponse.serviceFee},
                    "tags":  ${transferResponse.tags},
                    "created_at":  "${transferResponse.createdAt}",
                    "updated_at":  "${transferResponse.updatedAt}"
                 }"""

                var paymentResponse = Gson().fromJson(json, PaymentResult::class.java)
                transaction.context.paymentComplete(paymentResponse)
            }
            "FAILURE" -> {
                val json = """
                { 
                    "receipt_number": ${responseJson["receipt_number"]}, 
                    "last_four": ${responseJson["last_four"]}, 
                    "brand": ${responseJson["brand"]}, 
                    "state": ${responseJson["state"]}, 
                    "type" : ${responseJson["type"]}
                 }"""

                var failedResponse = Gson().fromJson(json, PaymentResult::class.java)
                transaction.context.paymentFailed(failedResponse)
            }
            else -> {
                val json = """
                { 
                    "error": ${responseJson["error"]}, 
                 }"""

                val errorResponse = Gson().fromJson(json, PaymentError::class.java)
                transaction.context.paymentError(errorResponse)
            }
        }
    }
}

