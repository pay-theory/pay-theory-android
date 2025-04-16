package com.paytheory.android.test.helpers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Utility functions for testing Kotlin Flows
 */
object FlowTestUtils {
    
    /**
     * Collects all values from a flow within a timeout period
     * 
     * @param T the type of values in the flow
     * @param timeout maximum duration to collect values
     * @return list of all collected values
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> Flow<T>.collectValues(
        timeout: Duration = 5.seconds
    ): List<T> = withTimeout(timeout) {
        toList()
    }
    
    /**
     * Collects a single value from a flow
     * 
     * @param T the type of value in the flow
     * @param timeout maximum duration to wait for a value
     * @return the first emitted value
     * @throws TimeoutCancellationException if no value is emitted within the timeout
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> Flow<T>.collectSingleValue(
        timeout: Duration = 5.seconds
    ): T = withTimeout(timeout) {
        this@collectSingleValue.toList().first()
    }
    
    /**
     * Executes a test with flow collection in a test scope
     * 
     * @param block the test code to execute
     * @return the result of the test
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun runFlowTest(block: suspend TestScope.() -> Unit) = runTest(UnconfinedTestDispatcher()) {
        block()
    }
    
    /**
     * Extension function to test a flow with a specific timeout
     * 
     * @param timeout the timeout duration
     * @param assertion the assertion to perform on collected values
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> Flow<T>.test(
        timeout: Duration = 5.seconds,
        assertion: suspend (List<T>) -> Unit
    ) {
        val values = withTimeout(timeout) {
            toList()
        }
        assertion(values)
    }
} 