package com.paytheory.lib.websocket

import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Response
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PTWebSocketListenerTest {

    // REAL implementation under test
    private lateinit var ptWebSocketListener: PTWebSocketListener

    // Mock external dependencies
    @MockK
    private lateinit var mockWebSocket: WebSocket
    
    @MockK
    private lateinit var mockResponse: Response

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Initialize real implementation
        ptWebSocketListener = PTWebSocketListener()
        
        // Mock WebSocket methods
        every { mockWebSocket.close(any(), any()) } returns true
    }

    @Test
    fun `onOpen should send connected message to channel`() = runTest {
        // Execute real method - no need to mock GlobalScope as we're testing the channel directly
        ptWebSocketListener.onOpen(mockWebSocket, mockResponse)
        
        // Collect the update from the channel
        val update = ptWebSocketListener.socketEventChannel.receive()
        
        // Verify
        assertEquals("connected to socket", update.text)
        assertFalse(ptWebSocketListener.socketEventChannel.isClosedForSend)
    }

    @Test
    fun `onMessage should send text message to channel`() = runTest {
        // Setup
        val messageText = "test-message"
        
        // Execute real method
        ptWebSocketListener.onMessage(mockWebSocket, messageText)
        
        // Collect the update from the channel
        val update = ptWebSocketListener.socketEventChannel.receive()
        
        // Verify
        assertEquals(messageText, update.text)
    }

    @Test
    fun `onClosing should send SocketAbortedException and close the channel`() = runTest {
        // Execute real method - no need to mock GlobalScope as we're testing the channel directly
        ptWebSocketListener.onClosing(mockWebSocket, 1000, "Normal closure")
        
        // Collect the exception from the channel
        val update = ptWebSocketListener.socketEventChannel.receive()
        
        // Verify
        assertTrue(update.exception is SocketAbortedException)
        verify { mockWebSocket.close(ptWebSocketListener.normalClosureStatus, null) }
    }

    @Test
    fun `onFailure should send exception to channel`() = runTest {
        // Setup
        val testException = IOException("Test exception")
        
        // Execute real method
        ptWebSocketListener.onFailure(mockWebSocket, testException, mockResponse)
        
        // Collect the update from the channel
        val update = ptWebSocketListener.socketEventChannel.receive()
        
        // Verify
        assertEquals(testException, update.exception)
    }
    
    @Test
    fun `SocketUpdate data class initialization should work correctly`() {
        // Setup
        val text = "test-text"
        val exception = Exception("test-exception")
        
        // Execute
        val update1 = SocketUpdate(text = text)
        val update2 = SocketUpdate(exception = exception)
        val update3 = SocketUpdate(text = text, exception = exception)
        
        // Verify
        assertEquals(text, update1.text)
        assertEquals(null, update1.exception)
        
        assertEquals(null, update2.text)
        assertEquals(exception, update2.exception)
        
        assertEquals(text, update3.text)
        assertEquals(exception, update3.exception)
    }
    
    @Test
    fun `SocketAbortedException should be created correctly`() {
        // Execute
        val exception = SocketAbortedException()
        
        // Verify
        assertNotNull(exception)
        assertEquals(null, exception.message)
    }
} 