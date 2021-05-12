package com.paytheory.exampleapplication.api

import com.paytheory.android.sdk.Constants
import com.paytheory.android.sdk.api.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import strikt.api.expectThat

/**
 * Unit tests for HTTP calls
 */
class ApiTests {

    private fun buildApiHeaders(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        headerMap["X-API-Key"] = "pt-sandbox-abel-123456789124564789456123"
        return headerMap
    }

    /**
     * interface for pt-token call
     */
    interface TestPtToken {
        /**
         * function that returns pt-token call
         * @param headers headers of pt-token call
         */
        @GET("token")
        fun doToken(@HeaderMap headers: Map<String, String>): Call<PTTokenResponse>
    }

    /**
     * function that creates api client
     */
    private fun ptTokenCall(): TestPtToken? {
        return Retrofit.Builder()
            .baseUrl(Constants("abel").API_BASE_PATH)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(ApiWorker.gsonConverter)
            .client(ApiWorker.client)
            .build()
            .create(TestPtToken::class.java)
    }

    /**
     * test pt token api creation and functions
     */
    @Test
    fun ptTokenTest() {
        val api = ptTokenCall()
        val call = api!!.doToken(buildApiHeaders())


        expectThat(call.request()) {
            assertThat("is GET method") {
                it.method == "GET"
            }
            assertThat("has correct URL") {
                it.url == "https://abel.token.service.paytheorystudy.com/token".toHttpUrlOrNull()
            }
            assertThat("Content-Type is correct") {
                it.headers["Content-Type"] == "application/json"
            }
        }
    }

    /**
     * test api service
     */
    @Test
    fun apiServiceTest() {
        val constants = Constants("abel")

        val apiService = ApiService(constants.API_BASE_PATH)

//        val api = apiService.ptTokenApiCall()

//        val call: io.reactivex.rxjava3.core.Observable<PTTokenResponse> = api.doToken(buildApiHeaders())



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
        assert(apiWorker.client is OkHttpClient)
        assert(apiWorker.gsonConverter is GsonConverterFactory)
    }

    /**
     * test pt token api service
     */
    @Test
    fun ptTokenApiServiceTest() {
        val authenticatorSelection = AuthenticatorSelection("test authenticator attachment", "test user verification")
        val arrayList = ArrayList<PubKeyCredParam>()
        arrayList.add(PubKeyCredParam(12345, "test"))

        val challengeOptions = ChallengeOptions("test challenge", Rp("Some Body", "1000"), User("12345", "Some Body",
            "Some One"), arrayList, authenticatorSelection,
            12345, "test attestation")

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