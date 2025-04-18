package com.paytheory.lib.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Observable
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.SocketPolicy
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

/**
 * Utility class containing helper methods for API service testing
 */
object TestApiUtils {

    /**
     * Creates a mock PTTokenResponse with test data
     */
    fun createMockPTTokenResponse(
        ptToken: String = "test-pt-token",
        origin: String = "https://test.paytheory.com",
        challenge: String = "test-challenge"
    ): PTTokenResponse {
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
            challenge,
            rp,
            user,
            pubKeyCredParams,
            authenticatorSelection,
            60000,
            "direct"
        )
        
        return PTTokenResponse(
            ptToken,
            origin,
            challengeOptions
        )
    }
    
    /**
     * Creates a mocked PTTokenApiService that returns the provided response
     */
    fun createMockPTTokenApiService(
        response: PTTokenResponse = createMockPTTokenResponse()
    ): PTTokenApiService {
        val mock = mockk<PTTokenApiService>()
        every { mock.doToken(any()) } returns Observable.just(response)
        return mock
    }
    
    /**
     * Creates a mock response for MockWebServer with the given response object
     */
    fun createMockResponse(
        responseObject: Any,
        responseCode: Int = HttpURLConnection.HTTP_OK,
        delayMs: Long = 0
    ): MockResponse {
        val gson = GsonBuilder()
            .setStrictness(Strictness.LENIENT)
            .disableHtmlEscaping()
            .create()
            
        val responseJson = gson.toJson(responseObject)
        
        val response = MockResponse()
            .setResponseCode(responseCode)
            .setBody(responseJson)
            
        if (delayMs > 0) {
            response.setBodyDelay(delayMs, TimeUnit.MILLISECONDS)
        }
            
        return response
    }
    
    /**
     * Creates a mock error response for MockWebServer
     */
    fun createErrorResponse(
        errorMessage: String = "An error occurred",
        responseCode: Int = HttpURLConnection.HTTP_INTERNAL_ERROR
    ): MockResponse {
        return MockResponse()
            .setResponseCode(responseCode)
            .setBody("{\"error\":\"$errorMessage\"}")
    }
    
    /**
     * Creates a GsonConverterFactory with lenient parsing for testing
     */
    fun createLenientGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .setStrictness(Strictness.LENIENT)
                .disableHtmlEscaping()
                .create()
        )
    }
    
    /**
     * Creates standard headers for API testing
     */
    fun createStandardHeaders(
        apiKey: String = "test_api_key",
        includeContentType: Boolean = true,
        additionalHeaders: Map<String, String> = emptyMap()
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["Authorization"] = "Bearer $apiKey"
        
        if (includeContentType) {
            headers["Content-Type"] = "application/json"
        }
        
        headers.putAll(additionalHeaders)
        return headers
    }
    
    /**
     * Simulates network conditions by creating appropriate MockResponse objects
     */
    object NetworkSimulator {
        /**
         * Creates a response that simulates a slow network
         */
        fun createSlowNetworkResponse(responseObject: Any): MockResponse {
            return createMockResponse(responseObject, delayMs = 1500)
        }
        
        /**
         * Creates a response that simulates a network timeout
         */
        fun createTimeoutResponse(): MockResponse {
            return MockResponse()
                .setSocketPolicy(SocketPolicy.NO_RESPONSE)
        }
        
        /**
         * Creates a response that simulates a connection error
         */
        fun createConnectionErrorResponse(): MockResponse {
            return MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST)
        }
        
        /**
         * Creates a response that simulates an unstable connection
         */
        fun createUnstableConnectionResponse(responseObject: Any): MockResponse {
            // First delays, then responds normally
            return createMockResponse(responseObject, delayMs = 800)
                .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY)
        }
    }
} 