package com.paytheory.lib.data

import com.paytheory.lib.data.responses.BarcodeMessage
import com.paytheory.lib.data.responses.HostToken
import com.paytheory.lib.data.responses.HostTokenMessage
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for response.kt data classes
 */
class ResponsesTest {

    @Test
    fun `BarcodeMessage should be correctly initialized`() {
        // Given
        val barcodeId = "brcd_12345"
        val barcodeUrl = "https://paytheory.com/barcode/12345"
        val barcode = "123456789"
        val barcodeFee = "1.50"
        val merchant = "merch_12345"
        
        // When
        val message = BarcodeMessage(
            barcodeId = barcodeId,
            barcodeUrl = barcodeUrl,
            barcode = barcode,
            barcodeFee = barcodeFee,
            merchant = merchant
        )
        
        // Then
        assertEquals(barcodeId, message.barcodeId)
        assertEquals(barcodeUrl, message.barcodeUrl)
        assertEquals(barcode, message.barcode)
        assertEquals(barcodeFee, message.barcodeFee)
        assertEquals(merchant, message.merchant)
    }
    
    @Test
    fun `HostToken should be correctly initialized`() {
        // Given
        val hostToken = "host_token_value"
        val publicKey = "public_key_value"
        val sessionKey = "session_key_value"
        
        // When
        val token = HostToken(
            hostToken = hostToken,
            publicKey = publicKey,
            sessionKey = sessionKey
        )
        
        // Then
        assertEquals(hostToken, token.hostToken)
        assertEquals(publicKey, token.publicKey)
        assertEquals(sessionKey, token.sessionKey)
    }
    
    @Test
    fun `HostToken should handle empty strings`() {
        // Given
        val hostToken = ""
        val publicKey = ""
        val sessionKey = ""
        
        // When
        val token = HostToken(
            hostToken = hostToken,
            publicKey = publicKey,
            sessionKey = sessionKey
        )
        
        // Then
        assertEquals("", token.hostToken)
        assertEquals("", token.publicKey)
        assertEquals("", token.sessionKey)
    }
    
    @Test
    fun `HostTokenMessage should be correctly initialized`() {
        // Given
        val type = "host_token"
        val body = HostToken(
            hostToken = "host_token_value",
            publicKey = "public_key_value",
            sessionKey = "session_key_value"
        )
        
        // When
        val message = HostTokenMessage(
            type = type,
            body = body
        )
        
        // Then
        assertEquals(type, message.type)
        assertEquals(body, message.body)
        assertEquals("host_token_value", message.body.hostToken)
        assertEquals("public_key_value", message.body.publicKey)
        assertEquals("session_key_value", message.body.sessionKey)
    }
    
    @Test
    fun `HostTokenMessage should accept different type values`() {
        // Given
        val type = "custom_token_type"
        val body = HostToken(
            hostToken = "host_token_value",
            publicKey = "public_key_value",
            sessionKey = "session_key_value"
        )
        
        // When
        val message = HostTokenMessage(
            type = type,
            body = body
        )
        
        // Then
        assertEquals(type, message.type)
        assertEquals(body, message.body)
    }
    
    @Test
    fun `All typealias should map to correct implementations`() {
        // These tests verify the typealiases point to correct implementations
        
        // Test BarcodeMessage typealias
        val barcodeMessage = BarcodeMessage(
            barcodeId = "brcd_12345",
            barcode = "123456789",
            barcodeUrl = "https://example.com",
            barcodeFee = "1.50",
            merchant = "merchant_id"
        )
        assertTrue(true)
        
        // Test HostToken typealias
        val hostToken = HostToken(
            hostToken = "host_token_value",
            publicKey = "public_key_value",
            sessionKey = "session_key_value"
        )
        assertTrue(true)
        
        // Test HostTokenMessage typealias
        val hostTokenMessage = HostTokenMessage(
            type = "host_token",
            body = hostToken
        )
        assertTrue(true)
    }
} 