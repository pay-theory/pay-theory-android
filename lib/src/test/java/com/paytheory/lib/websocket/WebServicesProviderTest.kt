package com.paytheory.lib.websocket

import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WebServicesProviderTest {

    // REAL implementation under test
    private lateinit var webServicesProvider: WebServicesProvider

    // Mock external dependencies
    @MockK
    private lateinit var mockWebSocket: WebSocket
    
    @MockK
    private lateinit var mockOkHttpClient: OkHttpClient
    
    @MockK
    private lateinit var mockBuilder: OkHttpClient.Builder
    
    @MockK
    private lateinit var mockDispatcher: Dispatcher
    
    @MockK
    private lateinit var mockExecutorService: ExecutorService
    
    @OptIn(DelicateCoroutinesApi::class)
    @MockK
    private lateinit var mockWebSocketListener: PTWebSocketListener
    
    @MockK
    private lateinit var mockSocketEventChannel: Channel<SocketUpdate>

    @OptIn(DelicateCoroutinesApi::class)
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Initialize real implementation
        webServicesProvider = WebServicesProvider()
        
        // Set up mock builder chain
        every { mockBuilder.readTimeout(any(), any()) } returns mockBuilder
        every { mockBuilder.connectTimeout(any(), any()) } returns mockBuilder
        every { mockBuilder.hostnameVerifier(any()) } returns mockBuilder
        every { mockBuilder.build() } returns mockOkHttpClient
        
        // Mock OkHttpClient.Builder static method
        mockkConstructor(OkHttpClient.Builder::class)
        every { anyConstructed<OkHttpClient.Builder>().readTimeout(any(), any()) } returns mockBuilder
        every { anyConstructed<OkHttpClient.Builder>().connectTimeout(any(), any()) } returns mockBuilder
        every { anyConstructed<OkHttpClient.Builder>().hostnameVerifier(any()) } returns mockBuilder
        every { anyConstructed<OkHttpClient.Builder>().build() } returns mockOkHttpClient
        
        // Set up dispatcher and executor
        every { mockOkHttpClient.dispatcher } returns mockDispatcher
        every { mockDispatcher.executorService } returns mockExecutorService
        every { mockExecutorService.shutdown() } just runs
        
        // Set up websocket creation
        every { mockOkHttpClient.newWebSocket(any(), any()) } returns mockWebSocket
        
        // Set up websocket listener
        every { mockWebSocketListener.socketEventChannel } returns mockSocketEventChannel
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `startSocket should create websocket with correct URL and return channel`() {
        // Set up test parameters
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test"
        
        // Capture request to verify URL construction
        val requestSlot = slot<Request>()
        every { mockOkHttpClient.newWebSocket(capture(requestSlot), any()) } returns mockWebSocket
        
        // Mock channel for socket updates
        every { mockSocketEventChannel.isClosedForSend } returns false
        
        // Create a real PTWebSocketListener to verify channel is returned
        mockkConstructor(PTWebSocketListener::class)
        val mockChannel = mockk<Channel<SocketUpdate>>()
        every { anyConstructed<PTWebSocketListener>().socketEventChannel } returns mockChannel
        
        // Execute real method
        val resultChannel = webServicesProvider.startSocket(ptToken, partner, stage)
        
        // Verify request URL construction
        verify { mockOkHttpClient.newWebSocket(any(), any()) }
        val capturedUrl = requestSlot.captured.url.toString()
        assertTrue(capturedUrl.contains(ptToken))
        assertTrue(capturedUrl.contains(partner))
        assertTrue(capturedUrl.contains(stage))
        assertEquals(mockChannel, resultChannel)
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `startSocket with listener should create websocket with correct URL`() {
        // Set up test parameters
        val ptToken = "test-token"
        val partner = "test-partner" 
        val stage = "test"
        
        // Capture request to verify URL construction
        val requestSlot = slot<Request>()
        every { mockOkHttpClient.newWebSocket(capture(requestSlot), mockWebSocketListener) } returns mockWebSocket
        
        // Execute real method
        webServicesProvider.startSocket(mockWebSocketListener, ptToken, partner, stage)
        
        // Verify request URL construction and parameters
        verify { mockOkHttpClient.newWebSocket(any(), mockWebSocketListener) }
        val capturedUrl = requestSlot.captured.url.toString()
        assertTrue(capturedUrl.contains(ptToken))
        assertTrue(capturedUrl.contains(partner))
        assertTrue(capturedUrl.contains(stage))
    }
    
    @Test
    fun `startSocket should throw SocketStartException when an error occurs`() {
        // Set up test parameters
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test"
        
        // Mock exception during socket creation
        every { mockOkHttpClient.newWebSocket(any(), any()) } throws Exception("Test exception")
        
        // Execute and verify exception
        var exceptionThrown = false
        try {
            webServicesProvider.startSocket(ptToken, partner, stage)
        } catch (e: WebServicesProvider.SocketStartException) {
            exceptionThrown = true
        }
        
        assertTrue(exceptionThrown)
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `sendMessage should call send on websocket`() {
        // Setup - first create a websocket
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test"
        
        // Mock PTWebSocketListener
        mockkConstructor(PTWebSocketListener::class)
        val mockChannel = mockk<Channel<SocketUpdate>>()
        every { anyConstructed<PTWebSocketListener>().socketEventChannel } returns mockChannel
        
        webServicesProvider.startSocket(ptToken, partner, stage)
        
        // Set up send method behavior
        every { mockWebSocket.send(any<String>()) } returns true
        
        // Execute real method
        val testMessage = "test-message"
        webServicesProvider.sendMessage(testMessage)
        
        // Verify
        verify { mockWebSocket.send(testMessage) }
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `sendMessage should throw SocketMessageException when an error occurs`() {
        // Setup - first create a websocket
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test"
        
        // Mock PTWebSocketListener
        mockkConstructor(PTWebSocketListener::class)
        val mockChannel = mockk<Channel<SocketUpdate>>()
        every { anyConstructed<PTWebSocketListener>().socketEventChannel } returns mockChannel
        
        webServicesProvider.startSocket(ptToken, partner, stage)
        
        // Set up send method to throw exception
        every { mockWebSocket.send(any<String>()) } throws Exception("Test exception")
        
        // Execute and verify exception
        var exceptionThrown = false
        try {
            webServicesProvider.sendMessage("test-message")
        } catch (e: WebServicesProvider.SocketMessageException) {
            exceptionThrown = true
        }
        
        assertTrue(exceptionThrown)
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `stopSocket should close websocket with normal closure status`() {
        // Setup - first create a websocket
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test"
        
        // Mock PTWebSocketListener
        mockkConstructor(PTWebSocketListener::class)
        val mockChannel = mockk<Channel<SocketUpdate>>(relaxed = true)
        every { anyConstructed<PTWebSocketListener>().socketEventChannel } returns mockChannel
        
        webServicesProvider.startSocket(ptToken, partner, stage)
        
        // Set up behavior for close
        every { mockWebSocket.close(any(), any()) } returns true
        every { mockChannel.close() } returns true
        
        // Execute real method
        webServicesProvider.stopSocket()
        
        // Verify
        verify { mockWebSocket.close(1000, null) }
        verify { mockChannel.close() }
    }
    
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun `stopSocket should handle IllegalArgumentException and throw SocketClosureException`() {
        // Setup - first create a websocket
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test"
        
        // Mock PTWebSocketListener
        mockkConstructor(PTWebSocketListener::class)
        val mockChannel = mockk<Channel<SocketUpdate>>(relaxed = true)
        every { anyConstructed<PTWebSocketListener>().socketEventChannel } returns mockChannel
        
        webServicesProvider.startSocket(ptToken, partner, stage)
        
        // Set up close to throw IllegalArgumentException
        every { mockWebSocket.close(any(), any()) } throws IllegalArgumentException("Test exception")
        
        // Execute and verify exception
        var exceptionThrown = false
        try {
            webServicesProvider.stopSocket()
        } catch (e: WebServicesProvider.SocketClosureException) {
            exceptionThrown = true
        }
        
        assertTrue(exceptionThrown)
    }
} 