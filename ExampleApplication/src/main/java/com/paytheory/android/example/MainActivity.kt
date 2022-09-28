package com.paytheory.android.example

import Address
import PayorInfo
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
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
    val apiKey = "API_KEY"
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
            "5135555555",
            Address(
                "10549 Reading Rd",
                "Apt 1",
                "Cincinnati",
                "OH",
                "45241",
                "USA")
        )

        //metadata configuration
        val metadata: HashMap<Any,Any> = hashMapOf(
            "studentId" to "student_1859034",
            "courseId" to "course_1859034"
        )

        try {
            //PayTheoryFragment configuration for card payments
            payTheoryFragment.configure(
                apiKey = apiKey,
                amount = 1000,
                transactionType = TransactionType.CARD,
                requireAccountName = true,
                requireBillingAddress = false,
                confirmation = true,
                feeMode = FeeMode.INTERCHANGE,
                metadata = metadata,
                payorInfo = payorInfo,
                sendReceipt = true,
                receiptDescription = "RECEIPT_DESCRIPTION",
                accountCode = "ACCOUNT_CODE",
                reference = "PAYMENT_REFERENCE",
                paymentParameters = "PAYMENT_PARAMETERS",
                payorId = "PAYOR_ID",
                invoiceId = "INVOICE_ID"
            )

            //PayTheoryFragment configuration for card payments
//            payTheoryFragment.tokenizePaymentMethod(
//                apiKey = apiKey,
//                tokenizationType = TokenizationType.CARD,
//                requireAccountName = true,
//                requireBillingAddress = false,
//                payorInfo = payorInfo,
//                payorId = "PAYOR_ID",
//                metadata = metadata
//            )

        } catch (e: Exception) {
            e.printStackTrace()
        }


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
    override fun paymentSuccess(completedTransactionResult: CompletedTransactionResult) {
        println(completedTransactionResult)
        showToast("Transaction Complete on Account XXXX${completedTransactionResult.lastFour}")
    }

    override fun paymentFailed(failedTransactionResult: FailedTransactionResult) {
        println(failedTransactionResult)
        showToast("Payment Failed on Account XXXX${failedTransactionResult.lastFour}")
    }

    override fun transactionError(error: Error) {
        println(error)
        showToast(error.reason)
    }

    //Demo function to display payment confirmation message to user
    override fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction) {
        Log.d("Pay Theory Demo", confirmationMessage.toString())

        val confirmationTextView = dialog!!.findViewById(R.id.popup_window_text) as TextView
        confirmationTextView.text = if (confirmationMessage.brand == "ACH") {
            "Are you sure you want to make a payment of ${getFormattedAmount(confirmationMessage.amount)}" +
                    " including the fee of ${getFormattedAmount(confirmationMessage.fee)} " +
                    "on account ending in ${confirmationMessage.lastFour}?"
        } else {
            "Are you sure you want to make a payment of ${getFormattedAmount(confirmationMessage.amount)}" +
                    " including the fee of ${getFormattedAmount(confirmationMessage.fee)} " +
                    "on ${confirmationMessage.brand} account beginning with ${confirmationMessage.firstSix}?"
        }

        val yesBtn = dialog!!.findViewById(R.id.btn_yes) as Button
        val noBtn = dialog!!.findViewById(R.id.btn_no) as Button

        yesBtn.setOnClickListener {
            dialog!!.dismiss()
            transaction.completeTransfer()
        }

        noBtn.setOnClickListener {
            dialog!!.dismiss()
            showToast("payment canceled")
            transaction.disconnect()
        }

        runOnUiThread {
            dialog!!.show()
        }
    }


    override fun barcodeSuccess(barcodeResult: BarcodeResult) {
        println(barcodeResult)
        showToast("Barcode Request Successful $barcodeResult")
    }

    override fun tokenizedSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        println(paymentMethodToken)
        showToast("Payment Method Tokenization Complete: ${paymentMethodToken.paymentMethodId}")
    }

    // Demo function to format dollar amount
    private fun getFormattedAmount(amount: String): String {
        var centsString: String? = null
        val cents = amount.toInt() % 100
        val dollars = (amount.toInt() - cents) / 100
        centsString = if (cents == 0) {
            "00"
        } else {
            cents.toString()
        }
        return "$$dollars.${centsString}"
    }
}
