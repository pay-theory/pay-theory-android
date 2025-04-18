package com.paytheory.lib.googlepay

import android.app.Activity
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import com.paytheory.lib.model.PaymentViewModel

/**
 * A factory object responsible for creating and providing instances of core Google Pay integration components.
 *
 * This factory centralizes the instantiation logic for classes like [GooglePayProcessorInterface],
 * [GooglePayClientInterface], and [GooglePayUtilInterface]. It helps manage dependencies and facilitates
 * testing by allowing the injection of mock or custom implementations via [setTestImplementations].
 *
 * Use this factory to obtain the necessary objects for integrating Google Pay functionality
 * into the Pay Theory SDK flow.
 */
object GooglePayFactory {
    
    // Default instances are used if no custom implementations are provided or set for testing.
    private var defaultClient: GooglePayClientInterface = GooglePayClient()
    private var defaultUtil: GooglePayUtilInterface = GooglePayUtil.getInstance()
    
    /**
     * Creates a new instance of a [GooglePayProcessorInterface].
     *
     * The processor is responsible for orchestrating the Google Pay payment flow, including
     * checking readiness, requesting payment data, and handling the results.
     *
     * @param configuration The active [PayTheoryConfiguration] containing API keys and Google Pay settings.
     * @param activity The host [Activity] required for launching the Google Pay payment sheet.
     * @param payable The [Payable] interface implementation for receiving callbacks ([handleSuccess], [handleFailure], [handleError]).
     * @param viewModel The [PaymentViewModel] used for managing shared payment state and data.
     * @param googlePayUtil (Optional) A specific instance of [GooglePayUtilInterface] to use. Defaults to the factory's default or test instance.
     * @return A fully configured instance implementing [GooglePayProcessorInterface].
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
            viewModel = viewModel
        )
    }
    
    /**
     * Retrieves an instance of [GooglePayUtilInterface].
     *
     * The utility class provides helper functions for interacting with the Google Pay API,
     * such as creating payment data requests and checking device readiness.
     * This method allows injecting a custom [GooglePayClientInterface] into the util instance.
     *
     * @param client (Optional) A specific instance of [GooglePayClientInterface] to be used by the util. Defaults to the factory's default or test client.
     * @return An instance implementing [GooglePayUtilInterface]. Note that `GooglePayUtil.getInstance` likely manages a singleton instance unless reset.
     */
    fun getGooglePayUtil(client: GooglePayClientInterface = defaultClient): GooglePayUtilInterface {
        return GooglePayUtil.getInstance(client)
    }
    
    /**
     * Creates a new instance of [GooglePayClientInterface].
     *
     * The client class is a wrapper around Google's `PaymentsClient`, providing methods
     * to interact directly with the Google Pay API (e.g., `isReadyToPay`, `loadPaymentData`).
     *
     * @return A new instance of the default [GooglePayClient].
     */
    fun getGooglePayClient(): GooglePayClientInterface {
        return GooglePayClient()
    }
    
    /**
     * **For Testing Only:** Sets custom default implementations for the Google Pay client and utility.
     *
     * This allows injecting mock or fake implementations during unit or integration tests
     * to isolate components and control dependencies.
     * Remember to call [resetToDefaultImplementations] after tests that use this method.
     *
     * @param client A custom implementation (e.g., a mock) of [GooglePayClientInterface].
     * @param util A custom implementation (e.g., a mock) of [GooglePayUtilInterface].
     */
    @JvmStatic
    fun setTestImplementations(
        client: GooglePayClientInterface,
        util: GooglePayUtilInterface
    ) {
        defaultClient = client
        defaultUtil = util
        GooglePayUtil.getInstance(client)
    }
    
    /**
     * **For Testing Only:** Resets the factory's default implementations to the production versions.
     *
     * This should be called in the teardown phase (`@After`) of tests that have used
     * [setTestImplementations] to ensure subsequent tests start with a clean state.
     * It also resets any singleton instance managed by [GooglePayUtil].
     */
    @JvmStatic
    fun resetToDefaultImplementations() {
        defaultClient = GooglePayClient()
        defaultUtil = GooglePayUtil.getInstance()
        GooglePayUtil.resetInstance()
    }
} 