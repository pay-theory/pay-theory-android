package com.paytheory.android.sdk.fragments

import Payment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.paytheory.android.sdk.Constants
import com.paytheory.android.sdk.R
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.configuration.*
import com.paytheory.android.sdk.validation.CVVFormattingTextWatcher
import com.paytheory.android.sdk.validation.CreditCardFormattingTextWatcher
import com.paytheory.android.sdk.validation.ExpirationFormattingTextWatcher
import com.paytheory.android.sdk.view.PayTheoryEditText
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
    }


    private lateinit var constants: Constants
    private lateinit var payTheoryTransaction: Transaction
    private var api_key: String = ""
    private var amount: Int = 0
    private var paymentType: PaymentType = PaymentType.CREDIT
    private var accountNameEnabled: Boolean = false
    private var billingAddressEnabled: Boolean = false
    private var feeMode : String = FeeMode.SURCHARGE
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
        payTheoryTransaction.disconnect()
        if (model !== null) {
            model!!.update(ConfigurationDetail())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun configure(
        apiKey: String,
        amount: Int,
        paymentType: PaymentType = PaymentType.CREDIT,
        requireAccountName: Boolean = true,
        requireBillingAddress: Boolean = true,
        feeMode: String = FeeMode.SURCHARGE) {

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
            if (this.api_key.length > 0) {
                val env = this.api_key.split("-")[2]
                this.constants = Constants(env)

                tags = hashMapOf("pay-theory-environment" to env)

                payTheoryTransaction =
                    Transaction(
                        this.activity!!,
                        api_key,
                        this.constants
                    )

                payTheoryTransaction.init()



                enableFields(this.paymentType, accountNameEnabled, billingAddressEnabled)

                val btn = activity!!.findViewById<Button>(R.id.submitButton)

                // credit card fields
                val (ccNumber, ccCVV, ccExpiration) = getCreditCardData()

                val hasCC = (ccNumber.visibility == View.VISIBLE
                        && ccCVV.visibility == View.VISIBLE
                        && ccExpiration.visibility == View.VISIBLE)

                // ach fields
                val (achAccount, achRouting) = getAchData()


                val achChooser: AppCompatAutoCompleteTextView = activity!!.findViewById(R.id.ach_type_choice)

                val items = listOf(getString(R.string.checking), getString(R.string.savings))
                val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_list_item, items)
                achChooser.setAdapter(adapter)

                // buyer options
                val accountName = activity!!.findViewById<PayTheoryEditText>(R.id.account_name)
                val billingAddress1 =
                    activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_1)
                val billingAddress2 =
                    activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_2)
                val billingCity = activity!!.findViewById<PayTheoryEditText>(R.id.billing_city)
                val billingState = activity!!.findViewById<PayTheoryEditText>(R.id.billing_state)
                val billingZip = activity!!.findViewById<PayTheoryEditText>(R.id.billing_zip)

                val hasACH = (achAccount.visibility == View.VISIBLE
                        && achRouting.visibility == View.VISIBLE)

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

                btn.setOnClickListener {
                    val buyerOptions = HashMap<String, String>()

                    if (hasAccountName) {
                        val names =
                            accountName.text.toString().split("\\s".toRegex()).toMutableList()
                        val firstName = names[0]
                        names.removeAt(0)
                        val lastName = names.joinToString(" ")
                        buyerOptions["first_name"] = firstName
                        buyerOptions["last_name"] = lastName
                    }

                    if (hasBillingAddress) {
                        buyerOptions["line_1"] = billingAddress1.text.toString()
                        buyerOptions["line_2"] = billingAddress2.text.toString()
                        buyerOptions["city"] = billingCity.text.toString()
                        buyerOptions["region"] = billingState.text.toString()
                        buyerOptions["postal_code"] = billingZip.text.toString()
                    }


                    if (hasCC) {
                        val expirationString = ccExpiration.text.toString()
                        val payment = Payment(
                            timing = System.currentTimeMillis(),
                            amount = amount,
                            type = PAYMENT_CARD,
                            number = ccNumber.text.toString().replace("\\s".toRegex(), ""),
                            security_code = ccCVV.text.toString(),
                            expiration_month = expirationString.split("/").first(),
                            expiration_year = expirationString.split("/").last(),
                            fee_mode = feeMode
                        )
                        makePayment(payment)
                    }

                    if (hasACH) {
                        val payment = Payment(
                            timing = System.currentTimeMillis(),
                            amount = amount,
                            account_type = achChooser.text.toString(),
                            type = BANK_ACCOUNT,
                            account_number = achAccount.text.toString(),
                            bank_code = achRouting.text.toString(),
                            fee_mode = feeMode
                        )
                        makePayment(payment)
                    }
                }
            }
        })
    }

    private fun getAchData(): Pair<PayTheoryEditText, PayTheoryEditText> {
        val achAccount = activity!!.findViewById<PayTheoryEditText>(R.id.ach_account_number)
        val achRouting = activity!!.findViewById<PayTheoryEditText>(R.id.ach_routing_number)
        return Pair(achAccount, achRouting)
    }

    private fun getCreditCardData(): Triple<PayTheoryEditText, PayTheoryEditText, PayTheoryEditText> {
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
        } else {
            if (accountNameEnabled) {
                enableAccountName()
            }
            enableCC()
        }


        if (billingAddressEnabled) {
            enableBillingAddress()
        }
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun makePayment(payment: Payment) {
        payTheoryTransaction.transact(payment)
    }

    private fun enableCC() {
        val ccNumber: PayTheoryEditText? = view?.findViewById(R.id.cc_number)
        ccNumber!!.visibility = View.VISIBLE
        val ccCVV: PayTheoryEditText? = view?.findViewById(R.id.cc_cvv)
        ccCVV!!.visibility = View.VISIBLE
        val ccExpiration: PayTheoryEditText? = view?.findViewById(R.id.cc_expiration)
        ccExpiration!!.visibility = View.VISIBLE
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


}









//TODO TAGS

//MAIN ACTIVITY
// val tags: HashMap<String, String> = hashMapOf("Customer_ID" to "12345ABC", "testing" to "123456789")
// payTheoryArgs.putSerializable(PayTheoryFragment.TAGS, tags)

//FRAGMENT
//        if ((arguments!!.getSerializable(TAGS) as HashMap<String, String>).isNotEmpty()){
//            tags.putAll(arguments!!.getSerializable(TAGS) as HashMap<String, String>)
//        }

//README
//        // create custom tags per transaction (OPTIONAL)
//        val tags: HashMap<String, String> = hashMapOf("Customer_ID" to "12345ABC")
//        payTheoryArgs.putSerializable(PayTheoryFragment.TAGS, tags)