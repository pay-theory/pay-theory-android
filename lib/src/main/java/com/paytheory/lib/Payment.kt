package com.paytheory.lib

import com.google.gson.Gson
import com.goterl.lazysodium.utils.Key
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.requests.ActionRequest
import com.paytheory.lib.data.requests.CashRequest
import com.paytheory.lib.data.payloads.PaymentData
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.payloads.PaymentMethodData
import com.paytheory.lib.data.requests.TransferPartOneRequest
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.nacl.encryptBox
import com.paytheory.lib.nacl.generateLocalKeyPair
import com.paytheory.lib.reactors.ConnectionReactors
import com.paytheory.lib.reactors.MessageReactors
import com.paytheory.lib.websocket.WebsocketMessageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Base64

/**
 * The `Payment` class orchestrates the payment process using Pay Theory's SDK.
 * It handles communication with the Pay Theory backend, including establishing a secure websocket connection,
 * sending payment requests, and receiving responses. It integrates with Google Play Integrity API
 * for enhanced security.
 *
 * @property packageNamed The application package name.
 * @property contextIn The application context that implements the `Payable` interface.
 * @property payTheoryDataIn Additional data to be sent with the payment request.
 * @property configurationIn PayTheoryConfiguration data class with api key, and metadata
 * @property viewModel PaymentViewModel object to manage payment related data
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Payment(
    packageNamed: String,
    contextIn: Payable,
    payTheoryDataIn: HashMap<Any, Any>? = hashMapOf(),
    configurationIn: PayTheoryConfiguration,
    viewModel: PaymentViewModel
) : PaymentMethodProcessor(contextIn, payTheoryDataIn, configurationIn, viewModel),
    WebsocketMessageHandler {

    /**
     * The package name of the calling application.
     */
    val packageName = packageNamed

    /**
     * A queue for payment requests that are made before the websocket is connected.
     */
    var queuedRequest: PaymentDetail? = null

    /**
     * Determines the type of message received from the websocket.
     *
     * @param message The message received from the websocket.
     * @return The type of message received, represented as a string.
     */
    private fun getWebSocketMessageType(message: String): String {
        return when {
            message.indexOf(COMPLETED_TRANSFER) > -1 -> COMPLETED_TRANSFER
            message.indexOf(BARCODE_RESULT) > -1 -> BARCODE_RESULT
            message.indexOf(TRANSFER_PART_ONE_RESULT) > -1 -> TRANSFER_PART_ONE_RESULT
            message.indexOf(HOST_TOKEN_RESULT) > -1 -> HOST_TOKEN_RESULT
            message.indexOf(WALLET_TRANSACTION_RESULT) > -1 -> WALLET_TRANSACTION_RESULT
            else -> UNKNOWN
        }
    }

    /**
     * Handles incoming messages from the websocket, triggering appropriate actions based on the message type.
     *
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
                when (getWebSocketMessageType(message)) {
                    HOST_TOKEN_RESULT -> messageReactors!!.onHostToken(message, this)
                    BARCODE_RESULT -> messageReactors!!.onBarcode(message, viewModel, this)
                    COMPLETED_TRANSFER -> messageReactors!!.completeTransaction(
                        message,
                        viewModel,
                        this
                    )
                    WALLET_TRANSACTION_RESULT -> messageReactors!!.onWalletTransaction(
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
        viewModel.disconnect()
    }

    /**
     * Establishes the ViewModel and other necessary components for communication with the Pay Theory backend.
     *
     * @param ptTokenResponse The `PTTokenResponse` object containing the Pay Theory token.
     * @param attestationResult The result of the Google Play Integrity check.
     */
    override fun establishViewModel(
        ptTokenResponse: PTTokenResponse,
        attestationResult: String?
    ) {

        connectionReactors = ConnectionReactors(
            ptTokenResponse.ptToken,
            attestationResult!!,
            viewModel,
            packageName,
            origin = "android"
        )
        messageReactors = MessageReactors(viewModel)
        viewModel.subscribeToSocketEvents(this, ptTokenResponse)
        if (queuedRequest != null)
            messageReactors!!.activePaymentDetail = queuedRequest

    }

    /**
     * Initiates the payment transaction by sending the payment details to the Pay Theory backend.
     *
     * @param payment The `PaymentDetail` object containing the payment information.
     */
    override fun process(
        payment: PaymentDetail
    ) {

        messageReactors!!.activePaymentDetail = payment
        val actionRequest = createInitialActionRequest(payment)
        if (viewModel.connected) {

            payable.handlePaymentStart(payment.type)
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Payment Requested")

        } else {
            queuedRequest = payment
            ptTokenApiCall(payable)
            println("Pay Theory Resetting Connection")
        }


    }

    /**
     * Generates the initial action request to be sent to the Pay Theory backend.
     *
     * This method determines whether the payment is a cash payment or a transfer, and
     * creates the appropriate `ActionRequest` object with encrypted payment details.
     *
     * @param payment The `PaymentDetail` object containing the payment information.
     * @return The generated `ActionRequest` object representing the initial request.
     */
    override fun createInitialActionRequest(payment: PaymentDetail): ActionRequest {
        //generate public key
        val keyPair = generateLocalKeyPair()
        publicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        //if payment type is "CASH" return cash com.paytheory.lib.data.requests.ActionRequest
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
        //if payment type is not "CASH" return transfer com.paytheory.lib.data.requests.ActionRequest
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
                this.hostToken,
                paymentMethodData,
                paymentData,
                payment.payorInfo,
                this.payTheoryData,
                configuration.metadata,
                sessionKey,
                System.currentTimeMillis()
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
}