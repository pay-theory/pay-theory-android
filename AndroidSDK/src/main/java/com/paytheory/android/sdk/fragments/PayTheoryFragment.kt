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
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.paytheory.android.sdk.Constants
import com.paytheory.android.sdk.PaymentMethodToken
import com.paytheory.android.sdk.R
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.configuration.*
import com.paytheory.android.sdk.validation.*
import com.paytheory.android.sdk.view.PayTheoryEditText
import kotlinx.coroutines.ExperimentalCoroutinesApi

private const val PAYTHEORYLAB = "paytheorylab"
private const val PAYTHEORYSTUDY = "paytheorystudy"
private const val PAYTHEORY = "paytheory"
private const val NO_NETWORK_CONNECTION = "No valid network connection"
private const val INVALID_APIKEY = "Invalid apikey"
private const val INVALID_AMOUNT = "Invalid amount"
private const val INVALID_CARD_NUMBER = "Invalid Card Number"
private const val INVALID_CVV = "Invalid CVV"
private const val INVALID_EXPIRATION = "Invalid Expiration"
private const val INVALID_ZIP_CODE = "Invalid Zip Code"
private const val INVALID_ACCOUNT_NUMBER = "Invalid Account Number"
private const val INVALID_NAME = "Invalid Name"
private const val INVALID_CONTACT_INFO = "Invalid Contact Info"
private const val INVALID_ACCOUNT_TYPE = "Invalid Account Type"
private const val INVALID_ROUTING_NUMBER = "Invalid Routing Number"

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
    @OptIn(ExperimentalCoroutinesApi::class)
    @Throws(Exception::class)
    fun configure(
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

        partner = apiKey.substring(0, apiKey.indexOf('-'))
        stage = apiKey.substring(apiKey.indexOf('-') + 1, apiKey.indexOf('-', apiKey.indexOf('-') + 1))

        if (stage != PAYTHEORYLAB && stage != PAYTHEORYSTUDY && stage != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (amount == 0) {
            throw IllegalArgumentException(INVALID_AMOUNT)
        }

        // Create pay_theory_data object for transaction message
        payTheoryData = createPayTheoryData(sendReceipt, receiptDescription, paymentParameters, payorId, invoiceId, accountCode, reference)
        // Set private variables
        this.apiKey = apiKey
        this.amount = amount
        this.transactionType = transactionType
        this.requireAccountName = requireAccountName
        this.requireBillingAddress = requireBillingAddress
        this.confirmation = confirmation
        this.feeMode = feeMode
        this.metadata = metadata
        this.payorInfo = payorInfo
        this.payorId = payorId
        this.partner = partner
        this.stage = stage
        this.constants = Constants(partner!!, stage!!)

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

//        payTheoryTransaction!!.init()

        enablePaymentFields(this.transactionType!!, this.requireAccountName!!, this.requireBillingAddress!!)

        val submitButton = requireActivity().findViewById<Button>(R.id.submitButton)

        // credit card fields
        val ccNumber = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_number)
        val ccCVV = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_cvv)
        val ccExpiration = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_expiration)
        val billingZip = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_zip)

        val isCardPayment = (ccNumber.visibility == View.VISIBLE
                && ccCVV.visibility == View.VISIBLE
                && ccExpiration.visibility == View.VISIBLE
                && billingZip.visibility == View.VISIBLE)

        // ach fields
        val (achAccount, achRouting) = getAchFields()

        val achChooser: AppCompatAutoCompleteTextView =
            requireActivity().findViewById(R.id.ach_type_choice)

        val items = listOf(getString(R.string.checking), getString(R.string.savings))

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
        achChooser.setAdapter(adapter)

        val isBankPayment = (achAccount.visibility == View.VISIBLE
                && achRouting.visibility == View.VISIBLE)

        // cash fields
        val cashBuyerContact =
            requireActivity().findViewById<PayTheoryEditText>(R.id.cash_buyer_contact)
        val cashBuyer = requireActivity().findViewById<PayTheoryEditText>(R.id.cash_buyer)

        val isCashPayment = (cashBuyerContact.visibility == View.VISIBLE
                && cashBuyer.visibility == View.VISIBLE)

        // buyer options
        val accountName = requireActivity().findViewById<PayTheoryEditText>(R.id.account_name)
        val billingAddress1 =
            requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_1)
        val billingAddress2 =
            requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_2)
        val billingCity = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_city)
        val billingState = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_state)

        val hasAccountName = accountName.visibility == View.VISIBLE

        val hasBillingAddress = (billingAddress1.visibility == View.VISIBLE
                && billingAddress2.visibility == View.VISIBLE
                && billingCity.visibility == View.VISIBLE
                && billingState.visibility == View.VISIBLE
                && billingZip.visibility == View.VISIBLE)

        //if card payment fields are active add text watcher validation
        if (isCardPayment) {
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
        if (isBankPayment) {
            val achRoutingNumberValidation: (PayTheoryEditText) -> RoutingNumberTextWatcher =
                { pt -> RoutingNumberTextWatcher(pt, submitButton)  }

            achRouting.addTextChangedListener(achRoutingNumberValidation(achRouting))
        }

        //if cash payment fields are active add text watcher validation
        if (isCashPayment) {
            val cashBuyerContactValidation: (PayTheoryEditText) -> CashBuyerContactTextWatcher =
                { pt -> CashBuyerContactTextWatcher(pt, submitButton) }

            cashBuyerContact.addTextChangedListener(
                cashBuyerContactValidation(
                    cashBuyerContact
                )
            )
        }

        submitButton.setOnClickListener {
            //text field validation
            val fieldsValid = validateFields(isCardPayment, isBankPayment, isCashPayment)

            //if payment requested fields are valid
            if (fieldsValid){
                if (hasAccountName) {
                    this.accountName = accountName.text.toString()
                }

                //If all billing address fields are visible get all field data
                if (hasBillingAddress) {
                    billingAddress = Address(
                        billingAddress1.text.toString().ifBlank { "" },
                        billingAddress2.text.toString().ifBlank { "" },
                        billingCity.text.toString().ifBlank { "" },
                        billingState.text.toString().ifBlank { "" },
                        billingZip.text.toString().ifBlank { "" },
                        "USA"
                    )

                    // else just get zip code
                } else {
                    billingAddress = Address(
                        "",
                        "",
                        "",
                        "",
                        billingZip.text.toString().ifBlank { "" },
                        "USA"
                    )
                }

                //Create card payment
                if (isCardPayment) {

                    val expirationString = ccExpiration.text.toString()
                    val expirationMonth = expirationString.split("/").first()
                    val expirationYear = "20" + expirationString.split("/").last()

                    val payment = Payment(
                        timing = System.currentTimeMillis(),
                        amount = amount,
                        type = PAYMENT_CARD,
                        name = if (!this.accountName.isNullOrBlank()) this.accountName else "",
                        number = ccNumber.text.toString().replace("\\s".toRegex(), ""),
                        security_code = ccCVV.text.toString(),
                        expiration_month = expirationMonth,
                        expiration_year = expirationYear,
                        fee_mode = feeMode,
                        address = billingAddress,
                        payorInfo = payorInfo
                    )
                    makePayment(payment)
                }

                //Create cash payment
                if (isCashPayment) {
                    val contact = cashBuyerContact.text.toString()
                    val buyer = cashBuyer.text.toString()

                    val payment = Payment(
                        timing = System.currentTimeMillis(),
                        amount = amount,
                        type = CASH,
                        buyer = buyer,
                        fee_mode = feeMode,
                        address = billingAddress,
                        buyerContact = contact,
                        payorInfo = payorInfo
                    )
                    makePayment(payment)
                }

                //Create bank paymentToken
                if (isBankPayment) {
                    val payment = Payment(
                        timing = System.currentTimeMillis(),
                        amount = amount,
                        account_type = achChooser.text.toString(),
                        type = BANK_ACCOUNT,
                        name = this.accountName,
                        account_number = achAccount.text.toString(),
                        bank_code = achRouting.text.toString(),
                        address = billingAddress,
                        fee_mode = feeMode,
                        payorInfo = payorInfo
                    )
                    makePayment(payment)
                }
            } else { // if fieldsValid = false
                //TODO - send message to user fields invalid
                println("INPUT FIELDS ARE INVALID ************************")
            }
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


    private fun createPayTheoryData(sendReceipt: Boolean?, receiptDescription: String?, paymentParameters: String?, payorId: String?, invoiceId: String?, accountCode: String?, reference: String?): HashMap<Any, Any> {
        //create pay_theory_data object for host:transfer_part1 action request
        val payTheoryData = hashMapOf<Any, Any>()
        //if send receipt is enabled add send_receipt and receipt_description to pay_theory_data
        if (sendReceipt == true) {
            payTheoryData["send_receipt"] = sendReceipt
            if (!receiptDescription.isNullOrBlank()){
                payTheoryData["receipt_description"] = receiptDescription
            }
        }
        // if paymentParameters is given add to pay_theory_data
        if (!paymentParameters.isNullOrBlank()) {
            payTheoryData["payment_parameters"] = paymentParameters
        }
        // if payorId is given add to pay_theory_data
        if (!payorId.isNullOrBlank()) {
            payTheoryData["payor_id"] = payorId
        }
        // if invoiceId is given add to pay_theory_data
        if (!invoiceId.isNullOrBlank()) {
            payTheoryData["invoice_id"] = invoiceId
        }
        // if account_code is given add to pay_theory_data
        if (!accountCode.isNullOrBlank()) {
            payTheoryData["account_code"] = accountCode
        }
        // if reference is given add to pay_theory_data
        if (!reference.isNullOrBlank()) {
            payTheoryData["reference"] = reference
        }

        this.sendReceipt = sendReceipt
        this.receiptDescription = receiptDescription
        this.paymentParameters = paymentParameters
        this.payorId = payorId
        this.invoiceId = invoiceId
        this.accountCode = accountCode
        this.reference = reference

        return payTheoryData
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
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tokenize(
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
            println("CHECK!!!!!!!!!!!!! $NO_NETWORK_CONNECTION")
//            throw IllegalArgumentException(NO_NETWORK_CONNECTION)
        }

        // Validation checks for input parameters
        if (apiKey.isBlank()) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }
        if (!apiKey.contains(PAYTHEORY)) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }

        partner = apiKey.substring(0, apiKey.indexOf('-'))
        stage = apiKey.substring(apiKey.indexOf('-') + 1, apiKey.indexOf('-', apiKey.indexOf('-') + 1))

        if (stage != PAYTHEORYLAB && stage != PAYTHEORYSTUDY && stage != PAYTHEORY) {
            throw IllegalArgumentException(INVALID_APIKEY)
        }

        // Create pay_theory_data object for transaction message
        payTheoryData = createPayTheoryData(null, null, null, payorId, null, null, null)
        // Set private variables
        this.apiKey = apiKey
        this.tokenizationType = tokenizationType
        this.metadata = metadata
        this.payorInfo = payorInfo
        this.payorId = payorId
        this.requireAccountName = requireAccountName
        this.requireBillingAddress = requireBillingAddress
        this.partner = partner
        this.stage = stage
        this.constants = Constants(partner!!, stage!!)

        model = ViewModelProvider(
            this,
            ConfigurationInjector(requireActivity().application,
                ConfigurationDetail()).provideConfigurationViewModelFactory()
        )[ConfigurationViewModel::class.java]

        // update Configuration Details object with payment data
        model!!.update(
            ConfigurationDetail(
                apiKey = apiKey,
                tokenizationType = tokenizationType,
                metadata = metadata,
                payorInfo = payorInfo,
                payorId = payorId,
                requireAccountName = requireAccountName,
                requireBillingAddress = requireBillingAddress
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

//        payTheoryTokenizeTransaction!!.init()

        enableTokenizationFields(this.tokenizationType!!, this.requireAccountName!!,
            this.requireBillingAddress!!
        )

        val submitButton = requireActivity().findViewById<Button>(R.id.submitButton)

        // credit card fields
        val ccNumber = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_number)
        val ccCVV = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_cvv)
        val ccExpiration = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_expiration)
        val billingZip = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_zip)

        val isCardPayment = (ccNumber.visibility == View.VISIBLE
                && ccCVV.visibility == View.VISIBLE
                && ccExpiration.visibility == View.VISIBLE
                && billingZip.visibility == View.VISIBLE)

        // ach fields
        val (achAccount, achRouting) = getAchFields()

        val achChooser: AppCompatAutoCompleteTextView =
            requireActivity().findViewById(R.id.ach_type_choice)

        val items = listOf(getString(R.string.checking), getString(R.string.savings))

        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
        achChooser.setAdapter(adapter)

        val isBankPayment = (achAccount.visibility == View.VISIBLE
                && achRouting.visibility == View.VISIBLE)

        // buyer options
        val accountName = requireActivity().findViewById<PayTheoryEditText>(R.id.account_name)
        val billingAddress1 =
            requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_1)
        val billingAddress2 =
            requireActivity().findViewById<PayTheoryEditText>(R.id.billing_address_2)
        val billingCity = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_city)
        val billingState = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_state)

        val hasAccountName = accountName.visibility == View.VISIBLE

        val hasBillingAddress = (billingAddress1.visibility == View.VISIBLE
                && billingAddress2.visibility == View.VISIBLE
                && billingCity.visibility == View.VISIBLE
                && billingState.visibility == View.VISIBLE
                && billingZip.visibility == View.VISIBLE)

        //if card payment fields are active add text watcher validation
        if (isCardPayment) {
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
        if (isBankPayment) {
            val achRoutingNumberValidation: (PayTheoryEditText) -> RoutingNumberTextWatcher =
                { pt -> RoutingNumberTextWatcher(pt, submitButton)  }

            achRouting.addTextChangedListener(achRoutingNumberValidation(achRouting))
        }

        submitButton.setOnClickListener {
            //text field validation
            val fieldsValid = validateFields(isCardPayment, isBankPayment, false)

            //if payment requested fields are valid
            if (fieldsValid){
                if (hasAccountName) {
                    this.accountName = accountName.text.toString()
                }
                //If all billing address fields are visible get all field data
                if (hasBillingAddress) {
                    billingAddress = Address(
                        billingAddress1.text.toString().ifBlank { "" },
                        billingAddress2.text.toString().ifBlank { "" },
                        billingCity.text.toString().ifBlank { "" },
                        billingState.text.toString().ifBlank { "" },
                        billingZip.text.toString().ifBlank { "" },
                        "USA"
                    )
                    // else just get zip code
                } else {
                    billingAddress = Address(
                        "",
                        "",
                        "",
                        "",
                        billingZip.text.toString().ifBlank { "" },
                        "USA"
                    )
                }

                //Create card paymentToken
                if (isCardPayment) {
                    val expirationString = ccExpiration.text.toString()
                    val expirationMonth = expirationString.split("/").first()
                    val expirationYear = "20" + expirationString.split("/").last()
                    val paymentToken = PaymentMethodTokenData(
                        timing = System.currentTimeMillis(),
                        type = PAYMENT_CARD,
                        name = if (!this.accountName.isNullOrBlank()) this.accountName else "",
                        number = ccNumber.text.toString().replace("\\s".toRegex(), ""),
                        security_code = ccCVV.text.toString(),
                        expiration_month = expirationMonth,
                        expiration_year = expirationYear,
                        address = billingAddress,
                        payorInfo = payorInfo
                    )
                    makePaymentMethodToken(paymentToken)
                }

                //Create bank paymentToken
                if (isBankPayment) {
                    val paymentToken = PaymentMethodTokenData(
                        timing = System.currentTimeMillis(),
                        account_type = achChooser.text.toString(),
                        type = BANK_ACCOUNT,
                        name = this.accountName,
                        account_number = achAccount.text.toString(),
                        bank_code = achRouting.text.toString(),
                        address = billingAddress,
                        payorInfo = payorInfo
                    )
                    makePaymentMethodToken(paymentToken)
                }
            } else { // if fieldsValid = false
                //TODO - send message to user fields invalid
                println("INPUT FIELDS ARE INVALID ************************")
            }
        }
    }

    private fun getAchFields(): Pair<PayTheoryEditText, PayTheoryEditText> {
        val achAccount = requireActivity().findViewById<PayTheoryEditText>(R.id.ach_account_number)
        val achRouting = requireActivity().findViewById<PayTheoryEditText>(R.id.ach_routing_number)
        return Pair(achAccount, achRouting)
    }

    private fun enablePaymentFields(
        transactionType: TransactionType,
        requireAccountName: Boolean,
        requireBillingAddress: Boolean
    ) {
        if (transactionType == TransactionType.BANK) {
            enableAccountName()
            enableACH()
        }
        if (transactionType == TransactionType.CARD) {
            if (requireAccountName) {
                enableAccountName()
            }
            enableCC()
        }
        if (transactionType == TransactionType.CASH) {
            enableCash()
        }

        if (requireBillingAddress) {
            enableBillingAddress()
        }
    }

    private fun enableTokenizationFields(
        tokenizationType: TokenizationType,
        requireAccountName: Boolean,
        requireBillingAddress: Boolean
    ) {
        if (tokenizationType == TokenizationType.BANK) {
            enableAccountName()
            enableACH()
        }
        if (tokenizationType == TokenizationType.CARD) {
            if (requireAccountName) {
                enableAccountName()
            }
            enableCC()
        }

        if (requireBillingAddress) {
            enableBillingAddress()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun makePayment(payment: Payment) {
        payTheoryTransaction!!.transact(payment)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun makePaymentMethodToken(paymentMethodTokenData: PaymentMethodTokenData) {
        payTheoryTokenizeTransaction!!.tokenize(paymentMethodTokenData)
    }

    private fun enableCC() {
        val ccNumber: PayTheoryEditText? = view?.findViewById(R.id.cc_number)
        ccNumber!!.visibility = View.VISIBLE
        val ccCVV: PayTheoryEditText? = view?.findViewById(R.id.cc_cvv)
        ccCVV!!.visibility = View.VISIBLE
        val ccExpiration: PayTheoryEditText? = view?.findViewById(R.id.cc_expiration)
        ccExpiration!!.visibility = View.VISIBLE
        val billingZip: PayTheoryEditText? = view?.findViewById(R.id.billing_zip)
        billingZip!!.visibility = View.VISIBLE
        val cvvAndExpiration: LinearLayout? = view?.findViewById(R.id.cvv_and_expiration)
        cvvAndExpiration!!.visibility = View.VISIBLE
    }

    private fun enableBillingAddress() {
        val line1: PayTheoryEditText? = view?.findViewById(R.id.billing_address_1)
        line1!!.visibility = View.VISIBLE
        val line2: PayTheoryEditText? = view?.findViewById(R.id.billing_address_2)
        line2!!.visibility = View.VISIBLE
        val city: PayTheoryEditText? = view?.findViewById(R.id.billing_city)
        city!!.visibility = View.VISIBLE
        val state: PayTheoryEditText? = view?.findViewById(R.id.billing_state)
        state!!.visibility = View.VISIBLE
        val billingZip: PayTheoryEditText? = view?.findViewById(R.id.billing_zip)
        billingZip!!.visibility = View.VISIBLE
    }

    private fun enableAccountName() {
        val accountName: PayTheoryEditText? = view?.findViewById(R.id.account_name)
        accountName!!.visibility = View.VISIBLE
    }

    private fun enableACH() {
        val achAccount: PayTheoryEditText? = view?.findViewById(R.id.ach_account_number)
        achAccount!!.visibility = View.VISIBLE
        val achRouting: PayTheoryEditText? = view?.findViewById(R.id.ach_routing_number)
        achRouting!!.visibility = View.VISIBLE
        val achChoice: TextInputLayout? = view?.findViewById(R.id.ach_type_choice_layout)
        achChoice!!.visibility = View.VISIBLE
    }

    private fun enableCash() {
        val cashBuyerContact: PayTheoryEditText? = view?.findViewById(R.id.cash_buyer_contact)
        cashBuyerContact!!.visibility = View.VISIBLE
        val cashBuyer: PayTheoryEditText? = view?.findViewById(R.id.cash_buyer)
        cashBuyer!!.visibility = View.VISIBLE
    }

    private fun validateFields(
        isCardPayment: Boolean,
        isBankPayment: Boolean,
        isCashPayment: Boolean
    ): Boolean {

        //If transaction is card payment validate card fields
        if (isCardPayment) {
            //Required zipcode field
            val billingZip = requireActivity().findViewById<PayTheoryEditText>(R.id.billing_zip)
            //card fields
            val ccNumber = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_number)
            val ccCVV = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_cvv)
            val ccExpiration = requireActivity().findViewById<PayTheoryEditText>(R.id.cc_expiration)
            return if (ccNumber.text.isNullOrBlank() || !ccNumber.error.isNullOrBlank()) {
                ccNumber.error = INVALID_CARD_NUMBER
                false
            } else if (ccCVV.text.isNullOrBlank() || !ccCVV.error.isNullOrBlank()) {
                ccCVV.error = INVALID_CVV
                false
            } else if (ccExpiration.text.isNullOrBlank() || !ccExpiration.error.isNullOrBlank() || ccExpiration.text.toString().length != 5) {
                ccExpiration.error = INVALID_EXPIRATION
                false
            } else if (billingZip.text.isNullOrBlank() || !billingZip.error.isNullOrBlank()) {
                billingZip.error = INVALID_ZIP_CODE
                false
            } else {
                true
            }
        }

        if (isBankPayment) {
            //Bank fields
            val achAccount = requireActivity().findViewById<PayTheoryEditText>(R.id.ach_account_number)
            val achRouting = requireActivity().findViewById<PayTheoryEditText>(R.id.ach_routing_number)
            val achChooser: AppCompatAutoCompleteTextView = requireActivity().findViewById(R.id.ach_type_choice)

            return if (achAccount.text.isNullOrBlank() || !achAccount.error.isNullOrBlank()) {
                achAccount.error = INVALID_ACCOUNT_NUMBER
                false
            } else if (achRouting.text.isNullOrBlank() || !achRouting.error.isNullOrBlank() || achRouting.text.toString().length != 9) {
                achRouting.error = INVALID_ROUTING_NUMBER
                false
            } else if (achChooser.text.toString() != "Checking" && achChooser.text.toString() != "Savings") {
                achChooser.error = INVALID_ACCOUNT_TYPE
                false
            } else {
                true
            }
        }

        if (isCashPayment) {
            // cash fields
            val cashBuyerContact =
                requireActivity().findViewById<PayTheoryEditText>(R.id.cash_buyer_contact)
            val cashBuyer = requireActivity().findViewById<PayTheoryEditText>(R.id.cash_buyer)
            return if (cashBuyerContact.text.isNullOrBlank() || !cashBuyerContact.error.isNullOrBlank()) {
                cashBuyerContact.error = INVALID_CONTACT_INFO
                false
            } else if (cashBuyer.text.isNullOrBlank() || !cashBuyer.error.isNullOrBlank()) {
                cashBuyer.error = INVALID_NAME
                false
            } else {
                true
            }
        }
        // If payment is not CARD BANK or CASH
        return false
    }
}
