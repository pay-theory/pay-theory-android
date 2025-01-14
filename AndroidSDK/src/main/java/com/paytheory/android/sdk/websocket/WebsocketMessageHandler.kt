package com.paytheory.android.sdk.websocket

/**
 * Interface that handles WebSocket messages
 */
interface WebsocketMessageHandler {
    /**
     * Function that handles receiving a message from the websocket
     *
     * @param message The message received from the websocket
     */
    fun receiveMessage(message:String)
    /**
     * Function that handles the closing of the websocket
     *
     * This function is called when the websocket is closed, either by the server or by the client.
     * It can be used to perform any necessary cleanup or to display a message to the user.
     */
    fun disconnect()
}