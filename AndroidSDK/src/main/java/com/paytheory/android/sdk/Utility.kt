package com.paytheory.android.sdk

import android.icu.util.TimeZone
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout
import com.paytheory.android.sdk.configuration.PaymentMethodType
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Contains utility functions for pay theory transactions
 * handles enabling fields based on request types
 * creates payTheoryData object for requests
 * NOT FOR PUBLIC SDK USE
 */
class Utility {

    /**
     * Retrieves bank account and routing fields
     */
    fun getAchFields(view: View): Pair<PayTheoryEditText, PayTheoryEditText> {
        val achAccount = view.findViewById<PayTheoryEditText>(R.id.ach_account_number)
        val achRouting = view.findViewById<PayTheoryEditText>(R.id.ach_routing_number)
        return Pair(achAccount, achRouting)
    }

    private fun enableCC(view: View) {
        val ccNumberLayout: TextInputLayout? = view.findViewById(R.id.cc_number_layout)
        ccNumberLayout!!.visibility = View.VISIBLE
        val ccCVV: TextInputLayout? = view.findViewById(R.id.cc_cvv_layout)
        ccCVV!!.visibility = View.VISIBLE
        val ccExpiration: TextInputLayout? = view.findViewById(R.id.cc_expiration_layout)
        ccExpiration!!.visibility = View.VISIBLE
        val billingZip: TextInputLayout? = view.findViewById(R.id.billing_zip_layout)
        billingZip!!.visibility = View.VISIBLE
        val cvvAndExpiration: LinearLayout? = view.findViewById(R.id.cvv_and_expiration)
        cvvAndExpiration!!.visibility = View.VISIBLE
    }

    private fun enableBillingAddress(view: View) {
        val line1: TextInputLayout? = view.findViewById(R.id.billing_address_1_layout)
        line1!!.visibility = View.VISIBLE
        val line2: TextInputLayout? = view.findViewById(R.id.billing_address_2_layout)
        line2!!.visibility = View.VISIBLE
        val city: TextInputLayout? = view.findViewById(R.id.billing_city_layout)
        city!!.visibility = View.VISIBLE
        val state: TextInputLayout? = view.findViewById(R.id.billing_state_layout)
        state!!.visibility = View.VISIBLE
        val billingZip: TextInputLayout? = view.findViewById(R.id.billing_zip_layout)
        billingZip!!.visibility = View.VISIBLE
    }

    private fun enableAccountName(view: View) {
        val accountName: TextInputLayout? = view.findViewById(R.id.account_name_layout)
        accountName!!.visibility = View.VISIBLE
    }

    private fun enableACH(view: View) {
        val achAccount: TextInputLayout? = view.findViewById(R.id.ach_account_number_layout)
        achAccount!!.visibility = View.VISIBLE
        val achRouting: TextInputLayout? = view.findViewById(R.id.ach_routing_number_layout)
        achRouting!!.visibility = View.VISIBLE
        val achChoice: TextInputLayout? = view.findViewById(R.id.ach_type_choice_layout)
        achChoice!!.visibility = View.VISIBLE
    }

    private fun enableCash(view: View) {
        val cashContact: TextInputLayout? = view.findViewById(R.id.cash_contact_layout)
        cashContact!!.visibility = View.VISIBLE
        val cashName: TextInputLayout? = view.findViewById(R.id.cash_name_layout)
        cashName!!.visibility = View.VISIBLE
    }

    /**
     * Enables payment fields based on transaction type
     */
    fun enablePaymentFields(
        view: View,
        paymentMethodType: PaymentMethodType,
        requireAccountName: Boolean,
        requireBillingAddress: Boolean
    ) {
        if (paymentMethodType == PaymentMethodType.BANK) {
            enableAccountName(view)
            enableACH(view)
        }
        if (paymentMethodType == PaymentMethodType.CARD) {
            if (requireAccountName) {
                enableAccountName(view)
            }
            enableCC(view)
        }
        if (paymentMethodType == PaymentMethodType.CASH) {
            enableCash(view)
        }

        if (requireBillingAddress && paymentMethodType != PaymentMethodType.CASH) {
            enableBillingAddress(view)
        }
    }

    /**
     * Enables tokenization fields based on tokenization type
     */
    fun enableTokenizationFields(
        view: View,
        paymentMethodType: PaymentMethodType,
        requireAccountName: Boolean,
        requireBillingAddress: Boolean
    ) {
        if (paymentMethodType == PaymentMethodType.BANK) {
            enableAccountName(view)
            enableACH(view)
        }
        if (paymentMethodType == PaymentMethodType.CARD) {
            if (requireAccountName) {
                enableAccountName(view)
            }
            enableCC(view)
        }

        if (requireBillingAddress) {
            enableBillingAddress(view)
        }
    }

    /**
     * Creates payTheoryData object for transfer requests
     * @param configuration PayTheoryConfiguration object
     * @return HashMap<Any, Any> of pay theory data for the request
     */
    fun createPayTheoryData(configuration: PayTheoryConfiguration): HashMap<Any, Any> {
        //create pay_theory_data object for host:transfer_part1 action request
        val payTheoryData = hashMapOf<Any, Any>()
        //if send receipt is enabled add send_receipt and receipt_description to pay_theory_data
        populateKey("send_receipt", payTheoryData, configuration.sendReceipt)
        if (configuration.skipTokenizeValidation == true) {
            populateKey("skip_validation", payTheoryData, configuration.skipTokenizeValidation!!)
        }
        if (configuration.sendReceipt == true) {
            populateKey("send_receipt", payTheoryData, configuration.sendReceipt)
            if (configuration.receiptDescription.isNotBlank()){
                populateKey("receipt_description", payTheoryData, configuration.receiptDescription)
            }
        }
        // if paymentParameters is given add to pay_theory_data
        if (!configuration.paymentParameters.isNullOrBlank()) {
            populateKey("payment_parameters", payTheoryData, configuration.paymentParameters!!)
        }
        // if payorId is given add to pay_theory_data
        if (!configuration.payorId.isNullOrBlank()) {
            populateKey("payor_id", payTheoryData, configuration.payorId!!)
        }
        // if invoiceId is given add to pay_theory_data
        if (!configuration.invoiceId.isNullOrBlank()) {
            populateKey("invoice_id", payTheoryData, configuration.invoiceId!!)
        }
        // if account_code is given add to pay_theory_data
        if (!configuration.accountCode.isNullOrBlank()) {
            populateKey("account_code", payTheoryData, configuration.accountCode!!)
        }
        // if reference is given add to pay_theory_data
        if (!configuration.reference.isNullOrBlank()) {
            populateKey("reference", payTheoryData, configuration.reference!!)
        }

        payTheoryData["fee"] = configuration.serviceFee as Any

        payTheoryData["timezone"] = TimeZone.getDefault().id

        return payTheoryData
    }

    private fun populateKey(key: String, payTheoryData: HashMap<Any,Any>, value: Any) {
        payTheoryData.set(key,value)
    }

}