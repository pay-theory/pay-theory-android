package com.paytheory.android.sdk.websocket

interface WebsocketMessageHandler {
    fun receiveMessage(message:String)
}