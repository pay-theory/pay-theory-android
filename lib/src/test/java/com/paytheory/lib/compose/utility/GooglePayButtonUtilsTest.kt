package com.paytheory.lib.compose.utility

import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for GooglePayButtonUtils
 */
class GooglePayButtonUtilsTest {

    @Test
    fun `mapButtonType maps all button types correctly`() {
        // Test each enum value mapping
        assertEquals(ButtonType.Pay, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.PAY))
        assertEquals(ButtonType.Checkout, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.CHECKOUT))
        assertEquals(ButtonType.Order, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.ORDER))
        assertEquals(ButtonType.Subscribe, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.SUBSCRIBE))
        assertEquals(ButtonType.Book, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.BOOK))
        assertEquals(ButtonType.Buy, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.BUY))
    }
    
    @Test
    fun `mapButtonTheme maps all button colors correctly`() {
        // Test each color mapping
        assertEquals(ButtonTheme.Dark, GooglePayButtonUtils.mapButtonTheme(GooglePayButtonColor.BLACK))
        assertEquals(ButtonTheme.Light, GooglePayButtonUtils.mapButtonTheme(GooglePayButtonColor.WHITE))
    }
    
    // Individual tests for better error reporting
    
    @Test
    fun `mapButtonType maps PAY correctly`() {
        assertEquals(ButtonType.Pay, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.PAY))
    }
    
    @Test
    fun `mapButtonType maps CHECKOUT correctly`() {
        assertEquals(ButtonType.Checkout, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.CHECKOUT))
    }
    
    @Test
    fun `mapButtonType maps ORDER correctly`() {
        assertEquals(ButtonType.Order, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.ORDER))
    }
    
    @Test
    fun `mapButtonType maps SUBSCRIBE correctly`() {
        assertEquals(ButtonType.Subscribe, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.SUBSCRIBE))
    }
    
    @Test
    fun `mapButtonType maps BOOK correctly`() {
        assertEquals(ButtonType.Book, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.BOOK))
    }
    
    @Test
    fun `mapButtonType maps BUY correctly`() {
        assertEquals(ButtonType.Buy, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.BUY))
    }
    
    @Test
    fun `mapButtonTheme maps BLACK correctly`() {
        assertEquals(ButtonTheme.Dark, GooglePayButtonUtils.mapButtonTheme(GooglePayButtonColor.BLACK))
    }
    
    @Test
    fun `mapButtonTheme maps WHITE correctly`() {
        assertEquals(ButtonTheme.Light, GooglePayButtonUtils.mapButtonTheme(GooglePayButtonColor.WHITE))
    }
} 