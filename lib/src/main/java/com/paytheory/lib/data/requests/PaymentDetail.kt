@file:Suppress("PropertyName")

package com.paytheory.lib.data.requests

import com.google.gson.annotations.SerializedName
import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PayorInfo

/**
 * Data class to store payment details
 * @param type type of payment
 * @param timing calculated timing
 * @param amount amount of payment
 * @param currency currency type of payment
 * @param name account holder name
 * @param account_number account number
 * @param account_type type of account
 * @param bank_code routing number
 * @param number card number
 * @param security_code card security code
 * @param expiration_year card expiration year
 * @param expiration_month card expiration month
 * @param address billing address
 * @param walletType type of digital wallet (e.g., GOOGLE_PAY)
 * @param digitalWalletPayload encrypted digital wallet token
 */
data class PaymentDetail (
    @SerializedName("type") val type: String,
    @SerializedName("timing") val timing: Long,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String = "USD",
    @SerializedName("name") val name: String? = "",
    @SerializedName("merchant") val merchant: String? = null,
    @SerializedName("service_fee") val service_fee: String? = null,
    @SerializedName("account_number") val account_number: String? = null,
    @SerializedName("account_type") val account_type: String? = null,
    @SerializedName("bank_code") val bank_code: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("security_code") val security_code: String? = null,
    @SerializedName("expiration_year") val expiration_year: String? = null,
    @SerializedName("expiration_month") val expiration_month: String? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("fee_mode") var fee_mode: String? = FeeMode.MERCHANT_FEE,
    @SerializedName("payor_info") var payorInfo: PayorInfo? = null,
    @SerializedName("buyer") val buyer: String? = null,
    @SerializedName("buyer_contact") val buyerContact: String? = null,
    @SerializedName("sessionKey") var sessionKey: String? = null,
    @SerializedName("wallet_type") val walletType: String? = null,
    @SerializedName("digital_wallet_payload") val digitalWalletPayload: String? = null
) 