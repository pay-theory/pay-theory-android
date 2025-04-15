package com.paytheory.lib.googlepay

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.data.ErrorCode
import com.paytheory.lib.data.PTError
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GooglePayProcessorTest {

    private lateinit var googlePayProcessor: GooglePayProcessor
    private lateinit var payable: Payable
    private lateinit var activity: Activity
    private lateinit var configuration: PayTheoryConfiguration
    private lateinit var viewModel: PaymentViewModel
    private lateinit var mockTask: Task<PaymentData>
    private lateinit var mockPaymentData: PaymentData
    
    @Before
    fun setup() {
        // Mock dependencies
        activity = mockk(relaxed = true)
        payable = mockk(relaxed = true)
        viewModel = mockk(relaxed = true)
        mockTask = mockk(relaxed = true)
        mockPaymentData = mockk(relaxed = true)
        
        val resources = mockk<Resources>()
        every { resources.getString(any()) } returns "12345678"
        every { (payable as Context).resources } returns resources
        
        // Create configuration
        configuration = PayTheoryConfiguration.Builder()
            .apiKey("test-paytheory-apikey")
            .amount(1000)
            .enableGooglePay(
                merchantName = "Test Merchant",
                environment = GooglePayEnvironment.TEST,
                billingAddressRequired = true,
                billingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
                shippingAddressRequired = false,
                phoneNumberRequired = false,
                allowPrepaidCards = true,
                allowCreditCards = true
            )
            .build()
            
        // Mock GooglePayUtil
        mockkObject(GooglePayUtil)
        val availabilityLiveData = MutableLiveData<Boolean>()
        availabilityLiveData.value = true
        
        every { 
            GooglePayUtil.isGooglePayAvailable(
                any(), 
                any(), 
                any()
            ) 
        } returns availabilityLiveData
        
        every { 
            GooglePayUtil.requestGooglePayment(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            ) 
        } returns mockTask
        
        every { GooglePayUtil.extractPaymentToken(any()) } returns "test-payment-token"
        
        // Mock task completion
        every { mockTask.isSuccessful } returns true
        every { mockTask.result } returns mockPaymentData
        
        // Create processor
        googlePayProcessor = GooglePayProcessor(
            configuration = configuration,
            activity = activity,
            payable = payable,
            viewModel = viewModel
        )
    }
    
    @Test
    fun `test isGooglePayAvailable calls GooglePayUtil correctly`() {
        // When
        googlePayProcessor.isGooglePayAvailable()
        
        // Then
        verify { 
            GooglePayUtil.isGooglePayAvailable(
                activity,
                configuration.googlePayEnvironment,
                configuration.googlePayBillingAddressRequired
            ) 
        }
    }
    
    @Test
    fun `test constructGooglePayPayment creates correct payment detail`() {
        // Use reflection to access private method
        val method = GooglePayProcessor::class.java.getDeclaredMethod(
            "constructGooglePayPayment", 
            String::class.java
        )
        method.isAccessible = true
        
        // When
        val paymentDetail = method.invoke(googlePayProcessor, "test-token")
        
        // Then
        assert(paymentDetail != null)
    }
    
    @Test
    fun `test initiateGooglePayPayment handles error gracefully`() {
        // Given
        val errorSlot = slot<PTError>()
        every { payable.handleError(capture(errorSlot)) } answers {}
        
        // Mock task to throw exception
        every { mockTask.addOnCompleteListener(any()) } answers {
            val callback = firstArg<(Task<PaymentData>) -> Unit>()
            callback.invoke(mockTask)
            mockTask
        }
        
        every { mockTask.isSuccessful } returns false
        every { mockTask.exception } returns Exception("Test exception")
        
        // When
        googlePayProcessor.initiateGooglePayPayment()
        
        // Then
        verify { payable.handleError(any()) }
        assert(errorSlot.captured.code == ErrorCode.GooglePayError)
    }
} 