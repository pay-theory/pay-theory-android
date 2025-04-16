package com.paytheory.lib.data.payloads

import com.google.gson.annotations.SerializedName

/**
 * Instrument data
 * @param type type of payment
 * @param name account holder name
 * @param account_number account number
 * @param account_type type of account
 * @param bank_code routing number
 * @param number card number
 * @param security_code card security code
 * @param expiration_year card expiration year
 * @param expiration_month card expiration month
 * @param address billing address
 */
data class PaymentMethodData (
    @SerializedName("name") val name: String? = "",
    @SerializedName("number") val number: String? = null,
    @SerializedName("security_code") val security_code: String? = null,
    @SerializedName("type") val type: String,
    @SerializedName("expiration_year") val expiration_year: String? = null,
    @SerializedName("expiration_month") val expiration_month: String? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("account_number") val account_number: String? = null,
    @SerializedName("account_type") val account_type: String? = null,
    @SerializedName("bank_code") val bank_code: String? = null,
) 