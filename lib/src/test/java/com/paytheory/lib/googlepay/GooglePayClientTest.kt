package com.paytheory.lib.googlepay

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.Executor

@RunWith(MockitoJUnitRunner::class)
class GooglePayClientTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @MockK
    private lateinit var activity: Activity
    
    @MockK
    private lateinit var paymentsClient: PaymentsClient
    
    @MockK
    private lateinit var isReadyToPayTask: Task<Boolean>
    
    @MockK
    private lateinit var loadPaymentDataTask: Task<PaymentData>
    
    @MockK
    private lateinit var paymentData: PaymentData
    
    private lateinit var googlePayClient: GooglePayClient
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        googlePayClient = GooglePayClient()
        
        mockkStatic(Wallet::class)
        every { Wallet.getPaymentsClient(any(), any()) } returns paymentsClient
        
        // Mock task behavior
        every { isReadyToPayTask.isSuccessful } returns true
        every { isReadyToPayTask.addOnCompleteListener(any()) } answers {
            val executor = Executor { it.run() }
            val callback = firstArg<com.google.android.gms.tasks.OnCompleteListener<Boolean>>()
            callback.onComplete(isReadyToPayTask)
            isReadyToPayTask
        }
        
        every { loadPaymentDataTask.addOnCompleteListener(any()) } returns loadPaymentDataTask
        
        // Mock PaymentsClient behavior
        every { paymentsClient.isReadyToPay(any()) } returns isReadyToPayTask
        every { paymentsClient.loadPaymentData(any()) } returns loadPaymentDataTask
        
        // Mock PaymentData
        val paymentDataJson = """
            {
                "paymentMethodData": {
                    "tokenizationData": {
                        "token": "exampleToken123"
                    }
                }
            }
        """.trimIndent()
        
        every { paymentData.toJson() } returns paymentDataJson
        
        // Mock static methods
        mockkStatic(IsReadyToPayRequest::class)
        mockkStatic(PaymentDataRequest::class)
        
        every { IsReadyToPayRequest.fromJson(any()) } returns mockk(relaxed = true)
        every { PaymentDataRequest.fromJson(any()) } returns mockk(relaxed = true)
    }
    
    @Test
    fun `test createPaymentsClient with TEST environment`() {
        val walletOptionsSlot = slot<Wallet.WalletOptions>()
        
        googlePayClient.createPaymentsClient(activity, GooglePayEnvironment.TEST)
        
        verify { Wallet.getPaymentsClient(activity, capture(walletOptionsSlot)) }
        assert(walletOptionsSlot.captured.environment == WalletConstants.ENVIRONMENT_TEST)
    }
    
    @Test
    fun `test createPaymentsClient with PRODUCTION environment`() {
        val walletOptionsSlot = slot<Wallet.WalletOptions>()
        
        googlePayClient.createPaymentsClient(activity, GooglePayEnvironment.PRODUCTION)
        
        verify { Wallet.getPaymentsClient(activity, capture(walletOptionsSlot)) }
        assert(walletOptionsSlot.captured.environment == WalletConstants.ENVIRONMENT_PRODUCTION)
    }
    
    @Test
    fun `test isReadyToPay success response`() {
        val observer = mockk<Observer<Boolean>>(relaxed = true)
        
        val isReadyToPayLiveData = googlePayClient.isReadyToPay(activity)
        isReadyToPayLiveData.observeForever(observer)
        
        verify { observer.onChanged(true) }
    }
    
    @Test
    fun `test isReadyToPay failure response`() {
        val observer = mockk<Observer<Boolean>>(relaxed = true)
        every { isReadyToPayTask.isSuccessful } returns false
        
        val isReadyToPayLiveData = googlePayClient.isReadyToPay(activity)
        isReadyToPayLiveData.observeForever(observer)
        
        verify { observer.onChanged(false) }
    }
    
    @Test
    fun `test createPaymentDataRequest with basic parameters`() {
        val price = "10.99"
        val merchantName = "Test Merchant"
        
        val requestJson = googlePayClient.createPaymentDataRequest(
            price = price,
            merchantName = merchantName,
            billingAddressRequired = false
        )
        
        val jsonObject = JSONObject(requestJson)
        
        assert(jsonObject.getJSONObject("transactionInfo").getString("totalPrice") == price)
        assert(jsonObject.getJSONObject("merchantInfo").getString("merchantName") == merchantName)
    }
    
    @Test
    fun `test createPaymentDataRequest with billing address required`() {
        val requestJson = googlePayClient.createPaymentDataRequest(
            price = "10.99",
            merchantName = "Test Merchant",
            billingAddressRequired = true,
            billingAddressFormat = GooglePayBillingAddressFormat.FULL
        )
        
        val jsonObject = JSONObject(requestJson)
        val cardMethod = jsonObject.getJSONArray("allowedPaymentMethods").getJSONObject(0)
        val parameters = cardMethod.getJSONObject("parameters")
        
        assert(parameters.getBoolean("billingAddressRequired"))
        assert(parameters.getJSONObject("billingAddressParameters").getString("format") == "FULL")
    }
    
    @Test
    fun `test loadPaymentData calls PaymentsClient correctly`() {
        val requestJson = "{}"
        
        googlePayClient.loadPaymentData(activity, requestJson)
        
        verify { paymentsClient.loadPaymentData(any()) }
    }
    
    @Test
    fun `test extractPaymentToken parses token correctly`() {
        val token = googlePayClient.extractPaymentToken(paymentData)
        
        assert(token == "exampleToken123")
    }
} 