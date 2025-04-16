package com.paytheory.lib.api

import okhttp3.OkHttpClient
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Tests for the ApiWorker singleton
 */
class ApiWorkerTest {
    
    @Test
    fun `client should create an OkHttpClient instance`() {
        // When
        val client = ApiWorker.client
        
        // Then
        assertNotNull("OkHttpClient should not be null", client)
        assertTrue("Client should be an OkHttpClient instance", client is OkHttpClient)
    }
    
    @Test
    fun `client should set proper timeouts`() {
        // When
        val client = ApiWorker.client
        
        // Then - Verify the client has reasonable timeout settings
        assertTrue("Connect timeout should be > 0", client.connectTimeoutMillis > 0)
        assertTrue("Read timeout should be > 0", client.readTimeoutMillis > 0)
    }
    
    @Test
    fun `gsonConverter should create a GsonConverterFactory instance`() {
        // When
        val converter = ApiWorker.gsonConverter
        
        // Then
        assertNotNull("GsonConverterFactory should not be null", converter)
        assertTrue("Converter should be a GsonConverterFactory instance", converter is GsonConverterFactory)
    }
    
    @Test
    fun `multiple calls to client should return the same instance`() {
        // When
        val client1 = ApiWorker.client
        val client2 = ApiWorker.client
        
        // Then
        assertTrue("Multiple calls to client should return the same instance", client1 === client2)
    }
    
    @Test
    fun `multiple calls to gsonConverter should return the same instance`() {
        // When
        val converter1 = ApiWorker.gsonConverter
        val converter2 = ApiWorker.gsonConverter
        
        // Then
        assertTrue("Multiple calls to gsonConverter should return the same instance", converter1 === converter2)
    }
} 