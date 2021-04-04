package com.paytheory.android.sdk

import TransferMessage
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
    @SerializedName("type") val type: String?)

/**
 * Data class that represents the error received if payment fails
 * @param reason reason the transaction failed
 */
data class PaymentError (
    @SerializedName("reason") val reason: String)

/**
 * Interface that handles a transaction completion, failure, and errors
 */
interface Payable {
    /**
     * Converts paymentResult as Payable
     * @param paymentResult reason the transaction failed
     */
    fun paymentComplete(paymentResult: TransferMessage)
    /**
     * Converts paymentFailure as Payable
     * @param paymentFailure reason the transaction failed
     */
    fun paymentFailed(paymentFailure: PaymentResult)
    /**
     * Converts paymentError as Payable
     * @param paymentError reason the transaction failed
     */
    fun paymentError(paymentError: PaymentError)
}