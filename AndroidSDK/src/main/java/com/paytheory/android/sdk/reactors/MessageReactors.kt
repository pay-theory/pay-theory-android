package com.paytheory.android.sdk.reactors
import com.google.gson.Gson
import com.paytheory.android.sdk.BarcodeResult
import com.paytheory.android.sdk.ConfirmationMessage
import com.paytheory.android.sdk.EncryptedMessage
import com.paytheory.android.sdk.EncryptedPaymentToken
import com.paytheory.android.sdk.ErrorCode
import com.paytheory.android.sdk.FailedTransactionResult
import com.paytheory.android.sdk.PTError
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.PaymentMethodToken
import com.paytheory.android.sdk.PaymentMethodTokenResults
import com.paytheory.android.sdk.SuccessfulTransactionResult
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.TransactionResult
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.data.BarcodeMessage
import com.paytheory.android.sdk.data.HostTokenMessage
import com.paytheory.android.sdk.data.Payment
import com.paytheory.android.sdk.data.PaymentMethodTokenData
import com.paytheory.android.sdk.nacl.decryptBox
import com.paytheory.android.sdk.websocket.WebSocketViewModel
import com.paytheory.android.sdk.websocket.WebsocketInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi

/*
* Modernization
* Errors have been updated to PTError
* supporting error code enum
* */

/**
 * Creates reactions based on WebSocket messages
 * @param viewModel view model of WebSocket
 * @param webSocketInteractor interactor for WebSocket
 */
@ExperimentalCoroutinesApi
class MessageReactors(private val viewModel: WebSocketViewModel, private val webSocketInteractor: WebsocketInteractor) {
    var activePayment: Payment? = null
    var activePaymentToken: PaymentMethodTokenData? = null
    private var hostToken = ""
    private var sessionKey = ""
    var socketPublicKey = ""
    private val mapUrl = "https://pay.vanilladirect.com/pages/locations"

