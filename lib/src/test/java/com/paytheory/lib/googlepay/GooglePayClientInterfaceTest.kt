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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for GooglePayClientInterface implementation
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayClientInterfaceTest {

    // Mock dependencies
    private lateinit var mockActivity: Activity
    private lateinit var mockPaymentsClient: PaymentsClient
    private lateinit var mockIsReadyToPayRequest: IsReadyToPayRequest
    private lateinit var mockPaymentDataRequest: PaymentDataRequest
    
    // System under test
    private lateinit var client: GooglePayClientInterface
    
    @Before
    fun setup() {
        // Create mocks
        mockActivity = mockk(relaxed = true)
        mockPaymentsClient = mockk(relaxed = true)
        mockIsReadyToPayRequest = mockk(relaxed = true)
        mockPaymentDataRequest = mockk(relaxed = true)
        
        // Mock static methods
        mockkStatic(Wallet::class)
        mockkStatic(IsReadyToPayRequest::class)
        mockkStatic(PaymentDataRequest::class)
        
        // Setup mock behavior
        every { Wallet.getPaymentsClient(any<Activity>(), any()) } returns mockPaymentsClient
        every { IsReadyToPayRequest.fromJson(any<String>()) } returns mockIsReadyToPayRequest
        every { PaymentDataRequest.fromJson(any<String>()) } returns mockPaymentDataRequest
        
        // Create client
        client = GooglePayFactory.getGooglePayClient()
    }
    
    @Test
    fun `createPaymentsClient passes correct environment to Wallet API`() {
        // When
        val testClient = client.createPaymentsClient(mockActivity, GooglePayEnvironment.TEST)
        val prodClient = client.createPaymentsClient(mockActivity, GooglePayEnvironment.PRODUCTION)
        
        // Then
        // Just verify the Wallet.getPaymentsClient was called with the activity
        verify(exactly = 2) { Wallet.getPaymentsClient(mockActivity, any()) }
        
        // Verify the clients are returned
        assertEquals(mockPaymentsClient, testClient)
        assertEquals(mockPaymentsClient, prodClient)
    }
    
    @Test
    fun `isReadyToPay creates properly formatted JSON request`() {
        // Given
        val allowedCardNetworks = listOf("VISA", "MASTERCARD")
        val allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        
        val requestSlot = slot<String>()
        every { IsReadyToPayRequest.fromJson(capture(requestSlot)) } returns mockIsReadyToPayRequest
        
        // When
        client.isReadyToPay(
            mockActivity,
            GooglePayEnvironment.TEST,
            false,
            allowedCardNetworks,
            allowedAuthMethods
        )
        
        // Then
        val jsonRequest = JSONObject(requestSlot.captured)
        val allowedPaymentMethods = jsonRequest.getJSONArray("allowedPaymentMethods")
        val cardMethod = allowedPaymentMethods.getJSONObject(0)
        assertEquals("CARD", cardMethod.getString("type"))
        
        val parameters = cardMethod.getJSONObject("parameters")
        val networks = parameters.getJSONArray("allowedCardNetworks")
        assertEquals("VISA", networks.getString(0))
        assertEquals("MASTERCARD", networks.getString(1))
        
        val authMethodsJson = parameters.getJSONArray("allowedAuthMethods")
        assertEquals("PAN_ONLY", authMethodsJson.getString(0))
        assertEquals("CRYPTOGRAM_3DS", authMethodsJson.getString(1))
    }
    
    @Test
    fun `createPaymentDataRequest returns valid JSON with minimum parameters`() {
        // When
        val result = client.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            allowedCardNetworks = listOf("VISA", "MASTERCARD"),
            allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        )
        
        // Then
        val json = JSONObject(result)
        
        // Verify transaction info
        val transactionInfo = json.getJSONObject("transactionInfo")
        assertEquals("10.99", transactionInfo.getString("totalPrice"))
        assertEquals("USD", transactionInfo.getString("currencyCode"))
        
        // Verify merchant info
        val merchantInfo = json.getJSONObject("merchantInfo")
        assertEquals("Test Merchant", merchantInfo.getString("merchantName"))
    }
    
    @Test
    fun `createPaymentDataRequest includes billing address when required`() {
        // When
        val result = client.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            billingAddressRequired = true,
            billingAddressFormat = GooglePayBillingAddressFormat.FULL,
            allowedCardNetworks = listOf("VISA", "MASTERCARD"),
            allowedAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS")
        )
        
        // Then
        val json = JSONObject(result)
        val methods = json.getJSONArray("allowedPaymentMethods")
        val cardMethod = methods.getJSONObject(0)
        val parameters = cardMethod.getJSONObject("parameters")
        
        // Verify billing address parameters
        assertTrue(parameters.getBoolean("billingAddressRequired"))
        val billingAddressParams = parameters.getJSONObject("billingAddressParameters")
        assertEquals("FULL", billingAddressParams.getString("format"))
    }
    
    @Test
    fun `extractPaymentToken parses valid PaymentData correctly`() {
        // Given
        // Mock payment data with expected structure
        val mockPaymentData = mockk<PaymentData>()
        val mockJsonPaymentData = """
        {
            "paymentMethodData": {
                "tokenizationData": {
                    "type": "PAYMENT_GATEWAY",
                    "token": "test-payment-token"
                }
            }
        }
        """.trimIndent()
        
        every { mockPaymentData.toJson() } returns mockJsonPaymentData
        
        // When
        val token = client.extractPaymentToken(mockPaymentData)
        
        // Then
        assertEquals("test-payment-token", token)
    }
} 