package com.paytheory.lib

import com.google.gson.Gson
import com.goterl.lazysodium.utils.Key
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.data.payloads.PaymentMethodData
import com.paytheory.lib.data.requests.ActionRequest
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.requests.TokenizeRequest
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.nacl.encryptBox
import com.paytheory.lib.nacl.generateLocalKeyPair
import com.paytheory.lib.reactors.ConnectionReactors
import com.paytheory.lib.reactors.MessageReactors
import com.paytheory.lib.websocket.WebsocketMessageHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.util.Base64

/**
 * `PaymentMethodToken` class manages the communication and logic for tokenizing payment methods.
 *
 * This class facilitates secure tokenization by using websockets to interact with the Pay Theory platform.
 * It handles establishing a connection, sending tokenization requests, receiving responses, and
 * managing the state of the tokenization process.
 *
 * @property packageNamed The name of the package where this class is used.
 * @property payable The [Payable] interface implementation to handle payment-related callbacks.
 * @property payTheoryDataIn Optional map of additional data to be passed to Pay Theory.
 * @property configurationIn The [PayTheoryConfiguration] object for metadata and customization.
 * @property viewModel The [PaymentViewModel] for managing payment states and UI updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentMethodToken(
    packageNamed: String,
    payable: Payable,
    payTheoryData: HashMap<Any, Any>? = hashMapOf(),
    configuration : PayTheoryConfiguration,
    viewModel: PaymentViewModel
) : PaymentMethodProcessor(payable,payTheoryData, configuration,viewModel), WebsocketMessageHandler {
    init {
        // This runs AFTER the base class init and AFTER constructor parameters are assigned.
        Timber.tag("DEBUG_PAYTHEORY").d("PaymentMethodToken SUBCLASS init block started.")

        // 'configuration' parameter should be valid here.
        if (!configuration.isTestMode) {
            Timber.tag("DEBUG_PAYTHEORY").d("Not in test mode, triggering integrity initialization via lazy property.")
            // Access the 'integrity' property inherited from PaymentMethodProcessor.
            // This executes the 'lazy' block defined in the base class.
            val initializeAction = integrity // Just accessing the property triggers it.
        } else {
            Timber.tag("DEBUG_PAYTHEORY").d("In test mode, skipping integrity initialization trigger.")
        }
    }
    /**
     * The name of the package in which this class is being used.
     */
    val packageName = packageNamed

    /**
     * Holds a payment detail object when a request is queued.
     */
    var queuedRequest: PaymentDetail? = null

    /**
     * Discovers the type of message received from the websocket.
     *
     * This private function examines the content of an incoming message to determine its type.
     * It checks for specific keywords that indicate whether the message is a result of a tokenization
     * request or a host token request.
     *
     * @param message The incoming message from the websocket.
     * @return A string representing the type of message ([TOKENIZE_RESULT], [HOST_TOKEN_RESULT], or [UNKNOWN]).
     */
    private fun getWebSocketMessageType(message: String): String {
        return when {
            message.indexOf(TOKENIZE_RESULT) > -1 -> TOKENIZE_RESULT
            message.indexOf(HOST_TOKEN_RESULT) > -1 -> HOST_TOKEN_RESULT
            message.indexOf(WALLET_TRANSACTION_RESULT) > -1 -> WALLET_TRANSACTION_RESULT
            else -> UNKNOWN
        }
    }

    /**
     * Processes a message received from the websocket.
     *
     * This function is the entry point for handling messages received from the websocket.
     * It performs different actions based on the content of the message.
     * It handles messages indicating connection status changes, internal server errors,
     * and tokenization results.
     *
     * @param message The incoming message from the websocket.
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
                    WALLET_TRANSACTION_RESULT -> messageReactors!!.onWalletTransaction(message, viewModel, this)
                    else -> messageReactors!!.onTokenError(message, this)
                }
            }
        }
    }

    /**
     * Disconnects from the websocket.
     *
     * This function terminates the websocket connection.
     */
    override fun disconnect() {
        viewModel.disconnect()
    }

    /**
     * Initializes the connection and message reactors for communication.
     *
     * This function sets up the necessary reactors for handling connection-related
     * events and messages received from the websocket. It also subscribes to
     * socket events. If there is a queued payment request, it sets it as active.
     *
     * @param ptTokenResponse The [PTTokenResponse] containing the Pay Theory token.
     * @param attestationResult The result of the device attestation.
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
        viewModel.subscribeToSocketEvents(this,ptTokenResponse)
        if (queuedRequest != null)
            messageReactors!!.activePaymentToken = queuedRequest

//        updatePayableReadyState(true)
    }

    /**
     * Creates an initial action request for tokenization.
     *
     * This function prepares a request to be sent through the websocket for
     * tokenizing a payment method. It generates a key pair, encrypts the
     * tokenization data, and structures the request in the required format.
     *
     * @param payment The [PaymentDetail] object containing the payment information.
     * @return An [ActionRequest] object ready to be sent through the websocket.
     */
    override fun createInitialActionRequest(payment: PaymentDetail): ActionRequest {
        //generate public key
        val keyPair = generateLocalKeyPair()
        publicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        val requestAction = TOKENIZE
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
        val tokenRequest = TokenizeRequest(
            this.hostToken,
            paymentMethodData,
            payment.payorInfo,
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
     * Initiates the process to tokenize a card or bank account.
     *
     * This function starts the tokenization process for a given payment method.
     * If the websocket is connected, it sends an action request to the websocket.
     * Otherwise, it queues the request and initiates the process to get a PT token.
     *
     * @param payment The [PaymentDetail] object containing the payment information to tokenize.
     */
    override fun process(payment: PaymentDetail) {
        messageReactors!!.activePaymentDetail = payment
        val actionRequest = createInitialActionRequest(payment)
        //Create card paymentToken
        if (viewModel.connected) {

            payable.handleTokenStart(payment.type)
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Payment Requested")

        } else {
            queuedRequest = payment
            ptTokenApiCall(payable)
            println("Pay Theory Resetting Connection")
        }
    }

}