package com.paytheory.android.sdk.fragments

import android.accounts.NetworkErrorException
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.sdk.*
import com.paytheory.android.sdk.configuration.*
import com.paytheory.android.sdk.data.Address
import com.paytheory.android.sdk.data.PaymentDetail
import com.paytheory.android.sdk.data.PaymentMethodTokenData
import com.paytheory.android.sdk.data.PayorInfo
import com.paytheory.android.sdk.state.ACHState
import com.paytheory.android.sdk.state.CardState
import com.paytheory.android.sdk.state.CashState
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.sdk.view.PayTheoryEditText
import com.paytheory.android.sdk.watchers.*

private val Utility = Utility()
private const val PAYTHEORYLAB = "paytheorylab"
private const val PAYTHEORYSTUDY = "paytheorystudy"
private const val PAYTHEORY = "paytheory"
private const val NO_NETWORK_CONNECTION = "No valid network connection"
private const val INVALID_APIKEY = "Invalid apikey"
private const val INVALID_AMOUNT = "Invalid amount"

/*
* Modernization
* Watcher calls have been updated to include fragment as parameter
* this is to support ValidAndEmpty Protocol
* */

/**
 * PayTheoryFragment populates required transaction input fields.
 * Flexible display that adds inputs dynamically as requested.
 */
class PayTheoryFragment : Fragment() {
    companion object {
        const val PAYMENT_CARD = "card"
        const val BANK_ACCOUNT = "ach"
        const val CASH = "cash"
    }
    private var apiKey: String? = null
    private var amount: Int? = null
    private var paymentMethodType: PaymentMethodType? = null
    private var tokenizationType: TokenizationType? = null
    private var requireAccountName: Boolean? = null
    private var requireBillingAddress: Boolean? = null
    private var confirmation: Boolean? = null
    private var feeMode: String? = null
    private var metadata: HashMap<Any, Any>? = null
    private var payTheoryData: HashMap<Any, Any>? = null
    private var payorInfo: PayorInfo? = null
    private var payorId: String? = null
    private var accountCode: String? = null
    private var reference: String? = null
    private var paymentParameters: String? = null
    private var invoiceId: String? = null
    private var sendReceipt: Boolean? = null
    private var receiptDescription: String? = null
    private var billingAddress: Address? = null
    private var accountName: String? = null
    private var partner: String? = null
    private var stage: String? = null
    private var constants: Constants? = null
    private var payTheoryPayment: Payment? = null
    private var payTheoryTokenizeTransaction: PaymentMethodToken? = null
    private var model: ConfigurationViewModel? = null
    private lateinit var submitButton: PayTheoryButton


    var card: CardState = CardState()
    var ach: ACHState = ACHState()
    var cash: CashState = CashState()

