package com.paytheory.android.sdk.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.HeaderMap




//{
//    "pt-token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJVSUQiOiJkODM4NjQyYi01OTRlLTRlMTctYmI4Zi0wZWNmNzNkYmFmYTYiLCJtZXJjaGFudCI6eyJmaXhlZF9mZWUiOnsiZmVlIjoxOTUsIm1lcmNoYW50IjoiSURlS29kRUg4cUsxa0tmR2YzMlRFd1RNIn0sInN1cmNoYXJnZSI6eyJmZWUiOjAsIm1lcmNoYW50IjoiSURpR2Z5R1JzQWN2VjJpMzR0bmFXQkxtIn0sImJhc2lzX3BvaW50cyI6eyJmZWUiOjM4MCwibWVyY2hhbnQiOiJJRDRDRWV5WVNldFUyZTg0NWljRjhHakYifX0sImNoYWxsZW5nZSI6Ik14c1pjSXhfd2xuaEY3LWFYQnVhM2ljOWVxT1VCN211bXpweVB0OGlBUHB4VTVUYTQwOFB6bWkyLUhZOXYtb0JDY1BKalY5Nzc0OEJaTmZYWVlfR0RySUFiYkp5TDIyNjhlTWVyN2dYamJqN3NiZnNuSUtJYTloNjNxbk84QW43YnBiTGlwTWM5U2ltNThmUDg0NDVfeWxZZGVZN0RPcERPVENjNXk0NVVVdz0iLCJvcmlnaW4iOiJodHRwczovL2Rldi5odG1sLmV4YW1wbGUucGF5dGhlb3J5c3R1ZHkuY29tIiwiaWF0IjoxNjE2Nzg0MzE3LCJleHAiOjE2MTY3ODQzNzd9.oRC0Qkgppnoq7KRX9offWxKFXRnLUPddz09YCpfU-KQ",
//    "origin": "https://dev.html.example.paytheorystudy.com",
//    "challengeOptions": {
//    "challenge": "MxsZcIx_wlnhF7-aXBua3ic9eqOUB7mumzpyPt8iAPpxU5Ta408Pzmi2-HY9v-oBCcPJjV97748BZNfXYY_GDrIAbbJyL2268eMer7gXjbj7sbfsnIKIa9h63qnO8An7bpbLipMc9Sim58fP8445_ylYdeY7DOpDOTCc5y45UUw=",
//    "rp": {
//        "name": "Pay Theory SDK",
//        "id": "dev.html.example.paytheorystudy.com"
//    },
//    "user": {
//        "id": "ID4CEeyYSetU2e845icF8GjF",
//        "name": "Pay Theory SDK",
//        "displayName": "Pay Theory SDK Merchant"
//    },
//    "pubKeyCredParams": [
//    {
//        "alg": -7,
//        "type": "public-key"
//    }
//    ],
//    "authenticatorSelection": {
//        "authenticatorAttachment": "platform",
//        "userVerification": "required"
//    },
//    "timeout": 300000,
//    "attestation": "none"
//}
//}


/**
 * Interface that handles data for pay theory challenge service
 */
interface PtTokenApiService {
    /**
     * Function that takes headers map and creates observable for the challenge response
     * @param headers headers map for api call
     */
    @GET("pt-token")
    fun doPtToken(@HeaderMap headers: Map<String, String>): Observable<PtTokenResponse>
}

/**
 * Data class that contains the challenge response
 * @param challenge challenge response string
 */
data class PtTokenResponse(
    @SerializedName("pt-token") val ptToken: String = "",
    @SerializedName("origin") val origin: String = "",
    @SerializedName("challengeOptions") val challengeOptions: PtTokenChallengeOptions? = null,
)

data class PtTokenChallengeOptions(
    @SerializedName("challenge") val challenge: String = "",
    @SerializedName("rp") val rp: Rp,
    @SerializedName("user") val user: User,
    @SerializedName("pubKeyCredParams") val pubKeyCredParams: PubKeyCredParams,
    @SerializedName("authenticatorSelection") val authenticatorSelection: AuthenticatorSelection,
    @SerializedName("timeout") val timeout: Int = 0,
    @SerializedName("attestation") val attestation: String = "",
)
data class Rp(
    @SerializedName("name") val name: String = "",
    @SerializedName("id") val id: String= "",

)
data class User(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("displayName") val displayName: String = "",

)

//TODO is an array object
data class PubKeyCredParams(
    @SerializedName("alg") val alg: Int = 0,
    @SerializedName("type") val type: String = "",
    )


data class AuthenticatorSelection(
    @SerializedName("authenticatorAttachment") val authenticatorAttachment: String = "",
    @SerializedName("userVerification") val userVerification: String = "",

    )

