package com.paytheory.lib.data.requests

import com.google.gson.annotations.SerializedName
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.payloads.PayorInfo

/**
 * Data class to store cash request
 * @param hostToken token with transaction details
 * @param paymentDetail object that contains payment details
 * @param timing calculated timing
 * @param payorInfo optional buyer options data
 */
data class CashRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("payment") val paymentDetail: PaymentDetail,
    @SerializedName("timing") val timing: Long,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?
) 