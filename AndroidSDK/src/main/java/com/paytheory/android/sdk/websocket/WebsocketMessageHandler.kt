package com.paytheory.android.sdk.websocket

/**
 * Interface that handles WebSocket messages
 */
interface WebsocketMessageHandler {
    /**
     * Function that handles a received message
     */
    fun receiveMessage(message:String)
    /**
     * Function that handles a disconnect
     */
    fun disconnect()
}