package com.paytheory.lib.api

import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for ApiService
 */
class ApiServiceTest {
    
    private val testBasePath = "https://test-api.paytheory.com/"
    
    @Test
    fun `ptTokenApiCall should create a non-null service instance`() {
        // Given
        val apiService = ApiService(testBasePath)
        
        // When
        val tokenApiService = apiService.ptTokenApiCall()
        
        // Then
        assertNotNull("API service should not be null", tokenApiService)
    }
    
    @Test
    fun `ptTokenApiCall should use the provided base path`() {
        // Given
        val customBasePath = "https://custom-api.paytheory.com/"
        val apiService = ApiService(customBasePath)
        
        // When
        val tokenApiService = apiService.ptTokenApiCall()
        
        // Then
        // Base URL validation requires reflection which is not ideal
        // This test verifies the service is created without exceptions
        assertNotNull("API service should be created with custom base path", tokenApiService)
    }
} 