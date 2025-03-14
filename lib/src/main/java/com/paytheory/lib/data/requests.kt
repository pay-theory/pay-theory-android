package com.paytheory.lib.data

import com.google.gson.annotations.SerializedName
import com.paytheory.lib.configuration.FeeMode

/**
 * Data class to store host token request
 * for testing attestation outside of production set requireAttestation to true
 */
data class HostTokenRequest(
    @SerializedName("ptToken") val ptToken: String,
    @SerializedName("attestation") val attestation: String,
    @SerializedName("timing") val timing: Long,
    @SerializedName("origin") val origin: String = "android",
    @SerializedName("application_package_name") val applicationPackageName: String,
    @SerializedName("require_attestation") val requireAttestation: Boolean = true,
)

/**
 * Data class to store cash request
 * @param hostToken token with transaction details
 * @param paymentDetail object that contains payment details
 * @param timing calculated timing
 * @param payorInfo optional buyer options data
 */
data class CashRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("payment") val paymentDetail: PaymentDetail,
    @SerializedName("timing") val timing: Long,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?
)


/**
 * Data class for transfer part one request
 * @param hostToken token with transaction details
 */
data class TransferPartOneRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("payment_method_data") val paymentMethodData: PaymentMethodData,
    @SerializedName("payment_data") val paymentData: PaymentData,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long
)

/**
 * Data class to store action message data
 * @param action type of action that will occur
 * @param encoded encoded message with transaction details
 * @param publicKey encryption key
 */
data class ActionRequest (
    @SerializedName("action") val action: String,
    @SerializedName("encoded") val encoded: String,
    @SerializedName("publicKey") val publicKey: String? = null,
    @SerializedName("sessionKey") val sessionKey: String? = null
)


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
)
