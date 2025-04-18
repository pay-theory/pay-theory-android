package com.paytheory.lib.compose.utility

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.googlepay.GooglePayProcessor
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GooglePayFormUtilsTest {
    
    private lateinit var mockPayable: Payable
    private lateinit var mockActivity: Activity
    private lateinit var mockConfiguration: PayTheoryConfiguration
    private lateinit var mockViewModel: PaymentViewModel
    private lateinit var mockGooglePayProcessor: GooglePayProcessor
    private lateinit var mockTask: Task<Boolean>
    
    @Before
    fun setup() {
        mockPayable = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)
        mockConfiguration = mockk(relaxed = true)
        mockViewModel = mockk(relaxed = true)
        mockGooglePayProcessor = mockk(relaxed = true)
        mockTask = mockk(relaxed = true)
        
        // Default configuration
        every { mockConfiguration.googlePayEnabled } returns true
        
        // Mock constructor for GooglePayProcessor
        mockkConstructor(GooglePayProcessor::class)
        every { anyConstructed<GooglePayProcessor>().isGooglePayAvailable() } returns mockTask
        every { anyConstructed<GooglePayProcessor>().initiateGooglePayPayment() } returns Unit
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `validateGooglePayContext returns false when activity is null`() {
        // Given a null activity
        val nullActivity: Activity? = null
        
        // When validating Google Pay context
        val result = GooglePayFormUtils.validateGooglePayContext(nullActivity, mockPayable)
        
        // Then result should be false and error should be reported
        assertFalse(result)
        verify { 
            mockPayable.handleError(match { 
                it.code == ErrorCode.GooglePayError &&
                it.error == "Context is invalid"
            })
        }
    }
    
    @Test
    fun `validateGooglePayContext returns true when activity is valid`() {
        // Given a valid activity
        val activity: Activity? = mockActivity
        
        // When validating Google Pay context
        val result = GooglePayFormUtils.validateGooglePayContext(activity, mockPayable)
        
        // Then result should be true and no error should be reported
        assertTrue(result)
        verify(exactly = 0) { mockPayable.handleError(any()) }
    }
    
    @Test(expected = IllegalStateException::class)
    fun `validateGooglePayConfiguration throws exception when Google Pay disabled`() {
        // Given a configuration with Google Pay disabled
        every { mockConfiguration.googlePayEnabled } returns false
        
        // When validating Google Pay configuration
        // Then it should throw IllegalStateException
        GooglePayFormUtils.validateGooglePayConfiguration(mockConfiguration)
    }
    
    @Test
    fun `validateGooglePayConfiguration completes successfully when Google Pay enabled`() {
        // Given a configuration with Google Pay enabled
        every { mockConfiguration.googlePayEnabled } returns true
        
        // When validating Google Pay configuration
        // Then it should complete successfully (no exception thrown)
        GooglePayFormUtils.validateGooglePayConfiguration(mockConfiguration)
    }
    
    @Test
    fun `createGooglePayProcessor returns processor instance`() {
        // When creating a Google Pay processor
        val result = GooglePayFormUtils.createGooglePayProcessor(
            mockPayable, mockActivity, mockConfiguration, mockViewModel
        )
        
        // Then it should return a GooglePayProcessor instance
        assertTrue(result is GooglePayProcessor)
    }
    
    @Test
    fun `checkGooglePayAvailability calls callback with true when available`() {
        // Given a processor that reports Google Pay is available
        every { mockGooglePayProcessor.isGooglePayAvailable() } returns mockTask
        
        // Capture the OnCompleteListener to simulate task completion
        val listenerSlot = slot<OnCompleteListener<Boolean>>()
        every { mockTask.addOnCompleteListener(capture(listenerSlot)) } answers {
            // Simulate successful task completion with result = true
            every { mockTask.isSuccessful } returns true
            every { mockTask.result } returns true
            
            // Trigger the listener
            listenerSlot.captured.onComplete(mockTask)
            mockTask
        }
        
        // Callback verification
        var callbackResult = false
        val callback: (Boolean) -> Unit = { available ->
            callbackResult = available
        }
        
        // When checking Google Pay availability
        GooglePayFormUtils.checkGooglePayAvailability(mockGooglePayProcessor, callback)
        
        // Then callback should be called with true
        assertTrue(callbackResult)
    }
    
    @Test
    fun `checkGooglePayAvailability calls callback with false when unavailable`() {
        // Given a processor that reports Google Pay is unavailable
        every { mockGooglePayProcessor.isGooglePayAvailable() } returns mockTask
        
        // Capture the OnCompleteListener to simulate task completion
        val listenerSlot = slot<OnCompleteListener<Boolean>>()
        every { mockTask.addOnCompleteListener(capture(listenerSlot)) } answers {
            // Simulate successful task completion with result = false
            every { mockTask.isSuccessful } returns true
            every { mockTask.result } returns false
            
            // Trigger the listener
            listenerSlot.captured.onComplete(mockTask)
            mockTask
        }
        
        // Callback verification
        var callbackResult = true
        val callback: (Boolean) -> Unit = { available ->
            callbackResult = available
        }
        
        // When checking Google Pay availability
        GooglePayFormUtils.checkGooglePayAvailability(mockGooglePayProcessor, callback)
        
        // Then callback should be called with false
        assertFalse(callbackResult)
    }
    
    @Test
    fun `checkGooglePayAvailability calls callback with false when task fails`() {
        // Given a processor where the task fails
        every { mockGooglePayProcessor.isGooglePayAvailable() } returns mockTask
        
        // Capture the OnCompleteListener to simulate task completion
        val listenerSlot = slot<OnCompleteListener<Boolean>>()
        every { mockTask.addOnCompleteListener(capture(listenerSlot)) } answers {
            // Simulate failed task completion
            every { mockTask.isSuccessful } returns false
            every { mockTask.result } returns true  // Should be ignored as task failed
            
            // Trigger the listener
            listenerSlot.captured.onComplete(mockTask)
            mockTask
        }
        
        // Callback verification
        var callbackResult = true
        val callback: (Boolean) -> Unit = { available ->
            callbackResult = available
        }
        
        // When checking Google Pay availability
        GooglePayFormUtils.checkGooglePayAvailability(mockGooglePayProcessor, callback)
        
        // Then callback should be called with false
        assertFalse(callbackResult)
    }
    
    @Test
    fun `initiateGooglePayPayment delegates to processor`() {
        // When initiating Google Pay payment
        GooglePayFormUtils.initiateGooglePayPayment(mockGooglePayProcessor)
        
        // Then processor's method should be called
        verify { mockGooglePayProcessor.initiateGooglePayPayment() }
    }
} 