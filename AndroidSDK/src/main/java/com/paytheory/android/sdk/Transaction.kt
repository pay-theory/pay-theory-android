package com.paytheory.android.sdk

import ActionRequest
import CashRequest
import PaymentMethodData
import Payment
import PaymentData
import TransferPartOneRequest
import TransferPartTwoRequest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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
    private val confirmation: Boolean,
    private val sendReceipt: Boolean,
    private val receiptDescription: String,
    private val metadata: HashMap<Any, Any>?,
    private val payTheoryData: HashMap<Any, Any>?
): WebsocketMessageHandler {
    private val googleProjectNumber = 192992826889
    private var originalConfirmation: ConfirmationMessage? = null
    @OptIn(ExperimentalCoroutinesApi::class)
    lateinit var viewModel: WebSocketViewModel
    var queuedRequest: Payment? = null
    var publicKey: String? = null
    var sessionKey:String? = null
    var hostToken:String? = null


    companion object {
        private const val CONNECTED = "connected to socket"
        private const val DISCONNECTED = "disconnected from socket"
        private const val INTERNAL_SERVER_ERROR = "Internal server error"
        private const val HOST_TOKEN_RESULT = "hostToken"
        private const val TRANSFER_PART_ONE_ACTION = "host:transfer_part1"
        private const val TRANSFER_PART_TWO_ACTION = "host:transfer_part2"
        private const val BARCODE_ACTION = "host:barcode"
        private const val BARCODE_RESULT = "BarcodeUid"
        private const val TRANSFER_PART_ONE_RESULT = "transfer_confirmation"
        private const val COMPLETED_TRANSFER = "transfer_complete"
        private const val UNKNOWN = "unknown"
        const val CASH = "CASH"

        @OptIn(ExperimentalCoroutinesApi::class)
        private var messageReactors: MessageReactors? = null
        @OptIn(ExperimentalCoroutinesApi::class)
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

    val observable = ApiService(constants.API_BASE_PATH).ptTokenApiCall().doToken(buildApiHeaders())

    observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        .subscribe({ ptTokenResponse: PTTokenResponse ->
            if (queuedRequest != null) {
                establishViewModel(ptTokenResponse)
            } else {
                googlePlayIntegrity(ptTokenResponse)
            }

        }, { error ->
            if (context is Payable) {
                if(error.message == "HTTP 404 "){
                    context.transactionError(TransactionError("Access Denied"))
                }
                else {
                    context.transactionError(TransactionError("Cannot connect to payment system"))
                }
            }
        }
        )
    }

    @ExperimentalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
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
            val integrityToken = it.token()
            establishViewModel(ptTokenResponse, integrityToken)
        }

        integrityTokenResponse.addOnFailureListener {
            if (context is Payable) {
                context.transactionError(TransactionError(it.message!!))
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun establishViewModel(ptTokenResponse: PTTokenResponse, attestationResult: String? = "") {
        webServicesProvider = WebServicesProvider()
        webSocketRepository = WebsocketRepository(webServicesProvider!!)
        webSocketInteractor = WebsocketInteractor(webSocketRepository!!)

        viewModel = WebSocketViewModel(webSocketInteractor!!, ptTokenResponse.ptToken, partner, stage)
        connectionReactors = ConnectionReactors(ptTokenResponse.ptToken, attestationResult!!, viewModel, webSocketInteractor!!)
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
    @RequiresApi(Build.VERSION_CODES.O)
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

    /**
     * Generate the initial action request
     * @param payment payment object to transact
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun generateQueuedActionRequest(payment: Payment): ActionRequest {

        //generate public key
        val keyPair = generateLocalKeyPair()
        publicKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)
        } else {
            android.util.Base64.encodeToString(keyPair.publicKey.asBytes,android.util.Base64.DEFAULT)
        }

        //if payment type is "CASH" return cash ActionRequest
        if (payment.type == CASH){
            val requestAction = BARCODE_ACTION
            val paymentRequest = CashRequest(this.hostToken, sessionKey ,payment, System.currentTimeMillis(), payment.payorInfo, metadata)
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

            val paymentRequest = TransferPartOneRequest(this.hostToken, paymentMethodData, paymentData, confirmation, payment.payorInfo, this.payTheoryData,
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
        originalConfirmation = confirmationMessage
    }

    /**
     * After payment confirmation is complete host:transfer_part2 action request is created
     * Function called from override fun paymentConfirmation
     * @param
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun completeTransfer() {
//        //set payer_id to payor_id
//        if (this.originalConfirmation?.payerId?.isNotBlank() == true){
//            originalConfirmation!!.payor_id = originalConfirmation!!.payerId
//        }

        val requestBody = TransferPartTwoRequest(originalConfirmation!!, metadata, sessionKey, System.currentTimeMillis())

        val encryptedBody = encryptBox(Gson().toJson(requestBody), Key.fromBase64String(messageReactors!!.socketPublicKey))

        val actionRequest = ActionRequest(TRANSFER_PART_TWO_ACTION, encryptedBody, publicKey, sessionKey)

        if (viewModel.connected) {
            viewModel.sendSocketMessage(Gson().toJson(actionRequest))
        } else{
            if (context is Payable) {
                context.transactionError(TransactionError("Failed to complete transaction"))
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
