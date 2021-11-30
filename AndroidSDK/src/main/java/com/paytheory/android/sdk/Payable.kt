package com.paytheory.android.sdk

import com.google.gson.annotations.SerializedName

/**
 * Data class that represents the payment results after transaction has been processed
 * @param receipt_number receipt/confirmation number for the transaction
 * @param last_four last four of account number
 * @param brand brand of card
 * @param state current state of transaction
 * @param amount amount of the transaction
 * @param service_fee service fee amount
 * @param tags custom tags that can be added to transaction
 * @param created_at creation time
 * @param updated_at updated time
 * @param type ACH or CARD
 */
data class PaymentResult (
    @SerializedName("receipt_number") val receipt_number: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("service_fee") val service_fee: String?,
    @SerializedName("tags") val tags: Map<String,String>?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    @SerializedName("type") val type: String?
)


/**
 * Data class that represents the payment results after transaction has been processed
 * @param receipt_number receipt/confirmation number for the transaction
 * @param last_four last four of account number
 * @param brand brand of card
 * @param state current state of transaction
 * @param type ACH or CARD
 */
data class PaymentResultFailure (
    @SerializedName("receipt_number") val receipt_number: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("state") val state: String,
    @SerializedName("type") val type: String?
)

/**
 * Data class that represents the error received if a transaction fails
 * @param reason reason the transaction failed
 */
data class TransactionError (
    @SerializedName("reason") val reason: String
)

/**
 * Data class to store resulting barcode data
 * @param
 */
data class BarcodeResult (
    @SerializedName("BarcodeUid") val barcodeUid: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
    @SerializedName("MapUrl") val mapUrl : String
)

/**
 * Payment confirmation data
 * @param
 */
data class TransactionConfirmation (
    @SerializedName("BarcodeUid") val barcodeUid: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
    @SerializedName("MapUrl") val mapUrl : String
)

/**
 * Interface that handles a transaction completion, failure, and errors
 */
interface Payable {
    /**
     * Converts paymentResult as Payable
     * @param paymentResult result of the completed transaction
     */
    fun paymentComplete(paymentResult: PaymentResult)

    /**
     * Converts paymentFailure as Payable
     * @param paymentFailure reason the transaction failed
     */
    fun paymentFailed(paymentFailure: PaymentResultFailure)

    /**
     * Converts transactionError as Payable
     * @param transactionError reason the transaction error
     */
    fun transactionError(transactionError: TransactionError)

    /**
     * method to handle confirmation of payment
     * @param transactionConfirmation confirmation data
     */
    fun confirmation(message: String, transaction: Transaction)

    /**
     * Converts barcodeResult as Payable
     * @param barcodeResult result of the completed barcode transaction
     */
    fun barcodeComplete(barcodeResult: BarcodeResult)
}