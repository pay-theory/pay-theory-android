package com.paytheory.lib.data.payable

import com.google.gson.annotations.SerializedName

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