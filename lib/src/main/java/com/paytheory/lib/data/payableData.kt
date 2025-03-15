package com.paytheory.lib.data

import com.google.gson.annotations.SerializedName

enum class ErrorCode {
    ActionComplete, //The payment action has already been completed.
    ActionInProgress, //A payment action is already in progress.
    AttestationFailed, //The device attestation process failed.
    InProgress, //Initialization is still in progress.
    InvalidAPIKey, //The provided API key is invalid or not recognized.
    NotValid, //The provided data is not valid.
    SocketError, //An error occurred with the WebSocket connection.
    TokenFailed	//Token generation or validation failed.
}

/**
 * Data class that represents the error received if a transaction fails
 * @param code the PTErrorCode
 * @param error a description of the error
 */
data class PTError (
    @SerializedName("code") val code: ErrorCode,
    @SerializedName("error") val error: String
)

/**
 * Data class to store resulting barcode data
 * @param barcodeId Pay Theory unique barcode identifier
 * @param barcodeUrl url to view barcode
 * @param barcode barcode number
 * @param barcodeFee barcode fee
 * @param merchant your Pay Theory merchant uid
 * @param mapUrl a url for a map to nearby barcode payment locations
 */
data class BarcodeResult (
    @SerializedName("BarcodeId") val barcodeId: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
    @SerializedName("mapUrl") val mapUrl : String
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
@Suppress("PropertyName")
data class PaymentMethodTokenResults (
    @SerializedName("state") val state: String,
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
 * Data class to store received encrypted message containing a payment token or payment method token
 */
data class EncryptedMessage (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: String,
    @SerializedName("public_key") val publicKey: String
)

/**
 * Data class to store received encrypted payment token
 */
data class EncryptedPaymentToken (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: String,
    @SerializedName("public_key") val publicKey: String
)