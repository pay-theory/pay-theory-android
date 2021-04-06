package com.paytheory.android.example.ui.ach

import Payment
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.sdk.Constants
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.data.LiveDataViewModel
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryEditText
import kotlinx.coroutines.ExperimentalCoroutinesApi

class AchFragment : Fragment() {
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
    private var tags: HashMap<String,String> = java.util.HashMap()

    /**
     * Display requested card fields
     * @return fragment_pay_theory_credit_card layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.paytheory.android.sdk.R.layout.fragment_pay_theory, container, false)
    }

    @ExperimentalCoroutinesApi
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()

        var model = activity?.run {
            ViewModelProvider(this).get(LiveDataViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        //retrieve data from live data viewModel
        this.api_key = model.selectedApiKey.value.toString()
        this.amount = model.selectedAmount.value!!
        val accountNameEnabled = model.selectedAccountNameField.value!!
        val billingAddressEnabled = model.selectedBillingAddressEnabled.value!!


        val env = this.api_key.split("-")[2]
        this.constants = Constants(env)

        tags = hashMapOf("pay-theory-environment" to env)

        payTheoryTransaction =
            Transaction(
                this.requireActivity(),
                api_key,
                this.constants
            )

        payTheoryTransaction.init()

        enableFields( accountNameEnabled, billingAddressEnabled)

        val btn = requireActivity().findViewById<Button>(com.paytheory.android.sdk.R.id.submitButton)

        // ach fields
        val (achAccount, achRouting) = getAchData()


        // buyer options
        val accountName = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.account_name)
        val billingAddress1 = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.billing_address_1)
        val billingAddress2 = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.billing_address_2)
        val billingCity = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.billing_city)
        val billingState = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.billing_state)
        val billingZip = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.billing_zip)


        val hasAccountName = accountName.visibility == View.VISIBLE

        val hasBillingAddress = (billingAddress1.visibility == View.VISIBLE
                && billingAddress2.visibility == View.VISIBLE
                && billingCity.visibility == View.VISIBLE
                && billingState.visibility == View.VISIBLE
                && billingZip.visibility == View.VISIBLE)

        btn.setOnClickListener {
            val buyerOptions = HashMap<String, String>()

            if (hasAccountName) {
                val names = accountName.text.toString().split("\\s".toRegex()).toMutableList()
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


                val payment = Payment(
                    timing = System.currentTimeMillis(),
                    amount = amount,
                    type = PayTheoryFragment.BANK_ACCOUNT,
                    account_number = achAccount.text.toString(),
                    bank_code = achRouting.text.toString()
                )
                makePayment(payment)
        }
    }

    private fun getAchData(): Pair<PayTheoryEditText, PayTheoryEditText> {
        val achAccount = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.ach_account_number)
        val achRouting = requireActivity().findViewById<PayTheoryEditText>(com.paytheory.android.sdk.R.id.ach_routing_number)
        return Pair(achAccount, achRouting)
    }

    private fun enableFields(
        accountNameEnabled: Boolean,
        billingAddressEnabled: Boolean
    ) {
            enableACH()
            if (accountNameEnabled) {
                enableAccountName()
            }

        if (billingAddressEnabled) {
            enableBillingAddress()
        }
    }

    @ExperimentalCoroutinesApi
    private fun makePayment(payment: Payment) {
        payTheoryTransaction.transact(payment)
    }

    private fun enableBillingAddress() {
        val line1: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.billing_address_1)
        line1!!.visibility = View.VISIBLE
        val line2: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.billing_address_2)
        line2!!.visibility = View.VISIBLE
        val city: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.billing_city)
        city!!.visibility = View.VISIBLE
        val state: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.billing_state)
        state!!.visibility = View.VISIBLE
        val zip: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.billing_zip)
        zip!!.visibility = View.VISIBLE
    }

    private fun enableAccountName() {
        val accountName: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.account_name)
        accountName!!.visibility = View.VISIBLE
    }

    private fun enableACH() {
        val achAccount: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.ach_account_number)
        achAccount!!.visibility = View.VISIBLE
        val achRouting: PayTheoryEditText? = view?.findViewById(com.paytheory.android.sdk.R.id.ach_routing_number)
        achRouting!!.visibility = View.VISIBLE
    }
}