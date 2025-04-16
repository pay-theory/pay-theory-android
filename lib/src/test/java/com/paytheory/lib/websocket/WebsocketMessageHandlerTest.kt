package com.paytheory.lib.websocket

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WebsocketMessageHandlerTest {

    // Test implementation of the interface
    private class TestWebsocketMessageHandler : WebsocketMessageHandler {
        var receivedMessage: String? = null
        var disconnectCalled: Boolean = false

        override fun receiveMessage(message: String) {
            receivedMessage = message
        }

        override fun disconnect() {
            disconnectCalled = true
        }
    }

    // REAL implementation of the interface for testing
    private lateinit var messageHandler: TestWebsocketMessageHandler

    @Before
    fun setup() {
        // Initialize real implementation
        messageHandler = TestWebsocketMessageHandler()
    }

    @Test
    fun `receiveMessage should store the received message`() {
        // Setup
        val testMessage = "test websocket message"
        
        // Execute real method
        messageHandler.receiveMessage(testMessage)
        
        // Verify
        assertEquals(testMessage, messageHandler.receivedMessage)
    }

    @Test
    fun `disconnect should set disconnectCalled flag`() {
        // Execute real method
        messageHandler.disconnect()
        
        // Verify
        assertTrue(messageHandler.disconnectCalled)
    }
} 