package com.paytheory.android.sdk.reactors

import ActionRequest
import HostTokenRequest
import com.google.gson.Gson
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

class ConnectionReactors(private val ptToken: String, private val viewModel: WebSocketViewModel, private val websocketInteractor: WebsocketInteractor) {
    @ExperimentalCoroutinesApi
    fun onConnected() {
        val hostTokenRequest =
            HostTokenRequest(ptToken, "native", System.currentTimeMillis())
        val encoded =
            Base64.getEncoder().encodeToString(Gson().toJson(hostTokenRequest).toByteArray())
        val actionRequest = ActionRequest("host:hostToken", encoded)
        viewModel.sendSocketMessage(Gson().toJson(actionRequest))
    }

    @ExperimentalCoroutinesApi
    fun onDisconnected() {
        websocketInteractor.stopSocket()
    }
}