    /**
     * Display requested card fields
     * @return fragment_pay_theory_credit_card layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.pay_theory_fragment, container, false)
    }


    override fun onDetach() {
        super.onDetach()
        if (payTheoryPayment != null) {
            payTheoryPayment!!.disconnect()
        }
        if (payTheoryTokenizeTransaction != null) {
            payTheoryTokenizeTransaction!!.disconnect()
        }
        if (model !== null) {
            model!!.update(ConfigurationDetail())
        }
    }

    /**
     * Configures field validation for payment fields by adding text watchers that validate
     * input and enable/disable the submit button accordingly.
     *
     * This function is called during payment configuration to set up validation for card, ACH, and cash payment fields.
     */
    private fun configureFieldValidation() {
        //if card payment fields are active add text watcher validation
        if (this.paymentMethodType==PaymentMethodType.CARD) {
            // get credit card fields

            val ccNumber = requireView().findViewById<PayTheoryEditText>(R.id.cc_number)
            val ccCVV = requireView().findViewById<PayTheoryEditText>(R.id.cc_cvv)
            val ccExpiration = requireView().findViewById<PayTheoryEditText>(R.id.cc_expiration)
            val billingZip = requireView().findViewById<PayTheoryEditText>(R.id.billing_zip)

            val ccNumberValidation: (PayTheoryEditText, PayTheoryFragment) -> CardNumberTextWatcher =
                { ptText, ptFragment -> CardNumberTextWatcher(ptText, ptFragment, submitButton)  }
            val cvvNumberValidation: (PayTheoryEditText) -> CVVTextWatcher =
                { pt -> CVVTextWatcher(pt, this, submitButton) }
            val expirationValidation: (PayTheoryEditText) -> ExpirationTextWatcher =
                { pt -> ExpirationTextWatcher(pt, this, submitButton) }
            val zipCodeValidation: (PayTheoryEditText) -> ZipCodeTextWatcher =
                { pt -> ZipCodeTextWatcher(pt, this, submitButton) }
            ccNumber.addTextChangedListener(ccNumberValidation(ccNumber,this))
            ccCVV.addTextChangedListener(cvvNumberValidation(ccCVV))
            ccExpiration.addTextChangedListener(expirationValidation(ccExpiration))
            billingZip.addTextChangedListener(zipCodeValidation(billingZip))
        }

        //if bank payment fields are active add text watcher validation
        if (this.paymentMethodType==PaymentMethodType.BANK) {
            // get ach fields
            val (achAccount, achRouting) = Utility.getAchFields(this.requireView())
            val achChooser: AppCompatAutoCompleteTextView =
                requireView().findViewById(R.id.ach_type_choice)
            val items = listOf(getString(R.string.checking), getString(R.string.savings))
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
            achChooser.setAdapter(adapter)

            val achRoutingNumberValidation: (PayTheoryEditText) -> RoutingNumberTextWatcher =
                { pt -> RoutingNumberTextWatcher(pt, this, submitButton)  }
            val achAccountNumberValidation: (PayTheoryEditText) -> AccountNumberTextWatcher =
                { pt -> AccountNumberTextWatcher(pt, this, submitButton)  }
            achRouting.addTextChangedListener(achRoutingNumberValidation(achRouting))
            achAccount.addTextChangedListener(achAccountNumberValidation(achAccount))
        }

        //if cash payment fields are active add text watcher validation
        if (this.paymentMethodType==PaymentMethodType.CASH) {
            // get cash fields
            val cashContact =
                requireView().findViewById<PayTheoryEditText>(R.id.cashContact)
            val cashName = requireView().findViewById<PayTheoryEditText>(R.id.cashName)

            val cashContactValidation: (PayTheoryEditText) -> CashContactTextWatcher =
                { pt -> CashContactTextWatcher(pt, this, submitButton) }

            val cashNameValidation: (PayTheoryEditText) -> CashNameTextWatcher =
                { pt -> CashNameTextWatcher(pt, this, submitButton) }

            cashContact.addTextChangedListener(
                cashContactValidation(
                    cashContact
                )
            )
            cashName.addTextChangedListener(
                cashNameValidation(
                    cashName
                )
            )
        }
    }


    /**
     * Parse and validate payment configuration object
     * configuration.apiKey must exist and contain a valid partner and stage
     * configuration.amount must be greater than 0
     * @param configuration PayTheoryConfiguration containing an apiKey and amount
     * @return Pair<partnerName:String, stageName:String>
     */
    private fun validatePaymentConfigAndExtractDetails(configuration: PayTheoryConfiguration): Pair<String, String> {
        // Validation checks for input parameters
        if (configuration.apiKey.isBlank()) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (!configuration.apiKey.contains(PAYTHEORY)) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }

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

