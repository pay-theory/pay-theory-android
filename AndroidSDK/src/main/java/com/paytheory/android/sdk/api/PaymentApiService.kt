import com.google.gson.annotations.SerializedName
import com.paytheory.android.sdk.PaymentResult
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface PaymentApiService {
    @POST("payment")
    fun postIdempotency(@HeaderMap headers: Map<String, String>,@Body paymentPostData: PaymentPostData): Observable<PaymentResult> // body data
}


data class ACHPaymentData (
    @SerializedName("account_number") var account_number:String,
    @SerializedName("bank_code") var routing_number: String,
    @SerializedName("account_type") var account_type: String = "CHECKING",
    @SerializedName("type") var type: String = "BANK_ACCOUNT",
)

data class CCPaymentData(
    @SerializedName("number") var number:String,
    @SerializedName("security_code") var security_code: String,
    @SerializedName("expiration_month") var expiration_month: String,
    @SerializedName("expiration_year") var expiration_year: String,
    @SerializedName("type") var type: String = "PAYMENT_CARD",
)

data class PaymentPostData(
    @SerializedName("payment") var payment: Any,
    @SerializedName("idempotencyToken") var idempotencyToken: String,
    @SerializedName("signature") var signature: String,
    @SerializedName("credId") var credId: String,
    @SerializedName("challenge") var challenge: String,
    @SerializedName("tags") var tags: Map<String,String> = HashMap<String,String>(),
    @SerializedName("buyerOptions") var buyerOptions: Map<String,String> = HashMap<String,String>(),
)