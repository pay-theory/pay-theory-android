package com.paytheory.lib.googlepay

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for GooglePayUtil that execute real implementation code
 * while only mocking external dependencies.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayUtilTest {

    // Mock dependencies (only things that need to be mocked)
    private lateinit var mockClient: GooglePayClientInterface
    private lateinit var mockActivity: Activity
    private lateinit var mockPaymentData: PaymentData
    private lateinit var mockConfiguration: PayTheoryConfiguration
    
    // REAL implementation under test
    private lateinit var googlePayUtil: GooglePayUtil
    
    @Before
    fun setup() {
        // Create mocks for external dependencies
        mockClient = mockk(relaxed = true)
        mockActivity = mockk(relaxed = true)
        mockPaymentData = mockk(relaxed = true)
        mockConfiguration = mockk(relaxed = true)
        
        // Configure mocks
        every { mockConfiguration.googlePayMerchantName } returns "Test Merchant"
        every { mockConfiguration.googlePayEnabled } returns true
        every { mockConfiguration.googlePayEnvironment } returns GooglePayEnvironment.TEST
        every { mockConfiguration.googlePayAllowPrepaidCards } returns true
        every { mockConfiguration.googlePayBillingAddressRequired } returns false
        every { mockConfiguration.googlePayBillingAddressFormat } returns GooglePayBillingAddressFormat.MINIMAL
        every { mockConfiguration.googlePayPhoneNumberRequired } returns false
        every { mockConfiguration.googlePayShippingAddressRequired } returns false
        
        // Reset singleton to ensure clean state
        GooglePayUtil.resetInstance()
        
        // Create the REAL util instance with our mock client
        googlePayUtil = GooglePayUtil.getInstance(mockClient)
    }
    
    @After
    fun tearDown() {
        // Reset the singleton after each test
        GooglePayUtil.resetInstance()
        clearAllMocks()
    }
    
    @Test
    fun `isGooglePayAvailable should check availability through client`() {
        // Set up mock behavior for the client
        val taskSource = TaskCompletionSource<Boolean>()
        taskSource.setResult(true)
        val mockTask: Task<Boolean> = taskSource.task
        
        // Define the allowed card networks and auth methods
        val allowedCardNetworks = listOf("VISA", "MASTERCARD")
        val allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        
        every { 
            mockClient.isReadyToPay(
                activity = mockActivity,
                environment = GooglePayEnvironment.TEST,
                billingAddressRequired = false,
                allowedCardNetworks = any(),
                allowedAuthMethods = any()
            ) 
        } returns mockTask
        
        // Execute the real method
        val result = googlePayUtil.isGooglePayAvailable(
            activity = mockActivity,
            environment = GooglePayEnvironment.TEST,
            billingAddressRequired = false,
            allowedCardNetworks = allowedCardNetworks,
            allowedAuthMethods = allowedAuthMethods
        )
        
        // Verify the client was called with correct parameters
        verify { 
            mockClient.isReadyToPay(
                activity = mockActivity,
                environment = GooglePayEnvironment.TEST,
                billingAddressRequired = false,
                allowedCardNetworks = allowedCardNetworks,
                allowedAuthMethods = allowedAuthMethods
            ) 
        }
        
        // Verify the result
        assertEquals(mockTask, result)
    }
    
    @Test
    fun `requestGooglePayment should create request and delegate to client`() {
        // Set up mocks
        val priceBigDecimal = BigDecimal("10.99")
        val requestJson = "{\"valid\":\"json\"}"
        val requestSlot = slot<String>()
        
        val taskSource = TaskCompletionSource<PaymentData>()
        taskSource.setResult(mockPaymentData)
        val mockTask: Task<PaymentData> = taskSource.task
        
        // Define allowed card networks and auth methods
        val allowedCardNetworks = listOf("VISA", "MASTERCARD")
        val allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        
        // Mock client behavior
        every { 
            mockClient.createPaymentDataRequest(
                price = any(),
                merchantName = any(),
                billingAddressRequired = any(),
                billingAddressFormat = any(),
                shippingAddressRequired = any(),
                phoneNumberRequired = any(),
                environment = any(),
                allowPrepaidCards = any(),
                allowCreditCards = any(),
                allowedCardNetworks = any(),
                allowedAuthMethods = any()
            ) 
        } returns requestJson
        
        every { 
            mockClient.loadPaymentData(mockActivity, any(), capture(requestSlot))
        } returns mockTask
        
        // Execute the real method
        val result = googlePayUtil.requestGooglePayment(
            activity = mockActivity,
            amount = priceBigDecimal,
            merchantName = "Test Merchant", 
            environment = GooglePayEnvironment.TEST,
            billingAddressRequired = false,
            billingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
            shippingAddressRequired = false,
            phoneNumberRequired = false,
            allowPrepaidCards = true,
            allowCreditCards = true,
            allowedCardNetworks = allowedCardNetworks,
            allowedAuthMethods = allowedAuthMethods
        )
        
        // Verify client methods were called
        verify { 
            mockClient.createPaymentDataRequest(
                price = priceBigDecimal.toString(),
                merchantName = "Test Merchant",
                billingAddressRequired = false,
                billingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
                shippingAddressRequired = false,
                phoneNumberRequired = false,
                environment = GooglePayEnvironment.TEST,
                allowPrepaidCards = true,
                allowCreditCards = true,
                allowedCardNetworks = allowedCardNetworks,
                allowedAuthMethods = allowedAuthMethods
            ) 
        }
        
        verify { 
            mockClient.loadPaymentData(mockActivity, GooglePayEnvironment.TEST, requestJson)
        }
        
        // Verify the captured request parameter
        assertEquals(requestJson, requestSlot.captured)
        
        // Verify the result
        assertEquals(mockTask, result)
    }
    
    @Test
    fun `extractPaymentToken should delegate to client extractPaymentToken`() {
        // Set up mock
        val expectedToken = "test-payment-token"
        
        every { 
            mockClient.extractPaymentToken(mockPaymentData) 
        } returns expectedToken
        
        // Execute the real method
        val result = googlePayUtil.extractPaymentToken(mockPaymentData)
        
        // Verify client method was called
        verify { mockClient.extractPaymentToken(mockPaymentData) }
        
        // Verify the result
        assertEquals(expectedToken, result)
    }
    
    @Test
    fun `getAllowedPaymentMethodsJson should generate valid JSON structure`() {
        // Execute the real method
        val result = googlePayUtil.getAllowedPaymentMethodsJson()
        
        // Get and verify the result
        val jsonArray = JSONArray(result.toString())
        assertTrue(jsonArray.length() > 0)
        
        // Verify the JSON structure
        val cardMethod = jsonArray.getJSONObject(0)
        assertEquals("CARD", cardMethod.getString("type"))
        
        // Verify parameters
        val parameters = cardMethod.getJSONObject("parameters")
        assertTrue(parameters.has("allowedCardNetworks"))
        assertTrue(parameters.has("allowedAuthMethods"))
        
        // Verify card networks
        val networks = parameters.getJSONArray("allowedCardNetworks")
        assertTrue(networks.length() > 0)
        
        // Verify auth methods
        val authMethods = parameters.getJSONArray("allowedAuthMethods")
        assertTrue(authMethods.length() > 0)
    }
} 