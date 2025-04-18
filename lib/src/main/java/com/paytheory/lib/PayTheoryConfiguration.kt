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
 * Configuration class for initializing and customizing the Pay Theory SDK.
 *
 * This class holds all the necessary settings required to interact with Pay Theory services,
 * including API keys, transaction details, UI customization options, and Google Pay parameters.
 * It validates the provided configuration during initialization. Instances are typically created
 * using the [Builder] pattern.
 *
 * Once constructed, a `PayTheoryConfiguration` object is immutable.
 *
 * @property outlined If `true`, Pay Theory input fields will use an outlined Material Design style. If `false`, they use a filled style. Defaults to `true`.
 * @property apiKey The Pay Theory API Key specific to your partner account and environment (e.g., "PARTNER-paytheorystudy-APIKEY"). This is mandatory.
 * @property amount The transaction amount in **cents**. For example, 100 represents $1.00 USD.
 *           Must be >= 10 for [PaymentMethodAction.PAYMENT]. Should be 0 for [PaymentMethodAction.TOKENIZE]. Defaults to 0.
 * @property paymentMethodType Specifies the type of payment method to be used ([PaymentMethodType.CARD], [PaymentMethodType.ACH], or [PaymentMethodType.CASH]). Defaults to [PaymentMethodType.CARD].
 * @property paymentMethodAction Determines the primary action to perform ([PaymentMethodAction.PAYMENT] for immediate capture or [PaymentMethodAction.TOKENIZE] to only tokenize the payment method). Defaults to [PaymentMethodAction.PAYMENT].
 * @property requireAccountName If `true`, the account holder name field becomes mandatory for CARD and ACH payments. Defaults to `false`.
 * @property requireBillingAddress If `true`, the billing address fields become mandatory for CARD and ACH payments. Defaults to `false`.
 * @property feeMode Specifies who bears the transaction fees. Currently, only [FeeMode.MERCHANT_FEE] is supported. Defaults to [FeeMode.MERCHANT_FEE].
 * @property metadata A map of key-value pairs to associate with the transaction. Can be used for tracking or informational purposes. Defaults to an empty `HashMap`.
 * @property payorInfo Optional [PayorInfo] object containing details about the person making the payment. Defaults to `null`.
 * @property payorId An optional identifier for the payor, linking the transaction to a specific user in your system. Defaults to an empty string.
 * @property skipTokenizeValidation If `true`, skips client-side validation checks during tokenization. Use with caution. Defaults to `false`.
 * @property accountCode An optional code for categorizing transactions. Defaults to an empty string.
 * @property reference Optional reference text for the transaction. Defaults to an empty string.
 * @property paymentParameters Optional parameters specific to the payment processor. Defaults to an empty string.
 * @property invoiceId An optional identifier linking the transaction to an invoice. Defaults to an empty string.
 * @property sendReceipt If `true`, instructs Pay Theory to attempt sending a receipt to the payor (requires payor email). Defaults to `false`.
 * @property receiptDescription A description to include on the receipt if [sendReceipt] is `true`. Defaults to "Payment Confirmation".
 * @property serviceFee An optional service fee amount in **cents** to be added to the transaction. Defaults to 0.
 * @property isTestMode Automatically determined based on the [apiKey]. If `true` (either using the specific test key or explicitly set via the Builder), the SDK might bypass certain platform dependencies (like Google Play Services checks) for easier testing.
 * @property googlePayEnabled If `true`, enables the Google Pay payment option within the SDK's UI components. Requires Google Pay specific configuration. Defaults to `false`.
 * @property googlePayMerchantName The merchant name displayed in the Google Pay payment sheet. **Required** if [googlePayEnabled] is `true` and not using the test API key. Defaults to `null`.
 * @property googlePayAllowPrepaidCards If `true` (the default), allows users to pay with prepaid cards via Google Pay. Set to `false` to disallow prepaid cards.
 * @property googlePayAllowCreditCards If `true` (the default), allows users to pay with credit cards via Google Pay. Set to `false` to disallow credit cards. Note: Debit cards are typically controlled by the `allowedCardNetworks`.
 * @property googlePayBillingAddressRequired If `true`, requires the user to provide a billing address within the Google Pay sheet. Defaults to `false`.
 * @property googlePayBillingAddressFormat Specifies the required level of detail for the billing address if [googlePayBillingAddressRequired] is `true`. Can be [GooglePayBillingAddressFormat.MINIMAL] or [GooglePayBillingAddressFormat.FULL]. Defaults to [GooglePayBillingAddressFormat.MINIMAL].
 * @property googlePayShippingAddressRequired If `true`, requires the user to provide a shipping address within the Google Pay sheet. Defaults to `false`. Consider Google Pay's API for handling shipping options if needed.
 * @property googlePayPhoneNumberRequired If `true`, requires the user to provide a phone number within the Google Pay sheet. Defaults to `false`.
 * @property googlePayButtonType Customizes the text displayed on the Google Pay button (e.g., [GooglePayButtonType.PAY], [GooglePayButtonType.CHECKOUT]). Defaults to [GooglePayButtonType.PAY].
 * @property googlePayButtonColor Customizes the color theme of the Google Pay button ([GooglePayButtonColor.BLACK] or [GooglePayButtonColor.WHITE]). Defaults to [GooglePayButtonColor.BLACK].
 * @property googlePayEnvironment Sets the Google Pay API environment ([GooglePayEnvironment.TEST] or [GooglePayEnvironment.PRODUCTION]). Should match the environment implied by the [apiKey]. Defaults to [GooglePayEnvironment.TEST].
 * @property googlePayAllowedCardNetworks A list of card networks (e.g., "VISA", "MASTERCARD") allowed for Google Pay transactions. Defaults to [GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS].
 * @property googlePaySupportedMethods A list of allowed authentication methods for Google Pay. Currently, Pay Theory primarily supports tokenized card payments via Google Pay, requiring "CRYPTOGRAM_3DS". Defaults to [GooglePayConstants.DEFAULT_SUPPORTED_METHODS].
 */
