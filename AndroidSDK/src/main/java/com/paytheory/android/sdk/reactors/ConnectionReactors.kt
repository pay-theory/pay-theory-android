package com.paytheory.android.sdk.reactors

import com.google.gson.Gson
import com.paytheory.android.sdk.data.ActionRequest
import com.paytheory.android.sdk.data.HostTokenRequest
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Base64

/**
 * Class that handles connection reactors for the WebSocket.
 *
 * @param ptToken The PayTheory token.
 * @param attestation The attestation string.
 * @param viewModel The WebSocketViewModel instance.
 * @param websocketInteractor The WebsocketInteractor instance.
 * @param applicationPackageName The application's package name.
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
     * Called when the WebSocket is connected. Sends a host token action request.
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
     * Called when the WebSocket is disconnected. Stops the socket interactions.
     */
    @ExperimentalCoroutinesApi
    fun onDisconnected() {
        websocketInteractor.stopSocket()
    }
}