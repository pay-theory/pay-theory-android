import com.google.gson.annotations.SerializedName
import com.paytheory.android.sdk.ConfirmationMessage
import com.paytheory.android.sdk.configuration.FeeMode


/**
 * Data class to store resulting transaction data
 * @param paymentDetailReference payment reference id
 * @param paymentSourceId payment source id
 * @param paymentApplicationId payment application id
 * @param state state of transaction
 * @param amount amount of transaction
 * @param cardBrand brand of the card
 * @param lastFour last four of card number
 * @param serviceFee service fee amount
 * @param currency currency type of transaction amount
 * @param createdAt transaction creation time
 * @param updatedAt transaction updated time
 * @param metadata optional metadata that are added to payment
 */
data class TransferMessage (
    @SerializedName("payment-detail-reference") val paymentDetailReference: String,
    @SerializedName("payment-source-id") val paymentSourceId: String,
    @SerializedName("payment-application-id") val paymentApplicationId: String,
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("card_brand") val cardBrand: String,
    @SerializedName("last_four") val lastFour: String,
    @SerializedName("service_fee") val serviceFee: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("metadata") var metadata: Map<String,String> = HashMap<String,String>()
)

/**
 * Data class to store resulting barcode data
 * @param
 */
data class BarcodeMessage (
    @SerializedName("BarcodeUid") val barcodeUid: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
)

/**
 * Data class to store host token message details
 * @param hostToken token with transaction details
 * @param publicKey encryption key to encode/decode messages
 * @param sessionKey encryption key to encode/decode messages
 */
data class HostTokenMessage (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: HostToken
)

/**
 * Data class to store host token message details
 * @param hostToken token with transaction details
 */
data class HostToken (
    @SerializedName("hostToken") val hostToken: String,
    @SerializedName("publicKey") val publicKey: String,
    @SerializedName("sessionKey") val sessionKey: String
)

//{"type": "host_token", "body": {"hostToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJob3N0VG9rZW4iOnsiZXhwIjoxNjYyMDQzODI4LCJtZXJjaGFudF91aWQiOiIxYjhjNjdlMC05ODcxLTQ0ZjUtOTViNC1jYWQ1NDVlOTM2ZTAiLCJjYXJkX3Byb2Nlc3NvciI6ImZpbml4IiwiYWNoX3Byb2Nlc3NvciI6ImZpbml4IiwiY2FzaF9wcm9jZXNzb3IiOiJwYXkgaXQgdG9kYXkiLCJjaGFsbGVuZ2UiOiJLcmV2OUtDaFBPeDN2Y1Qwb2JGLXRadGNpdW9wa3RHZmNqOFowSFo2d3NWQWRNV3RPdHRvbzFKSVhnSDIyN291ekxVQURaLUpjUkhJQ2FHX1Z1V1g0RUFhc3U1cldwMlktNUJ3ODNYOXIzWUtockFVSWgycl8wNU5qV29XeFloLVZnZlFzaC1RM2xYMXgxcXZUcDBHVm1uV1Jjb1c2YmJrQkJmcTQyY0NUa3M9Iiwib3JpZ2luIjoibmF0aXZlIiwiYXBpX2tleSI6ImFiZWwtcGF5dGhlb3J5bGFiLTVmNzVlOTRhNjZkYzVmODhhOGYyMDdmMzRmNjcwZWU3In0sImV4cCI6MTY2MjA0NDQzNC41MjI1MjM2LCJzZXNzaW9uS2V5IjoiWHlQT0NkamZJQU1DRV9nPSJ9.JRPzEKYe17Qo-XN2-Zr6-PvxfegwhsxUaMtNa5T2s6I", "publicKey": "vmE2tildFZaQeB/hXqGTfp3TeQalMQEeI60wqt1HYR0=", "sessionKey": "XyPOCdjfIAMCE_g="}}
/**
 * Data class to store host token request
 */
data class HostTokenRequest(
    @SerializedName("ptToken") val ptToken: String,
    @SerializedName("attestation") val attestation: String,
    @SerializedName("timing") val timing: Long,
    @SerializedName("origin") val origin: String = "android",
    @SerializedName("application_package_name") val applicationPackageName: String,
)

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
    @SerializedName("personal_address") val address: Address? = null
)

/**
 * Data class to store cash request
 * @param hostToken token with transaction details
 * @param payment object that contains payment details
 * @param timing calculated timing
 * @param payorInfo optional buyer options data
 */
data class CashRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("payment") val payment: Payment,
    @SerializedName("timing") val timing: Long,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?
)

/**
 * Payment data
 * @param currency currency type of transaction amount
 * @param amount amount of payment
 * @param fee_mode fee mode that will be used for transaction
 * @param payorInfo optional buyer options data
 */
data class PaymentData(
    @SerializedName("currency") val currency: String?,
    @SerializedName("amount") val amount: Int,
    @SerializedName("fee_mode") val fee_mode: String?
)

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

/**
 * Data class for transfer part one request
 * @param hostToken token with transaction details
 */
data class TransferPartOneRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("payment_method_data") val paymentMethodData: PaymentMethodData,
    @SerializedName("payment_data") val paymentData: PaymentData,
    @SerializedName("confirmation_needed") val confirmationNeeded: Boolean,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long

)

/**
 * Data class for the transfer part two
 */
data class TransferPartTwoRequest(
    @SerializedName("payment_prep") val paymentPrep: ConfirmationMessage,
    @SerializedName("tags") val tags: HashMap<Any, Any>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long
)

/**
 * Data class for tokenize request
 */
data class TokenizeRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("payment_method_data") val paymentMethodData: PaymentMethodData,
    @SerializedName("payor_info") val payorInfo: PayorInfo? = null,
    @SerializedName("pay_theory_data") val payTheoryData: HashMap<Any, Any>?,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long
)

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
    @SerializedName("line1") val line1: String? = "",
    @SerializedName("line2") val line2: String? = "",
    @SerializedName("city") val city: String? = "",
    @SerializedName("region") val region: String? = "",
    @SerializedName("postal_code") val postal_code: String? = "",
    @SerializedName("country") val country: String? = "USA"
)
/**
 * Data class to store payment details
 * @param type type of payment
 * @param timing calculated timing
 * @param amount amound of payment
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
data class Payment (
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

/**
 * Data class to store payment method token details
 * @param type type of payment
 * @param timing calculated timing
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
data class PaymentMethodTokenData (
    @SerializedName("type") val type: String,
    @SerializedName("timing") val timing: Long,
    @SerializedName("name") val name: String? = "",
    @SerializedName("account_number") val account_number: String? = null,
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