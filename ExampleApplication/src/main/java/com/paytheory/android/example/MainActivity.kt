package com.paytheory.android.example

import Address
import BuyerOptions
import PaymentConfirmation
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.paytheory.android.sdk.*
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.PaymentType
import com.paytheory.android.sdk.fragments.PayTheoryFragment


/**
 * Example activity class
 */
class MainActivity : FragmentActivity() , Payable {
    val apiKey = "My-Api-Key"
    var dialog : Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create confirmation view
        dialog = Dialog(this)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(false)
        dialog!!.setContentView(R.layout.confirmation_layout)

        //Create PayTheoryFragment
        var payTheoryFragment = this.supportFragmentManager
            .findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        //BuyerOptions configuration
        val buyerOptions = BuyerOptions("Jim", "Smith", "jim.smith@gmail.com", "513-123-4567",
            Address("123 Testing Lane", "Apt 2", "Cincinnati", "OH", "45236", "USA"))

        //tags configuration
        val tags = hashMapOf("pay-theory-account-code" to "ABC12345", "pay-theory-reference" to "12345ABC")

        //PayTheoryFragment configuration
        payTheoryFragment!!.configure(apiKey,8000, PaymentType.CREDIT, false, false, true, FeeMode.SERVICE_FEE, buyerOptions, tags)

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
        showToast("payment failed on account XXXX${paymentFailure.last_four} ${paymentFailure.type}")
    }

    override fun transactionError(transactionError: TransactionError) {
        showToast("an error occurred ${transactionError.reason}")
    }

    //Demo function to display payment confirmation message to user
    override fun confirmation(paymentConfirmation: PaymentConfirmation, transaction: Transaction) {
        Log.d("Pay Theory Demo", paymentConfirmation.toString())

        var confirmationTextView = dialog!!.findViewById(R.id.popup_window_text) as TextView
        confirmationTextView.text  = "Are you sure you want to make a payment of ${paymentConfirmation}on ${paymentConfirmation.bin.card_brand}" +
                " card beginning with ${paymentConfirmation.bin.first_six}?"

        val yesBtn = dialog!!.findViewById(R.id.btn_yes) as Button
        val noBtn = dialog!!.findViewById(R.id.btn_no) as Button

        yesBtn.setOnClickListener {
            dialog!!.dismiss()
            transaction.completeTransfer(paymentConfirmation)
        }

        noBtn.setOnClickListener {
            dialog!!.dismiss()
            transaction.disconnect()
        }

        runOnUiThread {
            dialog!!.show()
        }
    }

}
