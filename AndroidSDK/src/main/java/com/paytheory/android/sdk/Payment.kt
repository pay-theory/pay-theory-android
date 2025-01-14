package com.paytheory.android.sdk

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager.PrepareIntegrityTokenRequest
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenProvider
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import com.google.gson.Gson
import com.goterl.lazysodium.utils.Key
import com.paytheory.android.sdk.api.ApiService
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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger

/**
 * The `Payment` class orchestrates the payment process using Pay Theory's SDK.
 *
 * It handles communication with the Pay Theory backend, including establishing a secure websocket
 * connection, sending payment requests, and receiving responses. It also integrates with Google Play
 * Integrity API for enhanced security.
 *
 * @param context The application context.
 * @param partner Your Pay Theory partner identifier.
 * @param stage The environment to use (e.g., "paytheory", "paytheorystudy", "paytheorylab").
 * @param apiKey Your Pay Theory API key.
 * @param feeMode The fee mode to apply ("surcharge" or "service_fee").
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Payment(
    val context: Context,
    private val partner: String,
    private val stage: String,
    private val apiKey: String,
    val feeMode: String,
    private val constants: Constants,
    private val confirmation: Boolean? = false,
    private val metadata: HashMap<Any, Any>? = hashMapOf(),
    private val payTheoryData: HashMap<Any, Any>? = hashMapOf()
) : WebsocketMessageHandler {
    private lateinit var viewModel: WebSocketViewModel
    private var originalConfirmation: ConfirmationMessage? = null
    private var headerMap =
        mutableMapOf("Content-Type" to "application/json", "X-API-Key" to apiKey)
    private var queuedRequest: PaymentDetail? = null
    var publicKey: String? = null
    var sessionKey: String? = null
    var hostToken: String? = null
    var integrityTokenProvider: StandardIntegrityTokenProvider? = null
    private var resetCounter = 0
    private var ptResetCounter = 0
    private var isWarm: Boolean = false
    companion object {

        var sessionIsDirty = true
        private var messageReactors: MessageReactors? = null
        private var connectionReactors: ConnectionReactors? = null
        private var webServicesProvider: WebServicesProvider? = null
        private var webSocketRepository: WebsocketRepository? = null
        var webSocketInteractor: WebsocketInteractor? = null

        private const val CONNECTED = "connected to socket"
        private const val DISCONNECTED = "disconnected from socket"
        private const val INTERNAL_SERVER_ERROR = "Internal server error"
        private const val HOST_TOKEN_RESULT = "host_token"
        private const val TRANSFER_PART_ONE_ACTION = "host:transfer_part1"
        private const val TRANSFER_PART_TWO_ACTION = "host:transfer_part2"
        private const val BARCODE_ACTION = "host:barcode"
        private const val BARCODE_RESULT = "barcode_complete"
        private const val TRANSFER_PART_ONE_RESULT = "transfer_confirmation"
        private const val COMPLETED_TRANSFER = "transfer_complete"
        private const val UNKNOWN = "unknown"
        private const val CASH = "cash"
    }

    /**
     * Initializes the `Payment` instance, including warming up the Play Integrity API.
     */
    init {
        if (!isWarm) {
            warmUpPlayIntegrity()
            isWarm = true
        }
        setReady(false)
    }

    /**
     * Resets the Pay Theory token, attempting to reconnect to the server.
     */
    private fun resetPtToken() {
        if (ptResetCounter < 2000) {
//            println("PT Token Reconnect Counter: $ptResetCounter")
            ptResetCounter++
            ptTokenApiCall(this.context)
        } else {
            messageReactors?.onError("NETWORK_ERROR: Please check device connection", this)
        }
    }

    /**
     * Resets the socket connection in case of network failures.
     */
    fun resetSocket() {
        if (resetCounter < 50) {
//            println("Reconnect Counter: $resetCounter")
            resetCounter++
            ptTokenApiCall(this.context)
        } else {
            messageReactors?.onError("NETWORK_ERROR: Please check device connection", this)
        }
    }

    /**
     * Initiates the Pay Theory token API call to obtain a token.
     * @param context The application context.
     */
    @SuppressLint("CheckResult")
    private fun ptTokenApiCall(context: Context) {
        if (sessionIsDirty) {
            headerMap.put("x-session-key",UUID.randomUUID().toString())
            sessionIsDirty = false
        }


        val observable = ApiService(constants.apiBasePath).ptTokenApiCall().doToken(headerMap)
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            // handle success pt-token request
            .subscribe({ ptTokenResponse: PTTokenResponse ->
                ptResetCounter = 0
                googlePlayIntegrity(ptTokenResponse)
                // handle failed pt-token request
            }, { error ->
                if (context is Payable) {
                    // error "Unable to resolve host "evolve.paytheorystudy.com": No address associated with hostname"
                    if (error.message.toString().contains("Unable to resolve host")) {
//                        println(error.message.toString())
                        disconnect()
                        resetPtToken()
                    } else if (error.message.toString().contains("HTTP 500")) {
//                        println(error.message.toString())
                        disconnect()
                        resetSocket()
                    } else if (error.message == "HTTP 404 ") {
                        context.handleError(PTError(ErrorCode.SocketError,"Access Denied"))
                    } else {
                        println("ptTokenApiCall " + error.message)
                        context.handleError(PTError(ErrorCode.SocketError,error.message.toString()))
                    }
                }
            }
            )
    }

    /**
     * Prepares the Google Play Integrity API by initializing and potentially pre-fetching an integrity token.
     */
    private fun warmUpPlayIntegrity() {
        val googleProjectNumber: Long = context.resources.getString(R.string.google_project_number).toLong()
        val standardIntegrityManager = IntegrityManagerFactory.createStandard(context)


        // Prepare integrity token. Can be called once in a while to keep internal
        // state fresh.
        standardIntegrityManager.prepareIntegrityToken(
            PrepareIntegrityTokenRequest.builder()
                .setCloudProjectNumber(googleProjectNumber)
                .build()
        )
            .addOnSuccessListener { tokenProvider ->
                integrityTokenProvider = tokenProvider
                ptTokenApiCall(context)
            }
            .addOnFailureListener { exception ->
                Logger.getLogger("warmUpPlayIntegrity").log(Level.WARNING,exception.message.toString())
            }
    }

    /**
     * Initiates the Google Play Integrity check and proceeds to establish the websocket connection
     * if the integrity check is successful.
     * @param ptTokenResponse The response containing the Pay Theory token.
     */
    private fun googlePlayIntegrity(ptTokenResponse: PTTokenResponse) {

        // See above how to prepare integrityTokenProvider.

        // Request integrity token by providing a user action request hash. Can be called
        // several times for different user actions.
        val digest = MessageDigest.getInstance("SHA-256")
        val requestHash = digest.digest(ptTokenResponse.challengeOptions.challenge.toByteArray(Charsets.UTF_8))

        val integrityTokenResponse: Task<StandardIntegrityToken> =
            integrityTokenProvider!!.request(
                StandardIntegrityTokenRequest.builder()
                    .setRequestHash(Base64.getEncoder().encodeToString(requestHash))
                    .build()
            )
        integrityTokenResponse
            .addOnSuccessListener(OnSuccessListener { response ->
                establishViewModel(ptTokenResponse, response.token()) })
            .addOnFailureListener(OnFailureListener { exception ->
                if (context is Payable) {
                    if (exception.message?.contains("Network error") == true) {
                        println("Google Play Integrity API Network Error. Retrying...")
                        disconnect()
                        resetSocket()
                    } else {
                        context.handleError(PTError(ErrorCode.SocketError,exception.message!!))
                    }
                }
            })
    }

    /**
     * Signals whether the payment process is ready to begin.
     * @param isReady True if ready, false otherwise.
     */
    private fun setReady(isReady: Boolean) {
        if (context is Payable) {
            context.handleReady(isReady)
        }
    }

    /**
     * Establishes the websocket connection and initializes the necessary components for communication.
     * @param ptTokenResponse The response containing the Pay Theory token.
     * @param attestationResult The result of the Google Play Integrity check (optional).
     */
    private fun establishViewModel(
        ptTokenResponse: PTTokenResponse,
        attestationResult: String? = ""
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
            this.context.applicationContext.packageName
        )
        messageReactors = MessageReactors(viewModel, webSocketInteractor!!)
        viewModel.subscribeToSocketEvents(this)
        if (queuedRequest != null)
            messageReactors!!.activePaymentDetail = queuedRequest

        setReady(true)
    }

    /**
     * Initiates the payment transaction by sending the payment details to the Pay Theory backend.
     * @param payment The payment details object.
     */
    fun transact(
        payment: PaymentDetail
    ) {
        messageReactors!!.activePaymentDetail = payment
        val actionRequest = generateInitialActionRequest(payment)
        if (viewModel.connected) {
            if (context is Payable) {
                context.handlePaymentStart(payment.type)
                viewModel.sendSocketMessage(Gson().toJson(actionRequest))
                println("Pay Theory Payment Requested")
            }
        } else {
            queuedRequest = payment
            ptTokenApiCall(context)
            println("Pay Theory Resetting Connection")
        }
    }

    /**
     * Generates the initial action request to be sent to the Pay Theory backend.
     * @param payment The payment details object.
     * @return The generated `ActionRequest` object.
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
                metadata
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
                confirmation!!, payment.payorInfo, this.payTheoryData,
                metadata, sessionKey, System.currentTimeMillis()
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
     * @param confirmationMessage The confirmation message object.
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
            metadata,
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
     * @param message The received message.
     * @return The type of message.
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
     * @param message The received message.
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
}