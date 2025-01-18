package com.paytheory.android.sdk

import com.google.gson.annotations.SerializedName

/*
* Modernization
* Our legacy SDK did not include an error code
* This is our error code enum
* we are including this in errors, need to work through various scenarios and find where to use each
* */
enum class ErrorCode {
    ActionComplete, //The requested action has already been completed.
    ActionInProgress, //An action is currently in progress, preventing a new action.
    AttestationFailed, //The device attestation process failed.
    InProgress, //The operation is still in progress.
    InvalidAPIKey, //The provided API key is invalid or not recognized.
    InvalidParam, //An invalid parameter was provided to the SDK.
    NoFields, //Required fields were not provided for the operation.
    NotReady, //The SDK is not in a ready state for the requested operation.
    NotValid, //The provided data failed validation checks.
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
/**
 * Interface that responds for any transaction request or tokenization request
 */
interface Payable {
    /**
     * function to handle changes to SDK readiness
     * @param isReady Boolean indicating if the SDK is ready to interact
     */
    fun handleReady(isReady: Boolean)

    /**
     * function to indicate payment process is started
     * @param paymentType String indicating the type of payment started
     */
    fun handlePaymentStart(paymentType: String)

    /**
     * function to indicate tokenization process is started
     * @param paymentType String indicating the type of payment method being tokenized
     */
    fun handleTokenStart(paymentType: String)

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
     * function to handle any system errors from a user's device or Pay Theory
     * @param error reason for the failure
     */
    fun handleError(error: PTError)
}