package com.paytheory.lib.configuration

/**
 * Enum class that contains the types of payments
 */
enum class PaymentMethodType {
    CARD, ACH
}

/**
 * Enum class that contains the types of payments
 */
enum class PaymentMethodAction {
    TOKEN, PAYMENT
}


/**
 * Object that holds the fee mode values
 */
object FeeMode {
    const val MERCHANT_FEE = "merchant_fee"
}
