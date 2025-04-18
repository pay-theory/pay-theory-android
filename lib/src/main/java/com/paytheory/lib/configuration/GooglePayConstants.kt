package com.paytheory.lib.configuration

import com.google.android.gms.wallet.WalletConstants

/**
 * Constants class for Google Pay integration
 */
object GooglePayConstants {
    // Environment constants
    const val ENVIRONMENT_TEST = WalletConstants.ENVIRONMENT_TEST
    const val ENVIRONMENT_PRODUCTION = WalletConstants.ENVIRONMENT_PRODUCTION
    
    // Default supported card networks
    val DEFAULT_SUPPORTED_NETWORKS = listOf(
        "AMEX",
        "DISCOVER",
        "JCB",
        "MASTERCARD",
        "VISA"
    )
    
    // Only support CRYPTOGRAM_3DS initially as required by backend
    val DEFAULT_SUPPORTED_METHODS = listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"
    )
    
    // Gateway information used in tokenization parameters
    const val GATEWAY_NAME = "paytheory"
    const val GATEWAY_MERCHANT_ID = "BCR2DN4TZ342R7QF"
    
    // Country and currency codes
    const val COUNTRY_CODE = "US"
    const val CURRENCY_CODE = "USD"
} 