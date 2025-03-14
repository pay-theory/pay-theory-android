package com.paytheory.lib.reactors

import com.google.gson.Gson
import com.paytheory.lib.ErrorCode
import com.paytheory.lib.PTError
import com.paytheory.lib.data.ActionRequest
import com.paytheory.lib.data.HostTokenRequest
import com.paytheory.lib.model.PaymentViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Base64

class ConnectionReactors(
    private val ptToken: String,
    private val attestation: String,
    private val paymentViewModel: PaymentViewModel,
    private val applicationPackageName: String,
    private val origin: String,
) {

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
            paymentViewModel.payTheoryPayment.payable.handleError(PTError(ErrorCode.SocketError, "Error sending socket message: ${e.message}"))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onDisconnected() {
        paymentViewModel.disconnect()
    }
}