package com.paytheory.lib.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.Payment
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.ErrorCode
import com.paytheory.lib.data.PTError
import com.paytheory.lib.data.SuccessfulTransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class PaymentViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PaymentViewModel
    private lateinit var mockPayable: Payable
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockPayment: Payment

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockPayable = mock(Payable::class.java)
        val config = PayTheoryConfiguration(
            apiKey = "test-paytheory-apikey",
            amount = 1000,
            paymentMethodType = PaymentMethodType.CARD
        )
        mockPayment = mock(Payment::class.java)
        `when`(mockPayment.payable).thenReturn(mockPayable)

        viewModel = PaymentViewModel("test.package", config, mockPayable)
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        assertEquals(PaymentViewModel.PaymentState.Loading, viewModel.paymentState.first())
        assertNotEquals(PaymentViewModel.PaymentState.Success("12345"), viewModel.paymentState.first())
    }

    @Test
    fun `propagateState updates field state correctly`() {
        // Test EMPTY state transition
        propagateState(mockPayment, PaymentField.CARD_NUMBER, false, true)
        assertEquals(
            FieldState.EMPTY,
            paymentFieldState[PaymentField.CARD_NUMBER]
        )

        // Test READY state transition

        propagateState(mockPayment, PaymentField.CARD_NUMBER, true, false)
        assertEquals(
            FieldState.READY,
            paymentFieldState[PaymentField.CARD_NUMBER]
        )

        // Test INVALID state transition
        propagateState(mockPayment, PaymentField.CARD_NUMBER, false, false)
         assertEquals(
            FieldState.INVALID,
            paymentFieldState[PaymentField.CARD_NUMBER]
        )
    }

    @Test
    fun `submitPayment handles already in progress state`() {
        viewModel._paymentState.value = PaymentViewModel.PaymentState.Processing
        viewModel.submitPayment()
        verify(mockPayable).handleError(
            PTError(
                ErrorCode.ActionInProgress,
                "Payment already in progress"
            )
        )
    }

    @Test
    fun `clearSensitiveData resets all fields`() {
        // Set some data
        viewModel.updateCardNumber(SecureStringWrapper(SecureString("4111111111111111"),null))
        viewModel.clearSensitiveData()

        assertEquals("", viewModel.cardNumber.value.visibleValue)
        assertEquals(1, viewModel.clearCount.intValue)
        assertTrue(
            paymentFieldState.values.all { it == FieldState.INIT }
        )
    }

    @Test
    fun `paymentSuccess updates state correctly`() = runTest {
        val result = SuccessfulTransactionResult(
            "success", "1000", "Visa", "1111", "0",
            "USD", null, "12345", "2023-01-01", "pm_123", "pay_123"
        )
        viewModel.paymentSuccess(result)
        assertEquals(
            PaymentViewModel.PaymentState.Success("12345"),
            viewModel.paymentState.first()
        )
    }

    @Test
    fun `updateAddressLine2 does not trigger validation`() = runTest {
        val initialPaymentState = viewModel.paymentState.value
        viewModel.updateAddressLine2(SecureStringWrapper(SecureString("Test"),null))
        advanceUntilIdle()
        assertEquals(initialPaymentState, viewModel.paymentState.value)
    }

    @Test
    fun `isValidAccountAddress checks all address components`() {
        viewModel.updateAddressLine1(SecureStringWrapper(SecureString("Street"),null))
        viewModel.updateCity(SecureStringWrapper(SecureString("City"),null))
        viewModel.updateRegion(SecureStringWrapper(SecureString("State"),null))
        viewModel.updatePostalCode(SecureStringWrapper(SecureString("Zip"),null))

        assertTrue(viewModel.isValidAccountAddress())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}