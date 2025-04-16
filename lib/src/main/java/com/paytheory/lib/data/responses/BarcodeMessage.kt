package com.paytheory.lib.data.responses

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