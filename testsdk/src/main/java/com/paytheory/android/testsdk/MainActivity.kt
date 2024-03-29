package com.paytheory.android.testsdk

import Address
import PayorInfo
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.paytheory.android.sdk.*
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.TokenizationType
import com.paytheory.android.sdk.configuration.TransactionType
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton

/**
 * Demo Activity class using Pay Theory Android SDK
 */
class MainActivity : AppCompatActivity(), Payable {

    private val apiKey = "MY_API_KEY"
    private var confirmationPopUp : Dialog? = null
    private var messagePopUp : Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //DEMO - Create confirmation view
        confirmationPopUp = Dialog(this)
        confirmationPopUp!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmationPopUp!!.setCancelable(false)
        confirmationPopUp!!.setContentView(R.layout.confirmation_layout)
        confirmationPopUp!!.window?.setBackgroundDrawableResource(android.R.color.transparent)

        //DEMO - Create error view
        messagePopUp = Dialog(this)
        messagePopUp!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        messagePopUp!!.setCancelable(false)
        messagePopUp!!.setContentView(R.layout.message_layout)
        messagePopUp!!.window?.setBackgroundDrawableResource(android.R.color.transparent)

        //Create submit button
        val submitButton = this.findViewById(R.id.submit) as PayTheoryButton

        //Create PayTheoryFragment
        val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        //Set optional PayorInfo configuration
        val payorInfo = PayorInfo(
            "John",
            "Doe",
            "abel@paytheory.com",
            "5135555555",
            Address(
                "10549 Reading Rd",
                "Apt 1",
                "Cincinnati",
                "OH",
                "45241",
                "USA"
            )
        )

        //Set optional metadata configuration
        val metadata: HashMap<Any,Any> = hashMapOf(
            "studentId" to "student_1859034",
            "courseId" to "course_1859034"
        )

        //Keep in try catch for any additional errors
        try {
            payTheoryFragment.configureTransact(
                paymentButton = submitButton,
                apiKey = apiKey,
                amount = 15000,
                transactionType = TransactionType.CARD,
                requireAccountName = true,
                requireBillingAddress = false,
                confirmation = true,
                feeMode = FeeMode.SERVICE_FEE,
                metadata = metadata,
                payorInfo = payorInfo,
                accountCode = "Test Account Code",
                reference = "Test Reference",
                sendReceipt = true,
                receiptDescription = "Android Payment Receipt Test",
                //paymentParameters = "TEST_PARAMS",
                //invoiceId = "TEST_INVOICE",
                //payorId = "TEST_PAYOR_ID"
            )

            submitButton.setOnClickListener{
                payTheoryFragment.transact()
            }




            //PayTheoryFragment tokenize for card and bank payment methods
//            payTheoryFragment.configureTokenize(
//                tokenizeButton = submitButton,
//                apiKey = apiKey,
//                tokenizationType = TokenizationType.CARD,
//                requireAccountName = false,
//                requireBillingAddress = false,
//                payorInfo = payorInfo,
//                metadata = metadata
//            )
//
//
//            submitButton.setOnClickListener{
//                payTheoryFragment.tokenize()
//            }




        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //DEMO - function to display a message
    private fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(
                this, message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //Inherited from Payable interface
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        println(successfulTransactionResult)
//        showToast("Transaction Complete on Account XXXX${successfulTransactionResult.lastFour}")
        val messageTextView = messagePopUp!!.findViewById(R.id.popup_window_text) as TextView
        val okBtn = messagePopUp!!.findViewById(R.id.btn_ok) as Button
        messageTextView.text = successfulTransactionResult.toString()
        okBtn.setOnClickListener {
            messagePopUp!!.dismiss()
            finish()
            startActivity(intent)
        }
        runOnUiThread { messagePopUp!!.show() }
    }

    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        println(failedTransactionResult)
//        showToast("Payment Failed on Account XXXX${failedTransactionResult.lastFour}")
        val messageTextView = messagePopUp!!.findViewById(R.id.popup_window_text) as TextView
        val okBtn = messagePopUp!!.findViewById(R.id.btn_ok) as Button
        messageTextView.text = failedTransactionResult.toString()
        okBtn.setOnClickListener {
            messagePopUp!!.dismiss()
            finish()
            startActivity(intent)
        }
        runOnUiThread { messagePopUp!!.show() }
    }

    override fun handleError(error: Error) {
        System.err.println(error)
    }

    //DEMO - function to display payment confirmation message to user
    override fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction) {
//        showToast(confirmationMessage.toString())
        val confirmationTextView = confirmationPopUp!!.findViewById(R.id.popup_window_text) as TextView
        confirmationTextView.text = if (confirmationMessage.brand == "ACH") {
            "Are you sure you want to make a payment of ${getFormattedAmount(confirmationMessage.amount)}" +
                    " including the fee of ${getFormattedAmount(confirmationMessage.fee)} " +
                    "on account ending in ${confirmationMessage.lastFour}?"
        } else {
            "Are you sure you want to make a payment of ${getFormattedAmount(confirmationMessage.amount)}" +
                    " including the fee of ${getFormattedAmount(confirmationMessage.fee)} " +
                    "on ${confirmationMessage.brand} account beginning with ${confirmationMessage.firstSix}?"
        }

        val yesBtn = confirmationPopUp!!.findViewById(R.id.btn_yes) as Button
        val noBtn = confirmationPopUp!!.findViewById(R.id.btn_no) as Button

        yesBtn.setOnClickListener {
            confirmationPopUp!!.dismiss()
            transaction.completeTransfer()
        }

        noBtn.setOnClickListener {
            confirmationPopUp!!.dismiss()
            showToast("payment canceled")
            transaction.disconnect()
        }

        runOnUiThread {
            confirmationPopUp!!.show()
        }
    }


    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        println(barcodeResult)
//        showToast("Barcode Request Successful $barcodeResult")
        val messageTextView = messagePopUp!!.findViewById(R.id.popup_window_text) as TextView
        val okBtn = messagePopUp!!.findViewById(R.id.btn_ok) as Button
        messageTextView.text = barcodeResult.toString()
        okBtn.setOnClickListener {
            messagePopUp!!.dismiss()
            finish()
            startActivity(intent)
        }
        runOnUiThread { messagePopUp!!.show() }
    }

    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        println(paymentMethodToken)
//        showToast("Payment Method Tokenization Complete: ${paymentMethodToken.paymentMethodId}")
        val messageTextView = messagePopUp!!.findViewById(R.id.popup_window_text) as TextView
        val okBtn = messagePopUp!!.findViewById(R.id.btn_ok) as Button
        messageTextView.text = paymentMethodToken.toString()
        okBtn.setOnClickListener {
            messagePopUp!!.dismiss()
            finish()
            startActivity(intent)
        }
        runOnUiThread { messagePopUp!!.show() }
    }

    //DEMO - function to format dollar amount
    private fun getFormattedAmount(amount: String): String {
        val centsString: String?
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



//payTheoryFragment.transact(
//apiKey = apiKey,
//amount = 5050,
//transactionType = TransactionType.CARD,
//requireAccountName = false,
//requireBillingAddress = false,
//confirmation = false,
//feeMode = FeeMode.MERCHANT_FEE,
//metadata = metadata,
//payorInfo = payorInfo,
//accountCode = "Test Account Code",
//reference = "Test Reference",
//sendReceipt = true,
//receiptDescription = "Android Payment Receipt Test",
//paymentParameters = "TEST_PARAMS",
//invoiceId = "TEST_INVOICE",
//payorId = "TEST_PAYOR_ID"
//)