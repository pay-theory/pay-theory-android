package com.paytheory.lib.configuration

import com.google.android.gms.wallet.WalletConstants
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayEnumsTest {

    @Test
    fun `GooglePayEnvironment TEST should convert to WalletConstants ENVIRONMENT_TEST`() {
        // When
        val result = GooglePayEnvironment.TEST.toWalletConstant()
        
        // Then
        assertEquals(WalletConstants.ENVIRONMENT_TEST, result)
    }
    
    @Test
    fun `GooglePayEnvironment PRODUCTION should convert to WalletConstants ENVIRONMENT_PRODUCTION`() {
        // When
        val result = GooglePayEnvironment.PRODUCTION.toWalletConstant()
        
        // Then
        assertEquals(WalletConstants.ENVIRONMENT_PRODUCTION, result)
    }
    
    @Test
    fun `GooglePayButtonType enum should contain all required button types`() {
        // Test that all Google Pay button types are available in the enum
        val buttonTypes = listOf(
            GooglePayButtonType.PAY,
            GooglePayButtonType.CHECKOUT,
            GooglePayButtonType.ORDER,
            GooglePayButtonType.SUBSCRIBE,
            GooglePayButtonType.BOOK,
            GooglePayButtonType.BUY
        )
        
        // Verify the enum contains exactly the expected number of values
        assertEquals(buttonTypes.size, GooglePayButtonType.values().size)
        
        // Verify each expected value exists in the enum
        buttonTypes.forEach { buttonType ->
            val foundType = GooglePayButtonType.values().find { it == buttonType }
            assertEquals(buttonType, foundType)
        }
    }
    
    @Test
    fun `GooglePayButtonColor enum should contain BLACK and WHITE options`() {
        // Test that all Google Pay button colors are available in the enum
        val buttonColors = listOf(
            GooglePayButtonColor.BLACK,
            GooglePayButtonColor.WHITE
        )
        
        // Verify the enum contains exactly the expected number of values
        assertEquals(buttonColors.size, GooglePayButtonColor.values().size)
        
        // Verify each expected value exists in the enum
        buttonColors.forEach { buttonColor ->
            val foundColor = GooglePayButtonColor.values().find { it == buttonColor }
            assertEquals(buttonColor, foundColor)
        }
    }
    
    @Test
    fun `GooglePayBillingAddressFormat enum should contain MINIMAL and FULL options`() {
        // Test that all billing address format options are available in the enum
        val addressFormats = listOf(
            GooglePayBillingAddressFormat.MINIMAL,
            GooglePayBillingAddressFormat.FULL
        )
        
        // Verify the enum contains exactly the expected number of values
        assertEquals(addressFormats.size, GooglePayBillingAddressFormat.values().size)
        
        // Verify each expected value exists in the enum
        addressFormats.forEach { format ->
            val foundFormat = GooglePayBillingAddressFormat.values().find { it == format }
            assertEquals(format, foundFormat)
        }
    }
} 