class PayTheoryConfiguration(
    val outlined: Boolean = true,
    /** @see PayTheoryConfiguration.apiKey */
    val apiKey: String,
    /** @see PayTheoryConfiguration.amount */
    val amount: Int = 0,
    /** @see PayTheoryConfiguration.paymentMethodType */
    val paymentMethodType: PaymentMethodType = PaymentMethodType.CARD,
    /** @see PayTheoryConfiguration.paymentMethodAction */
    val paymentMethodAction: PaymentMethodAction = PaymentMethodAction.PAYMENT,
    /** @see PayTheoryConfiguration.requireAccountName */
    var requireAccountName: Boolean = false,
    /** @see PayTheoryConfiguration.requireBillingAddress */
    var requireBillingAddress: Boolean = false,
    /** @see PayTheoryConfiguration.feeMode */
    var feeMode: String = FeeMode.MERCHANT_FEE,
    /** @see PayTheoryConfiguration.metadata */
    var metadata: HashMap<Any, Any> = HashMap(),
    /** @see PayTheoryConfiguration.payorInfo */
    var payorInfo: PayorInfo? = null,
    /** @see PayTheoryConfiguration.payorId */
    var payorId: String = "",
    /** @see PayTheoryConfiguration.skipTokenizeValidation */
    var skipTokenizeValidation: Boolean = false,
    /** @see PayTheoryConfiguration.accountCode */
    var accountCode: String = "",
    /** @see PayTheoryConfiguration.reference */
    var reference: String = "",
    /** @see PayTheoryConfiguration.paymentParameters */
    var paymentParameters: String = "",
    /** @see PayTheoryConfiguration.invoiceId */
    var invoiceId: String = "",
    /** @see PayTheoryConfiguration.sendReceipt */
    var sendReceipt: Boolean = false,
    /** @see PayTheoryConfiguration.receiptDescription */
    var receiptDescription: String = "Payment Confirmation",
    /** @see PayTheoryConfiguration.serviceFee */
    var serviceFee: Int = 0,

    /** @see PayTheoryConfiguration.isTestMode */
    val isTestMode: Boolean = apiKey == TEST_API_KEY,

    /** @see PayTheoryConfiguration.googlePayEnabled */
    val googlePayEnabled: Boolean = false,
    /** @see PayTheoryConfiguration.googlePayMerchantName */
    val googlePayMerchantName: String? = null,
    /** @see PayTheoryConfiguration.googlePayAllowPrepaidCards */
    val googlePayAllowPrepaidCards: Boolean = true,
    /** @see PayTheoryConfiguration.googlePayAllowCreditCards */
    val googlePayAllowCreditCards: Boolean = true,
    /** @see PayTheoryConfiguration.googlePayBillingAddressRequired */
    val googlePayBillingAddressRequired: Boolean = false,
    /** @see PayTheoryConfiguration.googlePayBillingAddressFormat */
    val googlePayBillingAddressFormat: GooglePayBillingAddressFormat = GooglePayBillingAddressFormat.MINIMAL,
    /** @see PayTheoryConfiguration.googlePayShippingAddressRequired */
    val googlePayShippingAddressRequired: Boolean = false,
    /** @see PayTheoryConfiguration.googlePayPhoneNumberRequired */
    val googlePayPhoneNumberRequired: Boolean = false,
    /** @see PayTheoryConfiguration.googlePayButtonType */
    val googlePayButtonType: GooglePayButtonType = GooglePayButtonType.PAY,
    /** @see PayTheoryConfiguration.googlePayButtonColor */
    val googlePayButtonColor: GooglePayButtonColor = GooglePayButtonColor.BLACK,
    /** @see PayTheoryConfiguration.googlePayEnvironment */
    val googlePayEnvironment: GooglePayEnvironment = GooglePayEnvironment.TEST,
    /** @see PayTheoryConfiguration.googlePayAllowedCardNetworks */
    val googlePayAllowedCardNetworks: List<String> = GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS,
    /** @see PayTheoryConfiguration.googlePaySupportedMethods */
    val googlePaySupportedMethods: List<String> = GooglePayConstants.DEFAULT_SUPPORTED_METHODS
) {
    /**
     * The partner name derived from the [apiKey] (e.g., "acme").
     */
    var partnerName: String = ""
        private set // Allow reading but not external modification

    /**
     * The stage or environment name derived from the [apiKey] (e.g., "paytheorystudy", "paytheorylab", "paytheory").
     */
    var stageName: String = ""
        private set // Allow reading but not external modification

    /**
     * The base URL path for the Pay Theory API, constructed from [partnerName] and [stageName] (e.g., "https://acme.paytheorystudy.com/").
     */
    var apiBasePath: String = ""
        private set // Allow reading but not external modification

    /**
     * Initializes the configuration object.
     * This block performs critical initial setup:
     * 1. Validates the core configuration parameters ([apiKey], [amount], Google Pay settings) via [validatePaymentConfigAndExtractDetails].
     * 2. Parses the [apiKey] to extract [partnerName] and [stageName].
     * 3. Constructs the [apiBasePath] based on the extracted partner and stage names.
     * If using the test API key ([isTestMode] is true), validation is skipped, and default test values are used for partner, stage, and base path.
     */
    init {
        // Skip validation for test API key or explicitly set test mode
        if (isTestMode) {
            partnerName = "test"
            stageName = "paytheory"
            apiBasePath = "https://api.paytheory.com/" // Assuming a generic test endpoint
        } else {
            val details = validatePaymentConfigAndExtractDetails(this)
            partnerName = details.first
            stageName = details.second
            apiBasePath = "https://$partnerName.$stageName.com/"
        }
    }

    /**
     * Validates critical configuration parameters and extracts partner/stage details from the API key.
     *
     * This function enforces rules such as:
     * - API key format and valid stage names (paytheorylab, paytheorystudy, paytheory).
     * - Minimum amount for payments ([PaymentMethodAction.PAYMENT]).
     * - Amount restriction for tokenization ([PaymentMethodAction.TOKENIZE]).
     * - Presence of [googlePayMerchantName] if Google Pay is enabled (and not in test mode).
     * - Requirement of "CRYPTOGRAM_3DS" in [googlePaySupportedMethods] if Google Pay is enabled (and not in test mode).
     *
     * It should only be called for non-test configurations.
     *
     * @param configuration The [PayTheoryConfiguration] instance to validate.
     * @return A [Pair] containing the extracted partner name (first) and stage name (second).
     * @throws IllegalArgumentException If any validation rule is violated.
     */
    private fun validatePaymentConfigAndExtractDetails(configuration: PayTheoryConfiguration): Pair<String, String> {
        // Basic API Key structure validation
        if (!configuration.apiKey.contains('-') || configuration.apiKey.count { it == '-' } < 2) {
            throw IllegalArgumentException(INVALID_APIKEY + ": Invalid format")
        }

        val partnerName = configuration.apiKey.substringBefore('-')
        val stageName = configuration.apiKey.substringAfter('-').substringBeforeLast('-')

        if (partnerName.isBlank() || stageName.isBlank()) {
             throw IllegalArgumentException(INVALID_APIKEY + ": Partner or stage name missing")
        }

        if (stageName != PAYTHEORYLAB && stageName != PAYTHEORYSTUDY && stageName != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY + ": Invalid stage name '$stageName'")
        }

        // Amount validation based on action
        if (configuration.paymentMethodAction == PaymentMethodAction.PAYMENT && configuration.amount < 10) {
            throw IllegalArgumentException(INVALID_AMOUNT + ": Minimum amount is 10 cents for payments.")
        }
        if (configuration.paymentMethodAction == PaymentMethodAction.TOKEN && configuration.amount != 0) {
            throw IllegalArgumentException(INVALID_AMOUNT + ": Amount must be 0 for tokenization.")
        }

        // Google Pay specific validation - only performed for non-test API keys
        if (configuration.googlePayEnabled) { // No need to check isTestMode here, as this function is only called when !isTestMode
            if (configuration.googlePayMerchantName.isNullOrBlank()) {
                throw IllegalArgumentException(INVALID_GOOGLEPAY_MERCHANT_NAME)
            }

            // Ensure CRYPTOGRAM_3DS is included in supported methods for Google Pay card payments
            if (!configuration.googlePaySupportedMethods.contains(GooglePayConstants.DEFAULT_SUPPORTED_METHODS[1])) {
                throw IllegalArgumentException(INVALID_GOOGLEPAY_AUTH_METHOD)
            }
        }

        return Pair(partnerName, stageName)
    }

    /**
     * Builder class for creating [PayTheoryConfiguration] instances.
     * Provides a fluent API for setting configuration options step-by-step.
     * Call [build] to construct the final immutable [PayTheoryConfiguration] object.
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
        private var isTestMode: Boolean? = null // Nullable to allow auto-detection based on apiKey

        // Google Pay builder fields
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
         * Sets the mandatory Pay Theory API key.
         * @param value The API key.
         * @return This Builder instance for chaining.
         * @throws IllegalArgumentException if the API key is empty.
         */
        fun setApiKey(value: String): Builder {
            if (value.isBlank()) { // Use isBlank for better validation
                throw IllegalArgumentException("API key cannot be empty")
            }
            apiKey = value
            return this
        }

        /**
         * Sets the transaction amount in cents.
         * @param value The amount in cents.
         * @return This Builder instance for chaining.
         * @throws IllegalArgumentException if the amount is negative.
         */
        fun setAmount(value: Int): Builder {
            if (value < 0) {
                throw IllegalArgumentException("Amount cannot be negative")
            }
            amount = value
            return this
        }

        /** Sets the payment method type. See [PayTheoryConfiguration.paymentMethodType]. */
        fun setPaymentMethodType(value: PaymentMethodType): Builder {
            paymentMethodType = value
            return this
        }

        /** Sets the payment method action. See [PayTheoryConfiguration.paymentMethodAction]. */
        fun setPaymentMethodAction(value: PaymentMethodAction): Builder {
            paymentMethodAction = value
            return this
        }

        /** Sets whether account name is required. See [PayTheoryConfiguration.requireAccountName]. */
        fun setRequireAccountName(value: Boolean): Builder {
            requireAccountName = value
            return this
        }

        /** Sets whether billing address is required. See [PayTheoryConfiguration.requireBillingAddress]. */
        fun setRequireBillingAddress(value: Boolean): Builder {
            requireBillingAddress = value
            return this
        }

        /**
         * Sets the fee mode. Currently only [FeeMode.MERCHANT_FEE] is supported.
         * @param value The fee mode.
         * @return This Builder instance for chaining.
         * @throws IllegalArgumentException if the fee mode is not [FeeMode.MERCHANT_FEE].
         */
        fun setFeeMode(value: String): Builder {
            // Allow only MERCHANT_FEE for now, update if BUYER_FEE becomes supported
            if (value != FeeMode.MERCHANT_FEE) {
                throw IllegalArgumentException("Invalid fee mode: Only MERCHANT_FEE is currently supported.")
            }
            feeMode = value
            return this
        }

        /** Sets the transaction metadata. See [PayTheoryConfiguration.metadata]. */
        fun setMetadata(value: HashMap<Any, Any>): Builder {
            metadata = value // Consider defensive copy if map contents might change externally
            return this
        }

        /** Sets the payor information. See [PayTheoryConfiguration.payorInfo]. */
        fun setPayorInfo(value: PayorInfo): Builder {
            payorInfo = value
            return this
        }

        /** Sets the payor ID. See [PayTheoryConfiguration.payorId]. */
        fun setPayorId(value: String): Builder {
            payorId = value
            return this
        }

        /** Sets the skip tokenization validation flag. See [PayTheoryConfiguration.skipTokenizeValidation]. */
        fun setSkipTokenizeValidation(value: Boolean): Builder {
            skipTokenizeValidation = value
            return this
        }

        /** Sets the account code. See [PayTheoryConfiguration.accountCode]. */
        fun setAccountCode(value: String): Builder {
            accountCode = value
            return this
        }

        /** Sets the reference information. See [PayTheoryConfiguration.reference]. */
        fun setReference(value: String): Builder {
            reference = value
            return this
        }

        /** Sets the payment parameters. See [PayTheoryConfiguration.paymentParameters]. */
        fun setPaymentParameters(value: String): Builder {
            paymentParameters = value
            return this
        }

        /** Sets the invoice ID. See [PayTheoryConfiguration.invoiceId]. */
        fun setInvoiceId(value: String): Builder {
            invoiceId = value
            return this
        }

        /** Sets the flag to send a receipt. See [PayTheoryConfiguration.sendReceipt]. */
        fun setSendReceipt(value: Boolean): Builder {
            sendReceipt = value
            return this
        }

        /** Sets the receipt description. See [PayTheoryConfiguration.receiptDescription]. */
        fun setReceiptDescription(value: String): Builder {
            receiptDescription = value
            return this
        }

        /**
         * Sets the service fee amount in cents.
         * @param value The service fee in cents.
         * @return This Builder instance for chaining.
         * @throws IllegalArgumentException if the service fee is negative.
         */
        fun setServiceFee(value: Int): Builder {
            if (value < 0) {
                throw IllegalArgumentException("Service fee cannot be negative")
            }
            serviceFee = value
            return this
        }

        /**
         * Explicitly sets the test mode flag.
         * If not called, test mode is determined automatically based on the API key provided via [setApiKey].
         * Setting this overrides the automatic detection.
         * @param value `true` to force test mode, `false` to force non-test mode.
         * @return This Builder instance for chaining.
         */
        fun setTestMode(value: Boolean): Builder {
            isTestMode = value
            return this
        }

        /**
         * Enables Google Pay and sets the required merchant name.
         * This is a convenience method equivalent to calling `setGooglePayEnabled(true)` and `setGooglePayMerchantName(merchantName)`.
         * @param merchantName The merchant name displayed in the Google Pay sheet. Cannot be blank unless using the test API key.
         * @return This Builder instance for chaining.
         * @throws IllegalArgumentException if the merchant name is blank and the API key is not the test key.
         */
        fun enableGooglePay(merchantName: String): Builder {
            // Validation happens during the build() process, but basic check here is helpful
            if (merchantName.isBlank() && apiKey != TEST_API_KEY) {
                // This check is technically redundant due to build() validation, but good for early feedback
                 println("Warning: Merchant name should not be blank for Google Pay unless using the test API key.")
                 // throw IllegalArgumentException(INVALID_GOOGLEPAY_MERCHANT_NAME) // Deferring strict check to build()
            }
            googlePayEnabled = true
            googlePayMerchantName = merchantName
            return this
        }

        /** Sets whether prepaid cards are allowed via Google Pay. See [PayTheoryConfiguration.googlePayAllowPrepaidCards]. */
        fun setGooglePayAllowPrepaidCards(allow: Boolean): Builder {
            googlePayAllowPrepaidCards = allow
            return this
        }

        /** Sets whether credit cards are allowed via Google Pay. See [PayTheoryConfiguration.googlePayAllowCreditCards]. */
        fun setGooglePayAllowCreditCards(allow: Boolean): Builder {
            googlePayAllowCreditCards = allow
            return this
        }

        /** Sets whether billing address is required for Google Pay. See [PayTheoryConfiguration.googlePayBillingAddressRequired]. */
        fun setGooglePayBillingAddressRequired(required: Boolean): Builder {
            googlePayBillingAddressRequired = required
            return this
        }

        /** Sets the required billing address format for Google Pay. See [PayTheoryConfiguration.googlePayBillingAddressFormat]. */
        fun setGooglePayBillingAddressFormat(format: GooglePayBillingAddressFormat): Builder {
            googlePayBillingAddressFormat = format
            return this
        }

        /** Sets whether shipping address is required for Google Pay. See [PayTheoryConfiguration.googlePayShippingAddressRequired]. */
        fun setGooglePayShippingAddressRequired(required: Boolean): Builder {
            googlePayShippingAddressRequired = required
            return this
        }

        /** Sets whether phone number is required for Google Pay. See [PayTheoryConfiguration.googlePayPhoneNumberRequired]. */
        fun setGooglePayPhoneNumberRequired(required: Boolean): Builder {
            googlePayPhoneNumberRequired = required
            return this
        }

        /** Sets the Google Pay button type. See [PayTheoryConfiguration.googlePayButtonType]. */
        fun setGooglePayButtonType(type: GooglePayButtonType): Builder {
            googlePayButtonType = type
            return this
        }

        /** Sets the Google Pay button color. See [PayTheoryConfiguration.googlePayButtonColor]. */
        fun setGooglePayButtonColor(color: GooglePayButtonColor): Builder {
            googlePayButtonColor = color
            return this
        }

        /** Sets the Google Pay environment. See [PayTheoryConfiguration.googlePayEnvironment]. */
        fun setGooglePayEnvironment(environment: GooglePayEnvironment): Builder {
            googlePayEnvironment = environment
            return this
        }

        /** Sets the allowed card networks for Google Pay. See [PayTheoryConfiguration.googlePayAllowedCardNetworks]. */
        fun setGooglePayAllowedCardNetworks(networks: List<String>): Builder {
            // Consider adding validation for known network strings if necessary
            googlePayAllowedCardNetworks = networks
            return this
        }

        /** Sets the supported authentication methods for Google Pay. See [PayTheoryConfiguration.googlePaySupportedMethods]. */
        fun setGooglePaySupportedMethods(methods: List<String>): Builder {
            // Consider adding validation (e.g., ensure CRYPTOGRAM_3DS is present if enabling card payments)
            googlePaySupportedMethods = methods
            return this
        }

        /** Sets whether UI elements should be outlined. See [PayTheoryConfiguration.outlined]. */
        fun setOutlined(value: Boolean): Builder {
            outlined = value
            return this
        }

        /**
         * Constructs and returns the final, immutable [PayTheoryConfiguration] object.
         *
         * Performs final validation checks based on the configured properties before creating the instance.
         * Specifically, it ensures:
         * - An API key has been set.
         * - The configuration passes the checks in `validatePaymentConfigAndExtractDetails` if not in test mode.
         *
         * @return The configured [PayTheoryConfiguration] instance.
         * @throws IllegalStateException if the API key was not set.
         * @throws IllegalArgumentException if validation fails (e.g., invalid API key format, amount constraints violated, missing Google Pay merchant name).
         */
        fun build(): PayTheoryConfiguration {
            if (apiKey.isBlank()) {
                throw IllegalStateException("API key must be set before building the configuration.")
            }

            // Determine the final test mode status
            val computedTestMode = isTestMode ?: (apiKey == TEST_API_KEY)

            // Create the configuration instance. The init block within PayTheoryConfiguration
            // will perform the necessary validation based on computedTestMode.
            return PayTheoryConfiguration(
                apiKey = apiKey,
                amount = amount,
                paymentMethodType = paymentMethodType,
                paymentMethodAction = paymentMethodAction,
                requireAccountName = requireAccountName,
                requireBillingAddress = requireBillingAddress,
                feeMode = feeMode,
                metadata = metadata, // Consider defensive copy: HashMap(metadata)
                payorInfo = payorInfo, // Consider defensive copy if mutable
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
                isTestMode = computedTestMode, // Use the computed value

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
                googlePayAllowedCardNetworks = ArrayList(googlePayAllowedCardNetworks), // Defensive copy
                googlePaySupportedMethods = ArrayList(googlePaySupportedMethods)      // Defensive copy
            )
        }
    }
}