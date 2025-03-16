package com.paytheory.lib.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PaymentFieldViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: PaymentFieldViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        viewModel = PaymentFieldViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun getAllStates(): List<FieldState> = listOf(
        viewModel.nameState.value,
        viewModel.bankAccountState.value,
        viewModel.bankRoutingState.value,
        viewModel.bankAccountTypeState.value,
        viewModel.cardNumberState.value,
        viewModel.cardCvcState.value,
        viewModel.cardExpirationState.value,
        viewModel.addressState.value,
        viewModel.cityState.value,
        viewModel.regionState.value,
        viewModel.postalCodeState.value
    )

    @Test
    fun `initial state of all fields is INIT`() {
        getAllStates().forEach { state ->
            assertEquals(FieldState.INIT, state)
        }
    }

    @Test
    fun `updateNameState updates nameState`() = runTest {
        val newState = FieldState.READY
        viewModel.updateNameState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.nameState.value)
    }

    @Test
    fun `updateBankAccountState updates bankAccountState`() = runTest {
        val newState = FieldState.INVALID
        viewModel.updateBankAccountState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.bankAccountState.value)
    }

    @Test
    fun `updateBankRoutingState updates bankRoutingState`() = runTest {
        val newState = FieldState.EMPTY
        viewModel.updateBankRoutingState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.bankRoutingState.value)
    }

    @Test
    fun `updateBankAccountTypeState updates bankAccountTypeState`() = runTest {
        val newState = FieldState.READY
        viewModel.updateBankAccountTypeState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.bankAccountTypeState.value)
    }

    @Test
    fun `updateCardNumberState updates cardNumberState`() = runTest {
        val newState = FieldState.INVALID
        viewModel.updateCardNumberState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.cardNumberState.value)
    }

    @Test
    fun `updateCardCvcState updates cardCvcState`() = runTest {
        val newState = FieldState.EMPTY
        viewModel.updateCardCvcState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.cardCvcState.value)
    }

    @Test
    fun `updateCardExpirationState updates cardExpirationState`() = runTest {
        val newState = FieldState.READY
        viewModel.updateCardExpirationState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.cardExpirationState.value)
    }

    @Test
    fun `updateAddressState updates addressState`() = runTest {
        val newState = FieldState.INVALID
        viewModel.updateAddressState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.addressState.value)
    }

    @Test
    fun `updateCityState updates cityState`() = runTest {
        val newState = FieldState.EMPTY
        viewModel.updateCityState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.cityState.value)
    }

    @Test
    fun `updateRegionState updates regionState`() = runTest {
        val newState = FieldState.READY
        viewModel.updateRegionState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.regionState.value)
    }

    @Test
    fun `updatePostalCodeState updates postalCodeState`() = runTest {
        val newState = FieldState.INVALID
        viewModel.updatePostalCodeState(newState)
        advanceUntilIdle()
        assertEquals(newState, viewModel.postalCodeState.value)
    }

    @Test
    fun `updateState with NAME_ON_ACCOUNT updates nameState`() = runTest {
        viewModel.updateState(PaymentField.NAME_ON_ACCOUNT, FieldState.READY)
        advanceUntilIdle()
        assertEquals(FieldState.READY, viewModel.nameState.value)
    }

    @Test
    fun `updateState with BANK_ACCOUNT_NUMBER updates bankAccountState`() = runTest {
        viewModel.updateState(PaymentField.BANK_ACCOUNT_NUMBER, FieldState.INVALID)
        advanceUntilIdle()
        assertEquals(FieldState.INVALID, viewModel.bankAccountState.value)
    }

    @Test
    fun `updateState with BANK_ROUTING_NUMBER updates bankRoutingState`() = runTest {
        viewModel.updateState(PaymentField.BANK_ROUTING_NUMBER, FieldState.EMPTY)
        advanceUntilIdle()
        assertEquals(FieldState.EMPTY, viewModel.bankRoutingState.value)
    }

    @Test
    fun `updateState with CARD_NUMBER updates cardNumberState`() = runTest {
        viewModel.updateState(PaymentField.CARD_NUMBER, FieldState.READY)
        advanceUntilIdle()
        assertEquals(FieldState.READY, viewModel.cardNumberState.value)
    }

    @Test
    fun `updateState with CARD_EXPIRATION updates cardExpirationState`() = runTest {
        viewModel.updateState(PaymentField.CARD_EXPIRATION, FieldState.INVALID)
        advanceUntilIdle()
        assertEquals(FieldState.INVALID, viewModel.cardExpirationState.value)
    }

    @Test
    fun `updateState with CARD_CVC updates cardCvcState`() = runTest {
        viewModel.updateState(PaymentField.CARD_CVC, FieldState.EMPTY)
        advanceUntilIdle()
        assertEquals(FieldState.EMPTY, viewModel.cardCvcState.value)
    }

    @Test
    fun `updateState with ADDRESS_LINE1 updates addressState`() = runTest {
        viewModel.updateState(PaymentField.ADDRESS_LINE1, FieldState.READY)
        advanceUntilIdle()
        assertEquals(FieldState.READY, viewModel.addressState.value)
    }

    @Test
    fun `updateState with CITY updates cityState`() = runTest {
        viewModel.updateState(PaymentField.CITY, FieldState.INVALID)
        advanceUntilIdle()
        assertEquals(FieldState.INVALID, viewModel.cityState.value)
    }

    @Test
    fun `updateState with REGION updates regionState`() = runTest {
        viewModel.updateState(PaymentField.REGION, FieldState.EMPTY)
        advanceUntilIdle()
        assertEquals(FieldState.EMPTY, viewModel.regionState.value)
    }

    @Test
    fun `updateState with POSTAL_CODE updates postalCodeState`() = runTest {
        viewModel.updateState(PaymentField.POSTAL_CODE, FieldState.READY)
        advanceUntilIdle()
        assertEquals(FieldState.READY, viewModel.postalCodeState.value)
    }

    @Test
    fun `updateState with BANK_ACCOUNT_TYPE updates bankAccountTypeState`() = runTest {
        viewModel.updateState(PaymentField.BANK_ACCOUNT_TYPE, FieldState.INVALID)
        advanceUntilIdle()
        assertEquals(FieldState.INVALID, viewModel.bankAccountTypeState.value)
    }

    @Test
    fun `updateState with unhandled field does nothing`() = runTest {
        val initialState = getAllStates()
        viewModel.updateState(PaymentField.ADDRESS_LINE2, FieldState.READY)
        advanceUntilIdle()
        val updatedState = getAllStates()
        assertEquals(initialState, updatedState)
    }
}