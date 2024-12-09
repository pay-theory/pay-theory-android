package com.paytheory.android.sdk.reactors

import com.google.gson.Gson
import com.paytheory.android.sdk.data.ActionRequest
import com.paytheory.android.sdk.data.HostTokenRequest
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Base64

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
    private val websocketInteractor: WebsocketInteractor,
    private val applicationPackageName: String) {

    companion object {
        private const val HOST_ACTION = "host:hostToken"
    }

    /**
     * Called on websocket connection, creates the host token action request
     */
    @ExperimentalCoroutinesApi
    fun onConnected() {

        val requestData = HostTokenRequest(ptToken, attestation, System.currentTimeMillis(), "android", applicationPackageName)

        val encodedBody =
            Base64.getEncoder().encodeToString(Gson().toJson(requestData).toByteArray())

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