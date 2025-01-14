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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.security.MessageDigest
import java.util.Base64
import java.util.logging.Level
import java.util.logging.Logger

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
 * @param apiKey The API key for authentication.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentMethodToken(
    val context: Context,
    private val partner: String,
    private val stage: String,
    apiKey: String,
    private val constants: Constants,
    private val metadata: HashMap<Any, Any>?,
    private val payTheoryData: HashMap<Any, Any>? = null
) : WebsocketMessageHandler {
    private lateinit var viewModel: WebSocketViewModel
    private val googleProjectNumber = context.resources.getString(R.string.google_project_number).toLong()
    private val headerMap =
        mutableMapOf("Content-Type" to "application/json", "X-API-Key" to apiKey)
    private var queuedRequest: PaymentMethodTokenData? = null
    var publicKey: String? = null
    var sessionKey: String? = null
    var hostToken: String? = null
    var integrityTokenProvider: StandardIntegrityTokenProvider? = null
    private var resetCounter = 0
    private var ptResetCounter = 0
    private var isWarm: Boolean = false
    companion object {
        private var messageReactors: MessageReactors? = null
        private var connectionReactors: ConnectionReactors? = null
        private var webServicesProvider: WebServicesProvider? = null
        private var webSocketRepository: WebsocketRepository? = null
        var webSocketInteractor: WebsocketInteractor? = null

        private const val CONNECTED = "connected to socket"
        private const val DISCONNECTED = "disconnected from socket"
        private const val INTERNAL_SERVER_ERROR = "Internal server error"
        private const val HOST_TOKEN_RESULT = "host_token"
        private const val TOKENIZE = "host:tokenize"
        private const val TOKENIZE_RESULT = "tokenize_complete"
        private const val UNKNOWN = "unknown"
    }
    /*
    * Modernization
    * This is where we reset our token
    * */

    /**
     * Initializes the PaymentMethodToken instance by warming up Play Integrity and setting the ready state.
     */
    init {
        if (!isWarm) {
            warmUpPlayIntegrity()
            isWarm = true
        }
        setReady(false)
    }

    /**
     * Sets the ready state of the Payable context, indicating whether the tokenization process is ready.
     * @param isReady A boolean value representing the ready state.
     */
    private fun setReady(isReady: Boolean) {
        if (context is Payable) {
            context.handleReady(isReady)
        }
    }

    /**
     * Resets the Pay Theory token by making an API call to obtain a new token.
     */
    private fun resetPtToken() {
        if (ptResetCounter < 2000) {
//            println("PT Token Reconnect Counter: $ptResetCounter")
            ptResetCounter++
            ptTokenApiCall(this.context)
        } else {
            messageReactors?.onTokenError("NETWORK_ERROR: Please check device connection", this)
        }
    }

    /**
     * Resets the socket connection on network failures by disconnecting and attempting to reconnect.
     */
    fun resetSocket() {
        if (resetCounter < 50) {
            //println("Reconnect Counter: $resetCounter")
            resetCounter++
            ptTokenApiCall(this.context)
        } else {
            messageReactors?.onTokenError("NETWORK_ERROR: Please check device connection", this)
        }
    }

    /**
     * Makes an API call to obtain a Pay Theory token.
     * @param context The application context.
     */
    @SuppressLint("CheckResult")
    private fun ptTokenApiCall(context: Context) {
        val observable = ApiService(constants.apiBasePath).ptTokenApiCall().doToken(headerMap)
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            // handle success pt-token request
            .subscribe({ ptTokenResponse: PTTokenResponse ->
                ptResetCounter = 0
                googlePlayIntegrity(ptTokenResponse)
                // handle failed pt-token request
            }, { error ->
                Logger.getLogger("ptTokenApiCall").log(Level.WARNING,error.message.toString())
                if (context is Payable) {
                    if (error.message.toString().contains("Unable to resolve host")) {
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
     * Warms up Play Integrity by preparing an integrity token.
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
     * Performs Google Play Integrity check and establishes the WebSocketViewModel.
     * @param ptTokenResponse The Pay Theory token response.
     */
    private fun googlePlayIntegrity(ptTokenResponse: PTTokenResponse) {
//        val challenge = ptTokenResponse.challengeOptions.challenge
        // Create an instance of a manager.
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
     * Establishes the WebSocketViewModel and initiates the connection.
     * @param ptTokenResponse The Pay Theory token response.
     * @param attestationResult The attestation result from Play Integrity.
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
            null,
            this
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
            messageReactors!!.activePaymentToken = queuedRequest
    }

    /**
     * Final api call to complete transaction
     * @param paymentMethodTokenData payment method token object
     */
    fun tokenize(
        paymentMethodTokenData: PaymentMethodTokenData
    ) {
        messageReactors!!.activePaymentToken = paymentMethodTokenData
        val actionRequest = generateInitialActionRequest(paymentMethodTokenData)
        if (viewModel.connected) {
            if (context is Payable) {
                context.handleTokenStart(paymentMethodTokenData.type)
            }
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Token Requested")
        } else {
            queuedRequest = paymentMethodTokenData
            ptTokenApiCall(context)
            println("Pay Theory Resetting Connection")
        }
    }

    /**
     * Generates the initial action request for tokenization.
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
            metadata,
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
}