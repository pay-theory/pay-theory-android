package com.paytheory.lib.data.requests

import com.google.gson.annotations.SerializedName
import com.paytheory.lib.data.payloads.PaymentData
import com.paytheory.lib.data.payloads.PaymentMethodData
import com.paytheory.lib.data.payloads.PayorInfo

/**
 * Data class for transfer part one request
 * @param hostToken token with transaction details
 */
data class TransferPartOneRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("payment_method_data") val paymentMethodData: PaymentMethodData,
    @SerializedName("payment_data") val paymentData: PaymentData,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long
) 