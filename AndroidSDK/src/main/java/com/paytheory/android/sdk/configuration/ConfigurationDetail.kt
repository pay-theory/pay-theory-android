package com.paytheory.android.sdk.configuration

import PayorInfo

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
    const val INTERCHANGE = "interchange"
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
    var apiKey: String? = null,
    var amount: Int? = null,
    var transactionType: TransactionType? = TransactionType.CARD,
    var requireAccountName: Boolean? = false,
    var requireBillingAddress: Boolean? = false,
    var confirmation: Boolean? = false,
    var feeMode: String? = FeeMode.INTERCHANGE,
    var metadata: HashMap<Any, Any>? = HashMap(),
    var payorInfo: PayorInfo? = PayorInfo(),
    var payorId: String? = null,
    var accountCode: String? = null,
    var reference: String? = null,
    var paymentParameters: String? = null,
    var invoiceId: String? = null,
    var sendReceipt: Boolean? = false,
    var receiptDescription: String? = null,
)