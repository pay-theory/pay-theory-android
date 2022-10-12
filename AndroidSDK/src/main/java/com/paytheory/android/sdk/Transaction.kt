package com.paytheory.android.sdk

import ActionRequest
import CashRequest
import Payment
import PaymentData
import PaymentMethodData
import TransferPartOneRequest
import TransferPartTwoRequest
import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import com.google.gson.Gson
import com.goterl.lazysodium.utils.Key
import com.paytheory.android.sdk.api.ApiService
import com.paytheory.android.sdk.api.PTTokenResponse
import com.paytheory.android.sdk.nacl.encryptBox
import com.paytheory.android.sdk.nacl.generateLocalKeyPair
import com.paytheory.android.sdk.reactors.ConnectionReactors
import com.paytheory.android.sdk.reactors.MessageReactors
import com.paytheory.android.sdk.websocket.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.util.*


/**
 * Transaction Class is created after data validation and click listener is activated.
 * This hold all pay theory logic to process payments.
 * @param context the applications resources
 * @param apiKey the api-key that will be used to create payment transaction
 */
class Transaction(
    val context: Context,
    private val partner: String,
    private val stage: String,
    private val apiKey: String,
    val feeMode: String,
    private val constants: Constants,
    private val confirmation: Boolean? = false,
    private val sendReceipt: Boolean? = false,
    private val receiptDescription: String? = "",
    private val metadata: HashMap<Any, Any>? = hashMapOf(),
    private val payTheoryData: HashMap<Any, Any>? = hashMapOf()
): WebsocketMessageHandler {
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var viewModel: WebSocketViewModel
    private val googleProjectNumber = 192992826889
    private var originalConfirmation: ConfirmationMessage? = null
    private val headerMap = mutableMapOf("Content-Type" to "application/json", "X-API-Key" to apiKey)
    var queuedRequest: Payment? = null
    var publicKey: String? = null
    var sessionKey:String? = null
    var hostToken:String? = null

    companion object {
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

        @OptIn(ExperimentalCoroutinesApi::class)
        private var messageReactors: MessageReactors? = null
        @OptIn(ExperimentalCoroutinesApi::class)
        private var connectionReactors: ConnectionReactors? = null
        private var webServicesProvider: WebServicesProvider? = null
        private var webSocketRepository: WebsocketRepository? = null
        var webSocketInteractor: WebsocketInteractor? = null

        private lateinit var webSocketClient: WebSocketClient

    }

    /**
     * Initialize a transaction
     */
    init{
        ptTokenApiCall(context)
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("CheckResult")
    private fun ptTokenApiCall(context: Context){
    val observable = ApiService(constants.API_BASE_PATH).ptTokenApiCall().doToken(headerMap)
    observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        // handle success pt-token request
        .subscribe({ ptTokenResponse: PTTokenResponse ->
            if (queuedRequest != null) {
                establishViewModel(ptTokenResponse)
            } else {
                googlePlayIntegrity(ptTokenResponse)
            }
        // handle failed pt-token request
        }, { error ->
            if (context is Payable) {
                if(error.message == "HTTP 404 "){
                    context.handleError(Error("Access Denied"))
                }
                else {
                    println(error.message)
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
        val integrityManager = IntegrityManagerFactory.create(this.context)
        // Request the integrity token by providing the nonce as Pay Theory challenge string.
        val integrityTokenResponse: Task<IntegrityTokenResponse> =
            integrityManager.requestIntegrityToken(
                IntegrityTokenRequest.builder()
                    .setNonce(challenge)
                    .setCloudProjectNumber(googleProjectNumber)
                    .build())
        // handle success integrity token request
        integrityTokenResponse.addOnSuccessListener {
            establishViewModel(ptTokenResponse, it.token())
        }
        // handle failed integrity token request
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


    private fun createWebSocketClient(socketUrl: String) {
        val secureSocketUri = URI(socketUrl)
        webSocketClient = object : WebSocketClient(secureSocketUri) {

            override fun onOpen(handshakedata: ServerHandshake) {
                println("Pay Theory Connected")
//                subscribe()
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                println("closed with exit code $code additional info: $reason")
//                unsubscribe()
            }

            override fun onMessage(message: String) {
                println("received message: $message")
            }

            override fun onMessage(message: ByteBuffer) {
                println("received ByteBuffer")
            }

            override fun onError(ex: Exception) {
                println("error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                System.err.println("an error occurred:$ex")
            }

        }
    }

    private fun subscribe() {
        webSocketClient.send(
            "{\n" +
                    "    \"type\": \"subscribe\",\n" +
                    "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-EUR\"] }]\n" +
                    "}"
        )
    }

    private fun unsubscribe() {
        webSocketClient.send(
            "{\n" +
                    "    \"type\": \"unsubscribe\",\n" +
                    "    \"channels\": [\"ticker\"]\n" +
                    "}"
        )
    }



    @ExperimentalCoroutinesApi
    private fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String? = "") {

        createWebSocketClient("wss://${partner}.secure.socket.${stage}.com/${partner}?pt_token=${ptTokenResponse.ptToken}")
        val userAgent = System.getProperty( "http.agent" )
        webSocketClient.addHeader("User-Agent", userAgent)
        webSocketClient.connect()
//        webSocketClient.connect()

//        val webSocketClient = SocketClient(URI("wss://${partner}.secure.socket.${stage}.com/${partner}?pt_token=${ptTokenResponse.ptToken}"))
//        val webSocketClient = SocketClient(URI("wss://demo.piesocket.com/v3/channel_1?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self"))

//        val webSocketClient = object : WebSocketClient(URI("wss://${partner}.secure.socket.${stage}.com/${partner}?pt_token=${ptTokenResponse.ptToken}")) {
//
//            override fun onOpen(handshakedata: ServerHandshake) {
//                println(handshakedata)
//                println("new connection opened")
//            }
//
//            override fun onClose(code: Int, reason: String, remote: Boolean) {
//                println("closed with exit code $code additional info: $reason")
//            }
//
//            override fun onMessage(message: String) {
//                println("received message: $message")
//            }
//
//            override fun onMessage(message: ByteBuffer) {
//                println("received ByteBuffer")
//            }
//
//            override fun onError(ex: Exception) {
//                System.err.println("an error occurred:$ex")
//            }
//        }
//
//        try {
//            webSocketClient.setSocketFactory(SocketFactory.getDefault())
//        } catch (e: NoSuchAlgorithmException) {
//            e.printStackTrace()
//        }
//
//        webSocketClient.connect()



//        webServicesProvider = WebServicesProvider()
//        webSocketRepository = WebsocketRepository(webServicesProvider!!)
//        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)
//        viewModel = WebSocketViewModel(webSocketInteractor!!, ptTokenResponse.ptToken, partner, stage, this, null)
//        connectionReactors = ConnectionReactors(ptTokenResponse.ptToken, attestationResult!!, viewModel, webSocketInteractor!!, this.context.applicationContext.packageName)
//        messageReactors = MessageReactors(viewModel, webSocketInteractor!!)
//        viewModel.subscribeToSocketEvents(this)
//        if (queuedRequest != null)
//            messageReactors!!.activePayment = queuedRequest
    }

//    /**
//     * Initiate Transaction Class and calls pt-token endpoint
//     */
//    @ExperimentalCoroutinesApi
//    fun init() {
//        ptTokenApiCall(context)
//    }

    /**
     * Final api call to complete transaction
     * @param payment payment object to transact
     */
    @ExperimentalCoroutinesApi
    fun transact(
        payment: Payment
    ) {
        messageReactors!!.activePayment = payment
        val actionRequest =  generateInitialActionRequest(payment)
        if (viewModel.connected) {
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Payment Requested")
        } else {
            queuedRequest = payment
            ptTokenApiCall(context)
            println("Pay Theory Resetting Connection")
        }
    }

    /**
     * Generate the initial action request
     * @param payment payment object to transact
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun generateInitialActionRequest(payment: Payment): ActionRequest {
        //generate public key
        val keyPair = generateLocalKeyPair()
        publicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        //if payment type is "CASH" return cash ActionRequest
        if (payment.type == CASH){
            val requestAction = BARCODE_ACTION
            val paymentRequest = CashRequest(this.hostToken, sessionKey ,payment, System.currentTimeMillis(), payment.payorInfo, this.payTheoryData, metadata)
            val encryptedBody = encryptBox(Gson().toJson(paymentRequest), Key.fromBase64String(messageReactors!!.socketPublicKey))
            return ActionRequest(
                requestAction,
                encryptedBody,
                publicKey,
                sessionKey
            )
        }
        //if payment type is not "CASH" return transfer ActionRequest
        else {
            val requestAction = TRANSFER_PART_ONE_ACTION
            val paymentData = PaymentData(payment.currency, payment.amount, payment.fee_mode)
            val paymentMethodData = PaymentMethodData(payment.name, payment.number, payment.security_code, payment.type, payment.expiration_year,
                payment.expiration_month, payment.address, payment.account_number, payment.account_type, payment.bank_code )
            val paymentRequest = TransferPartOneRequest(this.hostToken, paymentMethodData, paymentData,
                confirmation!!, payment.payorInfo, this.payTheoryData,
                metadata, sessionKey, System.currentTimeMillis())
            val encryptedBody = encryptBox(Gson().toJson(paymentRequest), Key.fromBase64String(messageReactors!!.socketPublicKey))
            return ActionRequest(
                requestAction,
                encryptedBody,
                publicKey,
                sessionKey
            )
        }
    }

    /**
     * Set confirmation message to send host:transfer_part2 action request after user confirmation
     */
    fun setConfirmation(confirmationMessage: ConfirmationMessage){
        this.originalConfirmation = confirmationMessage.copy()
    }

    /**
     * After payment confirmation is complete host:transfer_part2 action request is created
     * Function called from override fun paymentConfirmation
     * @param
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completeTransfer() {
        // only for ach payments, need to set expiration to null back to tags-secure-socket
        if (originalConfirmation!!.expiration.isNullOrBlank() && originalConfirmation!!.brand == "ACH"){
            originalConfirmation!!.expiration = ""
        }
        val requestBody = TransferPartTwoRequest(originalConfirmation!!, metadata, sessionKey, System.currentTimeMillis())
        val encryptedBody = encryptBox(Gson().toJson(requestBody), Key.fromBase64String(messageReactors!!.socketPublicKey))
        val actionRequest = ActionRequest(TRANSFER_PART_TWO_ACTION, encryptedBody, publicKey, sessionKey)
        if (viewModel.connected) {
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
        } else{
            if (context is Payable) {
                context.handleError(Error("Failed to complete transaction"))
            }
            else{
                return
            }
        }
    }

    private fun discoverMessageType(message: String): String  {
        return when {
            message.indexOf(COMPLETED_TRANSFER) > -1 -> COMPLETED_TRANSFER
            message.indexOf(BARCODE_RESULT) > -1 -> BARCODE_RESULT
            message.indexOf(TRANSFER_PART_ONE_RESULT) > -1 -> TRANSFER_PART_ONE_RESULT
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
                    HOST_TOKEN_RESULT -> messageReactors!!.onHostToken(message, this)
                    TRANSFER_PART_ONE_RESULT -> messageReactors!!.confirmPayment(message,this)
                    BARCODE_RESULT -> messageReactors!!.onBarcode(message,viewModel,this)
                    COMPLETED_TRANSFER -> messageReactors!!.completeTransaction(message,viewModel, this)
                    else -> messageReactors!!.onError(message, this)
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