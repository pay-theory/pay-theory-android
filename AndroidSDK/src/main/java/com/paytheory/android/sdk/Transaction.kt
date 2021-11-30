package com.paytheory.android.sdk

import ActionRequest
import Payment
import TransferPartTwoMessage
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
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

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
    private val partner: String,
    private val stage: String,
    private val tags: HashMap<String, String>?
): WebsocketMessageHandler {

    private val GOOGLE_API = "AIzaSyDDn2oOEQGs-1ETypHoa9MIkJZZtjEAYBs"
    var queuedRequest: Payment? = null
    lateinit var viewModel: WebSocketViewModel
    var publicKey: String? = null
    var sessionKey:String? = null

    companion object {
        private const val CONNECTED = "connected to socket"
        private const val DISCONNECTED = "disconnected from socket"
        private const val HOST_TOKEN_RESULT = "hostToken"
//        private const val INSTRUMENT_TOKEN = "pt-instrument"
//        private const val PAYMENT_TOKEN = "payment-token"
        private const val TRANSFER_PART_ONE_ACTION = "host:transfer_part1"
        private const val TRANSFER_PART_TWO_ACTION = "host:transfer_part2"
//        private const val INSTRUMENT_ACTION = "host:ptInstrument"
        private const val BARCODE_ACTION = "host:barcode"
        private const val BARCODE_RESULT = "BarcodeUid"
        private const val TRANSFER_PART_ONE_RESULT = "payment"
        private const val COMPLETED_TRANSFER = "payment-detail-reference"
//        private const val TRANSFER_RESULT_FAIL = "type"
        private const val UNKNOWN = "unknown"
        const val PAYMENT_CARD = "PAYMENT_CARD"
        const val BANK_ACCOUNT = "BANK_ACCOUNT"
        const val CASH = "CASH"

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
                            context.transactionError(TransactionError("Access Denied"))
                        }
                        else {
                            context.transactionError(TransactionError(error.message!!))
                        }
                    }
                }
                )
        }else{
            if (context is Payable) {
                context.transactionError(TransactionError(constants.NO_INTERNET_ERROR))
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
                    context.transactionError(TransactionError(it.message!!))
                }
            }
    }

    @ExperimentalCoroutinesApi
    private fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String = "") {
        webServicesProvider = WebServicesProvider()
        webSocketRepository = WebsocketRepository(webServicesProvider!!)
        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)

        viewModel = WebSocketViewModel(webSocketInteractor!!, ptTokenResponse.ptToken, partner, stage)
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

        val actionRequest =  transferPartOne(payment)

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
     * Generate transfer part one action request
     * @param payment payment object to transact
     */
    fun transferPartOne(payment: Payment, sessionKey:String): ActionRequest {

        val keyPair = generateLocalKeyPair()

//        val paymentRequest = InstrumentRequest(messageReactors!!.hostToken, messageReactors!!.sessionKey,payment,
//            System.currentTimeMillis(), payment.buyerOptions)

        publicKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        } else {
            android.util.Base64.encodeToString(keyPair.publicKey.asBytes,android.util.Base64.DEFAULT)
        }

        val instrumentData = JSONObject()


        if (payment.type == PAYMENT_CARD) {
            instrumentData.put("name", payment.name)
            instrumentData.put("number", payment.number)
            instrumentData.put("security_code", payment.security_code)
            instrumentData.put("type", payment.type)
            instrumentData.put("expiration_year", payment.expiration_year)
            instrumentData.put("expiration_month", payment.expiration_month)
            instrumentData.put("address", payment.address)
        } else if (payment.type == BANK_ACCOUNT) {
            instrumentData.put("account_number", payment.account_number)
            instrumentData.put("account_type", payment.account_type)
            instrumentData.put("bank_code", payment.bank_code)
            instrumentData.put("name", payment.name)
            instrumentData.put("type", payment.type)
        } else if (payment.type == CASH){
            val encryptedRequestData = encryptBox(Gson().toJson("TODO"), Key.fromBase64String(messageReactors!!.socketPublicKey)) //TODO
            return ActionRequest(
                BARCODE_ACTION,
                encryptedRequestData,
                publicKey)
        } else {
            //TODO return failure
        }

        val paymentData = JSONObject()
        paymentData.put("fee_mode", payment.fee_mode)
        paymentData.put("currency", payment.currency)
        paymentData.put("amount", payment.amount)

        val buyerOptionsData = JSONObject()
        paymentData.put("buyer_options", payment.buyerOptions)

        val requestData = JSONObject()
        requestData.put("hostToken", messageReactors!!.hostToken)
        requestData.put("instrument_data",instrumentData)
        requestData.put("payment_data", paymentData)
        requestData.put("confirmation_needed", false) //TODO get confirmation boolean
        requestData.put("buyer_options", buyerOptionsData)
        requestData.put("tags", tags)
        requestData.put("sessionKey", sessionKey)
        requestData.put("timing", System.currentTimeMillis().toString())

        val encryptedRequestData = encryptBox(Gson().toJson(requestData), Key.fromBase64String(messageReactors!!.socketPublicKey))

        var requestAction = TRANSFER_PART_ONE_ACTION


        return ActionRequest(
            requestAction,
            encryptedRequestData,
            publicKey,
            sessionKey
        )
    }

    /**
     * Generate transfer part one action request
     * @param payment payment object to transact
     */
    fun transferPartTwo(message: String): ActionRequest {

        val payment = Gson().fromJson(message, TransferPartTwoMessage::class.java)

        val requestData = JSONObject()

        requestData.put("transfer", payment)
        requestData.put("tags",tags)
        requestData.put("sessionKey", messageReactors!!.sessionKey)
        requestData.put("timing", System.currentTimeMillis().toString())

        val encryptedRequestData = encryptBox(Gson().toJson(requestData), Key.fromBase64String(messageReactors!!.socketPublicKey))

        var requestAction = TRANSFER_PART_TWO_ACTION

        return ActionRequest(
            requestAction,
            encryptedRequestData,
            publicKey,
            messageReactors!!.sessionKey
        )
    }

    private fun discoverMessageType(message: String): String  {
        return when {
            message.indexOf(HOST_TOKEN_RESULT) > -1 -> HOST_TOKEN_RESULT
            message.indexOf(TRANSFER_PART_ONE_RESULT) > -1 -> TRANSFER_PART_ONE_RESULT
            message.indexOf(COMPLETED_TRANSFER) > -1 -> COMPLETED_TRANSFER
            message.indexOf(BARCODE_RESULT) > -1 -> BARCODE_RESULT
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
                    HOST_TOKEN_RESULT -> messageReactors!!.transferPartOne(message, this)
                    TRANSFER_PART_ONE_RESULT -> messageReactors!!.confirmPayment(message,this)
//                    TRANSFER_PART_ONE_RESULT -> messageReactors!!.transferPartTwo(message,this)
                    BARCODE_RESULT -> messageReactors!!.onBarcode(message,viewModel,this)
                    COMPLETED_TRANSFER -> messageReactors!!.completeTransfer(message,viewModel, this)
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
