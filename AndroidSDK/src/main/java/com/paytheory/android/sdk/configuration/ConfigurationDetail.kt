package com.paytheory.android.sdk.configuration

import com.paytheory.android.sdk.data.PayorInfo

/**
 * Enum class that contains the types of payments
 */
enum class TransactionType {
    CARD, BANK, CASH
}

/**
 * Enum class that contains the types of payment method tokenization
 */
enum class TokenizationType {
    CARD, BANK
}

/**
 * Object that holds the fee mode values
 */
object FeeMode {
    const val MERCHANT_FEE = "merchant_fee"
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
    var transactionType: TransactionType? = null,
    var tokenizationType: TokenizationType? = null,
    var requireAccountName: Boolean? = null,
    var requireBillingAddress: Boolean? = null,
    var confirmation: Boolean? = null,
    var feeMode: String? = null,
    var metadata: HashMap<Any, Any>? = null,
    var payorInfo: PayorInfo? = null,
    var payorId: String? = null,
    var accountCode: String? = null,
    var reference: String? = null,
    var paymentParameters: String? = null,
    var invoiceId: String? = null,
    var sendReceipt: Boolean? = null,
    var receiptDescription: String? = null,
)