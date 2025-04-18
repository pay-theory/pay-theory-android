package com.paytheory.lib.api

import com.google.gson.JsonObject
import com.paytheory.lib.integration.base.BaseIntegrationTest
import com.paytheory.lib.integration.doubles.NetworkServiceTestDouble
import com.paytheory.lib.integration.fixtures.TestDataProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Sample integration test for API services.
 * This test demonstrates how to use the integration test framework to test network services.
 */
@ExperimentalCoroutinesApi
class ApiServiceIntegrationSampleTest : BaseIntegrationTest() {
    
    private lateinit var networkServiceTestDouble: NetworkServiceTestDouble
    private lateinit var mockBaseUrl: String
    private lateinit var testApiService: PayTheoryApiService
    
    @Before
    override fun setUp() {
        super.setUp()
        
        // Create and start the network service test double
        networkServiceTestDouble = NetworkServiceTestDouble()
        mockBaseUrl = networkServiceTestDouble.start()
        
        // Create a Retrofit instance with the mock web server URL
        val retrofit = Retrofit.Builder()
            .baseUrl(mockBaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        // Create the API service
        testApiService = retrofit.create(PayTheoryApiService::class.java)
    }
    
    @After
    override fun tearDown() {
        // Stop the mock web server
        networkServiceTestDouble.stop()
        
        super.tearDown()
    }
    
    /**
     * Test successful payment token creation.
     */
    @Test
    fun testCreatePaymentToken() = runTest {
        // Given: A mock response for the create payment token endpoint
        val successResponse = JsonObject().apply {
            addProperty("token", TestDataProvider.createMockPaymentToken())
            addProperty("last_four", "1111")
            addProperty("card_type", "visa")
        }
        
        networkServiceTestDouble.enqueueResponse("/tokens", 200, successResponse)
        
        // When: We call the create payment token endpoint
        val tokenRequest = JsonObject().apply {
            addProperty("card_number", "4111111111111111")
            addProperty("expiration", "12/25")
            addProperty("cvv", "123")
        }
        
        val response = testApiService.createToken(tokenRequest)
        
        // Then: We should get a successful response with the expected data
        assertNotNull(response)
        assertEquals(200, response.code())
        
        val responseBody = response.body()
        assertNotNull(responseBody)
        
        val token = responseBody.get("token").asString
        Timber.d("Created token: $token")
        
        assertEquals("1111", responseBody.get("last_four").asString)
        assertEquals("visa", responseBody.get("card_type").asString)
    }
    
    /**
     * Test error handling for payment token creation.
     */
    @Test
    fun testCreatePaymentTokenError() = runTest {
        // Given: A mock error response for the create payment token endpoint
        val errorResponse = JsonObject().apply {
            addProperty("error", "Card declined")
            addProperty("code", "CARD_DECLINED")
            addProperty("message", "The card was declined")
        }
        
        networkServiceTestDouble.enqueueResponse("/tokens", 400, errorResponse)
        
        // When: We call the create payment token endpoint with invalid data
        val tokenRequest = JsonObject().apply {
            addProperty("card_number", "4111111111111111")
            addProperty("expiration", "12/21") // Expired card
            addProperty("cvv", "123")
        }
        
        val response = testApiService.createToken(tokenRequest)
        
        // Then: We should get an error response
        assertNotNull(response)
        assertEquals(400, response.code())
        
        val responseBody = response.body()
        assertNotNull(responseBody)
        
        assertEquals("Card declined", responseBody.get("error").asString)
        assertEquals("CARD_DECLINED", responseBody.get("code").asString)
    }
    
    /**
     * Verify that request data is correctly transmitted to the server.
     */
    @Test
    fun testRequestDataTransmission() = runTest {
        // Given: A mock response
        val successResponse = JsonObject().apply {
            addProperty("success", true)
        }
        
        networkServiceTestDouble.enqueueResponse("/process_payment", 200, successResponse)
        
        // When: We make a payment processing request
        val paymentRequest = JsonObject().apply {
            addProperty("token", TestDataProvider.createMockPaymentToken())
            addProperty("amount", 1000)
            addProperty("currency", "USD")
            addProperty("description", "Test payment")
        }
        
        testApiService.processPayment(paymentRequest)
        
        // Then: The request should be recorded with the correct data
        val recordedRequests = networkServiceTestDouble.getRecordedRequests()
        assertEquals(1, recordedRequests.size)
        
        val recordedRequest = recordedRequests[0]
        assertEquals("/process_payment", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
        
        // Verify the request body contains the expected data
        val requestBody = recordedRequest.body.readUtf8()
        Timber.d("Request body: $requestBody")
        
        assert(requestBody.contains("\"amount\":1000"))
        assert(requestBody.contains("\"currency\":\"USD\""))
        assert(requestBody.contains("\"description\":\"Test payment\""))
    }
} 