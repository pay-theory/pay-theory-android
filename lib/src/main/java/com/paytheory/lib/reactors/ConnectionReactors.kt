package com.paytheory.lib.reactors

import com.google.gson.Gson
import com.paytheory.lib.data.ActionRequest
import com.paytheory.lib.data.ErrorCode
import com.paytheory.lib.data.HostTokenRequest
import com.paytheory.lib.data.PTError
import com.paytheory.lib.model.PaymentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Base64

/**
 * Handles WebSocket connection events and related actions for the Pay Theory payment system.
 *
 * This class manages the connection lifecycle events and performs necessary actions when
 * connections are established or terminated. It handles token management and authentication
 * for secure payment processing.
 *
 * @property ptToken The Pay Theory authentication token
 * @property attestation The attestation string for security verification
 * @property paymentViewModel The view model managing payment state
 * @property applicationPackageName The Android application package name
 * @property origin The origin identifier for the connection
 */
class ConnectionReactors(
    private val ptToken: String,
    private val attestation: String,
    private val paymentViewModel: PaymentViewModel,
    private val applicationPackageName: String,
    private val origin: String,
) {

    /**
     * Handles successful WebSocket connection establishment.
     *
     * When a connection is established, this method:
     * 1. Creates a host token request with current timestamp and authentication details
     * 2. Encodes the request data in Base64 format
     * 3. Sends the encoded request through the WebSocket connection
     *
     * If any error occurs during this process, it will be handled through the payment view model's
     * error handling mechanism.
     *
     * @throws PTError with [ErrorCode.SocketError] if there's an error sending the socket message
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun onConnected() {
        try {
            val requestData = HostTokenRequest(
                ptToken,
                attestation,
                System.currentTimeMillis(),
                origin,
                applicationPackageName
            )
            val encodedBody = Base64.getEncoder()
                .encodeToString(Gson().toJson(requestData).toByteArray())
            val actionRequest = ActionRequest("host:hostToken", encodedBody)
            paymentViewModel.sendSocketMessage(Gson().toJson(actionRequest))
        } catch (e: Exception) {
            paymentViewModel.payTheoryProcessor.payable.handleError(PTError(ErrorCode.SocketError, "Error sending socket message: ${e.message}"))
        }
    }

    /**
     * Handles WebSocket connection termination.
     *
     * This method is called when the WebSocket connection is closed or lost.
     * It ensures proper cleanup by disconnecting the payment view model.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun onDisconnected() {
        paymentViewModel.disconnect()
    }
}