    /**
     * Called when host token message received from websocket
     * @param
     */
    @ExperimentalCoroutinesApi
    fun onHostToken(message: String, transaction: Transaction): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.body.publicKey
        sessionKey = hostTokenMessage.body.sessionKey
        hostToken = hostTokenMessage.body.hostToken
        transaction.publicKey = hostTokenMessage.body.publicKey
        transaction.sessionKey = hostTokenMessage.body.sessionKey
        transaction.hostToken = hostTokenMessage.body.hostToken
        return hostTokenMessage
    }

    /**
     * Called when host token message received from websocket
     * @param
     */
    @ExperimentalCoroutinesApi
    fun onTokenizeHostToken(message: String, paymentMethodToken: PaymentMethodToken): HostTokenMessage {
        val hostTokenMessage = Gson().fromJson(message, HostTokenMessage::class.java)
        socketPublicKey = hostTokenMessage.body.publicKey
        sessionKey = hostTokenMessage.body.sessionKey
        hostToken = hostTokenMessage.body.hostToken
        paymentMethodToken.publicKey = hostTokenMessage.body.publicKey
        paymentMethodToken.sessionKey = hostTokenMessage.body.sessionKey
        paymentMethodToken.hostToken = hostTokenMessage.body.hostToken
        return hostTokenMessage
    }

    /**
     * Sends payment confirmation to paymentConfirmation override method
     * @param
     */
    fun confirmPayment(message: String, transaction: Transaction? = null){
        //decrypt message
        val encryptedPaymentConfirmation = Gson().fromJson(message, EncryptedMessage::class.java)
        val decryptedMessage = decryptBox(encryptedPaymentConfirmation.body, encryptedPaymentConfirmation.publicKey)
        val confirmationMessage = Gson().fromJson(decryptedMessage, ConfirmationMessage::class.java)
        //set original confirmation/transaction with correct fee from Pay Theory
        transaction!!.setConfirmation(confirmationMessage)
        //Remove service_fee for any merchant_fee transaction
        if (transaction.feeMode == FeeMode.MERCHANT_FEE) {
            confirmationMessage.fee = "0"
        }
        //send user confirmation of payment
        if (transaction.context is Payable){
            transaction.context.confirmation(confirmationMessage, transaction)
        }
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onError(message: String, transaction: Transaction? = null) {
        /* fail if unknown websocket message */
        if (transaction != null) {
            if (transaction.context is Payable){
                transaction.context.handleError(PTError(ErrorCode.socketError,message))
            }
        }
    }

    /**
     * Function that handles incoming message for a unknown action
     * @param message message to be sent
     */
    fun onTokenError(message: String, paymentMethodToken: PaymentMethodToken? = null) {
        /* fail if unknown websocket message */
        if (paymentMethodToken != null) {
            if (paymentMethodToken.context is Payable){
                paymentMethodToken.context.handleError(PTError(ErrorCode.socketError,message))
            }
        }
    }

    /**
     * Function that handles incoming transfer response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun completeTransaction(message: String, viewModel: WebSocketViewModel, transaction: Transaction) {
        viewModel.disconnect()
        val encryptedTransferMessage = Gson().fromJson(message, EncryptedMessage::class.java)
        //decrypt message
        val decryptedMessage = decryptBox(encryptedTransferMessage.body, encryptedTransferMessage.publicKey)

        val transactionResult = Gson().fromJson(decryptedMessage, TransactionResult::class.java)

        //Remove service_fee for any merchant_fee transaction
        if (transaction.feeMode == FeeMode.MERCHANT_FEE) {
            transactionResult.serviceFee = "0"
        }

        if (transaction.context is Payable) when (transactionResult.state) {
            "SUCCEEDED" -> {
                val successfulTransactionResult = Gson().fromJson(decryptedMessage, SuccessfulTransactionResult::class.java)
                transaction.context.handleSuccess(successfulTransactionResult)
                Transaction.sessionIsDirty = true
            }
            "PENDING" -> {
                val successfulTransactionResult = Gson().fromJson(decryptedMessage, SuccessfulTransactionResult::class.java)
                transaction.context.handleSuccess(successfulTransactionResult)
                Transaction.sessionIsDirty = true
            }
            "FAILURE" -> {
                val failedTransactionResult = Gson().fromJson(decryptedMessage, FailedTransactionResult::class.java)
                transaction.context.handleFailure(failedTransactionResult)
            }

        else -> {
            transaction.context.handleError(PTError(ErrorCode.socketError,"Error retrieving payment confirmation"))
        }
        }
    }

    /**
     * Function that handles incoming barcode response
     * @param message message to be sent
     */
    @ExperimentalCoroutinesApi
    fun onBarcode(message: String, viewModel: WebSocketViewModel, transaction: Transaction) {
        println("Pay Theory Barcode Result")
        viewModel.disconnect()
        val encryptedBarcodeMessage = Gson().fromJson(message, EncryptedMessage::class.java)
        val decryptedMessage = decryptBox(encryptedBarcodeMessage.body, encryptedBarcodeMessage.publicKey)
        val barcodeMessageResult = Gson().fromJson(decryptedMessage, BarcodeMessage::class.java)

        if (transaction.context is Payable && barcodeMessageResult.barcode.isNotBlank() && barcodeMessageResult.barcodeUrl.isNotBlank()) {
            val barcodeResult = BarcodeResult(barcodeMessageResult.barcodeUid, barcodeMessageResult.barcodeUrl,
                barcodeMessageResult.barcode, barcodeMessageResult.barcodeFee, barcodeMessageResult.merchant, mapUrl)
            transaction.context.handleBarcodeSuccess(barcodeResult)
            Transaction.sessionIsDirty = true

        } else if (transaction.context is Payable) {
            transaction.context.handleError(PTError(ErrorCode.socketError,"Failed to Create Barcode"))
        }
    }

    /**
     * Sends payment confirmation to paymentConfirmation override method
     * @param
     */
    fun onCompleteToken(message: String, paymentMethodToken: PaymentMethodToken){
        //decrypt message
        val encryptedPaymentToken = Gson().fromJson(message, EncryptedPaymentToken::class.java)
        val decryptedMessage = decryptBox(encryptedPaymentToken.body, encryptedPaymentToken.publicKey)
        val paymentMethodTokenResult = Gson().fromJson(decryptedMessage, PaymentMethodTokenResults::class.java)
        if (paymentMethodToken.context is Payable) {
            paymentMethodToken.context.handleTokenizeSuccess(paymentMethodTokenResult)
            Transaction.sessionIsDirty = true
        }
    }
}