package com.paytheory.android.sdk.websocket

import android.content.Context
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.PaymentMethodToken
import com.paytheory.android.sdk.Transaction
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

/**
 * Create a WebSocket listener
 */
@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
class WebSocketListener(private val transaction: Transaction?, private val paymentMethodToken: PaymentMethodToken?) : WebSocketListener() {
    val socketEventChannel: Channel<SocketUpdate> = Channel(10)

    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                println("Pay Theory Connected")
                socketEventChannel.send(SocketUpdate("connected to socket"))
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                socketEventChannel.send(SocketUpdate(text))
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            GlobalScope.launch {
                try {
                    socketEventChannel.send(SocketUpdate(exception = SocketAbortedException()))
                    println("Pay Theory Disconnected onClosing")
                } catch (e: ClosedSendChannelException) {
                    println("Pay Theory Already Disconnected")
                    val error = e.message.toString()
                    println(error)
                    if (error.contains("Channel was closed")){
                        if (transaction != null){ // error for transaction request
                            if (transaction.context is Payable){
                                println("transaction reset socket")
                                transaction.resetSocket()
                            }
                        } else if (paymentMethodToken != null){
                            if (paymentMethodToken.context is Payable){
                                println("paymentMethodToken reset socket")
                                paymentMethodToken.resetSocket()
                            }
                        } else {
                            println("Cannot Reconnect to Pay Theory")
                        }
                    }
                }
            }
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        socketEventChannel.close()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                socketEventChannel.send(SocketUpdate(exception = t))
            }
        }
    }
    companion object {
        const val NORMAL_CLOSURE_STATUS = 1000
    }
}

/**
 * Create a exception class for WebSocket errors
 */
class SocketAbortedException : Exception()

/**
 * Data class that hold all WebSocket update data
 * @param text text string
 * @param byteString byte string
 * @param exception throwable exception for errors
 */
data class SocketUpdate(
    val text: String? = null,
    val byteString: ByteString? = null,
    val exception: Throwable? = null
)