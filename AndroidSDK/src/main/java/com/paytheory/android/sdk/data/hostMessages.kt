import com.google.gson.annotations.SerializedName
import com.paytheory.android.sdk.configuration.FeeMode

/**
 * Data class to store bin details
 * @param first_six first six digits of account number
 * @param last_four last four digits of card number
 * @param card_brand card brand of the card used for payment
 */
data class Bin (
    @SerializedName("first_six") val first_six: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("card_brand") val card_brand: String
)

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
 * @param tags optional tags that are added to payment
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
    @SerializedName("tags") var tags: Map<String,String> = HashMap<String,String>()
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
    @SerializedName("hostToken") val hostToken: String,
    @SerializedName("publicKey") val publicKey: String,
    @SerializedName("sessionKey") val sessionKey: String
)

/**
 * Data class to store host token request
 */
data class HostTokenRequest(
    @SerializedName("ptToken") val ptToken: String,
    @SerializedName("origin") val origin: String,
    @SerializedName("attestation") val attestation: String,
    @SerializedName("timing") val timing: Long
)

/**
 * Data class to store buyer option details
 * @param first_name first name of buyer
 * @param last_name last name of buyer
 * @param email email of buyer
 * @param phone phone number of buyer
 * @param address address of buyer
 */
data class BuyerOptions (
    @SerializedName("first_name") val first_name: String? = null,
    @SerializedName("last_name") val last_name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("address") val address: Address? = null
)

/**
 * Data class to store cash request
 * @param hostToken token with transaction details
 * @param payment object that contains payment details
 * @param timing calculated timing
 * @param buyerOptions optional buyer options data
 */
data class CashRequest(
    @SerializedName("hostToken") val hostToken: String?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("payment") val payment: Payment,
    @SerializedName("timing") val timing: Long,
    @SerializedName("buyer_options") val buyerOptions: BuyerOptions? = null,
    @SerializedName("tags") val tags: HashMap<String, String>?
)

/**
 * Payment data
 * @param currency currency type of transaction amount
 * @param amount amount of payment
 * @param fee_mode fee mode that will be used for transaction
 * @param buyerOptions optional buyer options data
 */
data class PaymentData(
    @SerializedName("currency") val currency: String?,
    @SerializedName("amount") val amount: Int,
    @SerializedName("fee_mode") val fee_mode: String?,
    @SerializedName("buyer_options") val buyerOptions: BuyerOptions? = null
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
data class InstrumentData (
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
    @SerializedName("instrument_data") val instrumentData: InstrumentData,
    @SerializedName("payment_data") val paymentData: PaymentData,
    @SerializedName("confirmation_needed") val confirmationNeeded: Boolean,
    @SerializedName("buyer_options") val buyerOptions: BuyerOptions? = null,
    @SerializedName("tags") val tags: HashMap<String, String>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long

)

/**
 * Data class for the transfer part two
 */
data class TransferPartTwoRequest(
    @SerializedName("transfer") val transferBody: PaymentConfirmation,
    @SerializedName("tags") val tags: HashMap<String, String>?,
    @SerializedName("sessionKey") val sessionKey: String?,
    @SerializedName("timing") val timing: Long
)

/**
 * Data class to store address details
 * @param city billing city
 * @param region billing region
 * @param postal_code billing postal code
 * @param line1 billing address line 1
 * @param line2 billing address line 2
 * @param country billing country
 */
data class Address (
    @SerializedName("line1") val line1: String? = null,
    @SerializedName("line2") val line2: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("postal_code") val postal_code: String? = null,
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
 * @param ptInstrument token with payment details
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
    @SerializedName("fee_mode") var fee_mode: String? = FeeMode.SURCHARGE,
    @SerializedName("buyer_options") var buyerOptions: BuyerOptions? = null,
    @SerializedName("buyer") val buyer: String? = null,
    @SerializedName("buyerContact") val buyerContact: String? = null,
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

/**
 * Data class to store payment confirmation details
 */
data class PaymentConfirmation (
    @SerializedName("bin") val bin: Bin,
    @SerializedName("idempotency") val idempotency: String,
    @SerializedName("payment") val payment: Payment,
    @SerializedName("payment-token") val paymentToken: String,
    @SerializedName("payment_intent_id") val paymentIntentId: String,
)