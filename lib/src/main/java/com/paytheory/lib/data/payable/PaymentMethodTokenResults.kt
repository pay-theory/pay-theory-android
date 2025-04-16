@file:Suppress("PropertyName")

package com.paytheory.lib.data.payable

import com.google.gson.annotations.SerializedName

/**
 * Data class to store payment method token result details
 */
data class PaymentMethodTokenResults (
    @SerializedName("state") val state: String,
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("payor_id") var payor_id: String?,
    @SerializedName("last_four") val lastFour: String?,
    @SerializedName("first_six") val firstSix: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("expiration") val expiration: String?,
    @SerializedName("payment_type") val paymentType: String
) 