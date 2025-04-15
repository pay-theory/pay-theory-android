package com.paytheory.lib

import com.paytheory.lib.configuration.*
import com.paytheory.lib.data.PayorInfo

private const val PAYTHEORYLAB = "paytheorylab"
private const val PAYTHEORYSTUDY = "paytheorystudy"
private const val PAYTHEORY = "paytheory"
private const val INVALID_APIKEY = "Invalid apikey"
private const val INVALID_AMOUNT = "Invalid amount"
private const val INVALID_GOOGLEPAY_MERCHANT_NAME = "Merchant name is required for Google Pay"
private const val INVALID_GOOGLEPAY_AUTH_METHOD = "CRYPTOGRAM_3DS authentication method is required for Google Pay"

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
 * @property googlePayEnabled Boolean flag to enable/disable Google Pay support. Default: false.
 * @property googlePayMerchantName String displayed in Google Pay payment sheet. Default: null.
 * @property googlePayAllowPrepaidCards Boolean to control acceptance of prepaid cards. Default: true.
 * @property googlePayAllowCreditCards Boolean to control acceptance of credit cards. Default: true.
 * @property googlePayBillingAddressRequired Boolean requiring billing address. Default: false.
 * @property googlePayBillingAddressFormat Format requirements for billing address. Default: MINIMAL.
 * @property googlePayShippingAddressRequired Boolean requiring shipping address. Default: false.
 * @property googlePayPhoneNumberRequired Boolean requiring phone number. Default: false.
 * @property googlePayButtonType Button text options. Default: PAY.
 * @property googlePayButtonColor Button color scheme. Default: BLACK.
 * @property googlePayEnvironment TEST or PRODUCTION environment selection. Default: TEST.
 * @property googlePayAllowedCardNetworks List of supported card networks. Default: DEFAULT_SUPPORTED_NETWORKS.
 * @property googlePaySupportedMethods Authentication methods. Default: DEFAULT_SUPPORTED_METHODS.
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
    var serviceFee: Int = 0, //in cents
    
    // Google Pay configuration parameters
    /**
     * Boolean flag to enable/disable Google Pay
     * Default: false
     */
    val googlePayEnabled: Boolean = false,
    
    /**
     * String displayed in Google Pay payment sheet
     * Required if googlePayEnabled is true
     * Default: null
     */
    val googlePayMerchantName: String? = null,
    
    /**
     * Boolean to control acceptance of prepaid cards
     * Default: true
     */
    val googlePayAllowPrepaidCards: Boolean = true,
    
    /**
     * Boolean to control acceptance of credit cards
     * Default: true
     */
    val googlePayAllowCreditCards: Boolean = true,
    
    /**
     * Boolean requiring billing address
     * Default: false
     */
    val googlePayBillingAddressRequired: Boolean = false,
    
    /**
     * Format requirements for billing address (MINIMAL or FULL)
     * Default: MINIMAL
     */
    val googlePayBillingAddressFormat: GooglePayBillingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
    
    /**
     * Boolean requiring shipping address
     * Default: false
     */
    val googlePayShippingAddressRequired: Boolean = false,
    
    /**
     * Boolean requiring phone number
     * Default: false
     */
    val googlePayPhoneNumberRequired: Boolean = false,
    
    /**
     * Button text options (PAY, CHECKOUT, SUBSCRIBE, etc.)
     * Default: PAY
     */
    val googlePayButtonType: GooglePayButtonType = GooglePayButtonType.PAY,
    
    /**
     * Button color scheme (BLACK, WHITE)
     * Default: BLACK
     */
    val googlePayButtonColor: GooglePayButtonColor = GooglePayButtonColor.BLACK,
    
    /**
     * TEST or PRODUCTION environment selection
     * Default: TEST
     */
    val googlePayEnvironment: GooglePayEnvironment = GooglePayEnvironment.TEST,
    
    /**
     * List of supported card networks
     * Default: DEFAULT_SUPPORTED_NETWORKS
     */
    val googlePayAllowedCardNetworks: List<String> = GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS,
    
    /**
     * Authentication methods (CRYPTOGRAM_3DS only for initial implementation)
     * Default: DEFAULT_SUPPORTED_METHODS
     */
    val googlePaySupportedMethods: List<String> = GooglePayConstants.DEFAULT_SUPPORTED_METHODS
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
     * @throws IllegalArgumentException If the apiKey is invalid, the amount is invalid for the specified payment method action, or Google Pay configuration is invalid.
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
        
        // Google Pay specific validation
        if (configuration.googlePayEnabled) {
            if (configuration.googlePayMerchantName.isNullOrBlank()) {
                throw IllegalArgumentException(INVALID_GOOGLEPAY_MERCHANT_NAME)
            }
            
            // Ensure CRYPTOGRAM_3DS is included in supported methods
            if (!configuration.googlePaySupportedMethods.contains("CRYPTOGRAM_3DS")) {
                throw IllegalArgumentException(INVALID_GOOGLEPAY_AUTH_METHOD)
            }
        }
        
        return Pair(partnerName, stageName)
    }

    /**
     * Builder class for PayTheoryConfiguration
     */
    class Builder {
        private var outlined: Boolean = true
        private var apiKey: String = ""
        private var amount: Int = 0
        private var paymentMethodType: PaymentMethodType = PaymentMethodType.CARD
        private var paymentMethodAction: PaymentMethodAction = PaymentMethodAction.PAYMENT
        private var requireAccountName: Boolean = false
        private var requireBillingAddress: Boolean = false
        private var feeMode: String = FeeMode.MERCHANT_FEE
        private var metadata: HashMap<Any, Any> = HashMap()
        private var payorInfo: PayorInfo? = null
        private var payorId: String = ""
        private var skipTokenizeValidation: Boolean = false
        private var accountCode: String = ""
        private var reference: String = ""
        private var paymentParameters: String = ""
        private var invoiceId: String = ""
        private var sendReceipt: Boolean = false
        private var receiptDescription: String = "Payment Confirmation"
        private var serviceFee: Int = 0
        
        // Google Pay builder fields with defaults
        private var googlePayEnabled: Boolean = false
        private var googlePayMerchantName: String? = null
        private var googlePayAllowPrepaidCards: Boolean = true
        private var googlePayAllowCreditCards: Boolean = true
        private var googlePayBillingAddressRequired: Boolean = false
        private var googlePayBillingAddressFormat: GooglePayBillingAddressFormat = GooglePayBillingAddressFormat.MINIMAL
        private var googlePayShippingAddressRequired: Boolean = false
        private var googlePayPhoneNumberRequired: Boolean = false
        private var googlePayButtonType: GooglePayButtonType = GooglePayButtonType.PAY
        private var googlePayButtonColor: GooglePayButtonColor = GooglePayButtonColor.BLACK
        private var googlePayEnvironment: GooglePayEnvironment = GooglePayEnvironment.TEST
        private var googlePayAllowedCardNetworks: List<String> = GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS
        private var googlePaySupportedMethods: List<String> = GooglePayConstants.DEFAULT_SUPPORTED_METHODS

        fun setApiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }

        fun setAmount(amount: Int): Builder {
            this.amount = amount
            return this
        }

        fun setPaymentMethodType(paymentMethodType: PaymentMethodType): Builder {
            this.paymentMethodType = paymentMethodType
            return this
        }

        fun setPaymentMethodAction(paymentMethodAction: PaymentMethodAction): Builder {
            this.paymentMethodAction = paymentMethodAction
            return this
        }

        fun setRequireAccountName(requireAccountName: Boolean): Builder {
            this.requireAccountName = requireAccountName
            return this
        }

        fun setRequireBillingAddress(requireBillingAddress: Boolean): Builder {
            this.requireBillingAddress = requireBillingAddress
            return this
        }

        fun setFeeMode(feeMode: String): Builder {
            this.feeMode = feeMode
            return this
        }

        fun setMetadata(metadata: HashMap<Any, Any>): Builder {
            this.metadata = metadata
            return this
        }

        fun setPayorInfo(payorInfo: PayorInfo): Builder {
            this.payorInfo = payorInfo
            return this
        }

        fun setPayorId(payorId: String): Builder {
            this.payorId = payorId
            return this
        }

        fun setSkipTokenizeValidation(skipTokenizeValidation: Boolean): Builder {
            this.skipTokenizeValidation = skipTokenizeValidation
            return this
        }

        fun setAccountCode(accountCode: String): Builder {
            this.accountCode = accountCode
            return this
        }

        fun setReference(reference: String): Builder {
            this.reference = reference
            return this
        }

        fun setPaymentParameters(paymentParameters: String): Builder {
            this.paymentParameters = paymentParameters
            return this
        }

        fun setInvoiceId(invoiceId: String): Builder {
            this.invoiceId = invoiceId
            return this
        }

        fun setSendReceipt(sendReceipt: Boolean): Builder {
            this.sendReceipt = sendReceipt
            return this
        }

        fun setReceiptDescription(receiptDescription: String): Builder {
            this.receiptDescription = receiptDescription
            return this
        }

        fun setServiceFee(serviceFee: Int): Builder {
            this.serviceFee = serviceFee
            return this
        }

        fun setOutlined(outlined: Boolean): Builder {
            this.outlined = outlined
            return this
        }
        
        // Google Pay builder methods
        
        /**
         * Enable Google Pay with required merchant name
         */
        fun enableGooglePay(merchantName: String): Builder {
            this.googlePayEnabled = true
            this.googlePayMerchantName = merchantName
            return this
        }
        
        fun setGooglePayAllowPrepaidCards(allow: Boolean): Builder {
            this.googlePayAllowPrepaidCards = allow
            return this
        }
        
        fun setGooglePayAllowCreditCards(allow: Boolean): Builder {
            this.googlePayAllowCreditCards = allow
            return this
        }
        
        fun setGooglePayBillingAddressRequired(required: Boolean): Builder {
            this.googlePayBillingAddressRequired = required
            return this
        }
        
        fun setGooglePayBillingAddressFormat(format: GooglePayBillingAddressFormat): Builder {
            this.googlePayBillingAddressFormat = format
            return this
        }
        
        fun setGooglePayShippingAddressRequired(required: Boolean): Builder {
            this.googlePayShippingAddressRequired = required
            return this
        }
        
        fun setGooglePayPhoneNumberRequired(required: Boolean): Builder {
            this.googlePayPhoneNumberRequired = required
            return this
        }
        
        fun setGooglePayButtonType(type: GooglePayButtonType): Builder {
            this.googlePayButtonType = type
            return this
        }
        
        fun setGooglePayButtonColor(color: GooglePayButtonColor): Builder {
            this.googlePayButtonColor = color
            return this
        }
        
        fun setGooglePayEnvironment(environment: GooglePayEnvironment): Builder {
            this.googlePayEnvironment = environment
            return this
        }
        
        fun setGooglePayAllowedCardNetworks(networks: List<String>): Builder {
            this.googlePayAllowedCardNetworks = networks
            return this
        }
        
        fun setGooglePaySupportedMethods(methods: List<String>): Builder {
            this.googlePaySupportedMethods = methods
            return this
        }

        fun build(): PayTheoryConfiguration {
            // Validation
            require(apiKey.isNotBlank()) { "API key is required" }
            
            if (paymentMethodAction == PaymentMethodAction.PAYMENT) {
                require(amount >= 10) { "Amount must be at least 10 cents for PAYMENT" }
            } else if (paymentMethodAction == PaymentMethodAction.TOKEN) {
                require(amount == 0) { "Amount must be 0 for TOKEN" }
            }
            
            // Google Pay specific validation
            if (googlePayEnabled) {
                require(!googlePayMerchantName.isNullOrBlank()) { INVALID_GOOGLEPAY_MERCHANT_NAME }
                
                // Ensure CRYPTOGRAM_3DS is included in supported methods
                if (googlePaySupportedMethods != GooglePayConstants.DEFAULT_SUPPORTED_METHODS) {
                    require(googlePaySupportedMethods.contains("CRYPTOGRAM_3DS")) { 
                        INVALID_GOOGLEPAY_AUTH_METHOD 
                    }
                }
            }

            return PayTheoryConfiguration(
                apiKey = apiKey,
                amount = amount,
                paymentMethodType = paymentMethodType,
                paymentMethodAction = paymentMethodAction,
                requireAccountName = requireAccountName,
                requireBillingAddress = requireBillingAddress,
                feeMode = feeMode,
                metadata = metadata,
                payorInfo = payorInfo,
                payorId = payorId,
                skipTokenizeValidation = skipTokenizeValidation,
                accountCode = accountCode,
                reference = reference,
                paymentParameters = paymentParameters,
                invoiceId = invoiceId,
                sendReceipt = sendReceipt,
                receiptDescription = receiptDescription,
                serviceFee = serviceFee,
                outlined = outlined,
                
                // Google Pay parameters
                googlePayEnabled = googlePayEnabled,
                googlePayMerchantName = googlePayMerchantName,
                googlePayAllowPrepaidCards = googlePayAllowPrepaidCards,
                googlePayAllowCreditCards = googlePayAllowCreditCards,
                googlePayBillingAddressRequired = googlePayBillingAddressRequired,
                googlePayBillingAddressFormat = googlePayBillingAddressFormat,
                googlePayShippingAddressRequired = googlePayShippingAddressRequired,
                googlePayPhoneNumberRequired = googlePayPhoneNumberRequired,
                googlePayButtonType = googlePayButtonType,
                googlePayButtonColor = googlePayButtonColor,
                googlePayEnvironment = googlePayEnvironment,
                googlePayAllowedCardNetworks = googlePayAllowedCardNetworks,
                googlePaySupportedMethods = googlePaySupportedMethods
            )
        }
    }
}