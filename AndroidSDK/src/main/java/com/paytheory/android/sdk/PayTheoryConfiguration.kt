package com.paytheory.android.sdk

import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.PaymentMethodType
import com.paytheory.android.sdk.data.PayorInfo
import com.paytheory.android.sdk.view.PayTheoryButton

/**
 * Class that handles PayTheory configuration
 */
class PayTheoryConfiguration(
    /**
     * Button to initialize PayTheory
     */
    var payTheoryButton: PayTheoryButton,
    /**
     * API Key used to interact with PayTheory services
     */
    var apiKey: String,
) {
    /**
     * Amount of transaction in cents
     * Example: 100 represents $1.00
     */
    var amount: Int = 0
    /**
     * Payment method type (CARD, ACH, or CASH)
     * Default: CARD
     */
    var paymentMethodType: PaymentMethodType? = PaymentMethodType.CARD
    /**
     * Sets whether or not account name is required
     * Default: false
     */
    var requireAccountName: Boolean? = false
    /**
     * Sets whether or not billing address is required
     * Default: false
     */
    var requireBillingAddress: Boolean? = false
    /**
     * Sets whether or not to show confirmation dialog
     * Default: false
     */
    var confirmation: Boolean? = false
    /**
     * Fee mode (MERCHANT_FEE or BUYER_FEE)
     * Default: MERCHANT_FEE
     */
    var feeMode: String? = FeeMode.MERCHANT_FEE
    /**
     * Metadata to be associated with the transaction
     */
    var metadata: HashMap<Any, Any>? = HashMap()
    /**
     * Payor information
     */
    var payorInfo: PayorInfo? = PayorInfo()
    /**
     * Payor ID
     */
    var payorId: String? = null

    var skipTokenizeValidation: Boolean? = false
    var accountCode: String? = null
    var reference: String? = null
    var paymentParameters: String? = null
    var invoiceId: String? = null
    var sendReceipt: Boolean = false
    var receiptDescription: String = "Payment Confirmation"
    var serviceFee: Int = 0 //in cents
}