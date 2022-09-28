package com.paytheory.android.sdk.fragments

import Address
import PayorInfo
import Payment
import PaymentMethodTokenData
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.Directory.PACKAGE_NAME
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
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

/**
 * PayTheoryFragment populates with required text inputs.
 * Flexible display that adds inputs dynamically as needed
 */
class PayTheoryFragment : Fragment() {
    companion object {
        const val PAYMENT_CARD = "card"
        const val BANK_ACCOUNT = "ach"
        const val CASH = "cash"
    }

    //default values for pay theory fragment
    private lateinit var constants: Constants
    private var payTheoryTransaction: Transaction? = null
    private var payTheoryTokenizeTransaction: PaymentMethodToken? = null
    private var apiKey: String? = null
    private var amount: Int? = null
    private var transactionType: TransactionType = TransactionType.CARD
    private var tokenizationType: TokenizationType = TokenizationType.CARD
    private var requireAccountName: Boolean = false
    private var requireBillingAddress: Boolean = false
    private var confirmation: Boolean = false
    private var feeMode: String = FeeMode.INTERCHANGE
    private var metadata: HashMap<Any, Any>? = hashMapOf()
    private var payTheoryData: HashMap<Any, Any>? = hashMapOf()
    private var payorInfo: PayorInfo? = PayorInfo()
    private var payorId: String? = null
    private var accountCode: String? = null
    private var reference: String? = null
    private var paymentParameters: String? = null
    private var invoiceId: String? = null
    private var sendReceipt: Boolean = false
    private var receiptDescription: String? = ""
    private var billingAddress: Address? = Address()
    private var accountName: String? = null
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
     * @param apiKey Pay Theory API-Key
     * @param amount Amount of transaction
     * @param transactionType Type of payment method
     * @param requireBillingAddress Boolean if billing address is required
     * @param feeMode Type of fee mode that will be processed
     * @param payorInfo Optional details about the buyer
     */
    @RequiresApi(Build.VERSION_CODES.O)
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
        if (model == null) {
            model = ViewModelProvider(
                this,
                ConfigurationInjector(
                    requireActivity().application, ConfigurationDetail(
                        apiKey = apiKey,
                        amount = amount,
                        transactionType = transactionType,
                        requireAccountName = requireAccountName,
                        requireBillingAddress = requireBillingAddress,
                        confirmation = confirmation,
                        feeMode = feeMode,
                        metadata = metadata,
                        payorInfo = payorInfo,
                        payorId = payorId,
                        accountCode = accountCode,
                        reference = reference,
                        paymentParameters = paymentParameters,
                        invoiceId = invoiceId,
                        sendReceipt = sendReceipt,
                        receiptDescription = receiptDescription
                    )
                ).provideConfigurationViewModelFactory()
            )[ConfigurationViewModel::class.java]
        }

        // update Configuration Details object with payment data
        model!!.update(
            ConfigurationDetail(
                apiKey = apiKey,
                amount = amount,
                transactionType = transactionType,
                requireAccountName = requireAccountName,
                requireBillingAddress = requireBillingAddress,
                confirmation = confirmation,
                feeMode = feeMode,
                metadata = metadata,
                payorInfo = payorInfo,
                payorId = payorId,
                accountCode = accountCode,
                reference = reference,
                paymentParameters = paymentParameters,
                invoiceId = invoiceId,
                sendReceipt = sendReceipt,
                receiptDescription = receiptDescription
            )
        )
        // set private variables for Pay Theory Fragment
        this.apiKey = model!!.configuration.value?.apiKey
        this.amount = model!!.configuration.value?.amount
        this.transactionType = model!!.configuration.value?.transactionType!!
        this.requireAccountName = model!!.configuration.value?.requireAccountName!!
        this.requireBillingAddress = model!!.configuration.value?.requireBillingAddress!!
        this.confirmation = model!!.configuration.value?.confirmation!!
        this.feeMode = model!!.configuration.value?.feeMode!!
        this.metadata = model!!.configuration.value?.metadata
        this.payorInfo = model!!.configuration.value?.payorInfo
        this.payorId = model!!.configuration.value?.payorId
        this.accountCode = model!!.configuration.value?.accountCode
        this.reference = model!!.configuration.value?.reference
        this.paymentParameters = model!!.configuration.value?.paymentParameters
        this.invoiceId = model!!.configuration.value?.invoiceId
        this.sendReceipt = model!!.configuration.value?.sendReceipt!!
        this.receiptDescription = model!!.configuration.value?.receiptDescription

