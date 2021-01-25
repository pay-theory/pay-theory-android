import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.*

interface IdempotencyApiService {
    @POST("idempotency")
    fun postIdempotency(
        @HeaderMap headers: Map<String, String>,
        @Body idempotencyPostData: IdempotencyPostData): Observable<IdempotencyResponse> // body data
}

data class IdempotencyPostData(
    @SerializedName("attestation") var attestation: String,
    @SerializedName("nonce") var nonce: String,
    @SerializedName("amount") var amount: Int,
    @SerializedName("type") var type: String = "android",
    @SerializedName("currency") var currency: String = "USD",
    @SerializedName("fee_mode") var fee_mode: String = "surcharge"
//amount, currency, fee_mode
)

data class IdempotencyResponse(
    @SerializedName("response") val response: String,
    @SerializedName("credId") val credId: String,
    @SerializedName("signature") val signature: String,
    @SerializedName("idempotency") val idempotency: String,
    @SerializedName("payment") val payment: IdempotencyPayment,
    @SerializedName("challenge") val challenge: String,
)

data class IdempotencyPayment(
    @SerializedName("currency") val currency: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("fee_mode") val signature: String,
    @SerializedName("appName") val idempotency: String,
    @SerializedName("platform") val payment: String,
    @SerializedName("service_fee") val service_fee: Int,
    @SerializedName("created") val created: Double,
)