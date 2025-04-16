package com.paytheory.lib.api

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for the PTTokenResponse data classes and their serialization
 */
class PTTokenResponseTest {
    
    @Test
    fun `PTTokenResponse should store and retrieve values correctly`() {
        // Given
        val ptToken = "test-pt-token"
        val origin = "android-test"
        val challengeOptions = createTestChallengeOptions()
        
        // When
        val response = PTTokenResponse(ptToken, origin, challengeOptions)
        
        // Then
        assertEquals(ptToken, response.ptToken)
        assertEquals(origin, response.origin)
        assertEquals(challengeOptions, response.challengeOptions)
    }
    
    @Test
    fun `ChallengeOptions should store and retrieve values correctly`() {
        // Given - Use helper method to create test data
        val challengeOptions = createTestChallengeOptions()
        
        // Then - Verify all properties
        assertEquals("test-challenge", challengeOptions.challenge)
        assertEquals("Pay Theory", challengeOptions.rp.name)
        assertEquals("test-merchant-id", challengeOptions.user.id)
        assertEquals(1, challengeOptions.pubKeyCredParams.size)
        assertEquals("platform", challengeOptions.authenticatorSelection.authenticatorAttachment)
        assertEquals(60000, challengeOptions.timeout)
        assertEquals("direct", challengeOptions.attestation)
    }
    
    @Test
    fun `Rp should store and retrieve values correctly`() {
        // Given
        val name = "Pay Theory"
        val id = "test.paytheory.com"
        
        // When
        val rp = Rp(name, id)
        
        // Then
        assertEquals(name, rp.name)
        assertEquals(id, rp.amount)
    }
    
    @Test
    fun `User should store and retrieve values correctly`() {
        // Given
        val id = "test-merchant-id"
        val name = "testApp"
        val displayName = "Test Application"
        
        // When
        val user = User(id, name, displayName)
        
        // Then
        assertEquals(id, user.id)
        assertEquals(name, user.name)
        assertEquals(displayName, user.displayName)
    }
    
    @Test
    fun `PubKeyCredParam should store and retrieve values correctly`() {
        // Given
        val alg = -7
        val type = "public-key"
        
        // When
        val pubKeyCredParam = PubKeyCredParam(alg, type)
        
        // Then
        assertEquals(alg, pubKeyCredParam.alg)
        assertEquals(type, pubKeyCredParam.type)
    }
    
    @Test
    fun `AuthenticatorSelection should store and retrieve values correctly`() {
        // Given
        val authenticatorAttachment = "platform"
        val userVerification = "required"
        
        // When
        val authenticatorSelection = AuthenticatorSelection(
            authenticatorAttachment,
            userVerification
        )
        
        // Then
        assertEquals(authenticatorAttachment, authenticatorSelection.authenticatorAttachment)
        assertEquals(userVerification, authenticatorSelection.userVerification)
    }
    
    /**
     * Helper method to create test challenge options
     */
    private fun createTestChallengeOptions(): ChallengeOptions {
        val rp = Rp("Pay Theory", "test.paytheory.com")
        val user = User("test-merchant-id", "testApp", "Test Application")
        val pubKeyCredParams = ArrayList<PubKeyCredParam>().apply {
            add(PubKeyCredParam(-7, "public-key"))
        }
        val authenticatorSelection = AuthenticatorSelection("platform", "required")
        
        return ChallengeOptions(
            challenge = "test-challenge",
            rp = rp,
            user = user,
            pubKeyCredParams = pubKeyCredParams,
            authenticatorSelection = authenticatorSelection,
            timeout = 60000,
            attestation = "direct"
        )
    }
} 