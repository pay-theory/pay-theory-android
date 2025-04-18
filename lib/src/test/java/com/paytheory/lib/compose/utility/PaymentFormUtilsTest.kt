package com.paytheory.lib.compose.utility

import android.content.Context
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.model.PaymentViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PaymentFormUtilsTest {

    private lateinit var mockContext: Context
    private lateinit var mockPayable: Payable
    private lateinit var mockConfiguration: PayTheoryConfiguration
    private lateinit var mockViewModel: PaymentViewModel

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockPayable = mockk(relaxed = true)
        mockConfiguration = mockk(relaxed = true)
        mockViewModel = mockk(relaxed = true)



        // Default configuration values
        every { mockConfiguration.skipTokenizeValidation } returns false
        every { mockConfiguration.sendReceipt } returns false
        every { mockConfiguration.receiptDescription } returns ""
        every { mockConfiguration.paymentParameters } returns ""
        every { mockConfiguration.payorId } returns ""
        every { mockConfiguration.invoiceId } returns ""
        every { mockConfiguration.accountCode } returns ""
        every { mockConfiguration.reference } returns ""
        every { mockConfiguration.serviceFee } returns 0
        every { mockConfiguration.isTestMode } returns false

        // Mock NetworkUtils
        mockkObject(NetworkUtils)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `validateNetworkConnectivity returns true when network available`() {
        // Given network is available
        every { NetworkUtils.isNetworkAvailable(mockContext) } returns true

        // When validating network connectivity
        val result = PaymentFormUtils.validateNetworkConnectivity(mockContext, mockPayable)

        // Then result should be true
        assertTrue(result)
        verify(exactly = 0) { mockPayable.handleError(any()) }
    }

    @Test
    fun `validateNetworkConnectivity returns false and calls error when network unavailable`() {
        // Given network is unavailable
        every { NetworkUtils.isNetworkAvailable(mockContext) } returns false

        // When validating network connectivity
        val result = PaymentFormUtils.validateNetworkConnectivity(mockContext, mockPayable)

        // Then result should be false and error should be reported
        assertFalse(result)
        verify {
            mockPayable.handleError(match {
                it.code == ErrorCode.TokenFailed &&
                it.error == NetworkUtils.NO_NETWORK_CONNECTION
            })
        }
    }

    @Test
    fun `createPayTheoryData includes basic values from configuration`() {
        // Given basic configuration

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should contain basic configuration values
        assertEquals(false, result["skip_validation"])
        assertEquals(false, result["send_receipt"])
        assertEquals(0, result["fee"])
        assertEquals("America/New_York", result["timezone"])
    }

    @Test
    fun `createPayTheoryData includes receipt description when enabled`() {
        // Given configuration with receipt enabled and description
        every { mockConfiguration.sendReceipt } returns true
        every { mockConfiguration.receiptDescription } returns "Receipt Description"

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should contain receipt description
        assertEquals(true, result["send_receipt"])
        assertEquals("Receipt Description", result["receipt_description"])
    }

    @Test
    fun `createPayTheoryData includes payment parameters when specified`() {
        // Given configuration with payment parameters
        every { mockConfiguration.paymentParameters } returns "payment_params"

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should contain payment parameters
        assertEquals("payment_params", result["payment_parameters"])
    }

    @Test
    fun `createPayTheoryData includes payorId when specified`() {
        // Given configuration with payorId
        every { mockConfiguration.payorId } returns "payor123"

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should contain payorId
        assertEquals("payor123", result["payorId"])
    }

    @Test
    fun `createPayTheoryData includes invoiceId when specified`() {
        // Given configuration with invoiceId
        every { mockConfiguration.invoiceId } returns "invoice123"

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should contain invoiceId
        assertEquals("invoice123", result["invoice_id"])
    }

    @Test
    fun `createPayTheoryData includes accountCode when specified`() {
        // Given configuration with accountCode
        every { mockConfiguration.accountCode } returns "account123"

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should contain accountCode
        assertEquals("account123", result["account_code"])
    }

    @Test
    fun `createPayTheoryData includes reference when specified`() {
        // Given configuration with reference
        every { mockConfiguration.reference } returns "ref123"

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should contain reference
        assertEquals("ref123", result["reference"])
    }

    @Test
    fun `createPayTheoryData uses test timezone when in test mode`() {
        // Given configuration in test mode
        every { mockConfiguration.isTestMode } returns true

        // When creating pay theory data
        val result = PaymentFormUtils.createPayTheoryData(mockConfiguration)

        // Then result should use "test" for timezone
        assertEquals("test", result["timezone"])
    }

    @Test
    fun `submitPaymentIfValid calls submitPayment when viewModel is valid`() {
        // Given valid viewModel
        every { mockViewModel.isValidAndReady } returns true
        every { mockViewModel.submitPayment() } returns Unit

        // When submitting payment
        val result = PaymentFormUtils.submitPaymentIfValid(mockViewModel)

        // Then submitPayment should be called and result should be true
        verify { mockViewModel.submitPayment() }
        assertTrue(result)
    }

    @Test
    fun `submitPaymentIfValid doesn't call submitPayment when viewModel is invalid`() {
        // Given invalid viewModel
        every { mockViewModel.isValidAndReady } returns false

        // When submitting payment
        val result = PaymentFormUtils.submitPaymentIfValid(mockViewModel)

        // Then submitPayment should not be called and result should be false
        verify(exactly = 0) { mockViewModel.submitPayment() }
        assertFalse(result)
    }
}
