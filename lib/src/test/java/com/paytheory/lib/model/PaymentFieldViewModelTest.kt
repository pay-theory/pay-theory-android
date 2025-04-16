package com.paytheory.lib.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PaymentFieldViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentFieldViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateNameState sets correct state`() = runTest {
        // Initial state should be INIT
        assertEquals(FieldState.INIT, viewModel.nameState.first())

        // Update state to READY
        viewModel.updateNameState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.nameState.first())

        // Update state to INVALID
        viewModel.updateNameState(FieldState.INVALID)
        assertEquals(FieldState.INVALID, viewModel.nameState.first())

        // Update state to EMPTY
        viewModel.updateNameState(FieldState.EMPTY)
        assertEquals(FieldState.EMPTY, viewModel.nameState.first())
    }

    @Test
    fun `updateBankAccountState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.bankAccountState.first())
        viewModel.updateBankAccountState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.bankAccountState.first())
    }

    @Test
    fun `updateBankRoutingState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.bankRoutingState.first())
        viewModel.updateBankRoutingState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.bankRoutingState.first())
    }

    @Test
    fun `updateBankAccountTypeState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.bankAccountTypeState.first())
        viewModel.updateBankAccountTypeState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.bankAccountTypeState.first())
    }

    @Test
    fun `updateCardNumberState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.cardNumberState.first())
        viewModel.updateCardNumberState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cardNumberState.first())
    }

    @Test
    fun `updateCardCvcState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.cardCvcState.first())
        viewModel.updateCardCvcState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cardCvcState.first())
    }

    @Test
    fun `updateCardExpirationState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.cardExpirationState.first())
        viewModel.updateCardExpirationState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cardExpirationState.first())
    }

    @Test
    fun `updateAddressState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.addressState.first())
        viewModel.updateAddressState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.addressState.first())
    }

    @Test
    fun `updateCityState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.cityState.first())
        viewModel.updateCityState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cityState.first())
    }

    @Test
    fun `updateRegionState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.regionState.first())
        viewModel.updateRegionState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.regionState.first())
    }

    @Test
    fun `updatePostalCodeState sets correct state`() = runTest {
        assertEquals(FieldState.INIT, viewModel.postalCodeState.first())
        viewModel.updatePostalCodeState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.postalCodeState.first())
    }

    @Test
    fun `updateState routes to correct field state update`() = runTest {
        // Test routing for each field
        
        // Test NAME_ON_ACCOUNT
        viewModel.updateState(PaymentField.NAME_ON_ACCOUNT, FieldState.READY)
        assertEquals(FieldState.READY, viewModel.nameState.first())
        
        // Test BANK_ACCOUNT_NUMBER
        viewModel.updateState(PaymentField.BANK_ACCOUNT_NUMBER, FieldState.INVALID)
        assertEquals(FieldState.INVALID, viewModel.bankAccountState.first())
        
        // Test BANK_ROUTING_NUMBER
        viewModel.updateState(PaymentField.BANK_ROUTING_NUMBER, FieldState.EMPTY)
        assertEquals(FieldState.EMPTY, viewModel.bankRoutingState.first())
        
        // Test BANK_ACCOUNT_TYPE
        viewModel.updateState(PaymentField.BANK_ACCOUNT_TYPE, FieldState.READY)
        assertEquals(FieldState.READY, viewModel.bankAccountTypeState.first())
        
        // Test CARD_NUMBER
        viewModel.updateState(PaymentField.CARD_NUMBER, FieldState.INVALID)
        assertEquals(FieldState.INVALID, viewModel.cardNumberState.first())
        
        // Test CARD_EXPIRATION
        viewModel.updateState(PaymentField.CARD_EXPIRATION, FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cardExpirationState.first())
        
        // Test CARD_CVC
        viewModel.updateState(PaymentField.CARD_CVC, FieldState.EMPTY)
        assertEquals(FieldState.EMPTY, viewModel.cardCvcState.first())
        
        // Test ADDRESS_LINE1
        viewModel.updateState(PaymentField.ADDRESS_LINE1, FieldState.READY)
        assertEquals(FieldState.READY, viewModel.addressState.first())
        
        // Test CITY
        viewModel.updateState(PaymentField.CITY, FieldState.INVALID)
        assertEquals(FieldState.INVALID, viewModel.cityState.first())
        
        // Test REGION
        viewModel.updateState(PaymentField.REGION, FieldState.READY)
        assertEquals(FieldState.READY, viewModel.regionState.first())
        
        // Test POSTAL_CODE
        viewModel.updateState(PaymentField.POSTAL_CODE, FieldState.EMPTY)
        assertEquals(FieldState.EMPTY, viewModel.postalCodeState.first())
        
        // Test ADDRESS_LINE2 (should not throw exception)
        viewModel.updateState(PaymentField.ADDRESS_LINE2, FieldState.READY)
    }
} 