package com.paytheory.paytheorylibrarysdk.fragments

import PaymentData
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.paytheory.paytheorylibrarysdk.R
import com.paytheory.paytheorylibrarysdk.classes.*
import com.paytheory.paytheorylibrarysdk.classes.validation.CVVFormattingTextWatcher
import com.paytheory.paytheorylibrarysdk.classes.validation.CreditCardFormattingTextWatcher
import com.paytheory.paytheorylibrarysdk.classes.validation.ExpirationFormattingTextWatcher
import com.paytheory.paytheorylibrarysdk.classes.view.PayTheoryEditText

/**
 * PayTheoryFragment populates with required text inputs.
 * Flexible display that adds inputs dynamically as needed
 */
class PayTheoryFragment : Fragment() {


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

        this.api_key = arguments!!.getString("api_key")!!
        this.amount = arguments!!.getInt("amount")

        enableAccountName()
        enableBillingAddress()
        enableCC()

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
        val hasACH = (achAccount.visibility == View.VISIBLE
                && achRouting.visibility == View.VISIBLE)

        // buyer options
        val accountName = activity!!.findViewById<PayTheoryEditText>(R.id.account_name)
        val billingAddress1 = activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_1)
        val billingAddress2 = activity!!.findViewById<PayTheoryEditText>(R.id.billing_address_2)
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
                val cardNumber = ccNumber.text.toString().replace("\\s".toRegex(), "")
                val cvv = ccCVV.text.toString()
                val expirationString = ccExpiration.text.toString()
                val expirationMonth = expirationString.split("/").first()
                val expirationYear = expirationString.split("/").last()

                val payment = PaymentData(
                    cardNumber,
                    cvv,
                    expirationMonth,
                    expirationYear,
                    "PAYMENT_CARD"
                )

                val payTheory = Transaction(
                    this.activity!!,
                    api_key,
                    payment,
                    tags,
                    buyerOptions
                )

                payTheory.init()
            }

        }
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