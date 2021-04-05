package com.paytheory.android.sdk.websocket

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

/**
 * Create a WebSocket listener
 */
@ExperimentalCoroutinesApi
class WebSocketListener : WebSocketListener() {

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
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                socketEventChannel.send(SocketUpdate(exception = SocketAbortedException()))
            }
        }
        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        socketEventChannel.close()
        println("Pay Theory Disconnected")
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