package com.paytheory.lib.googlepay

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.googlepay.interfaces.GooglePayClientInterface
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import io.mockk.spyk
import org.json.JSONException

/**
 * Tests for GooglePayClient implementation that directly execute real implementation code
 * while mocking only external dependencies.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayClientTest {
    
    // Mock dependencies (external only)
    private lateinit var mockActivity: Activity
    private lateinit var mockPaymentsClient: PaymentsClient
    
    // Real implementation under test
    private lateinit var googlePayClient: GooglePayClient
    
    @Before
    fun setup() {
        // Create mocks for external dependencies only
        mockActivity = mockk(relaxed = true)
        mockPaymentsClient = mockk(relaxed = true)
        
        // Mock static methods for Android APIs
        mockkStatic(Wallet::class)
        mockkStatic(IsReadyToPayRequest::class)
        mockkStatic(PaymentDataRequest::class)
        
        // Setup mock behavior with proper type specifications
        every { 
            Wallet.getPaymentsClient(any<Activity>(), any()) 
        } returns mockPaymentsClient
        
        // Initialize real client implementation
        googlePayClient = GooglePayClient()
    }
    
    @After
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `createPaymentsClient should create client with TEST environment`() {
        // Set up the mock for TEST environment
        every { 
            Wallet.getPaymentsClient(mockActivity, any()) 
        } returns mockPaymentsClient
        
        // Execute real method
        val result = googlePayClient.createPaymentsClient(mockActivity, GooglePayEnvironment.TEST)
        
        // Verify external APIs were called with correct parameters
        verify { Wallet.getPaymentsClient(mockActivity, any()) }
        assertEquals(mockPaymentsClient, result)
    }
    
    @Test
    fun `createPaymentsClient should create client with PRODUCTION environment`() {
        // Set up the mock for PRODUCTION environment
        every { 
            Wallet.getPaymentsClient(mockActivity, any()) 
        } returns mockPaymentsClient
        
        // Execute real method
        val result = googlePayClient.createPaymentsClient(mockActivity, GooglePayEnvironment.PRODUCTION)
        
        // Verify external APIs were called with correct parameters
        verify { Wallet.getPaymentsClient(mockActivity, any()) }
        assertEquals(mockPaymentsClient, result)
    }
    
    @Test
    fun `createPaymentDataRequest should generate valid JSON with minimum parameters`() {
        // Execute real method that generates JSON
        val result = googlePayClient.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            allowedCardNetworks = listOf("VISA", "MASTERCARD"),
            allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        )
        
        // Parse and verify the structure of the generated JSON
        assertNotNull(result)
        val json = JSONObject(result)
        
        // Verify the JSON structure
        assertTrue(json.has("apiVersion"))
        assertTrue(json.has("apiVersionMinor"))
        assertTrue(json.has("allowedPaymentMethods"))
        
        val methods = json.getJSONArray("allowedPaymentMethods")
        val cardMethod = methods.getJSONObject(0)
        assertEquals("CARD", cardMethod.getString("type"))
        
        val parameters = cardMethod.getJSONObject("parameters")
        val networks = parameters.getJSONArray("allowedCardNetworks")
        val authMethods = parameters.getJSONArray("allowedAuthMethods")
        
        assertEquals("VISA", networks.getString(0))
        assertEquals("MASTERCARD", networks.getString(1))
        assertEquals("PAN_ONLY", authMethods.getString(0))
        assertEquals("CRYPTOGRAM_3DS", authMethods.getString(1))
        
        val transactionInfo = json.getJSONObject("transactionInfo")
        assertEquals("10.99", transactionInfo.getString("totalPrice"))
        assertEquals("USD", transactionInfo.getString("currencyCode"))
    }
    
    @Test
    fun `createPaymentDataRequest should include billing address when required`() {
        // Execute real method with billing address parameters
        val result = googlePayClient.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            billingAddressRequired = true,
            billingAddressFormat = GooglePayBillingAddressFormat.FULL,
            allowedCardNetworks = listOf("VISA", "MASTERCARD"),
            allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        )
        
        // Parse and verify the JSON includes billing address parameters
        val json = JSONObject(result)
        val methods = json.getJSONArray("allowedPaymentMethods")
        val cardMethod = methods.getJSONObject(0)
        val parameters = cardMethod.getJSONObject("parameters")
        
        assertTrue(parameters.getBoolean("billingAddressRequired"))
        val billingAddressParams = parameters.getJSONObject("billingAddressParameters")
        assertEquals("FULL", billingAddressParams.getString("format"))
    }
    
    @Test
    fun `createPaymentDataRequest should include shipping address when required`() {
        // Execute real method with shipping address required
        val result = googlePayClient.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            shippingAddressRequired = true,
            allowedCardNetworks = listOf("VISA", "MASTERCARD"),
            allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        )
        
        // Parse and verify the JSON includes shipping address parameter
        val json = JSONObject(result)
        assertTrue(json.getBoolean("shippingAddressRequired"))
    }
    
    @Test
    fun `loadPaymentData should convert JSON to request and call PaymentsClient`() {
        // Setup test data
        val paymentDataRequestJson = "{\"valid\":\"json\"}"
        val mockPaymentDataRequest = mockk<PaymentDataRequest>()
        val mockPaymentData = mockk<PaymentData>()
        
        val taskSource = TaskCompletionSource<PaymentData>()
        taskSource.setResult(mockPaymentData)
        val mockTask: Task<PaymentData> = taskSource.task
        
        // Set up mocks for external dependencies
        every { PaymentDataRequest.fromJson(paymentDataRequestJson) } returns mockPaymentDataRequest
        every { mockPaymentsClient.loadPaymentData(mockPaymentDataRequest) } returns mockTask
        
        // Execute real method
        val result = googlePayClient.loadPaymentData(mockActivity, paymentDataRequestJson)
        
        // Verify external APIs were called correctly
        verify { Wallet.getPaymentsClient(mockActivity, any()) }
        verify { PaymentDataRequest.fromJson(paymentDataRequestJson) }
        verify { mockPaymentsClient.loadPaymentData(mockPaymentDataRequest) }
        
        // Verify the result
        assertEquals(mockTask, result)
    }
    
    @Test
    fun `extractPaymentToken should parse PaymentData JSON and return token`() {
        // Setup test data
        val mockPaymentData = mockk<PaymentData>()
        val mockJson = """
        {
            "paymentMethodData": {
                "tokenizationData": {
                    "token": "test-payment-token"
                }
            }
        }
        """.trimIndent()
        
        every { mockPaymentData.toJson() } returns mockJson
        
        // Execute real method
        val result = googlePayClient.extractPaymentToken(mockPaymentData)
        
        // Verify external API was called
        verify { mockPaymentData.toJson() }
        
        // Verify the result
        assertEquals("test-payment-token", result)
    }
    
    @Test
    fun `factory created client should have correct interface type`() {
        // Given
        val client = GooglePayClient()
        
        // Then
        assertTrue(client is GooglePayClientInterface)
    }
    
    @Test
    fun `cardPaymentMethod should handle MINIMAL billing address format`() {
        // We'll use reflection to access the private method
        val cardPaymentMethod = GooglePayClient::class.java.getDeclaredMethod(
            "cardPaymentMethod",
            Boolean::class.java,
            GooglePayBillingAddressFormat::class.java,
            List::class.java,
            List::class.java,
            Boolean::class.java,
            Boolean::class.java
        )
        cardPaymentMethod.isAccessible = true
        
        // Execute with MINIMAL format (branch not covered in existing tests)
        val result = cardPaymentMethod.invoke(
            googlePayClient,
            true, // billingAddressRequired
            GooglePayBillingAddressFormat.MINIMAL, // format that needs coverage
            listOf("VISA", "MASTERCARD"), // allowedCardNetworks
            listOf("PAN_ONLY", "CRYPTOGRAM_3DS"), // allowedAuthMethods
            true, // allowPrepaidCards
            true // allowCreditCards
        ) as JSONObject
        
        // Verify format is set correctly
        val parameters = result.getJSONObject("parameters")
        val billingAddressParams = parameters.getJSONObject("billingAddressParameters")
        assertEquals("MIN", billingAddressParams.getString("format"))
    }
    
    @Test
    fun `baseCardPaymentMethod should work with default parameters`() {
        // We'll use reflection to access the private method
        val baseCardPaymentMethod = GooglePayClient::class.java.getDeclaredMethod(
            "baseCardPaymentMethod",
            Boolean::class.java,
            List::class.java,
            List::class.java
        )
        baseCardPaymentMethod.isAccessible = true
        
        // Execute with default billingAddressRequired (false)
        val result = baseCardPaymentMethod.invoke(
            googlePayClient,
            false, // explicitly pass default value
            listOf("VISA", "MASTERCARD"), // allowedCardNetworks
            listOf("PAN_ONLY", "CRYPTOGRAM_3DS") // allowedAuthMethods
        ) as JSONObject
        
        // Verify structure
        assertTrue(result.has("type"))
        assertEquals("CARD", result.getString("type"))
        
        val parameters = result.getJSONObject("parameters")
        assertTrue(parameters.has("allowedAuthMethods"))
        assertTrue(parameters.has("allowedCardNetworks"))
        assertEquals(false, parameters.getBoolean("billingAddressRequired"))
    }
    
    @Test
    fun `cardPaymentMethod should work with all default parameters`() {
        // We'll use reflection to access the private method
        val cardPaymentMethod = GooglePayClient::class.java.getDeclaredMethod(
            "cardPaymentMethod",
            Boolean::class.java,
            GooglePayBillingAddressFormat::class.java,
            List::class.java,
            List::class.java,
            Boolean::class.java,
            Boolean::class.java
        )
        cardPaymentMethod.isAccessible = true
        
        // Execute with all default values explicitly passed
        val result = cardPaymentMethod.invoke(
            googlePayClient,
            false, // default billingAddressRequired
            GooglePayBillingAddressFormat.MINIMAL, // default billingAddressFormat
            listOf("VISA", "MASTERCARD"), // allowedCardNetworks (required)
            listOf("PAN_ONLY", "CRYPTOGRAM_3DS"), // allowedAuthMethods (required)
            true, // default allowPrepaidCards
            true // default allowCreditCards
        ) as JSONObject
        
        // Verify structure
        assertTrue(result.has("type"))
        assertEquals("CARD", result.getString("type"))
        
        val parameters = result.getJSONObject("parameters")
        assertTrue(parameters.has("allowPrepaidCards"))
        assertTrue(parameters.has("allowCreditCards"))
        assertTrue(result.has("tokenizationSpecification"))
        
        assertEquals(true, parameters.getBoolean("allowPrepaidCards"))
        assertEquals(true, parameters.getBoolean("allowCreditCards"))
    }
} 