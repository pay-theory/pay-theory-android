package com.paytheory.lib.reactors

import com.google.gson.Gson
import com.paytheory.lib.data.ActionRequest
import com.paytheory.lib.data.HostTokenRequest
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Base64

@ExperimentalCoroutinesApi
class ConnectionReactorsTest {

    private lateinit var connectionReactors: ConnectionReactors
    private lateinit var paymentViewModel: PaymentViewModel
    private val ptToken = "testPtToken"
    private val attestation = "testAttestation"
    private val applicationPackageName = "com.paytheory.test"
    private val testOrigin = "androidTest"

    @Before
    fun setup() {
        paymentViewModel = mockk(relaxed = true)
        connectionReactors = ConnectionReactors(
            ptToken,
            attestation,
            paymentViewModel,
            applicationPackageName,
            testOrigin
        )
    }

    @Test
    fun `onConnected sends correct host token action request`() {
        // Act
        connectionReactors.onConnected()
        Thread.sleep(500)

        // Assert
        verify {
            paymentViewModel.sendSocketMessage(match { message ->
                val actionRequest = Gson().fromJson(message, ActionRequest::class.java)
                assertEquals("host:hostToken", actionRequest.action)

                val decoded = String(Base64.getDecoder().decode(actionRequest.encoded))
                val hostTokenRequest = Gson().fromJson(decoded, HostTokenRequest::class.java)

                assertEquals(ptToken, hostTokenRequest.ptToken)
                assertEquals(attestation, hostTokenRequest.attestation)
                assertEquals(applicationPackageName, hostTokenRequest.applicationPackageName)
                assertEquals(testOrigin, hostTokenRequest.origin)

                // Check that the timing is not null and is a positive number.
                assertTrue(hostTokenRequest.timing > 0)
                true // Return true if all conditions pass, otherwise it will fail
            })
        }
    }

    @Test
    fun `onDisconnected calls disconnect on viewModel`() {
        // Act
        connectionReactors.onDisconnected()

        // Assert
        verify { paymentViewModel.disconnect() }
    }

    @Test
    fun `onConnected handles exceptions`() {
        // Arrange
        val exception = RuntimeException("Test Exception")
        every { paymentViewModel.sendSocketMessage(any()) } throws exception

        // Act
        connectionReactors.onConnected()
        Thread.sleep(500)
        // Assert: verify that the code does not crash.
        verify {
            paymentViewModel.sendSocketMessage(any())
        }
    }
}