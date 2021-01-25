package com.paytheory.android.sdk.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.*

interface ChallengeApiService {
    @GET("challenge")
    fun doChallenge(@HeaderMap headers: Map<String, String>): Observable<ChallengeResponse> // body data
}

data class ChallengeResponse(
    @SerializedName("challenge") val challenge: String = "",
)