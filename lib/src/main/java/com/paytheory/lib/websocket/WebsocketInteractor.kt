package com.paytheory.lib.websocket

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

/**
 * Creates WebSocket interactor to start, stop and send messages
 * @param repository WebSocket repository
 */
class WebsocketInteractor(private val repository: WebsocketRepository) {

    /**
     * Function to close WebSocket
     */
    @ExperimentalCoroutinesApi
    fun stopSocket() {
        repository.closeSocket()
    }

    /**
     * Function to send messages though a WebSocket
     */
    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        repository.sendMessage(message)
    }

    /**
     * Function to start WebSocket
     * @param ptToken token used for security
     */
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String, partner: String, stage: String): Channel<SocketUpdate> = repository.startSocket(ptToken, partner, stage)

}