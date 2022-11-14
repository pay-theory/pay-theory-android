package com.paytheory.android.sdk

import Address
import PayorInfo
import com.google.gson.annotations.SerializedName

/**
 * Data class that represents the error received if a transaction fails
 * @param reason reason the transaction failed
 */
data class Error (
    @SerializedName("reason") val reason: String
)

/**
 * Data class to store resulting barcode data
 * @param barcodeUid Pay Theory unique barcode identifier
 * @param barcodeUrl url to view barcode
 * @param barcode barcode number
 * @param barcodeFee barcode fee
 * @param merchant your Pay Theory merchant uid
 * @param mapUrl a url for a map to nearby barcode payment locations
 */
data class BarcodeResult (
    @SerializedName("BarcodeUid") val barcodeUid: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
    @SerializedName("mapUrl") val mapUrl : String
)

/**
 * Data class to store payment confirmation details
 */
data class ConfirmationMessage (
    @SerializedName("payment_token") val paymentToken: String,
    @SerializedName("payer_id") val payerId: String?,
    @SerializedName("processor_payment_method_id") val processorPaymentMethodId: String?,
    @SerializedName("merchant_uid") val merchantUid: String?,
    @SerializedName("last_four") val lastFour: String?,
    @SerializedName("first_six") val firstSix: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("session_key") val sessionKey: String,
    @SerializedName("processor") val processor: String,
    @SerializedName("expiration") var expiration: String?,
    @SerializedName("idempotency") val idempotency: String,
    @SerializedName("billing_name") val billingName: String?,
    @SerializedName("billing_address") val billingAddress: Address?,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("fee_mode") val fee_mode: String,
    @SerializedName("fee") var fee: String,
    @SerializedName("processor_merchant_id") val processor_merchant_id: String?,
    @SerializedName("payment_method") val payment_method: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("pay_theory_data") val pay_theory_data: HashMap<Any, Any>?,
    @SerializedName("payor_info") val payorInfo: PayorInfo?,
    @SerializedName("payor_id") var payor_id: String?,
    @SerializedName("invoice_id") val invoice_id: String?,
    @SerializedName("payment_intent_id") val paymentIntentId: String?,
)

/**
 * Data class to store transaction result data for successful, pending, failed transactions
 */
data class TransactionResult (
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("last_four") val lastFour: String,
    @SerializedName("service_fee") var serviceFee: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("receipt_number") val receiptNumber: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("payor_id") val payorId: String,
    @SerializedName("type") val type: String,
)

/**
 * Data class to store transaction result data for successful or pending transaction results
 */
data class SuccessfulTransactionResult (
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("last_four") val lastFour: String,
    @SerializedName("service_fee") val serviceFee: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("receipt_number") val receiptNumber: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("payor_id") val payorId: String,
)

/**
 * Data class to store payment method token result details
 */
data class PaymentMethodTokenResults (
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("payor_id") var payor_id: String?,
    @SerializedName("last_four") val lastFour: String?,
    @SerializedName("first_six") val firstSix: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("expiration") val expiration: String?,
    @SerializedName("payment_type") val paymentType: String
)

/**
 * Data class to store transaction result data for failed transactions results
 */
data class FailedTransactionResult (
    @SerializedName("state") val state: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("last_four") val lastFour: String,
    @SerializedName("receipt_number") val receiptNumber: String,
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("payor_id") val payorId: String,
)

/**
 * Data class to store received encrypted message
 */
data class EncryptedMessage (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: String,
    @SerializedName("public_key") val publicKey: String
)

/**
 * Data class to store payment confirmation details
 */
data class EncryptedPaymentToken (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: String,
    @SerializedName("public_key") val publicKey: String
)
/**
 * Interface that responds for any transaction request
 */
interface Payable {
    /**
     * function to handle successful payment results
     * @param successfulTransactionResult result for a successful transaction
     */
    fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult)
    /**
     * function to handle declined payment results
     * @param failedTransactionResult result for a failed transaction
     */
    fun handleFailure(failedTransactionResult: FailedTransactionResult)
    /**
     * function to handle successful barcode results
     * @param barcodeResult result for a successful barcode request
     */
    fun handleBarcodeSuccess(barcodeResult: BarcodeResult)
    /**
     * function to handle successful tokenization results
     * @param paymentMethodToken result for a payment method token request
     */
    fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults)
    /**
     * function to handle a confirmation when requested in transaction configuration
     * @param confirmationMessage payment confirmation step, enabled using PayTheoryFragment's transact function confirmation set to true
     */
    fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction)
    /**
     * function to handle any system errors from a user's device or Pay Theory
     * @param error reason for the failure
     */
    fun handleError(error: Error)
}