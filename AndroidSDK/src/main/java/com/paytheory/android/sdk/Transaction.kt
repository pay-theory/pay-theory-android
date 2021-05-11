package com.paytheory.android.sdk

import ActionRequest
import InstrumentRequest
import Payment
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.safetynet.SafetyNet
import com.google.gson.Gson
import com.goterl.lazycode.lazysodium.utils.Key
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

/**
 * Transaction Class is created after data validation and click listener is activated.
 * This hold all pay theory logic to process payments.
 * @param context the applications resources
 * @param apiKey the api-key that will be used to create payment transaction
 */
class Transaction(
    val context: Context,
    private val apiKey: String,
    private val constants: Constants,
    private val environment: String
): WebsocketMessageHandler {

    private val GOOGLE_API = "AIzaSyDDn2oOEQGs-1ETypHoa9MIkJZZtjEAYBs"
    var queuedRequest: Payment? = null
    lateinit var viewModel: WebSocketViewModel

    companion object {
        private const val CONNECTED = "connected to socket"
        private const val DISCONNECTED = "disconnected from socket"
        private const val HOST_TOKEN = "hostToken"
        private const val INSTRUMENT_TOKEN = "pt-instrument"
        private const val PAYMENT_TOKEN = "payment-token"
        private const val INSTRUMENT_ACTION = "host:ptInstrument"
        private const val TRANSFER_ACTION = "host:transfer"
        private const val TRANSFER_RESULT = "payment-detail-reference"
        private const val TRANSFER_RESULT_FAIL = "type"
        private const val UNKNOWN = "unknown"

        private var messageReactors: MessageReactors? = null
        private var connectionReactors: ConnectionReactors? = null



        private var webServicesProvider: WebServicesProvider? = null
        private var webSocketRepository: WebsocketRepository? = null
        var webSocketInteractor: WebsocketInteractor? = null
    }

    private fun buildApiHeaders(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        headerMap["X-API-Key"] = apiKey
        return headerMap
    }

    @ExperimentalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun ptTokenApiCall(context: Context){
        if(UtilMethods.isConnectedToInternet(context)){

            val observable = ApiService(constants.API_BASE_PATH).ptTokenApiCall().doToken(buildApiHeaders())

            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

                .subscribe({ ptTokenResponse: PTTokenResponse ->
                    if (queuedRequest != null) {
                        establishViewModel(ptTokenResponse)
                    } else {
                        callSafetyNet(ptTokenResponse)
                    }


                }, { error ->
                    if (context is Payable) {
                        if(error.message == "HTTP 403 "){
                            context.paymentError(PaymentError("Access Denied"))
                        }
                        else {
                            context.paymentError(PaymentError(error.message!!))
                        }
                    }
                }
                )
        }else{
            if (context is Payable) {
                context.paymentError(PaymentError(constants.NO_INTERNET_ERROR))
            }
        }
    }

    @ExperimentalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    private fun callSafetyNet(ptTokenResponse: PTTokenResponse) {
        val challenge = ptTokenResponse.challengeOptions.challenge
        SafetyNet.getClient(context).attest(challenge.toByteArray(), GOOGLE_API)
            .addOnSuccessListener {
                val attestationResult = it.jwsResult
                establishViewModel(ptTokenResponse, attestationResult)
            }.addOnFailureListener {
                if (context is Payable) {
                    context.paymentError(PaymentError(it.message!!))
                }
            }
    }

    @ExperimentalCoroutinesApi
    private fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String = "") {
        webServicesProvider = WebServicesProvider()
        webSocketRepository = WebsocketRepository(webServicesProvider!!)
        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)

        viewModel = WebSocketViewModel(webSocketInteractor!!, ptTokenResponse.ptToken, environment)
        connectionReactors = ConnectionReactors(ptTokenResponse.ptToken, attestationResult, viewModel, webSocketInteractor!!)
        messageReactors = MessageReactors(viewModel, webSocketInteractor!!)
        viewModel.subscribeToSocketEvents(this)
        if (queuedRequest != null)
            messageReactors!!.activePayment = queuedRequest
    }

    /**
     * Initiate Transaction Class and calls pt-token endpoint
     */
    @ExperimentalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    fun init() {
        ptTokenApiCall(context)
    }

    /**
     * Final api call to complete transaction
     * @param payment payment object to transact
     */
    @ExperimentalCoroutinesApi
    fun transact(
        payment: Payment
    ) {
        messageReactors!!.activePayment = payment

        val actionRequest =  generateQueuedActionRequest(payment)

        if (viewModel.connected) {
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
            println("Pay Theory Payment Requested")
        } else {
            queuedRequest = payment
            ptTokenApiCall(context)
            println("Pay Theory Resetting Connection")
        }
    }

    fun generateQueuedActionRequest(payment: Payment): ActionRequest {
        val keyPair = generateLocalKeyPair()
        val instrumentRequest = InstrumentRequest(messageReactors!!.hostToken, payment, System.currentTimeMillis(), payment.buyerOptions)
        val localPublicKey = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        } else {
            android.util.Base64.encodeToString(keyPair.publicKey.asBytes,android.util.Base64.DEFAULT)
        }

        val boxed = encryptBox(Gson().toJson(instrumentRequest), Key.fromBase64String(messageReactors!!.socketPublicKey))
        return ActionRequest(
            INSTRUMENT_ACTION,
            boxed,
            localPublicKey)
    }

    private fun discoverMessageType(message: String): String  {
        return when {
            message.indexOf(HOST_TOKEN) > -1 -> HOST_TOKEN
            message.indexOf(INSTRUMENT_TOKEN) > -1 -> INSTRUMENT_TOKEN
            message.indexOf(PAYMENT_TOKEN) > -1 -> PAYMENT_TOKEN
            message.indexOf(TRANSFER_RESULT) > -1 -> TRANSFER_RESULT
            message.indexOf(TRANSFER_RESULT_FAIL) > 3 -> TRANSFER_RESULT
            else -> UNKNOWN
        }
    }

    /**
     * Function to call next action based on incoming socket message
     * @param message the incoming message from socket
     */
    @ExperimentalCoroutinesApi
    override fun receiveMessage(message: String) {
        when (message) {
            CONNECTED -> { connectionReactors!!.onConnected() }
            DISCONNECTED -> { connectionReactors!!.onDisconnected()
            }
            else -> {
                when (discoverMessageType(message)) {
                    HOST_TOKEN -> messageReactors!!.onHostToken(message, this)
                    INSTRUMENT_TOKEN -> messageReactors!!.onInstrument(message, apiKey)
                    PAYMENT_TOKEN -> messageReactors!!.onIdempotency(message)
                    TRANSFER_RESULT -> messageReactors!!.onTransfer(message,viewModel, this)
                    else -> messageReactors!!.onUnknown(message)
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun disconnect() {
        if (webSocketInteractor != null) {
            webSocketInteractor!!.stopSocket()
        }
    }
}












//Not currently used

//import IdempotencyResponse
//import com.goterl.lazycode.lazysodium.utils.KeyPair




//        lateinit var keyPair: KeyPair
//        var hostToken = ""
//        var socketPublicKey = ""
//        var activePayment: Payment? = null



//@ExperimentalCoroutinesApi
//private val messengerConnections = arrayOf(
//    Transaction.CONNECTED,
//    Transaction.DISCONNECTED
//)



//DEPRECATED

//private fun getCardType(number: String): String {
//    val visa = Regex("^4[0-9]{12}(?:[0-9]{3})?$")
//    val mastercard = Regex("^5[1-5][0-9]{14}$")
//    val amx = Regex("^3[47][0-9]{13}$")
//    val diners = Regex("^3(?:0[0-5]|[68][0-9])[0-9]{11}$")
//    val discover = Regex("^6(?:011|5[0-9]{2})[0-9]{12}$")
//
//    return when {
//        visa.matches(number) -> "Visa"
//        mastercard.matches(number) -> "Mastercard"
//        amx.matches(number) -> "American Express"
//        diners.matches(number) -> "Diners"
//        discover.matches(number) -> "Discover"
//        else -> "Unknown"
//    }
//}
