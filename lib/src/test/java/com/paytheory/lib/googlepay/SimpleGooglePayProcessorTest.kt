package com.paytheory.lib.googlepay

import android.app.Activity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.api.ChallengeOptions
import com.paytheory.lib.api.Rp
import com.paytheory.lib.api.User
import com.paytheory.lib.api.PubKeyCredParam
import com.paytheory.lib.api.AuthenticatorSelection
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.googlepay.interfaces.GooglePayUtilInterface
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.reactors.MessageReactors
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Simple tests for GooglePayProcessor focusing on basic functionality and
 * improving coverage without needing to access private fields or methods
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class SimpleGooglePayProcessorTest {

    // Test dependencies
    private lateinit var mockActivity: Activity
    private lateinit var mockPayable: Payable
    private lateinit var mockViewModel: PaymentViewModel
    private lateinit var mockGooglePayUtil: GooglePayUtilInterface
    private lateinit var mockConfiguration: PayTheoryConfiguration
    private lateinit var mockPaymentData: PaymentData
    private lateinit var mockMessageReactors: MessageReactors
    
    // Special test API key
    private val TEST_API_KEY = "test-paytheory-apikey"
    
    @Before
    fun setup() {
        // Create relaxed mocks for all dependencies
        mockActivity = mockk(relaxed = true)
        mockPayable = mockk(relaxed = true)
        mockViewModel = mockk(relaxed = true)
        mockGooglePayUtil = mockk(relaxed = true)
        mockConfiguration = mockk(relaxed = true)
        mockPaymentData = mockk(relaxed = true)
        mockMessageReactors = mockk(relaxed = true)
        
        // Setup minimal configuration properties needed for tests
        every { mockConfiguration.isTestMode } returns true
        every { mockConfiguration.apiKey } returns TEST_API_KEY
        every { mockConfiguration.amount } returns 1099
        every { mockConfiguration.feeMode } returns "MERCHANT_FEE"
        every { mockConfiguration.payorInfo } returns null
        every { mockConfiguration.serviceFee } returns 0
        every { mockConfiguration.googlePayEnvironment } returns GooglePayEnvironment.TEST
        every { mockConfiguration.googlePayBillingAddressRequired } returns false
        every { mockConfiguration.googlePayMerchantName } returns "Test Merchant"
        every { mockConfiguration.googlePayBillingAddressFormat } returns GooglePayBillingAddressFormat.MINIMAL
        every { mockConfiguration.googlePayShippingAddressRequired } returns false
        every { mockConfiguration.googlePayPhoneNumberRequired } returns false
        every { mockConfiguration.googlePayAllowPrepaidCards } returns true
        every { mockConfiguration.googlePayAllowCreditCards } returns true
        
        // Mock Payable.getContext() to return null for test API key path
        every { mockPayable.getContext() } returns null
        
        // Setup view model mocks
        every { mockViewModel._paymentState } returns mockk(relaxed = true)
        every { mockViewModel.validateInputs() } just Runs
    }
    
    @Test
    fun `constructGooglePayPayment should create payment detail with correct parameters`() {
        // Given
        val testToken = "test-google-pay-token"
        val testAmount = 1099
        
        // Configure mock behavior for configuration
        every { mockConfiguration.amount } returns testAmount
        every { mockConfiguration.feeMode } returns "MERCHANT_FEE"
        every { mockConfiguration.payorInfo } returns null
        every { mockConfiguration.serviceFee } returns 0
        
        // Create a REAL GooglePayProcessor with the mocked dependencies
        val processor = GooglePayProcessor(
            payable = mockPayable,
            activity = mockActivity,
            configuration = mockConfiguration,
            viewModel = mockViewModel,
            googlePayUtil = mockGooglePayUtil
        )
        
        // Execute the REAL implementation method - this is what gives code coverage
        val paymentDetail = processor.constructGooglePayPayment(testToken)
        
        // Then - verify the output matches what we expect
        assertNotNull(paymentDetail)
        assertEquals("wallet", paymentDetail.type)
        assertEquals(testAmount, paymentDetail.amount)
        assertEquals("USD", paymentDetail.currency)
        assertEquals("GOOGLE_PAY", paymentDetail.walletType)
        assertEquals(testToken, paymentDetail.digitalWalletPayload)
        assertEquals("MERCHANT_FEE", paymentDetail.fee_mode)
    }
    
    @Test
    fun `isGooglePayAvailable should delegate to GooglePayUtil`() {
        // Given
        val expectedTask = TaskCompletionSource<Boolean>().apply { setResult(true) }.task
        
        every { 
            mockGooglePayUtil.isGooglePayAvailable(
                any(), 
                any(), 
                any()
            ) 
        } returns expectedTask
        
        // Create the processor
        val processor = GooglePayProcessor(
            payable = mockPayable,
            activity = mockActivity,
            configuration = mockConfiguration,
            viewModel = mockViewModel,
            googlePayUtil = mockGooglePayUtil
        )
        
        // When
        val result = processor.isGooglePayAvailable()
        
        // Then
        verify { 
            mockGooglePayUtil.isGooglePayAvailable(
                mockActivity, 
                GooglePayEnvironment.TEST, 
                false
            )
        }
        
        assertEquals(expectedTask, result)
    }
    
    @Test
    fun `initiateGooglePayPayment should call ptTokenApiCall when not connected`() {
        // Given
        val processor = spyk(
            GooglePayProcessor(
                payable = mockPayable,
                activity = mockActivity,
                configuration = mockConfiguration,
                viewModel = mockViewModel,
                googlePayUtil = mockGooglePayUtil
            )
        )
        
        // Mock the ptTokenApiCall method since we can't easily set connected=false
        every { processor.ptTokenApiCall(any()) } just Runs
        
        // When - expect ptTokenApiCall to be called by default as connected is false
        processor.initiateGooglePayPayment()
        
        // Then
        verify { processor.ptTokenApiCall(mockPayable) }
        verify(exactly = 0) { mockGooglePayUtil.requestGooglePayment(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }
    
    @Test
    fun `process method should log message and not take action`() {
        // Given
        val processor = GooglePayProcessor(
            payable = mockPayable,
            activity = mockActivity,
            configuration = mockConfiguration,
            viewModel = mockViewModel,
            googlePayUtil = mockGooglePayUtil
        )
        
        val paymentDetail = PaymentDetail(
            type = "wallet",
            timing = 123456789,
            amount = 1099,
            currency = "USD"
        )
        
        // When
        processor.process(paymentDetail)
        
        // Then - no assertion needed, just verifying it doesn't throw
    }
} 