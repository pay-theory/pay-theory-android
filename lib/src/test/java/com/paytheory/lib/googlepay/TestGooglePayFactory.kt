package com.paytheory.lib.googlepay

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentsClient
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayProcessorInterface
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.mockk
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

/**
 * Factory class for creating test mocks of Google Pay components
 * This simplifies creating and configuring mocks for testing
 */
object TestGooglePayFactory {
    
    /**
     * Creates a mock Google Pay client with default behavior
     *
     * @param isGooglePayAvailable Whether Google Pay should be reported as available
     * @param predefinedJson Optional predefined JSON for payment data request
     * @return A mock GooglePayClientInterface
     */
    fun createMockClient(
        isGooglePayAvailable: Boolean = true,
        predefinedJson: String? = null
    ): GooglePayClientInterface {
        return object : GooglePayClientInterface {
            override fun createPaymentsClient(
                activity: Activity,
                environment: GooglePayEnvironment
            ): PaymentsClient {
                return mockk(relaxed = true)
            }
            
            override fun isReadyToPay(
                activity: Activity,
                environment: GooglePayEnvironment,
                billingAddressRequired: Boolean,
                allowedCardNetworks: List<String>,
                allowedAuthMethods: List<String>
            ): Task<Boolean> {
                val source = TaskCompletionSource<Boolean>()
                source.setResult(isGooglePayAvailable)
                return source.task
            }
            
            override fun createPaymentDataRequest(
                price: String,
                merchantName: String,
                billingAddressRequired: Boolean,
                billingAddressFormat: GooglePayBillingAddressFormat,
                shippingAddressRequired: Boolean,
                phoneNumberRequired: Boolean,
                environment: GooglePayEnvironment,
                allowPrepaidCards: Boolean,
                allowCreditCards: Boolean,
                allowedCardNetworks: List<String>,
                allowedAuthMethods: List<String>
            ): String {
                return predefinedJson ?: createDefaultPaymentDataRequestJson(
                    price, merchantName, billingAddressRequired, allowedCardNetworks
                )
            }
            
            override fun loadPaymentData(
                activity: Activity,
                environment: GooglePayEnvironment,
                paymentDataRequestJson: String
            ): Task<PaymentData> {
                val source = TaskCompletionSource<PaymentData>()
                source.setResult(mockk(relaxed = true))
                return source.task
            }
            
            override fun extractPaymentToken(paymentData: PaymentData): String {
                return "test-payment-token"
            }
        }
    }
    
    /**
     * Creates a mock Google Pay util with default behavior
     *
     * @param isGooglePayAvailable Whether Google Pay should be reported as available
     * @return A mock GooglePayUtilInterface
     */
    fun createMockUtil(
        isGooglePayAvailable: Boolean = true
    ): GooglePayUtilInterface {
        return object : GooglePayUtilInterface {
            override fun isGooglePayAvailable(
                activity: Activity,
                environment: GooglePayEnvironment,
                billingAddressRequired: Boolean,
                allowedCardNetworks: List<String>,
                allowedAuthMethods: List<String>
            ): Task<Boolean> {
                val source = TaskCompletionSource<Boolean>()
                source.setResult(isGooglePayAvailable)
                return source.task
            }
            
            override fun requestGooglePayment(
                activity: Activity,
                amount: BigDecimal,
                merchantName: String,
                environment: GooglePayEnvironment,
                billingAddressRequired: Boolean,
                billingAddressFormat: GooglePayBillingAddressFormat,
                shippingAddressRequired: Boolean,
                phoneNumberRequired: Boolean,
                allowPrepaidCards: Boolean,
                allowCreditCards: Boolean,
                allowedCardNetworks: List<String>,
                allowedAuthMethods: List<String>
            ): Task<PaymentData> {
                val source = TaskCompletionSource<PaymentData>()
                source.setResult(mockk(relaxed = true))
                return source.task
            }
            
            override fun extractPaymentToken(paymentData: PaymentData): String {
                return "test-payment-token"
            }
            
            override fun getAllowedPaymentMethodsJson(): JSONArray {
                return JSONArray().put(
                    JSONObject(
                        mapOf(
                            "type" to "CARD",
                            "parameters" to mapOf(
                                "allowedAuthMethods" to arrayOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                                "allowedCardNetworks" to arrayOf("VISA", "MASTERCARD")
                            )
                        )
                    )
                )
            }
        }
    }
    
