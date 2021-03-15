import com.google.gson.annotations.SerializedName
import com.paytheory.android.sdk.PaymentResult
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

/**
 * Interface that handles data for payment api calls
 */
interface PaymentApiService {
    /**
     * Function that takes headers map, payment post body data, and creates observable for payment response
     * @param headers headers map for api call
     * @param paymentPostData payment body for api call
     */
    @POST("payment")
    fun postIdempotency(@HeaderMap headers: Map<String, String>,@Body paymentPostData: PaymentPostData): Observable<PaymentResult> // body data
}

/**
 * Data class that contains data for ACH payment request body
 * @param account_number ACH payment account number
 * @param routing_number ACH payment routing number
 * @param account_type "CHECKING" or "SAVINGS"
 * @param type "android"
 */
data class ACHPaymentData (
    @SerializedName("account_number") var account_number:String,
    @SerializedName("bank_code") var routing_number: String,
    @SerializedName("account_type") var account_type: String = "CHECKING",
    @SerializedName("type") var type: String = "BANK_ACCOUNT",
)

/**
 * Data class that contains data for card payment request body
 * @param number card number
 * @param security_code card security code or CVV
 * @param expiration_month card expiration month
 * @param expiration_year card expiration year
 * @param type "PAYMENT_CARD"
 */
data class CCPaymentData(
    @SerializedName("number") var number:String,
    @SerializedName("security_code") var security_code: String,
    @SerializedName("expiration_month") var expiration_month: String,
    @SerializedName("expiration_year") var expiration_year: String,
    @SerializedName("type") var type: String = "PAYMENT_CARD",
)

/**
 * Data class that contains data for ACH payment request body
 * @param payment payment details
 * @param idempotencyToken idempotency string
 * @param signature string for signing challenge and payment data
 * @param credId credentials for pay theory verifier service
 * @param challenge pay theory challenge string
 * @param tags custom tags that can be added to transaction
 * @param buyerOptions optional consumer identification details
 */
data class PaymentPostData(
    @SerializedName("payment") var payment: Any,
    @SerializedName("idempotencyToken") var idempotencyToken: String,
    @SerializedName("signature") var signature: String,
    @SerializedName("credId") var credId: String,
    @SerializedName("challenge") var challenge: String,
    @SerializedName("tags") var tags: Map<String,String> = HashMap<String,String>(),
    @SerializedName("buyerOptions") var buyerOptions: Map<String,String> = HashMap<String,String>(),
)
