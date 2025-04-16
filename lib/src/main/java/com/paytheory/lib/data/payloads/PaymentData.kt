package com.paytheory.lib.data.payloads

import com.google.gson.annotations.SerializedName

/**
 * Payment data
 * @param currency currency type of transaction amount
 * @param amount amount of payment
 * @param fee_mode fee mode that will be used for transaction
 */
data class PaymentData(
    @SerializedName("currency") val currency: String?,
    @SerializedName("amount") val amount: Int,
    @SerializedName("fee_mode") val fee_mode: String?
) 