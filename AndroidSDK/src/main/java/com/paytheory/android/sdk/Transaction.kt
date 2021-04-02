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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

/**
 * Transaction Class is created after data validation and click listener is activated.
 * This hold all pay theory logic to process payments.
 * @param context the applications rersources
 * @param apiKey the api-key that will be used to create payment transaction
 */
class Transaction(
    private val context: Context,
    private val apiKey: String
): WebsocketMessageHandler {

    private val GOOGLE_API = "AIzaSyDDn2oOEQGs-1ETypHoa9MIkJZZtjEAYBs"

    companion object {
        private const val CONNECTED = "connected to socket"
        private const val DISCONNECTED = "disconnected from socket"
        private const val HOST_TOKEN = "hostToken"
        private const val INSTRUMENT_TOKEN = "pt-instrument"
        private const val PAYMENT_TOKEN = "payment-token"
        private const val INSTRUMENT_ACTION = "host:ptInstrument"
        private const val TRANSFER_ACTION = "host:transfer"
        private const val UNKNOWN = "unknown"

        private var messageReactors: MessageReactors? = null
        private var connectionReactors: ConnectionReactors? = null

        private val webServicesProvider = WebServicesProvider()
        private val webSocketRepository = WebsocketRepository(webServicesProvider)
        val webSocketInteractor = WebsocketInteractor(webSocketRepository)
        lateinit var viewModel: WebSocketViewModel
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

            val observable = ApiService.ptTokenApiCall().doToken(buildApiHeaders())

            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ ptTokenResponse: PTTokenResponse ->

                    callSafetyNet(ptTokenResponse)

                }, { error ->
                    if (context is Payable) {
                        context.paymentError(PaymentError(error.message!!))
                    }
                }
                )
        }else{
            if (context is Payable) {
                context.paymentError(PaymentError(Constants.NO_INTERNET_ERROR))
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

                viewModel = WebSocketViewModel(webSocketInteractor, ptTokenResponse.ptToken)
                connectionReactors = ConnectionReactors(ptTokenResponse.ptToken, attestationResult, viewModel, webSocketInteractor)
                messageReactors = MessageReactors(viewModel, webSocketInteractor)
                viewModel.subscribeToSocketEvents(this)

            }.addOnFailureListener {
                if (context is Payable) {
                    context.paymentError(PaymentError(it.message!!))
                }
            }
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
        val keyPair = generateLocalKeyPair()
        val instrumentRequest = InstrumentRequest(messageReactors!!.hostToken, payment, System.currentTimeMillis())
        val localPublicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)

        val boxed = encryptBox(Gson().toJson(instrumentRequest), Key.fromBase64String(messageReactors!!.socketPublicKey))

        val actionRequest = ActionRequest(
            INSTRUMENT_ACTION,
            boxed,
            localPublicKey)
        viewModel.sendSocketMessage(Gson().toJson(actionRequest))
    }
    
    private fun discoverMessageType(message: String): String  {
        return when {
            message.indexOf(HOST_TOKEN) > -1 -> HOST_TOKEN
            message.indexOf(INSTRUMENT_TOKEN) > -1 -> INSTRUMENT_TOKEN
            message.indexOf(PAYMENT_TOKEN) > -1 -> PAYMENT_TOKEN
            else -> UNKNOWN
        }
    }

    /**
     * Function to call next action based on incoming socket message
     * @param message the incoming message from socket
     */
    @ExperimentalCoroutinesApi
    override fun receiveMessage(message: String) {
        println("message $message")
        when (message) {
            CONNECTED -> { connectionReactors!!.onConnected() }
            DISCONNECTED -> { connectionReactors!!.onDisconnected()
            }
            else -> {
                when (discoverMessageType(message)) {
                    HOST_TOKEN -> messageReactors!!.onHostToken(message)
                    INSTRUMENT_TOKEN -> messageReactors!!.onInstrument(message, apiKey)
                    PAYMENT_TOKEN -> messageReactors!!.onIdempotency(message)
                    TRANSFER_ACTION -> messageReactors!!.onTransfer(message)
                    else -> messageReactors!!.onUnknown(message)
                }
            }
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
