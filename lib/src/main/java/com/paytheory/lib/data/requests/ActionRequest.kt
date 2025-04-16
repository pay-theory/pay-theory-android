package com.paytheory.lib.data.requests

import com.google.gson.annotations.SerializedName

/**
 * Data class to store action message data
 * @param action type of action that will occur
 * @param encoded encoded message with transaction details
 * @param publicKey encryption key
 */
data class ActionRequest (
    @SerializedName("action") val action: String,
    @SerializedName("encoded") val encoded: String,
    @SerializedName("publicKey") val publicKey: String? = null,
    @SerializedName("sessionKey") val sessionKey: String? = null
) 