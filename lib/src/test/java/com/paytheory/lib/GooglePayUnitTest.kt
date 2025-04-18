package com.paytheory.lib

import com.paytheory.lib.googlepay.GooglePayClient
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Basic unit test for Google Pay as a simple proof of concept
 */
@RunWith(MockitoJUnitRunner::class)
class GooglePayUnitTest {

    @Test
    fun testGooglePayClientInitialization() {
        // Simple test to verify GooglePayClient can be instantiated
        val client = GooglePayClient()
        assertNotNull("GooglePayClient should be initialized", client)
    }
} 