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
import com.paytheory.android.sdk.configuration.TransactionType
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton

/**
 * Example Activity class using Pay Theory Android SDK
 */
class MainActivity : AppCompatActivity(), Payable {

    // PAY THEORY API KEY
    private val apiKey = "evolve-paytheorylab-d65599d803b25e048140dcd8b21455db"
    private var confirmationPopUp : Dialog? = null
    private var messagePopUp : Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // CREATE CONFIRMATION POP UP VIEW
        confirmationPopUp = Dialog(this)
        confirmationPopUp!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmationPopUp!!.setCancelable(false)
        confirmationPopUp!!.setContentView(R.layout.confirmation_layout)
        confirmationPopUp!!.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // CREATE MESSAGE POP UP VIEW
        messagePopUp = Dialog(this)
        messagePopUp!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        messagePopUp!!.setCancelable(false)
        messagePopUp!!.setContentView(R.layout.message_layout)
        messagePopUp!!.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // CREATE SUBMIT BUTTON TO INITIALIZE THE TRANSACTION
        val submitButton = this.findViewById(R.id.submit) as PayTheoryButton

        // CREATE PayTheoryFragment
        val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        // OPTIONAL PAYOR INFO FOR A TRANSACTION
        val payorInfo = PayorInfo(
            "Abel",
            "Collins",
            "abel@paytheory.com",
            "5131231234",
            Address(
                "10549 Reading Rd",
                "Apt 1",
                "Cincinnati",
                "OH",
                "45241",
                "USA"
            )
        )

        // OPTIONAL METADATA FOR A TRANSACTION
        val metadata: HashMap<Any,Any> = hashMapOf(
            "studentId" to "student_1859034",
            "courseId" to "course_1859034"
        )

        try {
            // CONFIGURE TRANSACTION
            payTheoryFragment.configureTransact(
                paymentButton = submitButton,
                apiKey = apiKey,
                amount = 10000,
                transactionType = TransactionType.BANK,
                requireAccountName = false,
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

            // TRANSACT ON SUBMIT BUTTON
            submitButton.setOnClickListener{
                payTheoryFragment.transact()
            }

//            // CONFIGURE TOKENIZATION
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
//            // TOKENIZE ON SUBMIT BUTTON
//            submitButton.setOnClickListener{
//                payTheoryFragment.tokenize()
//            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // GENERIC FUNCTION TO DISPLAY A MESSAGE
    private fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(
                this, message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // SUCCESSFUL CARD AND BANK PAYMENT RESPONSE HANDLER
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
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

    // FAILURE RESPONSE HANDLER
    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
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

    // SYSTEM ERROR RESPONSE HANDLER
    override fun handleError(error: Error) {
        System.err.println(error)
    }

    // GENERIC FUNCTION TO DISPLAY THE CONFIRMATION MESSAGE POP UP
    override fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction) {
        val confirmationTextView = confirmationPopUp!!.findViewById(R.id.popup_window_text) as TextView
        confirmationTextView.text = if (confirmationMessage.brand == "ACH") {
            "Are you sure you want to make a payment of ${formatDollarAmount(confirmationMessage.amount)}" +
                    " including the fee of ${formatDollarAmount(confirmationMessage.fee)} " +
                    "on account ending in ${confirmationMessage.lastFour}?"
        } else {
            "Are you sure you want to make a payment of ${formatDollarAmount(confirmationMessage.amount)}" +
                    " including the fee of ${formatDollarAmount(confirmationMessage.fee)} " +
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

    // SUCCESSFUL BARCODE RESPONSE HANDLER
    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        println(barcodeResult)
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

    // SUCCESSFUL TOKENIZATION RESPONSE HANDLER
    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        println(paymentMethodToken)
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

    // GENERIC FUNCTION TO FORMAT DOLLAR AMOUNT
    private fun formatDollarAmount(amount: String): String {
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