package com.paytheory.lib.data.payloads

import com.google.gson.annotations.SerializedName

/**
 * Data class to store payor details
 * @param first_name first name of buyer
 * @param last_name last name of buyer
 * @param email email of buyer
 * @param phone phone number of buyer
 * @param address address of buyer
 */
data class PayorInfo (
    @SerializedName("first_name") val first_name: String? = null,
    @SerializedName("last_name") val last_name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("personal_address") val address: Address
) 