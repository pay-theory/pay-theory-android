package com.paytheory.android.test.mocks

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityToken
import com.google.android.play.core.integrity.StandardIntegrityTokenRequest

/**
 * Fake implementation of Google Play Integrity API for testing
 */
class FakeIntegrityTokenProvider {
    
    // Control behavior of the fake provider
    var shouldSucceed = true
    var shouldDelay = false
    var delayMs = 200L
    
    // Fixed test token value (can be modified for specific test cases)
    var testTokenValue = "test-integrity-token-value"
    
    // Error to return when shouldSucceed is false
    var errorMessage = "Test integrity check failed"
    
    /**
     * Simulates requesting an integrity token
     */
    fun request(request: StandardIntegrityTokenRequest): Task<StandardIntegrityToken> {
        val taskSource = TaskCompletionSource<StandardIntegrityToken>()
        
        Thread {
            if (shouldDelay) {
                Thread.sleep(delayMs)
            }
            
            if (shouldSucceed) {
                taskSource.setResult(FakeStandardIntegrityToken(testTokenValue))
            } else {
                taskSource.setException(Exception(errorMessage))
            }
        }.start()
        
        return taskSource.task
    }
    
    /**
     * Set this provider to succeed with the given token value
     */
    fun setSuccessResponse(tokenValue: String = "test-integrity-token-value") {
        shouldSucceed = true
        testTokenValue = tokenValue
    }
    
    /**
     * Set this provider to fail with the given error message
     */
    fun setFailureResponse(error: String = "Test integrity check failed") {
        shouldSucceed = false
        errorMessage = error
    }
    
    /**
     * Fake implementation of StandardIntegrityToken
     */
    inner class FakeStandardIntegrityToken(private val tokenValue: String) : StandardIntegrityToken {
        override fun token(): String = tokenValue
    }
} 