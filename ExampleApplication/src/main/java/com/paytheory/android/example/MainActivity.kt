package com.paytheory.android.example

import Address
import PayorInfo
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.android.sdk.*
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.TransactionType
import com.paytheory.android.sdk.fragments.PayTheoryFragment


/**
 * Example activity class
 */
class MainActivity : AppCompatActivity() , Payable {
    val apiKey = "austin-paytheorylab-d7dbe665f5565fe8ae8a23eab45dd285"
    var dialog : Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create confirmation view
        dialog = Dialog(this)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(false)
        dialog!!.setContentView(R.layout.confirmation_layout)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)

        //Create PayTheoryFragment
        val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        //PayorInfo configuration
        val payorInfo = PayorInfo(
            "Abel",
            "Collins",
            "abel@paytheory.com",
            "513-123-4567",
            Address(
                "123 Testing Lane",
                "Apt 2",
                "Cincinnati",
                "OH",
                "45236",
                "USA")
        )

        //metadata configuration
        val metadata: HashMap<Any,Any> = hashMapOf(
            "pay-theory-account-code" to "test-acccount-code",
            "pay-theory-reference" to "android-test",
            "payment-parameters-name" to "expires-in-30-days" //TODO
        )


        //PayTheoryFragment configuration for card payments
        payTheoryFragment.configure(
            apiKey = apiKey,
            amount = 1000,
            transactionType = TransactionType.CARD,
            requireAccountName = false,
            requireBillingAddress = false,
            confirmation = true,
            feeMode = FeeMode.SERVICE_FEE,
            metadata = metadata,
            payorInfo = payorInfo,
            payorId = "payor-1234", //TODO
            accountCode = "test-account-code", //TODO
            reference = "android-test", //TODO
            paymentParameters = "expires-in-30-days", //TODO
            invoiceId = "pt_inv_XXXXXXXXX", //TODO
            sendReceipt = true,
            receiptDescription = "Test on Android SDK")

        //PayTheoryFragment configuration for bank account payments

        //payTheoryFragment.configure(apiKey,5600, TransactionType.BANK, false, false, false, FeeMode.SERVICE_FEE, payorInfo, metadata)

        //PayTheoryFragment configuration for cash payments
        //payTheoryFragment.configure(apiKey,7500, TransactionType.CASH, false, false,  false, FeeMode.SERVICE_FEE, payorInfo, metadata)
    }

    //Demo function to display payment response
    private fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(
                this, message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //Inherited from Payable interface
    override fun paymentComplete(paymentResult: PaymentResult) {
        showToast("payment successful on account XXXX${paymentResult.last_four}")
    }

    override fun barcodeComplete(barcodeResult: BarcodeResult) {
        showToast("barcode request successful $barcodeResult")
    }

    override fun paymentFailed(paymentFailure: PaymentResultFailure) {
        showToast("payment failed on account XXXX${paymentFailure.last_four}")
    }

    override fun transactionError(transactionError: TransactionError) {
        showToast("an error occurred ${transactionError.reason}")
    }

    //Demo function to display payment confirmation message to user
    override fun paymentConfirmation(confirmationData: PaymentConfirmation, transaction: Transaction) {
        Log.d("Pay Theory Demo", confirmationData.toString())

        val confirmationTextView = dialog!!.findViewById(R.id.popup_window_text) as TextView
        confirmationTextView.text = if (confirmationData.brand == "ACH") {
            "Are you sure you want to make a payment of $${confirmationData.amount.toFloat()/100}" +
                    " including the fee of $${confirmationData.fee!!.toFloat()/100} " +
                    "on account ending in ${confirmationData.lastFour}?"
        } else {
            "Are you sure you want to make a payment of $${confirmationData.amount.toFloat()/100}" +
                    " including the fee of $${confirmationData.fee!!.toFloat()/100} " +
                    "on ${confirmationData.brand} account beginning with ${confirmationData.firstSix}?"
        }

        val yesBtn = dialog!!.findViewById(R.id.btn_yes) as Button
        val noBtn = dialog!!.findViewById(R.id.btn_no) as Button

        yesBtn.setOnClickListener {
            dialog!!.dismiss()
            transaction.completeTransfer(confirmationData)
        }

        noBtn.setOnClickListener {
            dialog!!.dismiss()
            showToast("payment canceled on account beginning with ${confirmationData.firstSix}")
            transaction.disconnect()
        }

        runOnUiThread {
            dialog!!.show()
        }
    }

}
