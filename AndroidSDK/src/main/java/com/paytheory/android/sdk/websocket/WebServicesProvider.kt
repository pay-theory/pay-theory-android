package com.paytheory.android.sdk.websocket

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit

/**
 * Class that manages WebSocket actions: start, send message, stop
 */
class WebServicesProvider {
    val normalClosureStatus = 1000
    private var webSocket: WebSocket? = null



    private val socketOkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(39, TimeUnit.SECONDS)
        .hostnameVerifier ( hostnameVerifier = { _, _ -> true })
        .build()

    @OptIn(DelicateCoroutinesApi::class)
    @ExperimentalCoroutinesApi
    private var webSocketListener: WebSocketListener? = null

    /**
     * Function to create WebSocket and attach a WebSocket listener
     * @param ptToken token that is added to WebSocket messages for security
     */
    @OptIn(DelicateCoroutinesApi::class)
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken: String, partner: String, stage: String): Channel<SocketUpdate> =
        with(WebSocketListener()) {
            startSocket(this, ptToken, partner, stage)
            this@with.socketEventChannel
        }

    /**
     * Function to create WebSocket and attach URL
     * @param ptToken token that is added to WebSocket messages for security
     * @param webSocketListener WebSocket listener
     */
    @OptIn(DelicateCoroutinesApi::class)
    @ExperimentalCoroutinesApi
    fun startSocket(webSocketListener: WebSocketListener, ptToken: String, partner: String, stage: String) {
        this.webSocketListener = webSocketListener
        webSocket = socketOkHttpClient.newWebSocket(
            Request.Builder().url("wss://${partner}.secure.socket.${stage}.com/${partner}?pt_token=${ptToken}")
                .build(),
            webSocketListener
        )

        socketOkHttpClient.dispatcher.executorService.shutdown()
    }

    /**
     * Function to send messages to the server of WebSocket
     * @param message message that will be sent to server
     */
    @ExperimentalCoroutinesApi
    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    /**
     * Function to stop WebSocket connection
     */
    @OptIn(DelicateCoroutinesApi::class)
    @ExperimentalCoroutinesApi
    fun stopSocket() {
//        println("Pay Theory Requested Disconnect")
        try {
            webSocket?.close(normalClosureStatus, null)
            webSocket = null
            webSocketListener?.socketEventChannel?.close()
            webSocketListener = null
//            println("Pay Theory Disconnected stopSocket")
        } catch (ex: IllegalArgumentException) {
            println("error closing socket ${ex.message}")
            webSocket = null
            webSocketListener = null
        }
    }

}