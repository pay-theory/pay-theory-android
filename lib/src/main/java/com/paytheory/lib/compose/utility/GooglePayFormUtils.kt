package com.paytheory.lib.compose.utility

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
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
     * Validates if the current context can support Google Pay operations
     * 
     * @param activity The current activity context
     * @param payable The Payable implementation for callbacks
     * @return True if the context is valid, false otherwise
     */
    fun validateGooglePayContext(activity: Activity?, payable: Payable): Boolean {
        if (activity == null) {
            payable.handleError(PTError(ErrorCode.GooglePayUnavailable, "Google Pay requires an Activity context"))
            return false
        }
        return true
    }
    
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
     * Creates a Google Pay processor from the given parameters
     * 
     * @param payable The Payable implementation for callbacks
     * @param activity The current activity
     * @param configuration The PayTheory configuration
     * @param viewModel The payment view model
     * @return A configured GooglePayProcessor instance
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
     * Initiates a Google Pay availability check
     * 
     * @param googlePayProcessor The Google Pay processor to use
     * @param onAvailabilityResult Callback for availability result
     */
    fun checkGooglePayAvailability(
        googlePayProcessor: GooglePayProcessor,
        onAvailabilityResult: (Boolean) -> Unit
    ) {
        googlePayProcessor.isGooglePayAvailable().addOnCompleteListener { task ->
            val isAvailable = task.isSuccessful && task.result
            onAvailabilityResult(isAvailable)
        }
    }
    
    /**
     * Initiates a Google Pay payment
     * 
     * @param googlePayProcessor The Google Pay processor to use
     */
    fun initiateGooglePayPayment(googlePayProcessor: GooglePayProcessor) {
        googlePayProcessor.initiateGooglePayPayment()
    }
} 