package com.paytheory.lib.configuration

import com.google.android.gms.wallet.WalletConstants

/**
 * Enum for Google Pay environment settings
 */
enum class GooglePayEnvironment {
    TEST,
    PRODUCTION;
    
    /**
     * Converts the enum value to WalletConstants integer value
     */
    fun toWalletConstant(): Int {
        return when (this) {
            TEST -> WalletConstants.ENVIRONMENT_TEST
            PRODUCTION -> WalletConstants.ENVIRONMENT_PRODUCTION
        }
    }
}

/**
 * Enum for Google Pay button text types
 */
enum class GooglePayButtonType {
    PAY,
    CHECKOUT,
    ORDER,
    SUBSCRIBE,
    BOOK,
    BUY
}

/**
 * Enum for Google Pay button color options
 */
enum class GooglePayButtonColor {
    BLACK,  // Default
    WHITE
}

/**
 * Enum for Google Pay billing address format requirements
 */
enum class GooglePayBillingAddressFormat {
    MINIMAL,  // Just postal code
    FULL      // Complete address
} 