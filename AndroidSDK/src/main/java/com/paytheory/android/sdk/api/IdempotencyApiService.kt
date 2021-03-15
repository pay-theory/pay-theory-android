import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Interface that handles data for idempotency api calls
 */
interface IdempotencyApiService {
    /**
     * Function that takes headers map, idempotency body data, and creates observable for idempotency response
     * @param headers headers map for api call
     * @param idempotencyPostData idempotency body for api call
     */
    @POST("idempotency")
    fun postIdempotency(
        @HeaderMap headers: Map<String, String>,
        @Body idempotencyPostData: IdempotencyPostData): Observable<IdempotencyResponse>
}

/**
 * Data class that contains data for idempotency request body
 * @param attestation encoded SafetyNet attestation string
 * @param nonce encoded nonce string
 * @param amount amount of transaction
 * @param type "android"
 * @param currency "USD"
 * @param fee_mode surcharge or service_fee
 */
data class IdempotencyPostData(
    @SerializedName("attestation") var attestation: String,
    @SerializedName("nonce") var nonce: String,
    @SerializedName("amount") var amount: Int,
    @SerializedName("type") var type: String = "android",
    @SerializedName("currency") var currency: String = "USD",
    @SerializedName("fee_mode") var fee_mode: String = "surcharge"
)

/**
 * Data class that contains data for idempotency response body
 * @param response response data from idempotency call
 * @param credId credentials for pay theory verifier service
 * @param signature string for signing challenge and payment data
 * @param idempotency unique string identifier for a transaction
 * @param payment payment data
 * @param challenge pay theory challenge string
 */
data class IdempotencyResponse(
    @SerializedName("response") val response: String,
    @SerializedName("credId") val credId: String,
    @SerializedName("signature") val signature: String,
    @SerializedName("idempotency") val idempotency: String,
    @SerializedName("payment") val payment: IdempotencyPayment,
    @SerializedName("challenge") val challenge: String,
)

/**
 * Data class that contains data for idempotency response payment details
 * @param currency type of currency for payment
 * @param amount amount of transaction
 * @param fee_mode surcharge or service_fee
 * @param appName name of application
 * @param platform "android"
 * @param service_fee total amount of service fee if available
 * @param created time of creation
 */
data class IdempotencyPayment(
    @SerializedName("currency") val currency: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("fee_mode") val signature: String,
    @SerializedName("appName") val idempotency: String,
    @SerializedName("platform") val payment: String,
    @SerializedName("service_fee") val service_fee: Int,
    @SerializedName("created") val created: Double,
)