    /**
     * Apply configurations to execute a payment
     * configuration: PayTheoryConfiguration
     */
    @Throws(Exception::class)
    fun configurePayment(
        configuration: PayTheoryConfiguration
    ) {
        //Check internet
        if (!isNetworkAvailable(this.requireContext())) {
            throw NetworkErrorException(NO_NETWORK_CONNECTION)
        }
        val (partnerName, stageName) = validatePaymentConfigAndExtractDetails(configuration)

        // Set private variables
        applyConfiguration(configuration)

        //ensure button is disabled before validation
        this.submitButton.disable()

        this.partner = partnerName
        this.stage = stageName
        this.constants = Constants(partner!!, stage!!)

        // Create pay_theory_data object for transaction message
        payTheoryData = Utility.createPayTheoryData(configuration)

        model = ViewModelProvider(
            this,
            ConfigurationInjector(requireActivity().application,
            ConfigurationDetail()).provideConfigurationViewModelFactory()
        )[ConfigurationViewModel::class.java]

        // update Configuration Details object with payment data
        model!!.update(
            ConfigurationDetail(
                apiKey = this.apiKey,
                amount = this.amount,
                paymentMethodType = this.paymentMethodType,
                requireAccountName = this.requireAccountName,
                requireBillingAddress = this.requireBillingAddress,
                confirmation = this.confirmation,
                feeMode = this.feeMode,
                metadata = this.metadata,
                payorInfo = this.payorInfo,
                payorId = this.payorId,
                accountCode = this.accountCode,
                reference = this.reference,
                paymentParameters = this.paymentParameters,
                invoiceId = this.invoiceId,
                sendReceipt = this.sendReceipt,
                receiptDescription = this.receiptDescription
            )
        )

        payTheoryPayment =
            Payment(
                this.requireActivity() as Payable,
                this.partner!!,
                this.stage!!,
                this.constants!!,
                this.payTheoryData,
                configuration
            )



        Utility.enablePaymentFields(this.requireView(),this.paymentMethodType!!, this.requireAccountName!!, this.requireBillingAddress!!)

        configureFieldValidation()
    }

    private fun applyConfiguration(configuration: PayTheoryConfiguration) {
        this.apiKey = configuration.apiKey
        this.amount = configuration.amount
        this.submitButton = configuration.payTheoryButton
        this.paymentMethodType = configuration.paymentMethodType
        this.requireAccountName = configuration.requireAccountName
        this.requireBillingAddress = configuration.requireBillingAddress
        this.confirmation = configuration.confirmation
        this.feeMode = configuration.feeMode
        this.metadata = configuration.metadata
        this.payorInfo = configuration.payorInfo
        // Set private variables for payTheoryData
        this.sendReceipt = configuration.sendReceipt
        this.receiptDescription = configuration.receiptDescription
        this.paymentParameters = configuration.paymentParameters
        this.payorId = configuration.payorId
        this.invoiceId = configuration.invoiceId
        this.accountCode = configuration.accountCode
        this.reference = configuration.reference
    }

