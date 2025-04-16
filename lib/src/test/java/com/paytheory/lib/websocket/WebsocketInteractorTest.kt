package com.paytheory.lib.websocket

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WebsocketInteractorTest {

    // REAL implementation under test
    private lateinit var websocketInteractor: WebsocketInteractor

    // Mock dependencies
    @MockK
    private lateinit var mockRepository: WebsocketRepository

    @MockK
    private lateinit var mockSocketEventChannel: Channel<SocketUpdate>

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Initialize real implementation with mock dependencies
        websocketInteractor = WebsocketInteractor(mockRepository)
    }

    @Test
    fun `startSocket should call repository startSocket with correct parameters`() {
        // Setup
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test-stage"
        
        // Mock response
        every { 
            mockRepository.startSocket(ptToken, partner, stage) 
        } returns mockSocketEventChannel
        
        // Execute real method
        val result = websocketInteractor.startSocket(ptToken, partner, stage)
        
        // Verify
        verify { mockRepository.startSocket(ptToken, partner, stage) }
        assertEquals(mockSocketEventChannel, result)
    }

    @Test
    fun `sendMessage should call repository sendMessage with correct parameters`() {
        // Setup
        val message = "test-message"
        every { mockRepository.sendMessage(message) } returns Unit
        
        // Execute real method
        websocketInteractor.sendMessage(message)
        
        // Verify
        verify { mockRepository.sendMessage(message) }
    }

    @Test
    fun `stopSocket should call repository closeSocket`() {
        // Setup
        every { mockRepository.closeSocket() } returns Unit
        
        // Execute real method
        websocketInteractor.stopSocket()
        
        // Verify
        verify { mockRepository.closeSocket() }
    }
} 