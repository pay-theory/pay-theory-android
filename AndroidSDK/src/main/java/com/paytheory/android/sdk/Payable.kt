package com.paytheory.android.sdk

import com.google.gson.annotations.SerializedName

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
    @SerializedName("type") val type: String?,)

data class PaymentError (
    @SerializedName("reason") val reason: String,)


interface Payable {
    fun paymentComplete(paymentResult: PaymentResult)
    fun paymentFailed(paymentFailure: PaymentResult)
    fun paymentError(paymentError: PaymentError)
}