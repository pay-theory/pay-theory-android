package com.paytheory.lib
import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.PayorInfo

private const val PAYTHEORYLAB = "paytheorylab"
private const val PAYTHEORYSTUDY = "paytheorystudy"
private const val PAYTHEORY = "paytheory"
private const val INVALID_APIKEY = "Invalid apikey"
private const val INVALID_AMOUNT = "Invalid amount"

/**
 * Class that handles PayTheory configuration
 */
class PayTheoryConfiguration(
    val outlined: Boolean = true,
    /**
     * API Key used to interact with PayTheory services
     */
    val apiKey: String,
    /**
     * Amount of transaction in cents
     * Example: 100 represents $1.00
     */
    val amount: Int,
    /**
     * Payment method type (CARD, ACH, or CASH)
     * Default: CARD
     */
    val paymentMethodType: PaymentMethodType = PaymentMethodType.CARD,
    /**
     * Sets whether or not account name is required
     * Default: false
     */
    var requireAccountName: Boolean = false,
    /**
     * Sets whether or not billing address is required
     * Default: false
     */
    var requireBillingAddress: Boolean = false,
    /**
     * Fee mode (MERCHANT_FEE or BUYER_FEE)
     * Default: MERCHANT_FEE
     */
    var feeMode: String = FeeMode.MERCHANT_FEE,
    /**
     * Metadata to be associated with the transaction
     */
    var metadata: HashMap<Any, Any> = HashMap(),
    /**
     * Payor information
     */
    var payorInfo: PayorInfo? = null,
    /**
     * Payor ID
     */
    var payorId: String = "",

    var skipTokenizeValidation: Boolean = false,
    var accountCode: String = "",
    var reference: String = "",
    var paymentParameters: String = "",
    var invoiceId: String = "",
    var sendReceipt: Boolean = false,
    var receiptDescription: String = "Payment Confirmation",
    var serviceFee: Int = 0 //in cents
) {
    var partnerName: String = ""
    var stageName: String = ""
    var apiBasePath: String = ""
    init {
         val details = validatePaymentConfigAndExtractDetails(this)
         partnerName = details.first
         stageName = details.second
         apiBasePath = "https://$partnerName.$stageName.com/"
    }
    private fun validatePaymentConfigAndExtractDetails(configuration: PayTheoryConfiguration): Pair<String, String> {
        val partnerName = configuration.apiKey.substring(0, configuration.apiKey.indexOf('-'))
        val stageName =
            configuration.apiKey.substring(configuration.apiKey.indexOf('-') + 1, configuration.apiKey.indexOf('-', configuration.apiKey.indexOf('-') + 1))

        if (stageName != PAYTHEORYLAB && stageName != PAYTHEORYSTUDY && stageName != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (configuration.amount <= 0) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }
        return Pair(partnerName, stageName)
    }

}