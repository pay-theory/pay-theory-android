package com.paytheory.lib.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for model data structures and enums
 */
class ModelDataTest {

    @Before
    fun setup() {
        // Reset the global maps before each test
        for (field in PaymentField.entries) {
            paymentFieldState[field] = FieldState.INIT  // Reset to initial state
            paymentFieldValid[field] = true  // Reset to default true
            paymentFieldEmpty[field] = false // Reset to default false
        }
    }

    @Test
    fun `verify PaymentField enum contains all expected values`() {
        // Verify all PaymentField enum values exist
        val expectedFields = listOf(
            PaymentField.NAME_ON_ACCOUNT,
            PaymentField.BANK_ACCOUNT_NUMBER,
            PaymentField.BANK_ROUTING_NUMBER,
            PaymentField.BANK_ACCOUNT_TYPE,
            PaymentField.CARD_NUMBER,
            PaymentField.CARD_EXPIRATION,
            PaymentField.CARD_CVC,
            PaymentField.ADDRESS_LINE1,
            PaymentField.ADDRESS_LINE2,
            PaymentField.CITY,
            PaymentField.REGION,
            PaymentField.POSTAL_CODE
        )
        
        assertEquals(expectedFields.size, PaymentField.entries.size)
        for (field in expectedFields) {
            assertTrue("Missing expected field: $field", field in PaymentField.entries)
        }
    }
    
    @Test
    fun `verify FieldState enum contains all expected values`() {
        // Verify all FieldState enum values exist
        val expectedStates = listOf(
            FieldState.EMPTY,
            FieldState.READY,
            FieldState.INVALID,
            FieldState.INIT
        )
        
        assertEquals(expectedStates.size, FieldState.entries.size)
        for (state in expectedStates) {
            assertTrue("Missing expected state: $state", state in FieldState.entries)
        }
    }
    
    @Test
    fun `verify BankFields enum contains all expected values`() {
        // Verify all BankFields enum values exist
        val expectedFields = listOf(
            BankFields.NAME_ON_ACCOUNT,
            BankFields.BANK_ACCOUNT_NUMBER,
            BankFields.BANK_ROUTING_NUMBER,
            BankFields.BANK_ACCOUNT_TYPE
        )
        
        assertEquals(expectedFields.size, BankFields.entries.size)
        for (field in expectedFields) {
            assertTrue("Missing expected field: $field", field in BankFields.entries)
        }
    }
    
    @Test
    fun `verify CreditCardFields enum contains all expected values`() {
        // Verify all CreditCardFields enum values exist
        val expectedFields = listOf(
            CreditCardFields.CARD_NUMBER,
            CreditCardFields.CARD_EXPIRATION,
            CreditCardFields.CARD_CVC
        )
        
        assertEquals(expectedFields.size, CreditCardFields.entries.size)
        for (field in expectedFields) {
            assertTrue("Missing expected field: $field", field in CreditCardFields.entries)
        }
    }
    
    @Test
    fun `verify AddressFields enum contains all expected values`() {
        // Verify all AddressFields enum values exist
        val expectedFields = listOf(
            AddressFields.ADDRESS_LINE1,
            AddressFields.CITY,
            AddressFields.REGION,
            AddressFields.POSTAL_CODE
        )
        
        assertEquals(expectedFields.size, AddressFields.entries.size)
        for (field in expectedFields) {
            assertTrue("Missing expected field: $field", field in AddressFields.entries)
        }
    }
    
    @Test
    fun `verify paymentFieldValid map contains all PaymentField entries`() {
        // Ensure the global hash map has an entry for each PaymentField
        assertEquals(PaymentField.entries.size, paymentFieldValid.size)
        
        for (field in PaymentField.entries) {
            assertTrue("Map missing entry for $field", paymentFieldValid.containsKey(field))
            // Verify default value is true
            assertTrue(paymentFieldValid[field]!!)
        }
    }
    
    @Test
    fun `verify paymentFieldEmpty map contains all PaymentField entries`() {
        // Ensure the global hash map has an entry for each PaymentField
        assertEquals(PaymentField.entries.size, paymentFieldEmpty.size)
        
        for (field in PaymentField.entries) {
            assertTrue("Map missing entry for $field", paymentFieldEmpty.containsKey(field))
        }
    }
    
    @Test
    fun `verify paymentFieldState map contains all PaymentField entries`() {
        // Ensure the global hash map has an entry for each PaymentField
        assertEquals(PaymentField.entries.size, paymentFieldState.size)
        
        for (field in PaymentField.entries) {
            assertTrue("Map missing entry for $field", paymentFieldState.containsKey(field))
            // Verify default state is INIT
            assertEquals(FieldState.INIT, paymentFieldState[field])
        }
    }
} 