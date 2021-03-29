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

class MessageReactors(private val viewModel: WebSocketViewModel, private val webSocketInteractor: WebsocketInteractor) {
    var activePayment: Payment? = null
    var hostToken = ""
    var sessionKey = ""
    var socketPublicKey = ""


    fun onHostToken(message:String, apiKey:String): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson<HostTokenMessage>(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.publicKey
        sessionKey = hostTokenMessage.sessionKey
        hostToken = hostTokenMessage.hostToken
        return hostTokenMessage
    }
    fun onUnknown(message:String, apiKey:String): Any {
        return Gson().fromJson<Any>(message, Any::class.java)
    }

    @ExperimentalCoroutinesApi
    fun onInstrument(message:String, apiKey:String): InstrumentMessage {
        val instrumentMessage = Gson().fromJson<InstrumentMessage>(message, InstrumentMessage::class.java)
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
    @ExperimentalCoroutinesApi
    fun onIdempotency(message:String, apiKey:String): IdempotencyMessage {
        val idempotencyMessage = Gson().fromJson<IdempotencyMessage>(message, IdempotencyMessage::class.java)

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
    @ExperimentalCoroutinesApi
    fun onTransfer(message:String, apiKey:String): TransferMessage {
        return Gson().fromJson<TransferMessage>(message, TransferMessage::class.java)
    }
}

