package com.paytheory.android.sdk.configuration

/**
 * Enum class that contains the types of payments
 */
enum class TransactionType {
    CARD, BANK, CASH
}

/**
 * Object that holds the fee mode values
 */
object FeeMode {
    const val SURCHARGE = "surcharge"
    const val SERVICE_FEE = "service_fee"
}
/**
 * Data class that contains default configuration values
 * @param apiKey Pay Theory api-key
 * @param amount amount of transaction
 * @param requireAccountName value represents if account name field will be active
 * @param requireBillingAddress value represents if address field will be active
 * @param transactionType the type of transaction requested
 */
data class ConfigurationDetail(
    val apiKey: String = "",
    val amount: Int = 0,
    val transactionType: TransactionType = TransactionType.CARD,
    val requireAccountName: Boolean = false,
    val requireBillingAddress: Boolean = false,
    val requireConfirmation: Boolean = false,
    val feeMode: String = FeeMode.SURCHARGE,
    val sendReceipt: Boolean = false,
    val receiptDescription: String = ""
)