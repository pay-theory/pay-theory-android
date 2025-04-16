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
class WebsocketRepositoryTest {

    // REAL implementation under test
    private lateinit var websocketRepository: WebsocketRepository

    // Mock dependencies
    @MockK
    private lateinit var mockWebServicesProvider: WebServicesProvider

    @MockK
    private lateinit var mockSocketEventChannel: Channel<SocketUpdate>

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        // Initialize real implementation with mock dependencies
        websocketRepository = WebsocketRepository(mockWebServicesProvider)
    }

    @Test
    fun `startSocket should call WebServicesProvider startSocket with correct parameters`() {
        // Setup
        val ptToken = "test-token"
        val partner = "test-partner"
        val stage = "test-stage"
        
        // Mock response
        every { 
            mockWebServicesProvider.startSocket(ptToken, partner, stage) 
        } returns mockSocketEventChannel
        
        // Execute real method
        val result = websocketRepository.startSocket(ptToken, partner, stage)
        
        // Verify
        verify { mockWebServicesProvider.startSocket(ptToken, partner, stage) }
        assertEquals(mockSocketEventChannel, result)
    }

    @Test
    fun `sendMessage should call WebServicesProvider sendMessage with correct parameters`() {
        // Setup
        val message = "test-message"
        every { mockWebServicesProvider.sendMessage(message) } returns Unit
        
        // Execute real method
        websocketRepository.sendMessage(message)
        
        // Verify
        verify { mockWebServicesProvider.sendMessage(message) }
    }

    @Test
    fun `closeSocket should call WebServicesProvider stopSocket`() {
        // Setup
        every { mockWebServicesProvider.stopSocket() } returns Unit
        
        // Execute real method
        websocketRepository.closeSocket()
        
        // Verify
        verify { mockWebServicesProvider.stopSocket() }
    }
} 