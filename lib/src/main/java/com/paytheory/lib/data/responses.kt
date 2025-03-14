package com.paytheory.lib.data

import com.google.gson.annotations.SerializedName

/**
 * Data class to store resulting barcode data
 * @param barcodeId
 * @param barcodeUrl
 * @param barcode
 * @param barcodeFee
 * @param merchant
 */
data class BarcodeMessage (
    @SerializedName("barcode_id") val barcodeId: String,
    @SerializedName("barcode_url") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("fees") val barcodeFee: String,
    @SerializedName("merchant_uid") val merchant: String,
)

/**
 * Data class to store host token message details
 * @param type
 * @param body
 */
data class HostTokenMessage (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: HostToken
)

/**
 * Data class to store host token message details
 * @param hostToken token with transaction details
 * @param publicKey encryption key to encode/decode messages
 * @param sessionKey encryption key to encode/decode messages
 */
data class HostToken (
    @SerializedName("hostToken") val hostToken: String,
    @SerializedName("publicKey") val publicKey: String,
    @SerializedName("sessionKey") val sessionKey: String
)