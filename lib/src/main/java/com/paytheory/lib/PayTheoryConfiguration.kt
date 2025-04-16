package com.paytheory.lib

import com.paytheory.lib.configuration.*
import com.paytheory.lib.data.payloads.PayorInfo

private const val PAYTHEORYLAB = "paytheorylab"
private const val PAYTHEORYSTUDY = "paytheorystudy"
private const val PAYTHEORY = "paytheory"
private const val INVALID_APIKEY = "Invalid apikey"
private const val INVALID_AMOUNT = "Invalid amount"
private const val INVALID_GOOGLEPAY_MERCHANT_NAME = "Merchant name is required for Google Pay"
private const val INVALID_GOOGLEPAY_AUTH_METHOD = "CRYPTOGRAM_3DS authentication method is required for Google Pay"
private const val TEST_API_KEY = "test-paytheory-apikey"

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
 * @property isTestMode Boolean indicating if running in test mode. Default: true if apiKey is the test API key.
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
    
    /**
     * Whether the SDK is running in test mode
     * True when apiKey is the test API key, or explicitly set to true
     * Used to bypass Android platform dependencies in testing
     */
    val isTestMode: Boolean = apiKey == TEST_API_KEY,
    
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
        // Skip validation for test API key
        if (isTestMode) {
            partnerName = "test"
            stageName = "paytheory"
            apiBasePath = "https://api.paytheory.com/"
        } else {
            val details = validatePaymentConfigAndExtractDetails(this)
            partnerName = details.first
            stageName = details.second
            apiBasePath = "https://$partnerName.$stageName.com/"
        }
    }

    /**
     * Validates the payment configuration and extracts the partner name and stage name from the apiKey.
     *
     * @param configuration The PayTheoryConfiguration object to validate.
     * @return A Pair containing the partner name and stage name.
     * @throws IllegalArgumentException If the apiKey is invalid, the amount is invalid for the specified payment method action, or Google Pay configuration is invalid.
     */
    private fun validatePaymentConfigAndExtractDetails(configuration: PayTheoryConfiguration): Pair<String, String> {
        // Skip validation for test API key
        if (configuration.isTestMode) {
            return Pair("test", "paytheory")
        }
        
        val partnerName = configuration.apiKey.substring(0, configuration.apiKey.indexOf('-'))
        val stageName =
            configuration.apiKey.substring(configuration.apiKey.indexOf('-') + 1, configuration.apiKey.indexOf('-', configuration.apiKey.indexOf('-') + 1))

        if (stageName != PAYTHEORYLAB && stageName != PAYTHEORYSTUDY && stageName != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (amount < 10 && configuration.paymentMethodAction == PaymentMethodAction.PAYMENT) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }
        if (amount > 0 && configuration.paymentMethodAction == PaymentMethodAction.TOKEN) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }
        
        // Google Pay specific validation - only performed for non-test API keys
        if (configuration.googlePayEnabled && !configuration.isTestMode) {
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
        private var isTestMode: Boolean? = null
        
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

        /**
         * Sets the API key for interacting with PayTheory services.
         *
         * @param value The API key value.
         * @return The Builder instance.
         * @throws IllegalArgumentException if the API key is empty
         */
        fun setApiKey(value: String): Builder {
            if (value.isEmpty()) {
                throw IllegalArgumentException("API key cannot be empty")
            }
            apiKey = value
            return this
        }

        /**
         * Sets the transaction amount in cents.
         *
         * @param value The amount value in cents.
         * @return The Builder instance.
         * @throws IllegalArgumentException if the amount is negative
         */
        fun setAmount(value: Int): Builder {
            if (value < 0) {
                throw IllegalArgumentException("Amount cannot be negative")
            }
            amount = value
            return this
        }

        /**
         * Sets the payment method type.
         *
         * @param value The payment method type (CARD, ACH, or CASH).
         * @return The Builder instance.
         */
        fun setPaymentMethodType(value: PaymentMethodType): Builder {
            paymentMethodType = value
            return this
        }

        /**
         * Sets the payment method action.
         *
         * @param value The payment method action (PAYMENT or TOKEN).
         * @return The Builder instance.
         */
        fun setPaymentMethodAction(value: PaymentMethodAction): Builder {
            paymentMethodAction = value
            return this
        }

        /**
         * Sets whether the account name is required.
         *
         * @param value Boolean indicating if account name is required.
         * @return The Builder instance.
         */
        fun setRequireAccountName(value: Boolean): Builder {
            requireAccountName = value
            return this
        }

        /**
         * Sets whether the billing address is required.
         *
         * @param value Boolean indicating if billing address is required.
         * @return The Builder instance.
         */
        fun setRequireBillingAddress(value: Boolean): Builder {
            requireBillingAddress = value
            return this
        }

        /**
         * Sets the fee mode.
         *
         * @param value The fee mode (MERCHANT_FEE or BUYER_FEE).
         * @return The Builder instance.
         * @throws IllegalArgumentException if fee mode is invalid
         */
        fun setFeeMode(value: String): Builder {
            if (value != FeeMode.MERCHANT_FEE) {
                throw IllegalArgumentException("Invalid fee mode")
            }
            feeMode = value
            return this
        }

        /**
         * Sets the metadata to be associated with the transaction.
         *
         * @param value The metadata HashMap.
         * @return The Builder instance.
         */
        fun setMetadata(value: HashMap<Any, Any>): Builder {
            metadata = value
            return this
        }

        /**
         * Sets the payor information.
         *
         * @param value The PayorInfo object.
         * @return The Builder instance.
         */
        fun setPayorInfo(value: PayorInfo): Builder {
            payorInfo = value
            return this
        }

        /**
         * Sets the payor ID.
         *
         * @param value The payor ID.
         * @return The Builder instance.
         */
        fun setPayorId(value: String): Builder {
            payorId = value
            return this
        }

        /**
         * Sets the skip tokenize validation flag.
         *
         * @param value Boolean indicating if tokenization validation should be skipped.
         * @return The Builder instance.
         */
        fun setSkipTokenizeValidation(value: Boolean): Builder {
            skipTokenizeValidation = value
            return this
        }

        /**
         * Sets the account code.
         *
         * @param value The account code.
         * @return The Builder instance.
         */
        fun setAccountCode(value: String): Builder {
            accountCode = value
            return this
        }

        /**
         * Sets the reference information.
         *
         * @param value The reference information.
         * @return The Builder instance.
         */
        fun setReference(value: String): Builder {
            reference = value
            return this
        }

        /**
         * Sets the payment parameters.
         *
         * @param value The payment parameters.
         * @return The Builder instance.
         */
        fun setPaymentParameters(value: String): Builder {
            paymentParameters = value
            return this
        }

        /**
         * Sets the invoice ID.
         *
         * @param value The invoice ID.
         * @return The Builder instance.
         */
        fun setInvoiceId(value: String): Builder {
            invoiceId = value
            return this
        }

        /**
         * Sets the flag to determine if a receipt should be sent.
         *
         * @param value Boolean indicating if a receipt should be sent.
         * @return The Builder instance.
         */
        fun setSendReceipt(value: Boolean): Builder {
            sendReceipt = value
            return this
        }

        /**
         * Sets the receipt description.
         *
         * @param value The receipt description.
         * @return The Builder instance.
         */
        fun setReceiptDescription(value: String): Builder {
            receiptDescription = value
            return this
        }

        /**
         * Sets the service fee amount in cents.
         *
         * @param value The service fee amount in cents.
         * @return The Builder instance.
         * @throws IllegalArgumentException if service fee is negative
         */
        fun setServiceFee(value: Int): Builder {
            if (value < 0) {
                throw IllegalArgumentException("Service fee cannot be negative")
            }
            serviceFee = value
            return this
        }
        
        /**
         * Sets whether the SDK is running in test mode.
         * Setting to true will bypass Android platform dependencies.
         *
         * @param value Boolean indicating if in test mode.
         * @return The Builder instance.
         */
        fun setTestMode(value: Boolean): Builder {
            isTestMode = value
            return this
        }
        
        /**
         * Enables Google Pay with the provided merchant name.
         * This is a convenience method that enables Google Pay and sets the merchant name.
         *
         * @param merchantName The merchant name to display in Google Pay sheet.
         * @return The Builder instance.
         * @throws IllegalArgumentException if the merchant name is empty and not using the test API key
         */
        fun enableGooglePay(merchantName: String): Builder {
            if (merchantName.isBlank() && apiKey != TEST_API_KEY) {
                throw IllegalArgumentException(INVALID_GOOGLEPAY_MERCHANT_NAME)
            }
            googlePayEnabled = true
            googlePayMerchantName = merchantName
            return this
        }
        
        /**
         * Sets whether to allow prepaid cards for Google Pay.
         *
         * @param allow Boolean indicating if prepaid cards are allowed.
         * @return The Builder instance.
         */
        fun setGooglePayAllowPrepaidCards(allow: Boolean): Builder {
            googlePayAllowPrepaidCards = allow
            return this
        }
        
        /**
         * Sets whether to allow credit cards for Google Pay.
         *
         * @param allow Boolean indicating if credit cards are allowed.
         * @return The Builder instance.
         */
        fun setGooglePayAllowCreditCards(allow: Boolean): Builder {
            googlePayAllowCreditCards = allow
            return this
        }
        
        /**
         * Sets whether billing address is required for Google Pay.
         *
         * @param required Boolean indicating if billing address is required.
         * @return The Builder instance.
         */
        fun setGooglePayBillingAddressRequired(required: Boolean): Builder {
            googlePayBillingAddressRequired = required
            return this
        }
        
        /**
         * Sets the billing address format for Google Pay.
         *
         * @param format The billing address format (MINIMAL or FULL).
         * @return The Builder instance.
         */
        fun setGooglePayBillingAddressFormat(format: GooglePayBillingAddressFormat): Builder {
            googlePayBillingAddressFormat = format
            return this
        }
        
        /**
         * Sets whether shipping address is required for Google Pay.
         *
         * @param required Boolean indicating if shipping address is required.
         * @return The Builder instance.
         */
        fun setGooglePayShippingAddressRequired(required: Boolean): Builder {
            googlePayShippingAddressRequired = required
            return this
        }
        
        /**
         * Sets whether phone number is required for Google Pay.
         *
         * @param required Boolean indicating if phone number is required.
         * @return The Builder instance.
         */
        fun setGooglePayPhoneNumberRequired(required: Boolean): Builder {
            googlePayPhoneNumberRequired = required
            return this
        }
        
        /**
         * Sets the button type for Google Pay.
         *
         * @param type The button type (PAY, CHECKOUT, etc.).
         * @return The Builder instance.
         */
        fun setGooglePayButtonType(type: GooglePayButtonType): Builder {
            googlePayButtonType = type
            return this
        }
        
        /**
         * Sets the button color for Google Pay.
         *
         * @param color The button color (BLACK or WHITE).
         * @return The Builder instance.
         */
        fun setGooglePayButtonColor(color: GooglePayButtonColor): Builder {
            googlePayButtonColor = color
            return this
        }
        
        /**
         * Sets the environment for Google Pay.
         *
         * @param environment The environment (TEST or PRODUCTION).
         * @return The Builder instance.
         */
        fun setGooglePayEnvironment(environment: GooglePayEnvironment): Builder {
            googlePayEnvironment = environment
            return this
        }
        
        /**
         * Sets the allowed card networks for Google Pay.
         *
         * @param networks List of allowed card networks.
         * @return The Builder instance.
         */
        fun setGooglePayAllowedCardNetworks(networks: List<String>): Builder {
            googlePayAllowedCardNetworks = networks
            return this
        }
        
        /**
         * Sets the supported methods for Google Pay.
         *
         * @param methods List of supported authentication methods.
         * @return The Builder instance.
         */
        fun setGooglePaySupportedMethods(methods: List<String>): Builder {
            googlePaySupportedMethods = methods
            return this
        }
        
        /**
         * Sets whether the UI elements should be outlined.
         *
         * @param value Boolean indicating if UI elements should be outlined.
         * @return The Builder instance.
         */
        fun setOutlined(value: Boolean): Builder {
            outlined = value
            return this
        }

        /**
         * Builds and returns a PayTheoryConfiguration object with the configured properties.
         *
         * @return A PayTheoryConfiguration object.
         */
        fun build(): PayTheoryConfiguration {
            // Determine isTestMode based on API key if not explicitly set
            val computedTestMode = isTestMode ?: (apiKey == TEST_API_KEY)
            
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
                isTestMode = computedTestMode,
                
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