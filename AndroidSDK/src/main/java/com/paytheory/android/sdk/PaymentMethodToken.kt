package com.paytheory.android.sdk

import ActionRequest
import PaymentMethodData
import PaymentMethodTokenData
import TokenizeRequest
import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.gson.Gson
import com.goterl.lazysodium.utils.Key
import com.paytheory.android.sdk.api.ApiService
import com.paytheory.android.sdk.api.PTTokenResponse
import com.paytheory.android.sdk.reactors.*
import com.paytheory.android.sdk.nacl.encryptBox
import com.paytheory.android.sdk.nacl.generateLocalKeyPair
import com.paytheory.android.sdk.websocket.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import kotlin.collections.HashMap

/**
 *
 */
class PaymentMethodToken(
    val context: Context,
    private val partner: String,
    private val stage: String,
    private val apiKey: String,
    private val constants: Constants,
    private val metadata: HashMap<Any, Any>?,
    private val payTheoryData: HashMap<Any, Any>? = null
): WebsocketMessageHandler {
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var viewModel: WebSocketViewModel
    private val googleProjectNumber = 192992826889
    private val headerMap = mutableMapOf("Content-Type" to "application/json", "X-API-Key" to apiKey)
    var queuedRequest: PaymentMethodTokenData? = null
    var publicKey: String? = null
    var sessionKey:String? = null
    var hostToken:String? = null


    companion object {
        private const val CONNECTED = "connected to socket"
        private const val DISCONNECTED = "disconnected from socket"
        private const val INTERNAL_SERVER_ERROR = "Internal server error"
        private const val HOST_TOKEN_RESULT = "host_token"
        private const val TOKENIZE = "host:tokenize"
        private const val TOKENIZE_RESULT = "tokenize_complete"
        private const val UNKNOWN = "unknown"

        @OptIn(ExperimentalCoroutinesApi::class)
        private var messageReactors: MessageReactors? = null
        @OptIn(ExperimentalCoroutinesApi::class)
        private var connectionReactors: ConnectionReactors? = null
        private var webServicesProvider: WebServicesProvider? = null
        private var webSocketRepository: WebsocketRepository? = null
        var webSocketInteractor: WebsocketInteractor? = null
    }

    /**
     * Initialize a transaction
     */
    init{
        ptTokenApiCall(context)
    }

    fun resetSocket(){
        ptTokenApiCall(context=this.context)
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("CheckResult")
    private fun ptTokenApiCall(context: Context){

        val observable = ApiService(constants.API_BASE_PATH).ptTokenApiCall().doToken(headerMap)

        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            // handle success pt-token request
            .subscribe({ ptTokenResponse: PTTokenResponse ->
                googlePlayIntegrity(ptTokenResponse)
//            if (queuedRequest != null) {
//                establishViewModel(ptTokenResponse)
//            } else {
//                googlePlayIntegrity(ptTokenResponse)
//            }
                // handle failed pt-token request
            }, { error ->
                if (context is Payable) {
                    if(error.message == "HTTP 404 "){
                        context.handleError(Error("Access Denied"))
                    }
                    else {
                        context.handleError(Error(error.message.toString()))
                    }
                }
            }
            )
    }

    @ExperimentalCoroutinesApi
    private fun googlePlayIntegrity(ptTokenResponse: PTTokenResponse) {
        val challenge = ptTokenResponse.challengeOptions.challenge
        // Create an instance of a manager.
        val integrityManager =
            IntegrityManagerFactory.create(this.context)

        // Request the integrity token by providing the nonce as Pay Theory challenge string.
        val integrityTokenResponse: Task<IntegrityTokenResponse> =
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setNonce(challenge)
                    .setCloudProjectNumber(googleProjectNumber)
                    .build())

        integrityTokenResponse.addOnSuccessListener {
            establishViewModel(ptTokenResponse, it.token())
        }

        integrityTokenResponse.addOnFailureListener {
            if (context is Payable) {
                if (it.message?.contains("Network error") == true){
                    context.handleError(Error("Google Play Integrity: Please Check Network Connection"))
                } else {
                    context.handleError(Error(it.message!!))
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String? = "") {
        webServicesProvider = WebServicesProvider()
        webSocketRepository = WebsocketRepository(webServicesProvider!!)
        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)

        viewModel = WebSocketViewModel(webSocketInteractor!!, ptTokenResponse.ptToken, partner, stage, null, this)
        connectionReactors = ConnectionReactors(ptTokenResponse.ptToken, attestationResult!!, viewModel, webSocketInteractor!!, this.context.applicationContext.packageName)
        messageReactors = MessageReactors(viewModel, webSocketInteractor!!)
        viewModel.subscribeToSocketEvents(this)
        if (queuedRequest != null)
            messageReactors!!.activePaymentToken = queuedRequest
    }

    /**
     * Final api call to complete transaction
     * @param paymentMethodTokenData payment method token object
     */
    @ExperimentalCoroutinesApi
    fun tokenize(
        paymentMethodTokenData: PaymentMethodTokenData
    ) {

        messageReactors!!.activePaymentToken = paymentMethodTokenData

        val actionRequest =  generateInitialActionRequest(paymentMethodTokenData)

        if (viewModel.connected) {
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Token Requested")
        } else {
            queuedRequest = paymentMethodTokenData
            ptTokenApiCall(context)
            println("Pay Theory Resetting Connection")
        }
    }

    /**
     * Generate the initial action request
     * @param paymentMethodTokenData payment method token object
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun generateInitialActionRequest(paymentMethodTokenData: PaymentMethodTokenData): ActionRequest {
        //generate public key
        val keyPair = generateLocalKeyPair()
        publicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)

        val requestAction = TOKENIZE

        val paymentMethodData = PaymentMethodData(paymentMethodTokenData.name, paymentMethodTokenData.number, paymentMethodTokenData.security_code, paymentMethodTokenData.type, paymentMethodTokenData.expiration_year,
            paymentMethodTokenData.expiration_month, paymentMethodTokenData.address, paymentMethodTokenData.account_number, paymentMethodTokenData.account_type, paymentMethodTokenData.bank_code )

        val tokenRequest = TokenizeRequest(this.hostToken, paymentMethodData, paymentMethodTokenData.payorInfo, this.payTheoryData,metadata, sessionKey, System.currentTimeMillis())

        val encryptedBody = encryptBox(Gson().toJson(tokenRequest), Key.fromBase64String(messageReactors!!.socketPublicKey))

        return ActionRequest(
            requestAction,
            encryptedBody,
            publicKey,
            sessionKey
        )

    }

    private fun discoverMessageType(message: String): String  {
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
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun receiveMessage(message: String) {
        when (message) {
            CONNECTED -> { connectionReactors!!.onConnected() }
            DISCONNECTED -> { connectionReactors!!.onDisconnected() }
            INTERNAL_SERVER_ERROR -> { messageReactors!!.onError(message) }
            else -> {
                when (discoverMessageType(message)) {
                    HOST_TOKEN_RESULT -> messageReactors!!.onTokenizeHostToken(message, this)
                    TOKENIZE_RESULT -> messageReactors!!.onCompleteToken(message, this)
                    else -> messageReactors!!.onTokenError(message, this)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun disconnect() {
        if (webSocketInteractor != null) {
            webSocketInteractor!!.stopSocket()
        }
    }
}