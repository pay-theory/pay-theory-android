package com.paytheory.android.sdk.sockets

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class EchoWebSocketListener : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        webSocket.send("Hello It is me")
    }

    override fun onMessage(webSocket: WebSocket, text: String){
        super.onMessage(webSocket, text)
        outputData("Receiving $text")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        outputData("$code $reason")
    }

    private fun outputData(outputString: String) {
        Log.d("web socket", outputString)
    }
}