package com.paytheory.lib

import android.content.Context
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.PaymentMethodTokenData
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.reactors.ConnectionReactors
import com.paytheory.lib.reactors.MessageReactors
import com.paytheory.lib.websocket.WebsocketMessageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * PaymentMethodToken class that handles the communication and logic for tokenizing payment methods.
 *
 * This class utilizes websockets to communicate with the Pay Theory platform for secure tokenization.
 * It includes functionalities for establishing a connection, sending tokenization requests, and
 * receiving responses.
 *
 * @param payable The application context.
 * @param payTheoryData The PayTheory data object for additional information.
 * @param configuration The PayTheory configuration object for metadata and customization.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentMethodToken(
    packageNamed: String,
    payable: Payable,
    payTheoryDataIn: HashMap<Any, Any>? = hashMapOf(),
    configurationIn : PayTheoryConfiguration,
    viewModel: PaymentViewModel
) : PaymentMethodProcessor(payable,payTheoryDataIn, configurationIn,viewModel), WebsocketMessageHandler {

    var queuedRequest: PaymentMethodTokenData? = null

    /**
     * Discovers the message type based on the incoming message content.
     * @return String of message type
     * @param message The incoming message from the socket.
     */
    private fun getWebSocketMessageType(message: String): String {
        return when {
            message.indexOf(TOKENIZE_RESULT) > -1 -> TOKENIZE_RESULT
            message.indexOf(HOST_TOKEN_RESULT) > -1 -> HOST_TOKEN_RESULT
            else -> UNKNOWN
        }
    }

    /**
     * Function to call next action based on incoming socket message
     * @param message the incoming message from socket
     */
    override fun receiveMessage(message: String) {
        when (message) {
            CONNECTED -> {
                connectionReactors!!.onConnected()
            }
            DISCONNECTED -> {
                connectionReactors!!.onDisconnected()
            }
            INTERNAL_SERVER_ERROR -> {
                messageReactors!!.onTokenError(message, this)
            }
            else -> {
                when (getWebSocketMessageType(message)) {
                    HOST_TOKEN_RESULT -> messageReactors!!.onTokenizeHostToken(message, this)
                    TOKENIZE_RESULT -> messageReactors!!.onCompleteToken(message, this)
                    else -> messageReactors!!.onTokenError(message, this)
                }
            }
        }
    }

    /**
     * Disconnects the websocket connection.
     */
    override fun disconnect() {
        viewModel.disconnect()
    }

    override fun establishViewModel(
        ptTokenResponse: PTTokenResponse,
        attestationResult: String?
    ) {
        var packaged = "com.unit.test"
        if (this.payable is Context) packaged = (this.payable as Context).applicationContext.packageName
        connectionReactors = ConnectionReactors(
            ptTokenResponse.ptToken,
            attestationResult!!,
            viewModel,
            packaged,
            origin = "android"
        )
        messageReactors = MessageReactors(viewModel)
        viewModel.subscribeToSocketEvents(this,ptTokenResponse)
        if (queuedRequest != null)
            messageReactors!!.activePaymentToken = queuedRequest

        updatePayableReadyState(true)
    }
}