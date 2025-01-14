package com.paytheory.android.sdk

import android.content.Context
import com.google.gson.Gson
import com.goterl.lazysodium.utils.Key
import com.paytheory.android.sdk.api.PTTokenResponse
import com.paytheory.android.sdk.data.ActionRequest
import com.paytheory.android.sdk.data.CashRequest
import com.paytheory.android.sdk.data.PaymentData
import com.paytheory.android.sdk.data.PaymentDetail
import com.paytheory.android.sdk.data.PaymentMethodData
import com.paytheory.android.sdk.data.TransferPartOneRequest
import com.paytheory.android.sdk.data.TransferPartTwoRequest
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
 * The `Payment` class orchestrates the payment process using Pay Theory's SDK.
 * It handles communication with the Pay Theory backend, including establishing a secure websocket connection,
 * sending payment requests, and receiving responses. It integrates with Google Play Integrity API
 * for enhanced security.
 *
 * @param context The application context that implements the `Payable` interface.
 * @param partner Your Pay Theory partner identifier.
 * @param stage The environment to use (e.g., "paytheory", "paytheorystudy", "paytheorylab"). Defaults to "paytheory".
 * @param constants The constants object containing API endpoints and other configuration values.
 * @param payTheoryData Additional data to be sent with the payment request.
 * @param configuration PayTheoryConfiguration data class with api key, confirmation, and metadata
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Payment(
    override val context: Payable,
    override val partner: String,
    override val stage: String,
    override val constants: Constants,
    override val payTheoryData: HashMap<Any, Any>? = hashMapOf(),
    override val configuration : PayTheoryConfiguration
) : PaymentMethodProcessor(context,partner,stage,constants,payTheoryData, configuration), WebsocketMessageHandler {

    var queuedRequest: PaymentDetail? = null
    /**
     * Initiates the payment transaction by sending the payment details to the Pay Theory backend.
     * @param payment The `PaymentDetail` object containing the payment information.
     */
    fun transact(
        payment: PaymentDetail
    ) {
        messageReactors!!.activePaymentDetail = payment
        val actionRequest = generateInitialActionRequest(payment)
        if (viewModel.connected) {

            context.handlePaymentStart(payment.type)
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Payment Requested")

        } else {
            queuedRequest = payment
            ptTokenApiCall(context as Context)
            println("Pay Theory Resetting Connection")
        }
    }

    /**
     * Generates the initial action request to be sent to the Pay Theory backend.
     * @param payment The `PaymentDetail` object containing the payment information.
     * @return The generated `ActionRequest` object representing the initial request.
     */
    private fun generateInitialActionRequest(payment: PaymentDetail): ActionRequest {
        //generate public key
        val keyPair = generateLocalKeyPair()
        publicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        //if payment type is "CASH" return cash com.paytheory.android.sdk.data.ActionRequest
        if (payment.type == CASH) {
            val requestAction = BARCODE_ACTION
            val paymentRequest = CashRequest(
                this.hostToken,
                sessionKey,
                payment,
                System.currentTimeMillis(),
                payment.payorInfo,
                this.payTheoryData,
                configuration.metadata
            )
            val encryptedBody = encryptBox(
                Gson().toJson(paymentRequest),
                Key.fromBase64String(messageReactors!!.socketPublicKey)
            )
            return ActionRequest(
                requestAction,
                encryptedBody,
                publicKey,
                sessionKey
            )
        }
        //if payment type is not "CASH" return transfer com.paytheory.android.sdk.data.ActionRequest
        else {
            val requestAction = TRANSFER_PART_ONE_ACTION
            val paymentData = PaymentData(payment.currency, payment.amount, payment.fee_mode)
            val paymentMethodData = PaymentMethodData(
                payment.name,
                payment.number,
                payment.security_code,
                payment.type,
                payment.expiration_year,
                payment.expiration_month,
                payment.address,
                payment.account_number,
                payment.account_type,
                payment.bank_code
            )
            val paymentRequest = TransferPartOneRequest(
                this.hostToken, paymentMethodData, paymentData,
                configuration.confirmation!!, payment.payorInfo, this.payTheoryData,
                configuration.metadata, sessionKey, System.currentTimeMillis()
            )
            val encryptedBody = encryptBox(
                Gson().toJson(paymentRequest),
                Key.fromBase64String(messageReactors!!.socketPublicKey)
            )
            return ActionRequest(
                requestAction,
                encryptedBody,
                publicKey,
                sessionKey
            )
        }
    }

    /**
     * Sets the confirmation message to be used for sending the `host:transfer_part2` action request
     * after user confirmation.
     * @param confirmationMessage The `ConfirmationMessage` object containing the confirmation details.
     */
    fun setConfirmation(confirmationMessage: ConfirmationMessage) {
        this.originalConfirmation = confirmationMessage.copy()
    }

    /**
     * Completes the transfer process by sending the `host:transfer_part2` action request.
     */
    fun completeTransfer() {
        // only for ach payments, need to set expiration to null back to tags-secure-socket
        if (originalConfirmation!!.expiration.isNullOrBlank() && originalConfirmation!!.brand == "ACH") {
            originalConfirmation!!.expiration = ""
        }
        val requestBody = TransferPartTwoRequest(
            originalConfirmation!!,
            configuration.metadata,
            sessionKey,
            System.currentTimeMillis()
        )
        val encryptedBody = encryptBox(
            Gson().toJson(requestBody),
            Key.fromBase64String(messageReactors!!.socketPublicKey)
        )
        val actionRequest =
            ActionRequest(TRANSFER_PART_TWO_ACTION, encryptedBody, publicKey, sessionKey)
        if (viewModel.connected) {
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
        } else {
            if (context is Payable) {
                context.handleError(PTError(ErrorCode.SocketError,"System Connection Failed"))
            } else {
                return
            }
        }
    }

    /**
     * Determines the type of message received from the websocket.
     * @param message The message received from the websocket.
     * @return The type of message received, represented as a string.
     */
    private fun discoverMessageType(message: String): String {
        return when {
            message.indexOf(COMPLETED_TRANSFER) > -1 -> COMPLETED_TRANSFER
            message.indexOf(BARCODE_RESULT) > -1 -> BARCODE_RESULT
            message.indexOf(TRANSFER_PART_ONE_RESULT) > -1 -> TRANSFER_PART_ONE_RESULT
            message.indexOf(HOST_TOKEN_RESULT) > -1 -> HOST_TOKEN_RESULT
            else -> UNKNOWN
        }
    }

    /**
     * Handles incoming messages from the websocket, triggering appropriate actions based on the message type.
     * @param message The message received from the websocket.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun receiveMessage(message: String) {
        when (message) {
            CONNECTED -> {
                //println("Reconnect Counter: $resetCounter")
                connectionReactors!!.onConnected()
            }
            DISCONNECTED -> {
                connectionReactors!!.onDisconnected()
            }
            INTERNAL_SERVER_ERROR -> {
                messageReactors!!.onError(message, this)
            }
            else -> {
                when (discoverMessageType(message)) {
                    HOST_TOKEN_RESULT -> messageReactors!!.onHostToken(message, this)
                    TRANSFER_PART_ONE_RESULT -> messageReactors!!.confirmPayment(message, this)
                    BARCODE_RESULT -> messageReactors!!.onBarcode(message, viewModel, this)
                    COMPLETED_TRANSFER -> messageReactors!!.completeTransaction(
                        message,
                        viewModel,
                        this
                    )
                    else -> messageReactors!!.onError(message, this)
                }
            }
        }
    }

    /**
     * Disconnects from the websocket, stopping the socket connection.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun disconnect() {
        if (webSocketInteractor != null) {
            webSocketInteractor!!.stopSocket()
        }
    }

    /**
     * Establishes the ViewModel and other necessary components for communication with the Pay Theory backend.
     * @param ptTokenResponse The `PTTokenResponse` object containing the Pay Theory token.
     * @param attestationResult The result of the Google Play Integrity check.
     */
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
            messageReactors!!.activePaymentDetail = queuedRequest

        updatePayableReadyState(true)
    }
}