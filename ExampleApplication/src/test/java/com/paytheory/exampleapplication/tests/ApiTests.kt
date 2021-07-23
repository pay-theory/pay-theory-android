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

    private val partner = "abel"
    private val stage = "paytheorystudy"
    private val testChallenge ="test challenge"
    private val name = "Some Body"
    private val amount = "1000"
    private val userId = "12345"
    private val userName = "Some Body"
    private val displayName = "Some One"
    private val timeout = 12345
    private val attestation = "test attestation"
    private val testOrigin = "test origin"
    private val authenticatorAttatchment = "test authenticator attachment"
    private val userVerification = "test user verification"
    private val testToken = "test pt token"
    private val type = "public key"


    private fun buildApiHeaders(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        headerMap["X-API-Key"] = "pt-sandbox-test-123456789124564789456123"
        return headerMap
    }

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
        val authenticatorSelection = AuthenticatorSelection(authenticatorAttatchment, userVerification)
        val arrayList = ArrayList<PubKeyCredParam>()
        arrayList.add(PubKeyCredParam(12345, type))

        val challengeOptions = ChallengeOptions(testChallenge, Rp(name, amount), User(userId, userName, displayName), arrayList, authenticatorSelection,
            timeout, attestation)

        val ptTokenResponse = PTTokenResponse(testToken, testOrigin, challengeOptions)

        assert(challengeOptions.challenge == testChallenge)
        assert(challengeOptions.rp.amount == amount && challengeOptions.rp.name == name)
        assert(challengeOptions.user.id == userId && challengeOptions.user.displayName == displayName && challengeOptions.user.name == userName)
        assert(challengeOptions.pubKeyCredParams[0].alg == 12345 && challengeOptions.pubKeyCredParams[0].type == type)
        assert(challengeOptions.authenticatorSelection.authenticatorAttachment == authenticatorAttatchment && challengeOptions.authenticatorSelection.userVerification == userVerification)
        assert(challengeOptions.timeout == 12345)
        assert(challengeOptions.attestation == attestation)
        assert(ptTokenResponse.challengeOptions == challengeOptions && ptTokenResponse.origin == testOrigin && ptTokenResponse.ptToken == testToken)

    }
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
