package com.paytheory.lib

import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.configuration.PaymentMethodAction
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.PayorInfo

private const val PAYTHEORYLAB = "paytheorylab"
private const val PAYTHEORYSTUDY = "paytheorystudy"
private const val PAYTHEORY = "paytheory"
private const val INVALID_APIKEY = "Invalid apikey"
private const val INVALID_AMOUNT = "Invalid amount"

/**
 * Class that handles PayTheory configuration for payment transactions.
 *
 * This class encapsulates all the necessary parameters for setting up a transaction
 * with PayTheory services. It includes details such as API key, transaction amount,
 * payment method, and various other optional settings.
 *
 * @property outlined Boolean indicating if the UI elements should be outlined (default: true).
 * @property apiKey API Key used to interact with PayTheory services. This is a required parameter.
 * @property amount Amount of the transaction in cents. Example: 100 represents $1.00.
 *                    Must be > 5 for PaymentMethodAction.PAYMENT. Not allowed for PaymentMethodAction.TOKEN.
 *                    Defaults to 0.
 * @property paymentMethodType Payment method type (CARD, ACH, or CASH). Default: CARD.
 * @property paymentMethodAction Payment action type (PAYMENT or TOKENIZE). Default: PAYMENT.
 * @property requireAccountName Boolean indicating if the account name is required. Default: false.
 * @property requireBillingAddress Boolean indicating if the billing address is required. Default: false.
 * @property feeMode Fee mode (MERCHANT_FEE or BUYER_FEE). Default: MERCHANT_FEE.
 * @property metadata Metadata to be associated with the transaction. Default: empty HashMap.
 * @property payorInfo Payor information. Default: null.
 * @property payorId Payor ID. Default: empty string.
 * @property skipTokenizeValidation Boolean to skip the validation of tokenization. Default: false.
 * @property accountCode Account code. Default: empty string.
 * @property reference Reference information for the transaction. Default: empty string.
 * @property paymentParameters Payment parameters. Default: empty string.
 * @property invoiceId Invoice ID. Default: empty string.
 * @property sendReceipt Boolean to determine if a receipt should be sent. Default: false.
 * @property receiptDescription Description for the receipt. Default: "Payment Confirmation".
 * @property serviceFee Service fee amount in cents. Default: 0.
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
     * must be > 5 for PaymentMethodAction.PAYMENT
     * is not allowed for PaymentMethodAction.TOKEN
     */
    val amount: Int = 0,
    /**
     * Payment method type (CARD, ACH, or CASH)
     * Default: CARD
     */
    val paymentMethodType: PaymentMethodType = PaymentMethodType.CARD,
    /**
     * Payment action type (PAYMENT or TOKENIZE)
     * Default: CARD
     */
    val paymentMethodAction: PaymentMethodAction = PaymentMethodAction.PAYMENT,
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
    /**
     * The partner name, extracted from the apiKey during initialization.
     */
    var partnerName: String = ""

    /**
     * The stage name, extracted from the apiKey during initialization.
     */
    var stageName: String = ""

    /**
     * The base path for the API, constructed from partnerName and stageName during initialization.
     */
    var apiBasePath: String = ""

    /**
     * Initializes the configuration and sets the partnerName, stageName, and apiBasePath.
     *
     * It also validates the payment configuration and extracts the details from the provided apiKey.
     */
    init {
        val details = validatePaymentConfigAndExtractDetails(this)
        partnerName = details.first
        stageName = details.second
        apiBasePath = "https://$partnerName.$stageName.com/"
    }

    /**
     * Validates the payment configuration and extracts the partner name and stage name from the apiKey.
     *
     * @param configuration The PayTheoryConfiguration object to validate.
     * @return A Pair containing the partner name and stage name.
     * @throws IllegalArgumentException If the apiKey is invalid or the amount is invalid for the specified payment method action.
     */
    private fun validatePaymentConfigAndExtractDetails(configuration: PayTheoryConfiguration): Pair<String, String> {
        val partnerName = configuration.apiKey.substring(0, configuration.apiKey.indexOf('-'))
        val stageName =
            configuration.apiKey.substring(configuration.apiKey.indexOf('-') + 1, configuration.apiKey.indexOf('-', configuration.apiKey.indexOf('-') + 1))

        if (stageName != PAYTHEORYLAB && stageName != PAYTHEORYSTUDY && stageName != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (amount < 10 && paymentMethodAction == PaymentMethodAction.PAYMENT) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }
        if (amount > 0 && paymentMethodAction == PaymentMethodAction.TOKEN) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }
        return Pair(partnerName, stageName)
    }

}