        //Validation checks for PayTheoryFragment configs
        if (this.apiKey.isNullOrBlank()) {
            throw Exception("Invalid apikey")
        }
        if (!this.apiKey!!.contains("paytheory")) {
            throw Exception("Invalid apikey")
        }
        if (this.amount == null) {
            throw Exception("Invalid amount")
        }
        if (this.amount == 0) {
            throw Exception("Invalid amount")
        }

        val startIndex: Int = apiKey.indexOf('-')
        val partner: String = apiKey.substring(0, startIndex)
        val endIndex = apiKey.indexOf('-', apiKey.indexOf('-') + 1)
        val stage: String = apiKey.substring(startIndex + 1, endIndex)

        if (stage != "paytheorylab" && stage != "paytheorystudy" && stage != "paytheory") {
            throw Exception("Invalid apikey")
        }

        this.constants = Constants(partner, stage)

        //create pay_theory_data object for host:transfer_part1 action request
        val payTheoryData = hashMapOf<Any, Any>()

        //if send receipt is enabled add send_receipt and receipt_description to pay_theory_data
        if (this.sendReceipt) {
            payTheoryData["send_receipt"] = this.sendReceipt
            if (!this.receiptDescription.isNullOrBlank()){
                payTheoryData["receipt_description"] = this.receiptDescription!!
            }
        }

        // if paymentParameters is given add to pay_theory_data
        if (!this.paymentParameters.isNullOrBlank()) {
            payTheoryData["payment_parameters"] = this.paymentParameters!!
        }

        // if payorId is given add to pay_theory_data
        if (!this.payorId.isNullOrBlank()) {
            payTheoryData["payor_id"] = this.payorId!!
        }

        // if invoiceId is given add to pay_theory_data
        if (!this.invoiceId.isNullOrBlank()) {
            payTheoryData["invoice_id"] = this.invoiceId!!
        }

        // if account_code is given add to pay_theory_data
        if (!this.accountCode.isNullOrBlank()) {
            payTheoryData["account_code"] = this.accountCode!!
        }

        // if reference is given add to pay_theory_data
        if (!this.reference.isNullOrBlank()) {
            payTheoryData["reference"] = this.reference!!
        }

        this.payTheoryData = payTheoryData

        payTheoryTransaction =
            Transaction(
                this.requireActivity(),
                partner,
                stage,
                this.apiKey!!,
                this.feeMode,
                this.constants,
                this.confirmation,
                this.sendReceipt,
                this.receiptDescription,
                this.metadata,
                this.payTheoryData
            )

        payTheoryTransaction!!.init()

        enablePaymentFields(this.transactionType, requireAccountName!!, requireBillingAddress!!)

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
            val ccNumberValidation: (PayTheoryEditText) -> CreditCardFormattingTextWatcher =
                { pt -> CreditCardFormattingTextWatcher(pt, submitButton)  }
            val cvvNumberValidation: (PayTheoryEditText) -> CVVFormattingTextWatcher =
                { pt -> CVVFormattingTextWatcher(pt, submitButton) }
            val expirationValidation: (PayTheoryEditText) -> ExpirationFormattingTextWatcher =
                { pt -> ExpirationFormattingTextWatcher(pt, submitButton) }

