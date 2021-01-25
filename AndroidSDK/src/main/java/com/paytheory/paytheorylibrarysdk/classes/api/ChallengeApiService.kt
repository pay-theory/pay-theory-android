import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChallengeApiService {
    @Headers("Content-Type: application/json","X-API-Key: pt-sandbox-dev-f992c4a57b86cb16aefae30d0a450237")

    @GET("challenge")
    fun doChallenge(): Observable<ChallengeResponse> // body data
}

data class ChallengeResponse(
    @SerializedName("challenge") val challenge: String = "",
)