    /**
     * Creates a mock configuration for Google Pay tests
     *
     * @param amount Payment amount in cents
     * @param merchantName Merchant name to display
     * @param googlePayEnabled Whether Google Pay is enabled
     * @return A mock PayTheoryConfiguration
     */
    fun createMockConfiguration(
        amount: Int = 1099,
        merchantName: String = "Test Merchant",
        googlePayEnabled: Boolean = true
    ): PayTheoryConfiguration {
        val config = mockk<PayTheoryConfiguration>(relaxed = true)
        
        // Configure important properties for Google Pay
        io.mockk.every { config.apiKey } returns "test-paytheory-apikey"
        io.mockk.every { config.amount } returns amount
        io.mockk.every { config.googlePayEnabled } returns googlePayEnabled
        io.mockk.every { config.googlePayMerchantName } returns merchantName
        io.mockk.every { config.googlePayEnvironment } returns GooglePayEnvironment.TEST
        io.mockk.every { config.googlePayBillingAddressRequired } returns false
        io.mockk.every { config.googlePayBillingAddressFormat } returns GooglePayBillingAddressFormat.MINIMAL
        io.mockk.every { config.googlePayShippingAddressRequired } returns false
        io.mockk.every { config.googlePayPhoneNumberRequired } returns false
        io.mockk.every { config.googlePayAllowPrepaidCards } returns true
        io.mockk.every { config.googlePayAllowCreditCards } returns true
        io.mockk.every { config.feeMode } returns "MERCHANT_FEE" 
        
        // Add required card networks and auth methods
        io.mockk.every { config.googlePayAllowedCardNetworks } returns listOf("VISA", "MASTERCARD", "AMEX", "DISCOVER", "JCB")
        io.mockk.every { config.googlePaySupportedMethods } returns listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        
        return config
    }
    
    /**
     * Setup test environment with mocked dependencies
     * This configures the GooglePayFactory with test implementations
     * 
     * @param isGooglePayAvailable Whether Google Pay should be available
     */
    fun setupTestEnvironment(isGooglePayAvailable: Boolean = true) {
        val mockClient = createMockClient(isGooglePayAvailable)
        val mockUtil = createMockUtil(isGooglePayAvailable)
        
        GooglePayFactory.setTestImplementations(mockClient, mockUtil)
    }
    
    /**
     * Clean up test environment
     * This resets the GooglePayFactory to default implementations
     */
    fun cleanupTestEnvironment() {
        GooglePayFactory.resetToDefaultImplementations()
    }
    
    /**
     * Creates a default payment data request JSON for testing
     */
    private fun createDefaultPaymentDataRequestJson(
        price: String = "10.99",
        merchantName: String = "Test Merchant",
        billingAddressRequired: Boolean = false,
        allowedCardNetworks: List<String> = listOf("VISA", "MASTERCARD")
    ): String {
        val json = JSONObject()
        json.put("apiVersion", 2)
        json.put("apiVersionMinor", 0)
        
        // Payment methods
        val methods = JSONArray()
        val cardMethod = JSONObject()
        cardMethod.put("type", "CARD")
        
        // Parameters
        val parameters = JSONObject()
        val networks = JSONArray()
        allowedCardNetworks.forEach { networks.put(it) }
        
        val authMethods = JSONArray()
        authMethods.put("PAN_ONLY")
        authMethods.put("CRYPTOGRAM_3DS")
        
        parameters.put("allowedCardNetworks", networks)
        parameters.put("allowedAuthMethods", authMethods)
        parameters.put("billingAddressRequired", billingAddressRequired)
        
        cardMethod.put("parameters", parameters)
        methods.put(cardMethod)
        json.put("allowedPaymentMethods", methods)
        
        // Transaction info
        val transactionInfo = JSONObject()
        transactionInfo.put("totalPrice", price)
        transactionInfo.put("totalPriceStatus", "FINAL")
        transactionInfo.put("currencyCode", "USD")
        json.put("transactionInfo", transactionInfo)
        
        // Merchant info
        json.put("merchantInfo", JSONObject().put("merchantName", merchantName))
        
        return json.toString()
    }
} 