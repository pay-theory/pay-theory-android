package com.paytheory.lib.configuration

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

/**
 * Tests for PaymentMethodType and PaymentMethodAction enums
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class PaymentMethodEnumsTest {

    @Test
    fun `PaymentMethodType enum contains expected values`() {
        // Verify the enum contains all expected values
        val expectedTypes = listOf("CARD", "ACH")
        
        assertEquals(expectedTypes.size, PaymentMethodType.values().size)
        
        expectedTypes.forEach { expectedType ->
            val enumValue = PaymentMethodType.values().find { it.name == expectedType }
            assertNotNull("PaymentMethodType should contain $expectedType", enumValue)
        }
    }
    
    @Test
    fun `PaymentMethodAction enum contains expected values`() {
        // Verify the enum contains all expected values
        val expectedActions = listOf("PAYMENT", "TOKEN")
        
        assertEquals(expectedActions.size, PaymentMethodAction.values().size)
        
        expectedActions.forEach { expectedAction ->
            val enumValue = PaymentMethodAction.values().find { it.name == expectedAction }
            assertNotNull("PaymentMethodAction should contain $expectedAction", enumValue)
        }
    }
    
    @Test
    fun `PaymentMethodType values are correctly defined`() {
        assertEquals(PaymentMethodType.CARD, PaymentMethodType.valueOf("CARD"))
        assertEquals(PaymentMethodType.ACH, PaymentMethodType.valueOf("ACH"))
        
        // Test ordinals to ensure proper ordering
        assertEquals(0, PaymentMethodType.CARD.ordinal)
        assertEquals(1, PaymentMethodType.ACH.ordinal)
    }
    
    @Test
    fun `PaymentMethodAction values are correctly defined`() {
        assertEquals(PaymentMethodAction.PAYMENT, PaymentMethodAction.valueOf("PAYMENT"))
        assertEquals(PaymentMethodAction.TOKEN, PaymentMethodAction.valueOf("TOKEN"))
        
        // Test ordinals to ensure proper ordering
        assertEquals(0, PaymentMethodAction.TOKEN.ordinal)
        assertEquals(1, PaymentMethodAction.PAYMENT.ordinal)
    }
} 