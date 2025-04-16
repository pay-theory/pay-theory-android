package com.paytheory.lib.data.payable

import com.google.gson.annotations.SerializedName

/**
 * Data class to store resulting barcode data
 * @param barcodeId Pay Theory unique barcode identifier
 * @param barcodeUrl url to view barcode
 * @param barcode barcode number
 * @param barcodeFee barcode fee
 * @param merchant your Pay Theory merchant uid
 * @param mapUrl a url for a map to nearby barcode payment locations
 */
data class BarcodeResult (
    @SerializedName("BarcodeId") val barcodeId: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
    @SerializedName("mapUrl") val mapUrl : String
) 