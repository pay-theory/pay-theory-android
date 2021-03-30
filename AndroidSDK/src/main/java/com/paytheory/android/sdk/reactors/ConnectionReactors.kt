package com.paytheory.android.sdk.reactors

import ActionRequest
import HostTokenRequest
import com.google.gson.Gson
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

/**
 * Creates WebSocket connection reactors
 * @param ptToken pt-token string
 * @param attestation attestation string
 * @param viewModel viewModel for WebSocket
 * @param websocketInteractor interactor for the WebSocket
 */
class ConnectionReactors(
    private val ptToken: String,
    private val attestation: String,
    private val viewModel: WebSocketViewModel,
    private val websocketInteractor: WebsocketInteractor) {

    /**
     * Function that will send socket message when connected
     */
    @ExperimentalCoroutinesApi
    fun onConnected() {
        val hostTokenRequest =
            HostTokenRequest(ptToken, "native", attestation, System.currentTimeMillis())
        val encoded =
            Base64.getEncoder().encodeToString(Gson().toJson(hostTokenRequest).toByteArray())
        val actionRequest = ActionRequest("host:hostToken", encoded)
        viewModel.sendSocketMessage(Gson().toJson(actionRequest))
    }

    /**
     * Function that will stop socket interactions when disconnected
     */
    @ExperimentalCoroutinesApi
    fun onDisconnected() {
        websocketInteractor.stopSocket()
    }
}