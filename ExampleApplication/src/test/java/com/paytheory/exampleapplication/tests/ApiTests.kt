package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.Constants
import com.paytheory.android.sdk.api.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import strikt.api.expectThat
import kotlin.collections.ArrayList

/**
 * Unit tests for HTTP calls
 */
class ApiTests {

    val partner = "abel"
    val stage = "paytheorystudy"
    val testChallenge ="test challenge"
    val name = "Some Body"
    val amount = "1000"
    val userId = "12345"
    val userName = "Some Body"
    val displayName = "Some One"
    val timeout = 12345
    val attestation = "test attestation"


    private fun buildApiHeaders(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        headerMap["X-API-Key"] = "pt-sandbox-test-123456789124564789456123"
        return headerMap
    }

//    /**
//     * test pt token api creation and functions
//     */
//    @Test
//    fun ptTokenTest() {
//        val apiService = ApiService("test")
//
//        val api = apiService.ptTokenApiCall()
//        api.doToken(buildApiHeaders())
//
//        val call = api!!.doToken(buildApiHeaders())
//
//
//        expectThat(call.request()) {
//            assertThat("is GET method") {
//                it.method == "GET"
//            }
//            assertThat("has correct URL") {
//                it.url == "https://test.token.service.paytheorystudy.com/token".toHttpUrlOrNull()
//            }
//            assertThat("Content-Type is correct") {
//                it.headers["Content-Type"] == "application/json"
//            }
//        }
//    }

    /**
     * test api service
     */
    @Test
    fun apiServiceTest() {
        val constants = Constants(partner,stage)

        val apiService = ApiService(constants.API_BASE_PATH)

        assert( apiService.basePath == constants.API_BASE_PATH)

    }


    /**
     * test api worker
     */
    @Test
    fun apiWorkerTest() {
        val apiWorker = ApiWorker

        val interceptors = apiWorker.client.interceptors

        assert(interceptors.size == 1)
        assert(apiWorker.client.readTimeoutMillis == 20000)
        assert(apiWorker.client.connectTimeoutMillis == 15000)
    }

    /**
     * test pt token api service
     */
    @Test
    fun ptTokenApiServiceTest() {
        val authenticatorSelection = AuthenticatorSelection("test authenticator attachment", "test user verification")
        val arrayList = ArrayList<PubKeyCredParam>()
        arrayList.add(PubKeyCredParam(12345, "test"))

        val challengeOptions = ChallengeOptions(testChallenge, Rp(name, amount), User(userId, userName, displayName), arrayList, authenticatorSelection,
            timeout, attestation)

        val ptTokenResponse = PTTokenResponse("test pt token", "test origin", challengeOptions)

        assert(challengeOptions.challenge == "test challenge")
        assert(challengeOptions.rp.amount == "1000" && challengeOptions.rp.name == "Some Body")
        assert(challengeOptions.user.id == "12345" && challengeOptions.user.displayName == "Some One" && challengeOptions.user.name == "Some Body")
        assert(challengeOptions.pubKeyCredParams[0].alg == 12345 && challengeOptions.pubKeyCredParams[0].type == "test")
        assert(challengeOptions.authenticatorSelection.authenticatorAttachment == "test authenticator attachment" && challengeOptions.authenticatorSelection.userVerification == "test user verification")
        assert(challengeOptions.timeout == 12345)
        assert(challengeOptions.attestation == "test attestation")
        assert(ptTokenResponse.challengeOptions == challengeOptions && ptTokenResponse.origin == "test origin" && ptTokenResponse.ptToken == "test pt token")

    }
}