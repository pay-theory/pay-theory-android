package com.paytheory.android.sdk.fragments

import ACHPaymentData
import CCPaymentData
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.paytheory.android.sdk.R
import com.paytheory.android.sdk.Transaction
import com.paytheory.android.sdk.validation.CVVFormattingTextWatcher
import com.paytheory.android.sdk.validation.CreditCardFormattingTextWatcher
import com.paytheory.android.sdk.validation.ExpirationFormattingTextWatcher
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * PayTheoryFragment populates with required text inputs.
 * Flexible display that adds inputs dynamically as needed
 */
class PayTheoryFragment : Fragment() {
    companion object {
        val API_KEY = "api_key"
        val AMOUNT = "amount"
        val USE_ACH = "ach_enabled"
        val ACCOUNT_NAME_ENABLED = "account_name_enabled"
        val BILLING_ADDRESS_ENABLED = "billing_address_enabled"
    }

    private var api_key: String = ""
    private var amount: Int = 0
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        this.api_key = arguments!!.getString(API_KEY)!!
        this.amount = arguments!!.getInt(AMOUNT)

        val achEnabled = arguments!!.getBoolean(USE_ACH)
        val accountNameEnabled = arguments!!.getBoolean(ACCOUNT_NAME_ENABLED)
        val billingAddressEnabled = arguments!!.getBoolean(BILLING_ADDRESS_ENABLED)

        if (achEnabled) {
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

        val btn = activity!!.findViewById<Button>(R.id.submitButton)

        // credit card fields
        val ccNumber = activity!!.findViewById<PayTheoryEditText>(R.id.cc_number)
        val ccCVV = activity!!.findViewById<PayTheoryEditText>(R.id.cc_cvv)
        val ccExpiration = activity!!.findViewById<PayTheoryEditText>(R.id.cc_expiration)

        val hasCC = (ccNumber.visibility == View.VISIBLE
                && ccCVV.visibility == View.VISIBLE
                && ccExpiration.visibility == View.VISIBLE)

        // ach fields
        val achAccount = activity!!.findViewById<PayTheoryEditText>(R.id.ach_account_number)
        val achRouting = activity!!.findViewById<PayTheoryEditText>(R.id.ach_routing_number)


        // buyer options
        val accountName = activity!!.findViewById<PayTheoryEditText>(R.id.account_name)
        val billingAddress1 = activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_1)
        val billingAddress2 = activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_2)
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
            val ccNumberValidation: (PayTheoryEditText)-> CreditCardFormattingTextWatcher = { pt -> CreditCardFormattingTextWatcher(pt) }
            val cvvNumberValidation: (PayTheoryEditText)-> CVVFormattingTextWatcher = { pt -> CVVFormattingTextWatcher(pt) }
            val expirationValidation: (PayTheoryEditText)-> ExpirationFormattingTextWatcher = { pt -> ExpirationFormattingTextWatcher(pt) }

            ccNumber.addTextChangedListener(ccNumberValidation(ccNumber))
            ccCVV.addTextChangedListener(cvvNumberValidation(ccCVV))
            ccExpiration.addTextChangedListener(expirationValidation(ccExpiration))
        }

        btn.setOnClickListener {
            val buyerOptions = HashMap<String,String>()
            val tags = HashMap<String,String>()

            if (hasAccountName) {
                val names = accountName.text.toString().split("\\s".toRegex()).toMutableList()
                val firstName = names[0]
                names.removeFirst()
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
                val payment = CCPaymentData(
                    ccNumber.text.toString().replace("\\s".toRegex(), ""),
                    ccCVV.text.toString(),
                    expirationString.split("/").first(),
                    expirationString.split("/").last()
                )
                makePayment(payment,tags,buyerOptions)
            }

            if (hasACH) {
                val payment = ACHPaymentData(
                    achAccount.text.toString(),
                    achRouting.text.toString())
                makePayment(payment,tags,buyerOptions)
            }
        }
    }

    private fun makePayment(payment: Any, tags: Map<String,String>, buyerOptions: Map<String,String>) {
        val payTheoryTransaction =
            Transaction(
                this.activity!!,
                api_key,
                payment,
                tags,
                buyerOptions
            )


        payTheoryTransaction.init()
    }

    private fun showToast(message: String) {
        Toast.makeText(
            activity, message,
            Toast.LENGTH_LONG
        ).show()
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
    }


}