package com.paytheory.android.sdk.configuration

/**
 * Enum class that contains the types of payments
 */
enum class PaymentType {
    CREDIT, BANK, CASH
}

/**
 * Object that holds the fee mode values
 */
object FeeMode {
    val SURCHARGE = "surcharge"
    val SERVICE_FEE = "service_fee"
}
/**
 * Data class that contains default configuration values
 * @param apiKey Pay Theory api-key
 * @param amount amount of transaction
 * @param requireAccountName value represents if account name field will be active
 * @param requireAddress value represents if address field will be active
 * @param paymentType the type of payment that will be transacted
 */
data class ConfigurationDetail(
    val apiKey: String = "",
    val amount: Int = 0,
    val requireAccountName: Boolean = true,
    val requireAddress: Boolean = true,
    val paymentType: PaymentType = PaymentType.CREDIT,
    val feeMode: String = FeeMode.SURCHARGE
)