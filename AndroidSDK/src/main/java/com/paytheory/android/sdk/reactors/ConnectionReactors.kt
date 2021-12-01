package com.paytheory.android.sdk.reactors

import ActionRequest
import com.google.gson.Gson
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
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

    companion object {
        private const val HOST_ACTION = "host:hostToken"
    }

    /**
     * Called on websocket connection, creates the host token action request
     */
    @ExperimentalCoroutinesApi
    fun onConnected() {
        val requestData = JSONObject()
        requestData.put("ptToken",ptToken)
        requestData.put("origin","native")
        requestData.put("attestation",attestation)
        requestData.put("timing", System.currentTimeMillis().toString())

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