    /**
     * This function is used to submit a payment to Pay Theory or generate a barcode.
     *
     * It collects the payment details from the input fields, performs basic validation,
     * and initiates the payment process using the configured PayTheoryPayment object.
     * It supports card, bank account, and cash payment methods.
     */
    fun transact(){
        submitButton.disable()
        if (this.requireAccountName==true || paymentMethodType==PaymentMethodType.BANK) {
            val accountName = requireView().findViewById<PayTheoryEditText>(R.id.account_name)
            this.accountName = accountName.text.toString()
            accountName.text!!.clear()
        }
        // Zipcode required for all payments
        val billingZip = requireView().findViewById<PayTheoryEditText>(R.id.billing_zip)
        //If all billing address fields are visible get all field data
        if (this.requireBillingAddress==true) {
            val billingAddress1 =
                requireView().findViewById<PayTheoryEditText>(R.id.billing_address_1)
            val billingAddress2 =
                requireView().findViewById<PayTheoryEditText>(R.id.billing_address_2)
            val billingCity = requireView().findViewById<PayTheoryEditText>(R.id.billing_city)
            val billingState = requireView().findViewById<PayTheoryEditText>(R.id.billing_state)

            this.billingAddress = Address(
                billingAddress1.text.toString().ifBlank { "" },
                billingAddress2.text.toString().ifBlank { "" },
                billingCity.text.toString().ifBlank { "" },
                billingState.text.toString().ifBlank { "" },
                billingZip.text.toString().ifBlank { "" },
                "USA"
            )
            billingAddress1.text!!.clear()
            billingAddress2.text!!.clear()
            billingCity.text!!.clear()
            billingState.text!!.clear()
            billingZip.text!!.clear()
        // else just get zip code
        } else {
            this.billingAddress = Address(
                "",
                "",
                "",
                "",
                billingZip.text.toString().ifBlank { "" },
                "USA"
            )
            billingZip.text!!.clear()
        }
        //Create card payment
        if (paymentMethodType==PaymentMethodType.CARD) {
            val ccExpiration = requireView().findViewById<PayTheoryEditText>(R.id.cc_expiration)
            val ccNumber = requireView().findViewById<PayTheoryEditText>(R.id.cc_number)
            val ccCVV = requireView().findViewById<PayTheoryEditText>(R.id.cc_cvv)
            val expirationMonth = ccExpiration.text.toString().split("/").first()
            val expirationYear = "20" + ccExpiration.text.toString().split("/").last()

            val payment = PaymentDetail(
                timing = System.currentTimeMillis(),
                amount = this.amount!!,
                type = PAYMENT_CARD,
                name = if (!this.accountName.isNullOrBlank()) this.accountName else "",
                number = ccNumber.text.toString().replace("\\s".toRegex(), ""),
                security_code = ccCVV.text.toString(),
                expiration_month = expirationMonth,
                expiration_year = expirationYear,
                fee_mode = this.feeMode,
                address = this.billingAddress,
                payorInfo = this.payorInfo
            )
            executePaymentTransaction(payment)
            ccExpiration.text!!.clear()
            ccNumber.text!!.clear()
            ccCVV.text!!.clear()
            ccExpiration.text!!.clear()
            cardFieldValid = false
            expFieldValid = false
            cvvFieldValid = false
            zipCodeFieldValid = false
            cardFieldsValid = false
        }
        //Create bank paymentToken
        if (paymentMethodType==PaymentMethodType.BANK) {
            val achChooser: AppCompatAutoCompleteTextView = requireView().findViewById(R.id.ach_type_choice)
            val (achAccount, achRouting) = Utility.getAchFields(this.requireView())
            val accountType = achChooser.text.toString()

            val payment = PaymentDetail(
                timing = System.currentTimeMillis(),
                amount = this.amount!!,
                account_type = accountType,
                type = BANK_ACCOUNT,
                name = this.accountName,
                account_number = achAccount.text.toString(),
                bank_code = achRouting.text.toString(),
                address = this.billingAddress,
                fee_mode = this.feeMode,
                payorInfo = this.payorInfo
            )
            executePaymentTransaction(payment)
            achChooser.text!!.clear()
            achAccount.text!!.clear()
            achRouting.text!!.clear()
            accountNumberValid = false
            routingNumberValid = false
            bankFieldsValid = false
        }
        //Create cash payment
        if (paymentMethodType==PaymentMethodType.CASH) {
            val cashContact = requireView().findViewById<PayTheoryEditText>(R.id.cashContact)
            val cashName = requireView().findViewById<PayTheoryEditText>(R.id.cashName)

            val contact = cashContact.text.toString()
            val buyer = cashName.text.toString()

            val payment = PaymentDetail(
                timing = System.currentTimeMillis(),
                amount = this.amount!!,
                type = CASH,
                buyer = buyer,
                fee_mode = this.feeMode,
                address = this.billingAddress,
                buyerContact = contact,
                payorInfo = this.payorInfo
            )
            executePaymentTransaction(payment)
            cashContact.text!!.clear()
            cashName.text!!.clear()
            cashContactFieldValid = false
            cashNameFieldValid = false
        }
    }

