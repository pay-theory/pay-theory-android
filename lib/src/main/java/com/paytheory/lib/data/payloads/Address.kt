package com.paytheory.lib.data.payloads

import com.google.gson.annotations.SerializedName

/**
 * Data class to store address details
 * @param line1 billing address line 1
 * @param line2 billing address line 2
 * @param city billing city
 * @param region billing region
 * @param postal_code billing postal code
 * @param country billing country
 */
data class Address (
    @SerializedName("line1") val line1: String? = null,
    @SerializedName("line2") val line2: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("postal_code") val postal_code: String? = null,
    @SerializedName("country") val country: String = "US"
) 