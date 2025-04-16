package com.paytheory.lib.googlepay

import android.app.Activity
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import com.paytheory.lib.model.PaymentViewModel

/**
 * Factory class for creating Google Pay components
 * This facilitates dependency injection and makes testing easier
 */
object GooglePayFactory {
    
    // Default instances of components
    private var defaultClient: GooglePayClientInterface = GooglePayClient()
    private var defaultUtil: GooglePayUtilInterface = GooglePayUtil.getInstance()
    
    /**
     * Create a new GooglePayProcessor with the specified dependencies
     * 
     * @param configuration The Pay Theory configuration
     * @param activity The activity hosting the payment flow
     * @param payable The payable interface for callbacks
     * @param viewModel The payment view model for state management
     * @param googlePayUtil Optional custom GooglePayUtil implementation
     * @return A new GooglePayProcessor instance
     */
    fun createProcessor(
        configuration: PayTheoryConfiguration,
        activity: Activity,
        payable: Payable,
        viewModel: PaymentViewModel,
        googlePayUtil: GooglePayUtilInterface = defaultUtil
    ): GooglePayProcessorInterface {
        return GooglePayProcessor(
            configuration = configuration,
            activity = activity,
            payable = payable,
            viewModel = viewModel,
            googlePayUtil = googlePayUtil
        )
    }
    
    /**
     * Get the GooglePayUtil instance
     * 
     * @param client Optional custom GooglePayClient implementation
     * @return GooglePayUtil instance
     */
    fun getGooglePayUtil(client: GooglePayClientInterface = defaultClient): GooglePayUtilInterface {
        return GooglePayUtil.getInstance(client)
    }
    
    /**
     * Get a new GooglePayClient instance
     * 
     * @return A new GooglePayClient instance
     */
    fun getGooglePayClient(): GooglePayClientInterface {
        return GooglePayClient()
    }
    
    /**
     * Set custom default implementations for testing
     * This should only be used in test environments
     * 
     * @param client Custom GooglePayClient implementation
     * @param util Custom GooglePayUtil implementation
     */
    @JvmStatic
    fun setTestImplementations(
        client: GooglePayClientInterface,
        util: GooglePayUtilInterface
    ) {
        defaultClient = client
        defaultUtil = util
    }
    
    /**
     * Reset to default implementations
     * This should be called after tests to ensure clean state
     */
    @JvmStatic
    fun resetToDefaultImplementations() {
        defaultClient = GooglePayClient()
        defaultUtil = GooglePayUtil.getInstance()
        GooglePayUtil.resetInstance()
    }
} 