    /**
     * Apply configurations to execute tokenization of the payment method
     *
     * This function initializes the tokenization process by setting up the necessary
     * configurations, validating input, and creating a PayMethodToken object.
     * It supports card and bank account tokenization.
     *
     * configuration: PayTheoryConfiguration
     */
    @Throws(Exception::class)
    fun configureTokenize(
        configuration: PayTheoryConfiguration
    ) {
        //Check internet
        if (!isNetworkAvailable(this.requireContext())) {
            throw NetworkErrorException(NO_NETWORK_CONNECTION)
        }
        val (partnerName, stageName) = validateAndExtractTokenDetails(configuration)

        // Set private variables
        //ensure button is disabled before validation
        this.submitButton = configuration.payTheoryButton
        this.submitButton.disable()
        this.apiKey = configuration.apiKey
        this.paymentMethodType = configuration.paymentMethodType
        this.metadata = configuration.metadata
        this.payorInfo = configuration.payorInfo
        this.requireAccountName = configuration.requireAccountName
        this.requireBillingAddress = configuration.requireBillingAddress
        this.partner = partnerName
        this.stage = stageName
        this.constants = Constants(partner!!, stage!!)

        // Set private variables for payTheoryData
        this.payorId = configuration.payorId

        // Create pay_theory_data object for transaction message
        payTheoryData = Utility.createPayTheoryData(configuration)

        model = ViewModelProvider(
            this,
            ConfigurationInjector(requireActivity().application,
                ConfigurationDetail()).provideConfigurationViewModelFactory()
        )[ConfigurationViewModel::class.java]

        // update Configuration Details object with payment data
        model!!.update(
            ConfigurationDetail(
                apiKey = this.apiKey,
                paymentMethodType = this.paymentMethodType,
                metadata = this.metadata,
                payorInfo = this.payorInfo,
                payorId = this.payorId,
                requireAccountName = this.requireAccountName,
                requireBillingAddress = this.requireBillingAddress
            )
        )

        payTheoryTokenizeTransaction =
            PaymentMethodToken(
                this.requireActivity() as Payable,
                this.partner!!,
                this.stage!!,
                this.constants!!,
                this.payTheoryData,
                configuration
            )

        Utility.enableTokenizationFields(this.requireView(), this.paymentMethodType!!, this.requireAccountName!!,
            this.requireBillingAddress!!)

        configureFieldValidation()
    }

    /**
     * Parse and validate tokenization configuration object
     * configuration.apiKey must exist and contain a valid partner and stage
     * configuration.amount must be greater than 0
     * @param configuration PayTheoryConfiguration containing an apiKey with amount of 0
     * @return Pair<partnerName:String, stageName:String>
     */
    private fun validateAndExtractTokenDetails(configuration: PayTheoryConfiguration): Pair<String, String> {
        // Validation checks for input parameters
        if (configuration.apiKey.isBlank()) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (!configuration.apiKey.contains(PAYTHEORY)) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }

        val partnerName = configuration.apiKey.substring(0, configuration.apiKey.indexOf('-'))
        val stageName = configuration.apiKey.substring(
            configuration.apiKey.indexOf('-') + 1,
            configuration.apiKey.indexOf('-', configuration.apiKey.indexOf('-') + 1)
        )

