package com.paytheory.lib.data.payable

import com.google.gson.annotations.SerializedName

/**
 * Data class to store received encrypted payment token
 */
data class EncryptedPaymentToken (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: String,
    @SerializedName("public_key") val publicKey: String
) 