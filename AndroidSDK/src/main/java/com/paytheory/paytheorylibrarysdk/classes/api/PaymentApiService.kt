import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface PaymentApiService {
    @Headers("Content-Type: application/json","X-API-Key: pt-sandbox-dev-f992c4a57b86cb16aefae30d0a450237")

    @POST("payment")
    fun postIdempotency(@Body paymentPostData: PaymentPostData): Observable<PaymentResponse> // body data
}

data class PaymentPostData(
    @SerializedName("payment") var payment: PaymentData,
    @SerializedName("idempotencyToken") var idempotencyToken: String,
    @SerializedName("signature") var signature: String,
    @SerializedName("credId") var credId: String,
    @SerializedName("challenge") var challenge: String,
    @SerializedName("tags") var tags: Map<String,String> = HashMap<String,String>(),
    @SerializedName("buyerOptions") var buyerOptions: Map<String,String> = HashMap<String,String>(),
)

data class PaymentData(
    @SerializedName("number") var number:String,
    @SerializedName("security_code") var security_code: String,
    @SerializedName("expiration_month") var expiration_month: String,
    @SerializedName("expiration_year") var expiration_year: String,
    @SerializedName("type") var type: String = "PAYMENT_CARD",
)

data class PaymentResponse(
    @SerializedName("payment-detail-reference") val paymentDetailReference: String,
    @SerializedName("payment-source-id") val paymentSourceId: String,
    @SerializedName("payment-application-id") val paymentApplicationId: String,
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("service_fee") val service_fee: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("tags") val tags: Map<String,String>,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("updated_at") val updated_at: String,
)