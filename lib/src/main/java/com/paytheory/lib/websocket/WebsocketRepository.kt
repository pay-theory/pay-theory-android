package com.paytheory.lib.websocket

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

/**
 * Creates WebSocket repository to start, stop and send messages
 * @param webServicesProvider WebSocket web services provider
 */
class WebsocketRepository(private val webServicesProvider: WebServicesProvider) {

    /**
     * Function to start WebSocket
     * @param ptToken token used for security
     */
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String, partner: String, stage: String): Channel<SocketUpdate> =
        webServicesProvider.startSocket(ptToken, partner, stage)

    /**
     * Function to send messages though a WebSocket
     */
    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        webServicesProvider.sendMessage(message)
    }

    /**
     * Function to close WebSocket
     */
    @ExperimentalCoroutinesApi
    fun closeSocket() {
        webServicesProvider.stopSocket()
    }
}