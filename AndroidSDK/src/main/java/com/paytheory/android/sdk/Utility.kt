package com.paytheory.android.sdk

import android.app.Activity
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.paytheory.android.sdk.configuration.TokenizationType
import com.paytheory.android.sdk.configuration.TransactionType
import com.paytheory.android.sdk.fragments.*
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
    fun getAchFields(activity: Activity): Pair<PayTheoryEditText, PayTheoryEditText> {
        val achAccount = activity.findViewById<PayTheoryEditText>(R.id.ach_account_number)
        val achRouting = activity.findViewById<PayTheoryEditText>(R.id.ach_routing_number)
        return Pair(achAccount, achRouting)
    }

    private fun enableCC(view: View) {
        val ccNumber: PayTheoryEditText? = view.findViewById(R.id.cc_number)
        ccNumber!!.visibility = View.VISIBLE
        val ccCVV: PayTheoryEditText? = view.findViewById(R.id.cc_cvv)
        ccCVV!!.visibility = View.VISIBLE
        val ccExpiration: PayTheoryEditText? = view.findViewById(R.id.cc_expiration)
        ccExpiration!!.visibility = View.VISIBLE
        val billingZip: PayTheoryEditText? = view.findViewById(R.id.billing_zip)
        billingZip!!.visibility = View.VISIBLE
        val cvvAndExpiration: LinearLayout? = view.findViewById(R.id.cvv_and_expiration)
        cvvAndExpiration!!.visibility = View.VISIBLE
    }

    private fun enableBillingAddress(view: View) {
        val line1: PayTheoryEditText? = view.findViewById(R.id.billing_address_1)
        line1!!.visibility = View.VISIBLE
        val line2: PayTheoryEditText? = view.findViewById(R.id.billing_address_2)
        line2!!.visibility = View.VISIBLE
        val city: PayTheoryEditText? = view.findViewById(R.id.billing_city)
        city!!.visibility = View.VISIBLE
        val state: PayTheoryEditText? = view.findViewById(R.id.billing_state)
        state!!.visibility = View.VISIBLE
        val billingZip: PayTheoryEditText? = view.findViewById(R.id.billing_zip)
        billingZip!!.visibility = View.VISIBLE
    }

    private fun enableAccountName(view: View) {
        val accountName: PayTheoryEditText? = view.findViewById(R.id.account_name)
        accountName!!.visibility = View.VISIBLE
    }

    private fun enableACH(view: View) {
        val achAccount: PayTheoryEditText? = view.findViewById(R.id.ach_account_number)
        achAccount!!.visibility = View.VISIBLE
        val achRouting: PayTheoryEditText? = view.findViewById(R.id.ach_routing_number)
        achRouting!!.visibility = View.VISIBLE
        val achChoice: TextInputLayout? = view.findViewById(R.id.ach_type_choice_layout)
        achChoice!!.visibility = View.VISIBLE
    }

    private fun enableCash(view: View) {
        val cashContact: PayTheoryEditText? = view.findViewById(R.id.cashContact)
        cashContact!!.visibility = View.VISIBLE
        val cashName: PayTheoryEditText? = view.findViewById(R.id.cashName)
        cashName!!.visibility = View.VISIBLE
    }

    /**
     * Enables payment fields based on transaction type
     */
    fun enablePaymentFields(
        view: View,
        transactionType: TransactionType,
        requireAccountName: Boolean,
        requireBillingAddress: Boolean
    ) {
        if (transactionType == TransactionType.BANK) {
            enableAccountName(view)
            enableACH(view)
        }
        if (transactionType == TransactionType.CARD) {
            if (requireAccountName) {
                enableAccountName(view)
            }
            enableCC(view)
        }
        if (transactionType == TransactionType.CASH) {
            enableCash(view)
        }

        if (requireBillingAddress && transactionType != TransactionType.CASH) {
            enableBillingAddress(view)
        }
    }

    /**
     * Enables tokenization fields based on tokenization type
     */
    fun enableTokenizationFields(
        view: View,
        tokenizationType: TokenizationType,
        requireAccountName: Boolean,
        requireBillingAddress: Boolean
    ) {
        if (tokenizationType == TokenizationType.BANK) {
            enableAccountName(view)
            enableACH(view)
        }
        if (tokenizationType == TokenizationType.CARD) {
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
     */
    fun createPayTheoryData(sendReceipt: Boolean?, receiptDescription: String?, paymentParameters: String?, payorId: String?, invoiceId: String?, accountCode: String?, reference: String?): HashMap<Any, Any> {
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

        return payTheoryData
    }
}