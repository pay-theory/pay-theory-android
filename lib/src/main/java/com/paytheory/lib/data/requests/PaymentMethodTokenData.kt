@file:Suppress("PropertyName")

package com.paytheory.lib.data.requests

import com.google.gson.annotations.SerializedName
import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PayorInfo

/**
 * Data class to store payment method token details
 * @param type type of payment
 * @param timing calculated timing
 * @param name account holder name
 * @param accountNumber account number
 * @param account_type type of account
 * @param bank_code routing number
 * @param number card number
 * @param security_code card security code
 * @param expiration_year card expiration year
 * @param expiration_month card expiration month
 * @param address billing address
 */
data class PaymentMethodTokenData (
    @SerializedName("type") val type: String,
    @SerializedName("timing") val timing: Long,
    @SerializedName("name") val name: String? = "",
    @SerializedName("account_number") val accountNumber: String? = null,
    @SerializedName("account_type") val account_type: String? = null,
    @SerializedName("bank_code") val bank_code: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("security_code") val security_code: String? = null,
    @SerializedName("expiration_year") val expiration_year: String? = null,
    @SerializedName("expiration_month") val expiration_month: String? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("payor_info") var payorInfo: PayorInfo? = null,
    @SerializedName("sessionKey") var sessionKey: String? = null,
) 