package com.paytheory.lib.googlepay

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks

/**
 * Utility class for testing Task-based APIs
 */
object TestTaskUtils {
    
    /**
     * Creates a successful Task with the given result
     *
     * @param result The result value
     * @return A completed Task with the result
     */
    fun <T> taskWithResult(result: T): Task<T> {
        val source = TaskCompletionSource<T>()
        source.setResult(result)
        return source.task
    }
    
    /**
     * Creates a failed Task with the given exception
     *
     * @param exception The exception
     * @return A failed Task with the exception
     */
    fun <T> taskWithException(exception: Exception): Task<T> {
        val source = TaskCompletionSource<T>()
        source.setException(exception)
        return source.task
    }
    
    /**
     * Creates a failed Task with an ApiException for the given status code
     *
     * @param statusCode The status code for the ApiException
     * @return A failed Task with an ApiException
     */
    fun <T> taskWithApiException(statusCode: Int): Task<T> {
        val status = Status(statusCode)
        val exception = ApiException(status)
        return taskWithException(exception)
    }
    
    /**
     * Creates a Task for cancelled Google Pay payment (status code 10)
     *
     * @return A failed Task with the cancelled exception
     */
    fun <T> cancelledGooglePayTask(): Task<T> {
        return taskWithApiException(10)
    }
    
    /**
     * Creates a Task for general Google Pay error (status code other than 10)
     *
     * @param statusCode The status code to use (default: 15)
     * @return A failed Task with the error exception
     */
    fun <T> errorGooglePayTask(statusCode: Int = 15): Task<T> {
        return taskWithApiException(statusCode)
    }
    
    /**
     * Creates a Task that will complete after a delay
     *
     * @param result The result value
     * @param delayMs The delay in milliseconds
     * @return A Task that will complete after the delay
     */
    fun <T> delayedTask(result: T, delayMs: Long): Task<T> {
        return Tasks.forResult(result)
    }
} 