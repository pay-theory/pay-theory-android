package com.paytheory.lib.api

import android.annotation.SuppressLint
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.reactivex.rxjava3.core.Observable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection

/**
 * Unit tests for PTTokenApiService
 */
@RunWith(RobolectricTestRunner::class)
class PTTokenApiServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: PTTokenApiService
    private val gson = Gson()

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val baseUrl = mockWebServer.url("/").toString()

        apiService = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PTTokenApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `doToken should include headers in the request`() {
        // Given
        val mockResponse = getMockTokenResponse()
        val responseJson = gson.toJson(mockResponse)
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_OK)
                .setBody(responseJson)
        )

        val headers = mapOf(
            "Authorization" to "Bearer test_token",
            "Content-Type" to "application/json",
            "X-Custom-Header" to "CustomValue"
        )

        // When
        apiService.doToken(headers).test()

        // Then
        val request = mockWebServer.takeRequest()
        assertEquals("Bearer test_token", request.getHeader("Authorization"))
        assertEquals("application/json", request.getHeader("Content-Type"))
        assertEquals("CustomValue", request.getHeader("X-Custom-Header"))
    }

    @Test
    fun `mocked api service should behave correctly`() {
        // Given
        val mockPTTokenApiService = mockk<PTTokenApiService>()
        val mockResponse = getMockTokenResponse()
        val headerSlot = slot<Map<String, String>>()
        
        every { mockPTTokenApiService.doToken(capture(headerSlot)) } returns Observable.just(mockResponse)

        val headers = mapOf(
            "Authorization" to "Bearer test_token",
            "Content-Type" to "application/json"
        )

        // When
        val result = mockPTTokenApiService.doToken(headers).blockingFirst()

        // Then
        verify { mockPTTokenApiService.doToken(headers) }
        assertEquals("test-pt-token", result.ptToken)
        assertEquals("Bearer test_token", headerSlot.captured["Authorization"])
    }

    /**
     * Creates a mock token response for testing
     */
    private fun getMockTokenResponse(): PTTokenResponse {
        val pubKeyCredParams = arrayListOf(
            PubKeyCredParam(-7, "public-key")
        )
        
        val authenticatorSelection = AuthenticatorSelection(
            "platform",
            "required"
        )
        
        val rp = Rp(
            "Pay Theory",
            "paytheory.com"
        )
        
        val user = User(
            "test-merchant-id",
            "Pay Theory Test App",
            "Pay Theory"
        )
        
        val challengeOptions = ChallengeOptions(
            "test-challenge",
            rp,
            user,
            pubKeyCredParams,
            authenticatorSelection,
            60000,
            "direct"
        )
        
        return PTTokenResponse(
            "test-pt-token",
            "https://test.paytheory.com",
            challengeOptions
        )
    }
} 