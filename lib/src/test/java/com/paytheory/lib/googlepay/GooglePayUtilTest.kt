package com.paytheory.lib.googlepay

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class GooglePayUtilTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    @MockK
    private lateinit var activity: Activity
    
    @MockK
    private lateinit var googlePayClient: GooglePayClient
    
    @MockK
    private lateinit var paymentDataTask: Task<PaymentData>
    
    @MockK
    private lateinit var paymentData: PaymentData
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        mockkObject(GooglePayUtil)
        
        // Replace the real GooglePayClient with our mock
        val clientField = GooglePayUtil::class.java.getDeclaredField("googlePayClient")
        clientField.isAccessible = true
        clientField.set(GooglePayUtil, googlePayClient)
        
        // Set up common mocks
        val isReadyToPayLiveData = MutableLiveData<Boolean>()
        isReadyToPayLiveData.value = true
        
        every { 
            googlePayClient.isReadyToPay(
                any(), 
                any(), 
                any(), 
                any(), 
                any()
            ) 
        } returns isReadyToPayLiveData
        
        every { 
            googlePayClient.createPaymentDataRequest(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            ) 
        } returns "{}"
        
        every { googlePayClient.loadPaymentData(any(), any()) } returns paymentDataTask
        
        every { googlePayClient.extractPaymentToken(any()) } returns "test_token"
    }
    
    @Test
    fun `test isGooglePayAvailable calls client correctly`() {
        GooglePayUtil.isGooglePayAvailable(
            activity,
            GooglePayEnvironment.TEST,
            true
        )
        
        verify { 
            googlePayClient.isReadyToPay(
                activity,
                GooglePayEnvironment.TEST,
                true
            ) 
        }
    }
    
    @Test
    fun `test requestGooglePayment calls client methods correctly`() {
        val amount = BigDecimal("15.99")
        val merchantName = "Test Merchant"
        
        GooglePayUtil.requestGooglePayment(
            activity,
            amount,
            merchantName,
            GooglePayEnvironment.PRODUCTION,
            true,
            GooglePayBillingAddressFormat.FULL,
            true,
            true,
            false,
            true
        )
        
        verify { 
            googlePayClient.createPaymentDataRequest(
                price = amount.toString(),
                merchantName = merchantName,
                billingAddressRequired = true,
                billingAddressFormat = GooglePayBillingAddressFormat.FULL,
                shippingAddressRequired = true,
                phoneNumberRequired = true,
                environment = GooglePayEnvironment.PRODUCTION,
                allowPrepaidCards = false,
                allowCreditCards = true
            ) 
        }
        
        verify { googlePayClient.loadPaymentData(activity, any()) }
    }
    
    @Test
    fun `test extractPaymentToken delegates to client`() {
        GooglePayUtil.extractPaymentToken(paymentData)
        
        verify { googlePayClient.extractPaymentToken(paymentData) }
    }
} 