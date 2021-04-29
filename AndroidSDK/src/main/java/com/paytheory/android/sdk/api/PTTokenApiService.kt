package com.paytheory.android.sdk.api

import com.google.gson.annotations.SerializedName
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*
/**
 * Interface that handles data for pay theory challenge service
 */
interface PTTokenApiService {
    /**
     * Function that takes headers map and creates observable for the pt-token response
     * @param headers headers map for api call
     */
    @GET("token")
    fun doToken(@HeaderMap headers: Map<String, String>): Observable<PTTokenResponse>
}

/**
 * Data class that contains the challenge response
 */
data class PTTokenResponse(
    @SerializedName("pt-token") val ptToken: String,
    @SerializedName("origin") val origin: String,
    @SerializedName("challengeOptions") val challengeOptions: ChallengeOptions
)

/**
 * Data class that contains data for idempotency response payment details
 * @param challenge challenge for attestation
 * @param rp rp of challenge
 * @param user user of challenge
 * @param pubKeyCredParams public key parameters of challenge
 * @param authenticatorSelection of challenge
 * @param timeout of challenge
 * @param attestation attestation requirements of challenge
 */
data class ChallengeOptions(
    @SerializedName("challenge") val challenge: String,
    @SerializedName("rp") val rp: Rp,
    @SerializedName("user") val user: User,
    @SerializedName("pubKeyCredParams") val pubKeyCredParams: ArrayList<PubKeyCredParam>,
    @SerializedName("authenticatorSelection") val authenticatorSelection: AuthenticatorSelection,
    @SerializedName("timeout") val timeout: Int,
    @SerializedName("attestation") val attestation: String
)

/**
 * Data class that contains challengeOptions rp
 * @param name rp name
 */
data class Rp(
    @SerializedName("name") val name: String,
    @SerializedName("id") val amount: String
)

/**
 * Data class that contains challengeOptions user
 * @param id merchant identity
 * @param name application name
 * @param displayName display name
 */
data class User(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String
)

/**
 * Data class that contains challengeOptions pubKeyCredParam
 * @param alg pubKeyCredParams algorithm
 * @param type public key
 */
data class PubKeyCredParam(
    @SerializedName("alg") val alg: Int,
    @SerializedName("type") val type: String
)

/**
 * Data class that contains challengeOptions authenticator selection
 * @param authenticatorAttachment platform
 * @param userVerification required
 */
data class AuthenticatorSelection(
    @SerializedName("authenticatorAttachment") val authenticatorAttachment: String,
    @SerializedName("userVerification") val userVerification: String
)