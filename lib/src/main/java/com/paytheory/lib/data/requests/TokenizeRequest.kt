package com.paytheory.lib.data.requests

import com.google.gson.annotations.SerializedName
import com.paytheory.lib.data.payloads.PaymentMethodData
import com.paytheory.lib.data.payloads.PayorInfo

/**
 * Data class for tokenize request
 */
data class TokenizeRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("payment_method_data") val paymentMethodData: PaymentMethodData,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long
) 