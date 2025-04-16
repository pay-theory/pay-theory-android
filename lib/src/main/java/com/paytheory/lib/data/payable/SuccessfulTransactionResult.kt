package com.paytheory.lib.data.payable

import com.google.gson.annotations.SerializedName

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