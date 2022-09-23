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
@ExperimentalCoroutinesApi
class ConnectionReactors(
    private val ptToken: String,
    private val attestation: String,
    private val viewModel: WebSocketViewModel,
    private val websocketInteractor: WebsocketInteractor) {

    companion object {
        private const val HOST_ACTION = "host:hostToken"
    }

    /**
     * Called on websocket connection, creates the host token action request
     */
    @ExperimentalCoroutinesApi
    fun onConnected() {

        val requestData = HostTokenRequest(ptToken, attestation, System.currentTimeMillis(), "android")

        val encodedBody = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(Gson().toJson(requestData).toByteArray())
        } else {
            android.util.Base64.encodeToString(Gson().toJson(requestData).toByteArray(),android.util.Base64.DEFAULT)
        }

        val actionRequest = ActionRequest(HOST_ACTION, encodedBody)
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