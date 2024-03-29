package com.paytheory.android.sdk.websocket

import com.paytheory.android.sdk.PaymentMethodToken
import com.paytheory.android.sdk.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

/**
 * Class that manages WebSocket actions: start, send message, stop
 */
class WebServicesProvider(private val transaction: Transaction?, private val paymentMethodToken: PaymentMethodToken?) {

    private var _webSocket: WebSocket? = null



    private val socketOkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(39, TimeUnit.SECONDS)
        .hostnameVerifier ( hostnameVerifier = { _, _ -> true })
        .build()

    @ExperimentalCoroutinesApi
    private var _webSocketListener: WebSocketListener? = null

    /**
     * Function to create WebSocket and attach a WebSocket listener
     * @param ptToken token that is added to WebSocket messages for security
     */
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken: String, partner: String, stage: String): Channel<SocketUpdate> =
        with(WebSocketListener(transaction, paymentMethodToken)) {
            startSocket(this, ptToken, partner, stage)
            this@with.socketEventChannel
        }

    /**
     * Function to create WebSocket and attach URL
     * @param ptToken token that is added to WebSocket messages for security
     * @param webSocketListener WebSocket listener
     */
    @ExperimentalCoroutinesApi
    fun startSocket(webSocketListener: WebSocketListener, ptToken: String, partner: String, stage: String) {
        _webSocketListener = webSocketListener
        _webSocket = socketOkHttpClient.newWebSocket(
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
        _webSocket?.send(message)
    }

    /**
     * Function to stop WebSocket connection
     */
    @ExperimentalCoroutinesApi
    fun stopSocket() {
//        println("Pay Theory Requested Disconnect")
        try {
            _webSocket?.close(NORMAL_CLOSURE_STATUS, null)
            _webSocket = null
            _webSocketListener?.socketEventChannel?.close()
            _webSocketListener = null
//            println("Pay Theory Disconnected stopSocket")
        } catch (ex: IllegalArgumentException) {
            println("error closing socket ${ex.message}")
            _webSocket = null
            _webSocketListener = null
        }
    }

    companion object {
        const val NORMAL_CLOSURE_STATUS = 1000
    }

}