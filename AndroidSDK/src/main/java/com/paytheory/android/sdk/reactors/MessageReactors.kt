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
import com.paytheory.android.sdk.nacl.encryptBox
import com.paytheory.android.sdk.nacl.generateLocalKeyPair
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
     * @param apiKey api-key used for transaction
     */
    fun onHostToken(message: String): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.publicKey
        sessionKey = hostTokenMessage.sessionKey
        hostToken = hostTokenMessage.hostToken
        return hostTokenMessage
    }

    /**
     * Function that creates a message for a unknown action
     * @param message message to be sent
     * @param apiKey api-key used for transaction
     */
    fun onUnknown(message: String): Any {
        return Gson().fromJson(message, Any::class.java)
    }

    /**
     * Function that creates a message for a instrument action
     * @param message message to be sent
     * @param apiKey api-key used for transaction
     */
    @ExperimentalCoroutinesApi
    fun onInstrument(message:String, apiKey:String): InstrumentMessage {
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
     * Function that creates a message for a idempotency action
     * @param message message to be sent
     * @param apiKey api-key used for transaction
     */
    @ExperimentalCoroutinesApi
    fun onIdempotency(message: String): IdempotencyMessage {
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
     * Function that creates a message for a transfer action
     * @param message message to be sent
     * @param apiKey api-key used for transaction
     */
    @ExperimentalCoroutinesApi
    fun onTransfer(message: String): TransferMessage {
        return Gson().fromJson(message, TransferMessage::class.java)
    }
}

