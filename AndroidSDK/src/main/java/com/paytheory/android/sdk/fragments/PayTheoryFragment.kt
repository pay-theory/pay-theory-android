package com.paytheory.android.sdk.fragments

import Address
import Payment
import PaymentMethodTokenData
import PayorInfo
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
    private var transactionType: TransactionType? = null
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
    private var payTheoryTransaction: Transaction? = null
    private var payTheoryTokenizeTransaction: PaymentMethodToken? = null
    private var model: ConfigurationViewModel? = null
    private lateinit var submitButton: PayTheoryButton


    /**
     * Display requested card fields
     * @return fragment_pay_theory_credit_card layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_pay_theory, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        if (payTheoryTransaction != null) {
            payTheoryTransaction!!.disconnect()
        }
        if (payTheoryTokenizeTransaction != null) {
            payTheoryTokenizeTransaction!!.disconnect()
        }
        if (model !== null) {
            model!!.update(ConfigurationDetail())
        }
    }

    /**
     * Create configurations to execute a payment and contains default configuration
     * @param apiKey Your Pay Theory api-key
     * @param amount Amount of transaction in USD cents
     * @param transactionType TransactionType.CARD or TransactionType.BANK or TransactionType.CASH
     * @param requireAccountName Enable account name for the transaction
     * @param requireBillingAddress Enable billing address for the transaction
     * @param confirmation: Enable a user confirmation step for the transaction
     * @param feeMode Pay Theory Fee Mode (FeeMode.INTERCHANGE or FeeMode.SERVICE_FEE)
     * @param metadata Optional Transaction metadata
     * @param payorInfo Optional details about the payor
     * @param payorId Optional Pay Theory payorId
     * @param accountCode Optional account code for the transaction
     * @param reference Optional reference for the transaction
     * @param paymentParameters Optional Pay Theory Payment Parameters
     * @param invoiceId Optional Pay Theory invoiceId
     * @param sendReceipt Enable a receipt to be sent to a payor for the transaction
     * @param receiptDescription Add description to receipt for transaction
     */
    @Throws(Exception::class)
    fun configureTransact(
        paymentButton: PayTheoryButton,
        apiKey: String,
        amount: Int,
        transactionType: TransactionType? = TransactionType.CARD,
        requireAccountName: Boolean? = false,
        requireBillingAddress: Boolean? = false,
        confirmation: Boolean? = false,
        feeMode: String? = FeeMode.INTERCHANGE,
        metadata: HashMap<Any, Any>? = HashMap(),
        payorInfo: PayorInfo? = PayorInfo(),
        payorId: String? = null,
        accountCode: String? = null,
        reference: String? = null,
        paymentParameters: String? = null,
        invoiceId: String? = null,
        sendReceipt: Boolean? = false,
        receiptDescription: String? = null
    ) {
        //Check internet
        if (!isNetworkAvailable(this.requireContext())) {
            throw NetworkErrorException(NO_NETWORK_CONNECTION)
        }
        // Validation checks for input parameters
        if (apiKey.isBlank()) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (!apiKey.contains(PAYTHEORY)) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }

        val partnerName = apiKey.substring(0, apiKey.indexOf('-'))
        val stageName = apiKey.substring(apiKey.indexOf('-') + 1, apiKey.indexOf('-', apiKey.indexOf('-') + 1))

        if (stageName != PAYTHEORYLAB && stageName != PAYTHEORYSTUDY && stageName != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (amount == 0) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }

        // Set private variables
        //ensure button is disabled before validation
        this.submitButton = paymentButton
        this.submitButton.disable()
        this.apiKey = apiKey
        this.amount = amount
        this.transactionType = transactionType
        this.requireAccountName = requireAccountName
        this.requireBillingAddress = requireBillingAddress
        this.confirmation = confirmation
        this.feeMode = feeMode
        this.metadata = metadata
        this.payorInfo = payorInfo
        this.partner = partnerName
        this.stage = stageName
        this.constants = Constants(partner!!, stage!!)

        // Set private variables for payTheoryData
        this.sendReceipt = sendReceipt
        this.receiptDescription = receiptDescription
        this.paymentParameters = paymentParameters
        this.payorId = payorId
        this.invoiceId = invoiceId
        this.accountCode = accountCode
        this.reference = reference

        // Create pay_theory_data object for transaction message
        payTheoryData = Utility.createPayTheoryData(sendReceipt, receiptDescription, paymentParameters, payorId, invoiceId, accountCode, reference)

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
                transactionType = this.transactionType,
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

        payTheoryTransaction =
            Transaction(
                this.requireActivity(),
                this.partner!!,
                this.stage!!,
                this.apiKey!!,
                this.feeMode!!,
                this.constants!!,
                this.confirmation,
                this.sendReceipt,
                this.receiptDescription,
                this.metadata,
                this.payTheoryData
            )

        Utility.enablePaymentFields(this.requireView(),this.transactionType!!, this.requireAccountName!!, this.requireBillingAddress!!)

        //if card payment fields are active add text watcher validation
        if (this.transactionType==TransactionType.CARD) {
            // get credit card fields
            val ccNumber = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_number)
            val ccCVV = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_cvv)
            val ccExpiration = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_expiration)
            val billingZip = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_zip)

            val ccNumberValidation: (PayTheoryEditText) -> CardNumberTextWatcher =
                { pt -> CardNumberTextWatcher(pt, submitButton)  }
            val cvvNumberValidation: (PayTheoryEditText) -> CVVTextWatcher =
                { pt -> CVVTextWatcher(pt, submitButton) }
            val expirationValidation: (PayTheoryEditText) -> ExpirationTextWatcher =
                { pt -> ExpirationTextWatcher(pt, submitButton) }
            val zipCodeValidation: (PayTheoryEditText) -> ZipCodeTextWatcher =
                { pt -> ZipCodeTextWatcher(pt, submitButton) }
            ccNumber.addTextChangedListener(ccNumberValidation(ccNumber))
            ccCVV.addTextChangedListener(cvvNumberValidation(ccCVV))
            ccExpiration.addTextChangedListener(expirationValidation(ccExpiration))
            billingZip.addTextChangedListener(zipCodeValidation(billingZip))
        }

        //if bank payment fields are active add text watcher validation
        if (this.transactionType==TransactionType.BANK) {
            // get ach fields
            val (achAccount, achRouting) = Utility.getAchFields(this.requireActivity())
            val achChooser: AppCompatAutoCompleteTextView =
                requireActivity().findViewById(R.id.ach_type_choice)
            val items = listOf(getString(R.string.checking), getString(R.string.savings))
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
            achChooser.setAdapter(adapter)

            val achRoutingNumberValidation: (PayTheoryEditText) -> RoutingNumberTextWatcher =
                { pt -> RoutingNumberTextWatcher(pt, submitButton)  }
            val achAccountNumberValidation: (PayTheoryEditText) -> AccountNumberTextWatcher =
                { pt -> AccountNumberTextWatcher(pt, submitButton)  }
            achRouting.addTextChangedListener(achRoutingNumberValidation(achRouting))
            achAccount.addTextChangedListener(achAccountNumberValidation(achAccount))
        }

        //if cash payment fields are active add text watcher validation
        if (this.transactionType==TransactionType.CASH) {
            // get cash fields
            val cashContact =
                requireActivity().findViewById<PayTheoryEditText>(R.id.cashContact)
            val cashName = requireActivity().findViewById<PayTheoryEditText>(R.id.cashName)

            val cashContactValidation: (PayTheoryEditText) -> CashContactTextWatcher =
                { pt -> CashContactTextWatcher(pt, submitButton) }

            val cashNameValidation: (PayTheoryEditText) -> CashNameTextWatcher =
                { pt -> CashNameTextWatcher(pt, submitButton) }

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

    fun transact(){
        if (this.requireAccountName==true) {
            val accountName = requireActivity().findViewById<PayTheoryEditText>(R.id.account_name)
            this.accountName = accountName.text.toString()
        }
        // Zipcode required for all payments
        val billingZip = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_zip)
        //If all billing address fields are visible get all field data
        if (this.requireBillingAddress==true) {
            val billingAddress1 =
                requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_1)
            val billingAddress2 =
                requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_2)
            val billingCity = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_city)
            val billingState = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_state)

            this.billingAddress = Address(
                billingAddress1.text.toString().ifBlank { "" },
                billingAddress2.text.toString().ifBlank { "" },
                billingCity.text.toString().ifBlank { "" },
                billingState.text.toString().ifBlank { "" },
                billingZip.text.toString().ifBlank { "" },
                "USA"
            )
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
        }
        //Create card payment
        if (transactionType==TransactionType.CARD) {
            val ccExpiration = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_expiration)
            val ccNumber = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_number)
            val ccCVV = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_cvv)
            val expirationMonth = ccExpiration.text.toString().split("/").first()
            val expirationYear = "20" + ccExpiration.text.toString().split("/").last()

            val payment = Payment(
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
            makePayment(payment)
        }
        //Create bank paymentToken
        if (transactionType==TransactionType.BANK) {
            val achChooser: AppCompatAutoCompleteTextView = requireActivity().findViewById(R.id.ach_type_choice)
            val (achAccount, achRouting) = Utility.getAchFields(this.requireActivity())
            val accountName = requireActivity().findViewById<PayTheoryEditText>(R.id.account_name)
            this.accountName = accountName.text.toString()
            val accountType = achChooser.text.toString()

            val payment = Payment(
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
            makePayment(payment)
        }
        //Create cash payment
        if (transactionType==TransactionType.CASH) {
            val cashContact = requireActivity().findViewById<PayTheoryEditText>(R.id.cashContact)
            val cashName = requireActivity().findViewById<PayTheoryEditText>(R.id.cashName)

            val contact = cashContact.text.toString()
            val buyer = cashName.text.toString()

            val payment = Payment(
                timing = System.currentTimeMillis(),
                amount = this.amount!!,
                type = CASH,
                buyer = buyer,
                fee_mode = this.feeMode,
                address = this.billingAddress,
                buyerContact = contact,
                payorInfo = this.payorInfo
            )
            makePayment(payment)
        }
    }



    /**
     * Create configurations to execute a tokenization of the payment method
     * @param apiKey Your Pay Theory api-key
     * @param tokenizationType TokenizationType.CARD or TokenizationType.BANK
     * @param requireAccountName Enable account name for the transaction
     * @param requireBillingAddress Enable billing address for the transaction
     * @param payorInfo Optional details about the payor
     * @param payorId Optional Pay Theory payorId
     * @param metadata Optional Transaction metadata
     */
    @Throws(Exception::class)
    fun configureTokenize(
        tokenizeButton: PayTheoryButton,
        apiKey: String,
        tokenizationType: TokenizationType? = TokenizationType.CARD,
        requireAccountName: Boolean? = false,
        requireBillingAddress: Boolean? = false,
        payorInfo: PayorInfo? = PayorInfo(),
        payorId: String? = null,
        metadata: HashMap<Any, Any>? = HashMap(),
    ) {
        //Check internet
        if (!isNetworkAvailable(this.requireContext())) {
            throw NetworkErrorException(NO_NETWORK_CONNECTION)
        }
        // Validation checks for input parameters
        if (apiKey.isBlank()) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (!apiKey.contains(PAYTHEORY)) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }

        val partnerName = apiKey.substring(0, apiKey.indexOf('-'))
        val stageName = apiKey.substring(apiKey.indexOf('-') + 1, apiKey.indexOf('-', apiKey.indexOf('-') + 1))

        if (stageName != PAYTHEORYLAB && stageName != PAYTHEORYSTUDY && stageName != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (amount == 0) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }

        // Set private variables
        //ensure button is disabled before validation
        this.submitButton = tokenizeButton
        this.submitButton.disable()
        this.apiKey = apiKey
        this.tokenizationType = tokenizationType
        this.metadata = metadata
        this.payorInfo = payorInfo
        this.requireAccountName = requireAccountName
        this.requireBillingAddress = requireBillingAddress
        this.partner = partnerName
        this.stage = stageName
        this.constants = Constants(partner!!, stage!!)

        // Set private variables for payTheoryData
        this.payorId = payorId

        // Create pay_theory_data object for transaction message
        payTheoryData = Utility.createPayTheoryData(null, null, null, payorId, null, null, null)

        model = ViewModelProvider(
            this,
            ConfigurationInjector(requireActivity().application,
                ConfigurationDetail()).provideConfigurationViewModelFactory()
        )[ConfigurationViewModel::class.java]

        // update Configuration Details object with payment data
        model!!.update(
            ConfigurationDetail(
                apiKey = this.apiKey,
                tokenizationType = this.tokenizationType,
                metadata = this.metadata,
                payorInfo = this.payorInfo,
                payorId = this.payorId,
                requireAccountName = this.requireAccountName,
                requireBillingAddress = this.requireBillingAddress
            )
        )

        payTheoryTokenizeTransaction =
            PaymentMethodToken(
                this.requireActivity(),
                this.partner!!,
                this.stage!!,
                this.apiKey!!,
                this.constants!!,
                this.metadata,
                this.payTheoryData
            )

        Utility.enableTokenizationFields(this.requireView(), this.tokenizationType!!, this.requireAccountName!!,
            this.requireBillingAddress!!)

        //if card payment fields are active add text watcher validation
        if (this.tokenizationType==TokenizationType.CARD) {
            // get credit card fields
            val ccNumber = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_number)
            val ccCVV = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_cvv)
            val ccExpiration = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_expiration)
            val billingZip = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_zip)

            val ccNumberValidation: (PayTheoryEditText) -> CardNumberTextWatcher =
                { pt -> CardNumberTextWatcher(pt, submitButton)  }
            val cvvNumberValidation: (PayTheoryEditText) -> CVVTextWatcher =
                { pt -> CVVTextWatcher(pt, submitButton) }
            val expirationValidation: (PayTheoryEditText) -> ExpirationTextWatcher =
                { pt -> ExpirationTextWatcher(pt, submitButton) }
            val zipCodeValidation: (PayTheoryEditText) -> ZipCodeTextWatcher =
                { pt -> ZipCodeTextWatcher(pt, submitButton) }
            ccNumber.addTextChangedListener(ccNumberValidation(ccNumber))
            ccCVV.addTextChangedListener(cvvNumberValidation(ccCVV))
            ccExpiration.addTextChangedListener(expirationValidation(ccExpiration))
            billingZip.addTextChangedListener(zipCodeValidation(billingZip))
        }

        //if bank payment fields are active add text watcher validation
        if (this.tokenizationType==TokenizationType.BANK) {
            // get ach fields
            val (achAccount, achRouting) = Utility.getAchFields(this.requireActivity())
            val achChooser: AppCompatAutoCompleteTextView =
                requireActivity().findViewById(R.id.ach_type_choice)
            val items = listOf(getString(R.string.checking), getString(R.string.savings))
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
            achChooser.setAdapter(adapter)

            val achRoutingNumberValidation: (PayTheoryEditText) -> RoutingNumberTextWatcher =
                { pt -> RoutingNumberTextWatcher(pt, submitButton)  }
            val achAccountNumberValidation: (PayTheoryEditText) -> AccountNumberTextWatcher =
                { pt -> AccountNumberTextWatcher(pt, submitButton)  }
            achRouting.addTextChangedListener(achRoutingNumberValidation(achRouting))
            achAccount.addTextChangedListener(achAccountNumberValidation(achAccount))
        }
    }

    fun tokenize(){
        if (this.requireAccountName==true) {
            val accountName = requireActivity().findViewById<PayTheoryEditText>(R.id.account_name)
            this.accountName = accountName.text.toString()
        }
        // Zipcode required for all payments
        val billingZip = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_zip)
        //If all billing address fields are visible get all field data
        if (this.requireBillingAddress==true) {
            val billingAddress1 =
                requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_1)
            val billingAddress2 =
                requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_2)
            val billingCity = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_city)
            val billingState = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_state)

            this.billingAddress = Address(
                billingAddress1.text.toString().ifBlank { "" },
                billingAddress2.text.toString().ifBlank { "" },
                billingCity.text.toString().ifBlank { "" },
                billingState.text.toString().ifBlank { "" },
                billingZip.text.toString().ifBlank { "" },
                "USA"
            )
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
        }

        //Create card paymentToken
        if (this.tokenizationType==TokenizationType.CARD) {
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
        }

        //Create bank paymentToken
        if (this.tokenizationType==TokenizationType.BANK) {
            val achChooser: AppCompatAutoCompleteTextView = requireActivity().findViewById(R.id.ach_type_choice)
            val (achAccount, achRouting) = Utility.getAchFields(this.requireActivity())
            val accountName = requireActivity().findViewById<PayTheoryEditText>(R.id.account_name)
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
        }
    }
    private fun makePayment(payment: Payment) {
        payTheoryTransaction!!.transact(payment)
    }

    private fun makePaymentMethodToken(paymentMethodTokenData: PaymentMethodTokenData) {
        payTheoryTokenizeTransaction!!.tokenize(paymentMethodTokenData)
    }
}



private fun isNetworkAvailable(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }