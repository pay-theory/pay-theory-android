import com.google.gson.annotations.SerializedName

data class Bin (
    @SerializedName("first_six") val first_six: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("card_brand") val card_brand: String
)
data class PreparedPayment(
    @SerializedName("currency") val currency: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("fee_mode") val fee_mode: String,
    @SerializedName("merchant") val merchant: String,
    @SerializedName("service_fee") val service_fee: Int
)
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
data class IdempotencyMessage (
    @SerializedName("payment-token") val paymentToken: String,
    @SerializedName("idempotency") val idempotency: String,
    @SerializedName("bin") val bin: Bin,
    @SerializedName("payment") val payment: PreparedPayment
)
data class InstrumentMessage (
    @SerializedName("pt-instrument") val ptInstrument: String
)
data class HostTokenMessage (
    @SerializedName("hostToken") val hostToken: String,
    @SerializedName("publicKey") val publicKey: String,
    @SerializedName("sessionKey") val sessionKey: String
)
data class BuyerOptions (
    @SerializedName("first_name") val first_name: String? = null,
    @SerializedName("last_name") val last_name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("address") val address: Address? = null
)
data class InstrumentRequest (
    @SerializedName("hostToken") val hostToken: String,
    @SerializedName("payment") val payment: Payment,
    @SerializedName("timing") val timing: Long,
    @SerializedName("buyerOptions") val buyerOptions: BuyerOptions? = null
)
data class HostTokenRequest (
    @SerializedName("ptToken") val ptToken: String,
    @SerializedName("origin") val origin: String,
    @SerializedName("attestation") val attestation: String,
    @SerializedName("timing") val timing: Long
)
data class Address (
    @SerializedName("city") val city: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("postal_code") val postal_code: String? = null,
    @SerializedName("line1") val line1: String? = null,
    @SerializedName("line2") val line2: String? = null,
    @SerializedName("country") val country: String? = null
)
data class Payment (
    @SerializedName("type") val type: String,
    @SerializedName("timing") val timing: Long,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String = "USD",
    @SerializedName("name") val name: String? = null,
    @SerializedName("account_number") val account_number: String? = null,
    @SerializedName("account_type") val account_type: String? = null,
    @SerializedName("bank_code") val bank_code: String? = null,
    @SerializedName("number") val number: String? = null,
    @SerializedName("security_code") val security_code: String? = null,
    @SerializedName("expiration_year") val expiration_year: String? = null,
    @SerializedName("expiration_month") val expiration_month: String? = null,
    @SerializedName("address") val address: Address? = null,
    @SerializedName("pt-instrument") var ptInstrument: String? = null
)
data class IdempotencyRequest (
    @SerializedName("apiKey") val apiKey: String,
    @SerializedName("payment") val payment: Payment,
    @SerializedName("hostToken") val hostToken: String,
    @SerializedName("sessionKey") val sessionKey: String,
    @SerializedName("timing") val timing: Long
)
data class Transfer (
    @SerializedName("payment-token") val paymentToken: String,
    @SerializedName("idempotency") val idempotency: String,
)
data class TransferRequest (
    @SerializedName("transfer") val transfer: Transfer,
    @SerializedName("timing") val timing: Long,
    @SerializedName("tags") var tags: Map<String,String> = HashMap<String,String>()
)
data class ActionRequest (
    @SerializedName("action") val action: String,
    @SerializedName("encoded") val encoded: String,
    @SerializedName("publicKey") val publicKey: String? = null
)