        if (stageName != PAYTHEORYLAB && stageName != PAYTHEORYSTUDY && stageName != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (configuration.amount > 0) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }
        return Pair(partnerName, stageName)
    }

    /**
     * This function is used to tokenize a card or bank account.
     *
     * It collects the payment details from the input fields, performs basic validation,
     * and initiates the tokenization process using the configured PayTheoryTokenizeTransaction object.
     * It supports card and bank account tokenization methods.
     */
    fun tokenize(){
        submitButton.disable()
        if (this.requireAccountName==true) {
            val accountName = requireView().findViewById<PayTheoryEditText>(R.id.account_name)
            this.accountName = accountName.text.toString()
            accountName.text!!.clear()
        }
        // Zipcode required for all payments
        val billingZip = requireView().findViewById<PayTheoryEditText>(R.id.billing_zip)
        //If all billing address fields are visible get all field data
        if (this.requireBillingAddress==true) {
            val billingAddress1 =
                requireView().findViewById<PayTheoryEditText>(R.id.billing_address_1)
            val billingAddress2 =
                requireView().findViewById<PayTheoryEditText>(R.id.billing_address_2)
            val billingCity = requireView().findViewById<PayTheoryEditText>(R.id.billing_city)
            val billingState = requireView().findViewById<PayTheoryEditText>(R.id.billing_state)

            this.billingAddress = Address(
                billingAddress1.text.toString().ifBlank { "" },
                billingAddress2.text.toString().ifBlank { "" },
                billingCity.text.toString().ifBlank { "" },
                billingState.text.toString().ifBlank { "" },
                billingZip.text.toString().ifBlank { "" },
                null
            )

            billingAddress1.text!!.clear()
            billingAddress2.text!!.clear()
            billingCity.text!!.clear()
            billingState.text!!.clear()
            billingZip.text!!.clear()
            // else just get zip code
        } else {
            this.billingAddress = Address(
                null,
                null,
                null,
                null,
                billingZip.text.toString().ifBlank { "" },
                null
            )
            billingZip.text!!.clear()
        }

        //Create card paymentToken
        if (this.paymentMethodType==PaymentMethodType.CARD) {
            val ccExpiration = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_expiration)
            val ccNumber = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_number)
            val ccCVV = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_cvv)
            val expirationMonth = ccExpiration.text.toString().split("/").first()
            val expirationYear = "20" + ccExpiration.text.toString().split("/").last()

            val paymentToken = PaymentMethodTokenData(
                timing = System.currentTimeMillis(),
                type = PAYMENT_CARD,
                name = if (!this.accountName.isNullOrBlank()) this.accountName else "",
                number = ccNumber.text.toString().replace("\\s".toRegex(), ""),
                security_code = ccCVV.text.toString(),
                expiration_month = expirationMonth,
                expiration_year = expirationYear,
                address = this.billingAddress,
                payorInfo = this.payorInfo
            )


            makePaymentMethodToken(paymentToken)
            ccExpiration.text!!.clear()
            ccNumber.text!!.clear()
            ccCVV.text!!.clear()
            ccExpiration.text!!.clear()
            cardFieldValid = false
            expFieldValid = false
            cvvFieldValid = false
            zipCodeFieldValid = false
            cardFieldsValid = false
        }

        //Create bank paymentToken
        if (this.paymentMethodType == PaymentMethodType.BANK) {
            val achChooser: AppCompatAutoCompleteTextView = requireView().findViewById(R.id.ach_type_choice)
            val (achAccount, achRouting) = Utility.getAchFields(this.requireView())
            val accountName = requireView().findViewById<PayTheoryEditText>(R.id.account_name)
            this.accountName = accountName.text.toString()
            val accountType = achChooser.text.toString()

            val paymentToken = PaymentMethodTokenData(
                timing = System.currentTimeMillis(),
                account_type = accountType,
                type = BANK_ACCOUNT,
                name = if (!this.accountName.isNullOrBlank()) this.accountName else "",
                account_number = achAccount.text.toString(),
                bank_code = achRouting.text.toString(),
                address = this.billingAddress,
                payorInfo = this.payorInfo
            )
            makePaymentMethodToken(paymentToken)
            achChooser.text!!.clear()
            achAccount.text!!.clear()
            achRouting.text!!.clear()
            accountNumberValid = false
            routingNumberValid = false
            bankFieldsValid = false
        }
    }

    /**
     * Initiates a payment transaction using the provided payment details.
     *
     * @param payment The payment details object containing information about the payment.
     */
    private fun executePaymentTransaction(payment: PaymentDetail) {
        payTheoryPayment!!.transact(payment)
    }

    /**
     * Initiates the process of generating a payment method token.
     *
     * @param paymentMethodTokenData The data required to generate the payment method token.
     */
    private fun makePaymentMethodToken(paymentMethodTokenData: PaymentMethodTokenData) {
        payTheoryTokenizeTransaction!!.tokenize(paymentMethodTokenData)
    }
}



/**
 * Checks if the device has an active network connection.
 *
 * @param context The context to access system services.
 * @return True if the device has an active network connection, false otherwise.
 */
private fun isNetworkAvailable(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } == true
    }