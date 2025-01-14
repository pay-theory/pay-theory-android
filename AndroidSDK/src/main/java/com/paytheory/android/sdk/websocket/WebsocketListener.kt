package com.paytheory.android.sdk.websocket

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
class WebSocketListener : WebSocketListener() {
    val socketEventChannel: Channel<SocketUpdate> = Channel(10)
    /**
     * normal closure status constant
     *
     * @constructor Create empty Normal closure status
     */
    val normalClosureStatus = 1000
    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                println("Pay Theory Connected")
                socketEventChannel.send(SocketUpdate("connected to socket"))
            }
        }
    }

    /**
     * On message
     *
     * @param webSocket web socket
     * @param text text string
     * Function that sends message update to channel
     */
    override fun onMessage(webSocket: WebSocket, text: String) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                socketEventChannel.send(SocketUpdate(text))
            }
        }
    }

    /**
     * On closing
     *
     * @param webSocket web socket
     * @param code code
     * @param reason reason
     * Function that sends closing update to channel
     */
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            GlobalScope.launch {
                try {
                    socketEventChannel.send(SocketUpdate(exception = SocketAbortedException()))
                    println("Pay Theory Disconnected")
                } catch (e: ClosedSendChannelException) {
                    val error = e.message.toString()
                    println(error)
                }
            }
        webSocket.close(normalClosureStatus, null)
        socketEventChannel.close()
        println("Pay Theory Disconnected")
    }

    
    /**
     * On failure
     *
     * @param webSocket web socket
     * @param t throwable
     * @param response response
     * Function that sends failure update to channel
     */
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                socketEventChannel.send(SocketUpdate(exception = t))
            }
        }
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