            ccNumber.addTextChangedListener(ccNumberValidation(ccNumber))
            ccCVV.addTextChangedListener(cvvNumberValidation(ccCVV))
            ccExpiration.addTextChangedListener(expirationValidation(ccExpiration))
        }

        //if bank payment fields are active add text watcher validation
        if (isBankPayment) {
            val achRoutingNumberValidation: (PayTheoryEditText) -> RoutingNumberFormattingTextWatcher =
                { pt -> RoutingNumberFormattingTextWatcher(pt, submitButton)  }

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
                    val expirationYear = expirationString.split("/").last()

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
                println("INPUT FIELDS ARE INVALID *******************************************************************************************************************************")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    fun tokenizePaymentMethod(
        apiKey: String,
        tokenizationType: TokenizationType? = TokenizationType.CARD,
        requireAccountName: Boolean? = false,
        requireBillingAddress: Boolean? = false,
        payorInfo: PayorInfo? = PayorInfo(),
        payorId: String? = null,
        metadata: HashMap<Any, Any>? = HashMap(),
    ) {
        if (model == null) {
            model = ViewModelProvider(
                this,
                ConfigurationInjector(
                    requireActivity().application,
                    ConfigurationDetail(
                        apiKey = apiKey,
                        tokenizationType = tokenizationType,
                        metadata = metadata,
                        payorInfo = payorInfo,
                        payorId = payorId,
                        requireAccountName = requireAccountName,
                        requireBillingAddress = requireBillingAddress
                    )
                ).provideConfigurationViewModelFactory()
            )[ConfigurationViewModel::class.java]
        }

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
        // set private variables for Pay Theory Fragment
        this.apiKey = model!!.configuration.value?.apiKey
        this.tokenizationType = model!!.configuration.value?.tokenizationType!!
        this.metadata = model!!.configuration.value?.metadata
        this.payorInfo = model!!.configuration.value?.payorInfo
        this.payorId = model!!.configuration.value?.payorId
        this.requireAccountName = model!!.configuration.value?.requireAccountName!!
        this.requireBillingAddress = model!!.configuration.value?.requireBillingAddress!!

        //Validation checks for PayTheoryFragment configs
        if (this.apiKey.isNullOrBlank()) {
            throw Exception("Invalid apikey")
        }
        if (!this.apiKey!!.contains("paytheory")) {
            throw Exception("Invalid apikey")
        }

        val startIndex: Int = apiKey.indexOf('-')
        val partner: String = apiKey.substring(0, startIndex)
        val endIndex = apiKey.indexOf('-', apiKey.indexOf('-') + 1)
        val stage: String = apiKey.substring(startIndex + 1, endIndex)

        if (stage != "paytheorylab" && stage != "paytheorystudy" && stage != "paytheory") {
            throw Exception("Invalid apikey")
        }

        this.constants = Constants(partner, stage)

        //create pay_theory_data object for host:transfer_part1 action request
        val payTheoryData = hashMapOf<Any, Any>()

        // if payorId is given add to pay_theory_data
        if (!this.payorId.isNullOrBlank()) {
            payTheoryData["payor_id"] = this.payorId!!
        }

        payTheoryTokenizeTransaction =
            PaymentMethodToken(
                this.requireActivity(),
                partner,
                stage,
                this.apiKey!!,
                this.constants,
                this.metadata,
                this.payTheoryData
            )

        payTheoryTokenizeTransaction!!.init()

        enableTokenizationFields(this.tokenizationType, this.requireAccountName, this.requireBillingAddress)

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
            val ccNumberValidation: (PayTheoryEditText) -> CreditCardFormattingTextWatcher =
                { pt -> CreditCardFormattingTextWatcher(pt, submitButton) }
            val cvvNumberValidation: (PayTheoryEditText) -> CVVFormattingTextWatcher =
                { pt -> CVVFormattingTextWatcher(pt, submitButton) }
            val expirationValidation: (PayTheoryEditText) -> ExpirationFormattingTextWatcher =
                { pt -> ExpirationFormattingTextWatcher(pt, submitButton) }

            ccNumber.addTextChangedListener(ccNumberValidation(ccNumber))
            ccCVV.addTextChangedListener(cvvNumberValidation(ccCVV))
            ccExpiration.addTextChangedListener(expirationValidation(ccExpiration))
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
                    val expirationYear = expirationString.split("/").last()
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
                println("INPUT FIELDS ARE INVALID *******************************************************************************************************************************")
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

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun makePayment(payment: Payment) {
        payTheoryTransaction!!.transact(payment)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                ccNumber.error = "Invalid Card Number"
                false
            } else if (ccCVV.text.isNullOrBlank() || !ccCVV.error.isNullOrBlank()) {
                ccCVV.error = "Invalid CVV"
                false
            } else if (ccExpiration.text.isNullOrBlank() || !ccExpiration.error.isNullOrBlank()) {
                ccExpiration.error = "Invalid Expiration"
                false
            } else if (billingZip.text.isNullOrBlank() || !billingZip.error.isNullOrBlank()) {
                billingZip.error = "Invalid Zip Code"
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
                achAccount.error = "Invalid Account Number"
                false
            } else if (achRouting.text.isNullOrBlank() || !achRouting.error.isNullOrBlank() || achRouting.text.toString().length != 9) {
                achRouting.error = "Invalid Routing Number"
                false
            } else if (achChooser.text.toString() != "Checking" && achChooser.text.toString() != "Savings") {
                achChooser.error = "Invalid Account Type"
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
                cashBuyerContact.error = "Invalid Contact Info"
                false
            } else if (cashBuyer.text.isNullOrBlank() || !cashBuyer.error.isNullOrBlank()) {
                cashBuyer.error = "Invalid Name"
                false
            } else {
                true
            }
        }
        // If payment is not CARD BANK or CASH
        return false
    }
}
