package com.paytheory.android.test.helpers

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Utility functions for testing LiveData
 */
object LiveDataTestUtils {
    
    /**
     * Gets the value of a LiveData or waits for it to have a value
     * 
     * @param T the type of the LiveData
     * @param time timeout duration
     * @param timeUnit timeout unit
     * @return the value of the LiveData
     * @throws TimeoutException if the value is not set within the timeout
     */
    fun <T> LiveData<T>.getValueOrAwait(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getValueOrAwait.removeObserver(this)
            }
        }
        
        this.observeForever(observer)
        
        // Don't wait indefinitely if the LiveData is not set
        if (!latch.await(time, timeUnit)) {
            this.removeObserver(observer)
            throw TimeoutException("LiveData value was never set.")
        }
        
        @Suppress("UNCHECKED_CAST")
        return data as T
    }
    
    /**
     * Observes a LiveData until it changes a specific number of times
     * 
     * @param T the type of the LiveData
     * @param count the number of changes to observe
     * @param timeout timeout duration
     * @param timeUnit timeout unit
     * @return list of all observed values
     * @throws TimeoutException if the count is not reached within the timeout
     */
    fun <T> LiveData<T>.observeForValues(
        count: Int,
        timeout: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): List<T> {
        val data = mutableListOf<T>()
        val latch = CountDownLatch(count)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data.add(value)
                latch.countDown()
                if (latch.count == 0L) {
                    this@observeForValues.removeObserver(this)
                }
            }
        }
        
        this.observeForever(observer)
        
        if (!latch.await(timeout, timeUnit)) {
            this.removeObserver(observer)
            throw TimeoutException("LiveData did not receive $count values in time")
        }
        
        return data
    }
    
    /**
     * Creates a test observer for a LiveData
     * 
     * @param T the type of the LiveData
     * @return a TestObserver that records all values
     */
    fun <T> LiveData<T>.test(): TestObserver<T> {
        val observer = TestObserver<T>()
        this.observeForever(observer)
        return observer
    }
    
    /**
     * Test observer that records all values for testing
     */
    class TestObserver<T> : Observer<T> {
        private val _values = mutableListOf<T>()
        val values: List<T> get() = _values
        
        override fun onChanged(value: T) {
            _values.add(value)
        }
        
        fun clear() {
            _values.clear()
        }
    }
} 