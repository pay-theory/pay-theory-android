package com.paytheory.android.sdk

import android.content.Context
import com.google.gson.Gson
import com.goterl.lazysodium.utils.Key
import com.paytheory.android.sdk.api.PTTokenResponse
import com.paytheory.android.sdk.data.ActionRequest
import com.paytheory.android.sdk.data.PaymentMethodData
import com.paytheory.android.sdk.data.PaymentMethodTokenData
import com.paytheory.android.sdk.data.TokenizeRequest
import com.paytheory.android.sdk.nacl.encryptBox
import com.paytheory.android.sdk.nacl.generateLocalKeyPair
import com.paytheory.android.sdk.reactors.ConnectionReactors
import com.paytheory.android.sdk.reactors.MessageReactors
import com.paytheory.android.sdk.websocket.WebServicesProvider
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import com.paytheory.android.sdk.websocket.WebsocketMessageHandler
import com.paytheory.android.sdk.websocket.WebsocketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Base64

/**
 * PaymentMethodToken class that handles the communication and logic for tokenizing payment methods.
 *
 * This class utilizes websockets to communicate with the Pay Theory platform for secure tokenization.
 * It includes functionalities for establishing a connection, sending tokenization requests, and
 * receiving responses.
 *
 * @param context The application context.
 * @param partner The partner identifier.
 * @param stage The environment stage (e.g., "sandbox", "production").
 * @param constants The constants object for configuration.
 * @param payTheoryData The PayTheory data object for additional information.
 * @param configuration The PayTheory configuration object for metadata and customization.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentMethodToken(
    override val context: Payable,
    override val partner: String,
    override val stage: String,
    override val constants: Constants,
    override val payTheoryData: HashMap<Any, Any>? = hashMapOf(),
    override val configuration : PayTheoryConfiguration
) : PaymentMethodProcessor(context,partner,stage,constants,payTheoryData, configuration), WebsocketMessageHandler {

    var queuedRequest: PaymentMethodTokenData? = null
    /**
     * Final api call to complete transaction
     * @param paymentMethodTokenData Data object of payment method
     */
    fun tokenize(
        paymentMethodTokenData: PaymentMethodTokenData
    ) {
        messageReactors!!.activePaymentToken = paymentMethodTokenData
        val actionRequest = generateInitialActionRequest(paymentMethodTokenData)
        if (viewModel.connected) {
            context.handleTokenStart(paymentMethodTokenData.type)

            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Token Requested")
        } else {
            queuedRequest = paymentMethodTokenData
            ptTokenApiCall(context as Context)
            println("Pay Theory Resetting Connection")
        }
    }

    /**
     * Generates the initial action request for tokenization.
     * @return ActionRequest Object to send through websocket
     * @param paymentMethodTokenData The payment method token data.
     */
    private fun generateInitialActionRequest(paymentMethodTokenData: PaymentMethodTokenData): ActionRequest {
        //generate public key
        val keyPair = generateLocalKeyPair()
        publicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        val requestAction = TOKENIZE
        val paymentMethodData = PaymentMethodData(
            paymentMethodTokenData.name,
            paymentMethodTokenData.number,
            paymentMethodTokenData.security_code,
            paymentMethodTokenData.type,
            paymentMethodTokenData.expiration_year,
            paymentMethodTokenData.expiration_month,
            paymentMethodTokenData.address,
            paymentMethodTokenData.account_number,
            paymentMethodTokenData.account_type,
            paymentMethodTokenData.bank_code
        )
        val tokenRequest = TokenizeRequest(
            this.hostToken,
            paymentMethodData,
            paymentMethodTokenData.payorInfo,
            this.payTheoryData,
            configuration.metadata,
            sessionKey,
            System.currentTimeMillis()
        )
        val encryptedBody = encryptBox(
            Gson().toJson(tokenRequest),
            Key.fromBase64String(messageReactors!!.socketPublicKey)
        )
        return ActionRequest(
            requestAction,
            encryptedBody,
            publicKey,
            sessionKey
        )
    }

    /**
     * Discovers the message type based on the incoming message content.
     * @return String of message type
     * @param message The incoming message from the socket.
     */
    private fun discoverMessageType(message: String): String {
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
                when (discoverMessageType(message)) {
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
        if (webSocketInteractor != null) {
            webSocketInteractor!!.stopSocket()
        }
    }

    override fun establishViewModel(
        ptTokenResponse: PTTokenResponse,
        attestationResult: String?
    ) {
        webServicesProvider = WebServicesProvider()
        webSocketRepository = WebsocketRepository(webServicesProvider!!)
        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)
        viewModel = WebSocketViewModel(
            webSocketInteractor!!,
            ptTokenResponse.ptToken,
            partner,
            stage,
            this,
            null
        )
        connectionReactors = ConnectionReactors(
            ptTokenResponse.ptToken,
            attestationResult!!,
            viewModel,
            webSocketInteractor!!,
            (this.context as Context).applicationContext.packageName
        )
        messageReactors = MessageReactors(viewModel, webSocketInteractor!!)
        viewModel.subscribeToSocketEvents(this)
        if (queuedRequest != null)
            messageReactors!!.activePaymentToken = queuedRequest

        updatePayableReadyState(true)
    }
}