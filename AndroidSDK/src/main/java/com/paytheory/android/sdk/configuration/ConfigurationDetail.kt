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
    const val INTERCHANGE = "surcharge"
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
    val confirmation: Boolean = false,
    val feeMode: String = FeeMode.INTERCHANGE,
    val sendReceipt: Boolean = false,
    val receiptDescription: String = ""

//            TODO
//    apiKey: String,
//    amount: Int,
//    transactionType: TransactionType,
//    requireAccountName: Boolean = false,
//    requireBillingAddress: Boolean = false,
//    confirmation: Boolean = false,
//    feeMode: String = FeeMode.INTERCHANGE,
//    metadata: HashMap<Any, Any> = hashMapOf(),
//    payorInfo: PayorInfo = PayorInfo(),
//    payorId: String? = null,
//    accountCode: String? = null,
//    reference: String? = null,
//    paymentParameters: String? = null,
//    invoiceId: String? = null,
//    sendReceipt: Boolean = false,
//    receiptDescription: String = ""
)