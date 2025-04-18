package com.paytheory.lib.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    // ENHANCED FLOW TESTING

    @Test
    fun `test field state transitions`() = runTest {
        // Initial state should be INIT
        assertEquals(FieldState.INIT, viewModel.nameState.first())
        
        // Update to EMPTY
        viewModel.updateNameState(FieldState.EMPTY)
        assertEquals(FieldState.EMPTY, viewModel.nameState.first())
        
        // Update to INVALID
        viewModel.updateNameState(FieldState.INVALID)
        assertEquals(FieldState.INVALID, viewModel.nameState.first())
        
        // Update to READY
        viewModel.updateNameState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.nameState.first())
        
        // Update back to INIT
        viewModel.updateNameState(FieldState.INIT)
        assertEquals(FieldState.INIT, viewModel.nameState.first())
    }
    
    @Test
    fun `test multiple field updates simultaneously`() = runTest {
        // Check initial states
        assertEquals(FieldState.INIT, viewModel.nameState.first())
        assertEquals(FieldState.INIT, viewModel.cardNumberState.first())
        assertEquals(FieldState.INIT, viewModel.postalCodeState.first())
        
        // Update all fields
        viewModel.updateNameState(FieldState.READY)
        viewModel.updateCardNumberState(FieldState.INVALID)
        viewModel.updatePostalCodeState(FieldState.EMPTY)
        
        // Check updated states
        assertEquals(FieldState.READY, viewModel.nameState.first())
        assertEquals(FieldState.INVALID, viewModel.cardNumberState.first())
        assertEquals(FieldState.EMPTY, viewModel.postalCodeState.first())
    }
    
    @Test
    fun `reset all fields to INIT state`() = runTest {
        // First set all fields to various states
        viewModel.updateNameState(FieldState.READY)
        viewModel.updateCardNumberState(FieldState.INVALID)
        viewModel.updateCardCvcState(FieldState.EMPTY)
        viewModel.updateAddressState(FieldState.READY)
        
        // Verify states were set
        assertEquals(FieldState.READY, viewModel.nameState.first())
        assertEquals(FieldState.INVALID, viewModel.cardNumberState.first())
        assertEquals(FieldState.EMPTY, viewModel.cardCvcState.first())
        assertEquals(FieldState.READY, viewModel.addressState.first())
        
        // Now reset all fields by calling updateState for each PaymentField
        for (field in PaymentField.entries) {
            viewModel.updateState(field, FieldState.INIT)
        }
        
        // Verify all fields were reset to INIT
        assertEquals(FieldState.INIT, viewModel.nameState.first())
        assertEquals(FieldState.INIT, viewModel.cardNumberState.first())
        assertEquals(FieldState.INIT, viewModel.cardCvcState.first())
        assertEquals(FieldState.INIT, viewModel.cardExpirationState.first())
        assertEquals(FieldState.INIT, viewModel.addressState.first())
        assertEquals(FieldState.INIT, viewModel.cityState.first())
        assertEquals(FieldState.INIT, viewModel.regionState.first())
        assertEquals(FieldState.INIT, viewModel.postalCodeState.first())
        assertEquals(FieldState.INIT, viewModel.bankAccountState.first())
        assertEquals(FieldState.INIT, viewModel.bankRoutingState.first())
        assertEquals(FieldState.INIT, viewModel.bankAccountTypeState.first())
    }

    // ORIGINAL TESTS

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

    // COMPREHENSIVE STATE TRANSITION TESTING
    
    @Test
    fun `test all field state transitions`() = runTest {
        // Define all possible field states
        val allStates = listOf(FieldState.INIT, FieldState.EMPTY, FieldState.INVALID, FieldState.READY)
        
        // Test each field with all possible state transitions
        for (field in PaymentField.entries) {
            for (state in allStates) {
                viewModel.updateState(field, state)
                
                // Verify state was updated correctly for each field
                when (field) {
                    PaymentField.NAME_ON_ACCOUNT -> assertEquals(state, viewModel.nameState.first())
                    PaymentField.BANK_ACCOUNT_NUMBER -> assertEquals(state, viewModel.bankAccountState.first())
                    PaymentField.BANK_ROUTING_NUMBER -> assertEquals(state, viewModel.bankRoutingState.first())
                    PaymentField.BANK_ACCOUNT_TYPE -> assertEquals(state, viewModel.bankAccountTypeState.first())
                    PaymentField.CARD_NUMBER -> assertEquals(state, viewModel.cardNumberState.first())
                    PaymentField.CARD_EXPIRATION -> assertEquals(state, viewModel.cardExpirationState.first())
                    PaymentField.CARD_CVC -> assertEquals(state, viewModel.cardCvcState.first())
                    PaymentField.ADDRESS_LINE1 -> assertEquals(state, viewModel.addressState.first())
                    PaymentField.CITY -> assertEquals(state, viewModel.cityState.first())
                    PaymentField.REGION -> assertEquals(state, viewModel.regionState.first())
                    PaymentField.POSTAL_CODE -> assertEquals(state, viewModel.postalCodeState.first())
                    // ADDRESS_LINE2 doesn't have a corresponding state field
                    else -> { /* No assertion needed for ADDRESS_LINE2 */ }
                }
            }
        }
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
    
    // EDGE CASE TESTING
    
    @Test
    fun `rapid sequential updates are processed correctly`() = runTest {
        // Check initial state
        assertEquals(FieldState.INIT, viewModel.cardNumberState.first())
        
        // Perform rapid updates
        viewModel.updateCardNumberState(FieldState.EMPTY)
        assertEquals(FieldState.EMPTY, viewModel.cardNumberState.first())
        
        viewModel.updateCardNumberState(FieldState.INVALID)
        assertEquals(FieldState.INVALID, viewModel.cardNumberState.first())
        
        viewModel.updateCardNumberState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cardNumberState.first())
        
        viewModel.updateCardNumberState(FieldState.EMPTY)
        assertEquals(FieldState.EMPTY, viewModel.cardNumberState.first())
        
        viewModel.updateCardNumberState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cardNumberState.first())
    }
    
    @Test
    fun `duplicate state updates are still processed`() = runTest {
        // Check initial state
        assertEquals(FieldState.INIT, viewModel.cityState.first())
        
        // Update to READY
        viewModel.updateCityState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cityState.first())
        
        // Update to READY again (should still update the flow)
        viewModel.updateCityState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cityState.first())
        
        // Update to READY again (should still update the flow)
        viewModel.updateCityState(FieldState.READY)
        assertEquals(FieldState.READY, viewModel.cityState.first())
    }
} 