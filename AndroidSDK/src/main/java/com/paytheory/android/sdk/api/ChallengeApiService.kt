package com.paytheory.android.sdk.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.*

/**
 * Interface that handles data for pay theory challenge service
 */
interface ChallengeApiService {
    /**
     * Function that takes headers map and creates observable for the challenge response
     * @param headers headers map for api call
     */
    @GET("challenge")
    fun doChallenge(@HeaderMap headers: Map<String, String>): Observable<ChallengeResponse>
}

/**
 * Data class that contains the challenge response
 * @param challenge challenge response string
 */
data class ChallengeResponse(
    @SerializedName("challenge") val challenge: String = "",
)