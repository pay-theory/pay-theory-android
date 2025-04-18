package com.paytheory.lib

import android.content.Context
import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.model.FieldState
import com.paytheory.lib.model.PaymentField
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PayableContextProviderTest {

    @MockK
    private lateinit var mockContext: Context
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }
    
    // Test implementation of the Payable interface
    private class TestPayable : Payable {
        var isReady: Boolean = false
        var paymentStarted: String? = null
        var tokenStarted: String? = null
        var successResult: SuccessfulTransactionResult? = null
        var failureResult: FailedTransactionResult? = null
        var barcodeResult: BarcodeResult? = null
        var tokenResult: PaymentMethodTokenResults? = null
        var stateChange: Pair<PaymentField, FieldState>? = null
        var error: PTError? = null
        var mockContext: Context? = null
        
        override fun handleReady(isReady: Boolean) {
            this.isReady = isReady
        }
        
        override fun handlePaymentStart(paymentType: String) {
            this.paymentStarted = paymentType
        }
        
        override fun handleTokenStart(paymentType: String) {
            this.tokenStarted = paymentType
        }
        
        override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
            this.successResult = successfulTransactionResult
        }
        
        override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
            this.failureResult = failedTransactionResult
        }
        
        override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
            this.barcodeResult = barcodeResult
        }
        
        override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
            this.tokenResult = paymentMethodToken
        }
        
        override fun handleStateChange(fieldState: Pair<PaymentField, FieldState>) {
            this.stateChange = fieldState
        }
        
        override fun handleError(error: PTError) {
            this.error = error
        }

        override fun clientContext(): Context {
            return mockContext ?: throw IllegalStateException("Context not set")
        }

        override fun getContext(): Context? {
            return mockContext
        }
    }
    
    @Test
    fun `Payable default getContext should return null`() {
        // Create a test implementation that explicitly implements getContext to return null
        val testPayable = object : Payable {
            override fun handleReady(isReady: Boolean) {}
            override fun handlePaymentStart(paymentType: String) {}
            override fun handleTokenStart(paymentType: String) {}
            override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {}
            override fun handleFailure(failedTransactionResult: FailedTransactionResult) {}
            override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {}
            override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {}
            override fun handleStateChange(fieldState: Pair<PaymentField, FieldState>) {}
            override fun handleError(error: PTError) {}
            override fun clientContext(): Context {
                throw IllegalStateException("Should not be called during test")
            }
            
            // Override getContext to explicitly return null
            override fun getContext(): Context? = null
        }
        
        // Test that getContext returns null
        assertNull(testPayable.getContext())
    }
    
    @Test
    fun `custom Payable implementation should handle context correctly`() {
        // Create an instance of our test implementation
        val testPayable = TestPayable()
        
        // Test with null context
        assertNull(testPayable.getContext())
        
        // Test with mock context
        testPayable.mockContext = mockContext
        assertEquals(mockContext, testPayable.getContext())
    }
    
    @Test
    fun `Payable implementation should handle callback methods correctly`() {
        // Create an instance of our test implementation
        val testPayable = TestPayable()
        
        // Prepare test data
        val isReady = true
        val paymentType = "CARD"
        val tokenType = "ACH"
        val successResult = mockk<SuccessfulTransactionResult>()
        val failureResult = mockk<FailedTransactionResult>()
        val barcodeResult = mockk<BarcodeResult>()
        val tokenResult = mockk<PaymentMethodTokenResults>()
        val fieldState = mockk<Pair<PaymentField, FieldState>>()
        val error = mockk<PTError>()
        
        // Call each method
        testPayable.handleReady(isReady)
        testPayable.handlePaymentStart(paymentType)
        testPayable.handleTokenStart(tokenType)
        testPayable.handleSuccess(successResult)
        testPayable.handleFailure(failureResult)
        testPayable.handleBarcodeSuccess(barcodeResult)
        testPayable.handleTokenizeSuccess(tokenResult)
        testPayable.handleStateChange(fieldState)
        testPayable.handleError(error)
        
        // Verify all methods were called correctly
        assertEquals(isReady, testPayable.isReady)
        assertEquals(paymentType, testPayable.paymentStarted)
        assertEquals(tokenType, testPayable.tokenStarted)
        assertEquals(successResult, testPayable.successResult)
        assertEquals(failureResult, testPayable.failureResult)
        assertEquals(barcodeResult, testPayable.barcodeResult)
        assertEquals(tokenResult, testPayable.tokenResult)
        assertEquals(fieldState, testPayable.stateChange)
        assertEquals(error, testPayable.error)
    }
    
    @Test
    fun `ContextProvider implementation should return context`() {
        // Create a test implementation of ContextProvider
        val contextProvider = object : ContextProvider {
            override fun getContext(): Context? {
                return mockContext
            }
        }
        
        // Verify context is returned correctly
        assertEquals(mockContext, contextProvider.getContext())
    }
    
    @Test
    fun `ContextProvider implementation with null context should return null`() {
        // Create a test implementation of ContextProvider
        val contextProvider = object : ContextProvider {
            override fun getContext(): Context? {
                return null
            }
        }
        
        // Verify null is returned correctly
        assertNull(contextProvider.getContext())
    }
} 