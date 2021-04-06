package com.paytheory.exampleapplication.api

import com.paytheory.android.sdk.Constants
import com.paytheory.android.sdk.api.ApiWorker
import com.paytheory.android.sdk.api.PTTokenResponse
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
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
        headerMap["X-API-Key"] = "pt-sandbox-finix-3f77175085e9834c6f514a77eddfdb87"
        return headerMap
    }

    /**
     * function that creates api client
     */
    fun ptTokenCall(): TestPtToken? {
        return Retrofit.Builder()
            .client(ApiWorker.client)
            .baseUrl(Constants("finix").API_BASE_PATH)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ApiWorker.gsonConverter)
            .build()
            .create(TestPtToken::class.java)
    }

    /**
     * interface for pt-token call
     */
    interface TestPtToken {
        /**
         * function that returns pt-token call
         * @param headers headers of pt-token call
         */
        @GET("pt-token")
        fun doToken(@HeaderMap headers: Map<String, String>): retrofit2.Call<PTTokenResponse>
    }

    /**
     * test pt token api creation and functions
     */
    @Test
    fun ptTokenTest() {
        val api = ptTokenCall()
        val call: retrofit2.Call<PTTokenResponse> = api!!.doToken(buildApiHeaders())


        expectThat(call.request()) {
            assertThat("is GET method") {
                it.method == "GET"
            }
            assertThat("has correct URL") {
                it.url == "https://dev.tags.api.paytheorystudy.com/pt-token".toHttpUrlOrNull()
            }
            assertThat("Content-Type is correct") {
                it.headers["Content-Type"] == "application/json"
            }
        }
    }
}