package com.paytheory.android.sdk.fragments

import Address
import BuyerOptions
import Payment
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
import com.paytheory.android.sdk.R
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.configuration.*
import com.paytheory.android.sdk.validation.CVVFormattingTextWatcher
import com.paytheory.android.sdk.validation.CashBuyerContactTextWatcher
import com.paytheory.android.sdk.validation.CreditCardFormattingTextWatcher
import com.paytheory.android.sdk.validation.ExpirationFormattingTextWatcher
import com.paytheory.android.sdk.view.PayTheoryEditText
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Pay Theory Class
 */
@RequiresOptIn(message = "This API is being actively developed and may be subject to change.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class PayTheory

/**
 * PayTheoryFragment populates with required text inputs.
 * Flexible display that adds inputs dynamically as needed
 */
class PayTheoryFragment : Fragment() {
    companion object {
        const val API_KEY = "api_key"
        const val AMOUNT = "amount"
        const val USE_ACH = "ach_enabled"
        const val ACCOUNT_NAME_ENABLED = "account_name_enabled"
        const val BILLING_ADDRESS_ENABLED = "billing_address_enabled"
        const val TAGS = "tags"
        const val PAYMENT_CARD = "PAYMENT_CARD"
        const val BANK_ACCOUNT = "BANK_ACCOUNT"
        const val CASH = "CASH"
    }


    private lateinit var constants: Constants
    private var payTheoryTransaction: Transaction? = null
    private var api_key: String = ""
    private var amount: Int = 0
    private var paymentType: PaymentType = PaymentType.CREDIT
    private var accountNameEnabled: Boolean = false
    private var billingAddressEnabled: Boolean = false
    private var feeMode : String = FeeMode.SURCHARGE
    private var billingAddress: Address = Address("","","","","","")
    private var accountName = ""
    private var tags: HashMap<String,String> = java.util.HashMap()
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onDetach() {
        super.onDetach()
        if (payTheoryTransaction != null) {
            payTheoryTransaction!!.disconnect()
        }
        if (model !== null) {
            model!!.update(ConfigurationDetail())
        }
    }

    /**
     * Create configurations to execute a payment
     * @param apiKey Pay Theory API-Key
     * @param amount Amount of transaction
     * @param paymentType Type of payment method
     * @param requireBillingAddress Boolean if billing address is required
     * @param feeMode Type of fee mode that will be processed
     * @param buyerOptions Optional details about the buyer
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun configure(
        apiKey: String,
        amount: Int,
        paymentType: PaymentType = PaymentType.CREDIT,
        requireAccountName: Boolean = true,
        requireBillingAddress: Boolean = true,
        feeMode: String = FeeMode.SURCHARGE,
        buyerOptions: BuyerOptions? = null) {

        if (model == null) {
            model = ViewModelProvider(
                this,
                ConfigurationInjector(requireActivity().application, ConfigurationDetail()).provideConfigurationViewModelFactory()
            ).get(
                ConfigurationViewModel::class.java
            )
        }

        model!!.update(ConfigurationDetail(apiKey,amount,requireAccountName,requireBillingAddress,paymentType))
        model!!.configuration.observe(this.viewLifecycleOwner, { configurationDetail ->
            this.api_key = configurationDetail.apiKey
            this.amount = configurationDetail.amount
            this.paymentType = configurationDetail.paymentType
            this.accountNameEnabled = configurationDetail.requireAccountName
            this.billingAddressEnabled = configurationDetail.requireAddress
            this.feeMode = configurationDetail.feeMode
            if (this.api_key.isNotEmpty()) {
                val startIndex: Int = api_key.indexOf('-')
                val env: String = api_key.substring(0, startIndex)
                val endIndex = api_key.indexOf('-', api_key.indexOf('-') + 1)
                val stage: String = api_key.substring(startIndex+1, endIndex)

                this.constants = Constants(env, stage)

                tags = hashMapOf("pay-theory-environment" to env)

                payTheoryTransaction =
                    Transaction(
                        this.activity!!,
                        api_key,
                        this.constants,
                        env,
                        stage
                    )

                payTheoryTransaction!!.init()



                enableFields(this.paymentType, accountNameEnabled, billingAddressEnabled)

                val btn = activity!!.findViewById<Button>(R.id.submitButton)

                // credit card fields
                val (ccNumber, ccCVV, ccExpiration) = getCreditCardFields()

                val hasCC = (ccNumber.visibility == View.VISIBLE
                        && ccCVV.visibility == View.VISIBLE
                        && ccExpiration.visibility == View.VISIBLE)


                // ach fields
                val (achAccount, achRouting) = getAchFields()

                val achChooser: AppCompatAutoCompleteTextView = activity!!.findViewById(R.id.ach_type_choice)

                val items = listOf(getString(R.string.checking), getString(R.string.savings))

                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
                achChooser.setAdapter(adapter)

                val hasACH = (achAccount.visibility == View.VISIBLE
                        && achRouting.visibility == View.VISIBLE)

                // cash fields
                val cashBuyerContact = activity!!.findViewById<PayTheoryEditText>(R.id.cash_buyer_contact)
                val cashBuyer = activity!!.findViewById<PayTheoryEditText>(R.id.cash_buyer)


                val hasCASH = (cashBuyerContact.visibility == View.VISIBLE
                        && cashBuyer.visibility == View.VISIBLE)

                // buyer options
                val accountName = activity!!.findViewById<PayTheoryEditText>(R.id.account_name)
                val billingAddress1 =
                    activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_1)
                val billingAddress2 =
                    activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_2)
                val billingCity = activity!!.findViewById<PayTheoryEditText>(R.id.billing_city)
                val billingState = activity!!.findViewById<PayTheoryEditText>(R.id.billing_state)
                val billingZip = activity!!.findViewById<PayTheoryEditText>(R.id.billing_zip)

                val hasAccountName = accountName.visibility == View.VISIBLE

                val hasBillingAddress = (billingAddress1.visibility == View.VISIBLE
                        && billingAddress2.visibility == View.VISIBLE
                        && billingCity.visibility == View.VISIBLE
                        && billingState.visibility == View.VISIBLE
                        && billingZip.visibility == View.VISIBLE)


                if (hasCC) {
                    val ccNumberValidation: (PayTheoryEditText) -> CreditCardFormattingTextWatcher =
                        { pt -> CreditCardFormattingTextWatcher(pt) }
                    val cvvNumberValidation: (PayTheoryEditText) -> CVVFormattingTextWatcher =
                        { pt -> CVVFormattingTextWatcher(pt) }
                    val expirationValidation: (PayTheoryEditText) -> ExpirationFormattingTextWatcher =
                        { pt -> ExpirationFormattingTextWatcher(pt) }

                    ccNumber.addTextChangedListener(ccNumberValidation(ccNumber))
                    ccCVV.addTextChangedListener(cvvNumberValidation(ccCVV))
                    ccExpiration.addTextChangedListener(expirationValidation(ccExpiration))
                }

                if (hasCASH) {
                    val cashBuyerContactValidation: (PayTheoryEditText) -> CashBuyerContactTextWatcher =
                        { pt -> CashBuyerContactTextWatcher(pt)}

                    cashBuyerContact.addTextChangedListener(cashBuyerContactValidation(cashBuyerContact))
                }


                btn.setOnClickListener {


                    if (hasAccountName) {
                        this.accountName = accountName.text.toString()
                    }

                    if (hasBillingAddress) {
                        billingAddress = Address(billingCity.text.toString(),  billingState.text.toString(),billingZip.text.toString(), billingAddress1.text.toString(), billingAddress2.text.toString(), "USA" )
                    }


                    if (hasCC) {
                        val expirationString = ccExpiration.text.toString()
                        val payment = Payment(
                            timing = System.currentTimeMillis(),
                            amount = amount,
                            type = PAYMENT_CARD,
                            name = this.accountName,
                            number = ccNumber.text.toString().replace("\\s".toRegex(), ""),
                            security_code = ccCVV.text.toString(),
                            expiration_month = expirationString.split("/").first(),
                            expiration_year = expirationString.split("/").last(),
                            fee_mode = feeMode,
                            address = billingAddress,
                            buyerOptions = buyerOptions
                        )
                        makePayment(payment)
                    }

                    if (hasCASH) {
                        val email = cashBuyerContact.text.toString()
                        val buyer = cashBuyer.text.toString()

                        val payment = Payment(
                            timing = System.currentTimeMillis(),
                            amount = amount,
                            type = CASH,
                            buyer = buyer,
                            fee_mode = feeMode,
                            address = billingAddress,
                            buyerContact = email
                        )
                        makePayment(payment)
                    }

                    if (hasACH) {
                        if (achChooser.text.toString() == "Checking" || achChooser.text.toString() == "Savings"){
                            val payment = Payment(
                                timing = System.currentTimeMillis(),
                                amount = amount,
                                account_type = achChooser.text.toString(),
                                type = BANK_ACCOUNT,
                                name = this.accountName,
                                account_number = achAccount.text.toString(),
                                bank_code = achRouting.text.toString(),
                                fee_mode = feeMode,
                                address = billingAddress
                            )
                            makePayment(payment)
                        }
                        else {
                            achChooser.error = "Account type required."
                        }

                    }
                }
            }
        })
    }

    private fun getAchFields(): Pair<PayTheoryEditText, PayTheoryEditText> {
        val achAccount = activity!!.findViewById<PayTheoryEditText>(R.id.ach_account_number)
        val achRouting = activity!!.findViewById<PayTheoryEditText>(R.id.ach_routing_number)
        return Pair(achAccount, achRouting)
    }

    private fun getCreditCardFields(): Triple<PayTheoryEditText, PayTheoryEditText, PayTheoryEditText> {
        val ccNumber = activity!!.findViewById<PayTheoryEditText>(R.id.cc_number)
        val ccCVV = activity!!.findViewById<PayTheoryEditText>(R.id.cc_cvv)
        val ccExpiration = activity!!.findViewById<PayTheoryEditText>(R.id.cc_expiration)
        return Triple(ccNumber, ccCVV, ccExpiration)
    }

    private fun enableFields(
        paymentType: PaymentType,
        accountNameEnabled: Boolean,
        billingAddressEnabled: Boolean
    ) {
        if (paymentType == PaymentType.BANK) {
            enableAccountName()
            enableACH()
        }
        if (paymentType == PaymentType.CREDIT) {
            if (accountNameEnabled) {
                enableAccountName()
            }
            enableCC()
        }
        if (paymentType == PaymentType.CASH) {
            enableCash()
        }


        if (billingAddressEnabled) {
            enableBillingAddress()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun makePayment(payment: Payment) {
        payTheoryTransaction!!.transact(payment)
    }

    private fun enableCC() {
        val ccNumber: PayTheoryEditText? = view?.findViewById(R.id.cc_number)
        ccNumber!!.visibility = View.VISIBLE
        val ccCVV: PayTheoryEditText? = view?.findViewById(R.id.cc_cvv)
        ccCVV!!.visibility = View.VISIBLE
        val ccExpiration: PayTheoryEditText? = view?.findViewById(R.id.cc_expiration)
        ccExpiration!!.visibility = View.VISIBLE
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
        val zip: PayTheoryEditText? = view?.findViewById(R.id.billing_zip)
        zip!!.visibility = View.VISIBLE
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

    private fun enableCash(){
        val cashBuyerContact: PayTheoryEditText? = view?.findViewById(R.id.cash_buyer_contact)
        cashBuyerContact!!.visibility = View.VISIBLE

        val cashBuyer: PayTheoryEditText? = view?.findViewById(R.id.cash_buyer)
        cashBuyer!!.visibility = View.VISIBLE
    }


}
