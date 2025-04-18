package com.paytheory.lib.compose.utility

import android.app.Activity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.googlepay.GooglePayProcessor
import com.paytheory.lib.model.PaymentViewModel

/**
 * Utility class for Google Pay form operations
 * 
 * This class contains extracted logic for Google Pay availability checking and payment initiation,
 * following the testing strategy of isolating business logic from UI components.
 */
object GooglePayFormUtils {
    
    /**
     * Validates if the configuration has Google Pay enabled
     * 
     * @param configuration The PayTheory configuration to check
     * @throws IllegalStateException if Google Pay is not enabled
     */
    fun validateGooglePayConfiguration(configuration: PayTheoryConfiguration) {
        if (!configuration.googlePayEnabled) {
            throw IllegalStateException("Google Pay must be enabled in PayTheoryConfiguration")
        }
    }

    /**
     * Validates the context for Google Pay processing.
     *
     * @param activity The activity being used for Google Pay
     * @param payable The Payable interface implementation
     * @return True if the context is valid, false otherwise
     */
    fun validateGooglePayContext(activity: Activity?, payable: Payable): Boolean {
        if (activity == null) {
            payable.handleError(
                PTError(
                    ErrorCode.GooglePayError,
                    "Context is invalid"
                )
            )
            return false
        }
        return true
    }

    /**
     * Creates a Google Pay processor instance.
     *
     * @param payable The Payable interface implementation
     * @param activity The activity being used for Google Pay
     * @param configuration Pay Theory configuration with Google Pay settings
     * @param viewModel The PaymentViewModel for managing payment data
     * @return A GooglePayProcessor instance
     */
    fun createGooglePayProcessor(
        payable: Payable,
        activity: Activity,
        configuration: PayTheoryConfiguration,
        viewModel: PaymentViewModel
    ): GooglePayProcessor {
        return GooglePayProcessor(payable, activity, configuration, viewModel)
    }

    /**
     * Checks if Google Pay is available on the device.
     *
     * @param googlePayProcessor The GooglePayProcessor instance
     * @param onAvailabilityChecked Callback to handle the result of availability check
     */
    fun checkGooglePayAvailability(
        googlePayProcessor: GooglePayProcessor,
        onAvailabilityChecked: (Boolean) -> Unit
    ) {
        val isAvailableTask: Task<Boolean> = googlePayProcessor.isGooglePayAvailable()

        isAvailableTask.addOnCompleteListener { task ->
            Log.d("GPFormUtils", "Availability check task completed. Success: ${task.isSuccessful}")
            if (task.isSuccessful) {
                val isReady = task.result
                Log.d("GPFormUtils", "Reporting availability to callback: $isReady")
                onAvailabilityChecked(isReady)
            } else {
                Log.e("GPFormUtils", "Availability check task failed.", task.exception)
                Log.d("GPFormUtils", "Reporting availability to callback: false (due to error)")
                onAvailabilityChecked(false)
            }
        }
    }

    /**
     * Initiates the Google Pay payment flow.
     *
     * @param googlePayProcessor The GooglePayProcessor instance
     */
    fun initiateGooglePayPayment(googlePayProcessor: GooglePayProcessor) {
        Log.d("initiateGooglePayPayment", "Calling requestGooglePayment")
        googlePayProcessor.initiateGooglePayPayment()
    }
} 