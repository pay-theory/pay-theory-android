package com.paytheory.android.example

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.paytheory.android.sdk.*

/**
 * Example Activity
 */
class MainActivity : AppCompatActivity(), Payable {
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

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.credit_card, R.id.ach, R.id.cash))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    private fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(
                this, message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //Inherited from Payable interface
    override fun handleSuccess(transactionResult: SuccessfulTransactionResult) {
        println("transactionResult $transactionResult")
        showToast("Transaction Complete on Account XXXX${transactionResult.lastFour}")
    }

    override fun handleFailure(transactionResult: FailedTransactionResult) {
        println("paymentFailure $transactionResult")
        showToast("Payment Failed on Account XXXX${transactionResult.lastFour}")
    }

    override fun handleError(error: Error) {
        println("handleError $error")
        showToast("Error occurred ${error.reason}")
    }

    //Demo function to display payment confirmation message to user
    override fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction) {
        Log.d("Pay Theory Demo", confirmationMessage.toString())

        val confirmationTextView = dialog!!.findViewById(R.id.popup_window_text) as TextView
        confirmationTextView.text = if (confirmationMessage.brand == "ACH") {
            "Are you sure you want to make a payment of $${confirmationMessage.amount.toFloat()/100}" +
                    " including the fee of $${confirmationMessage.fee!!.toFloat()/100} " +
                    "on account ending in ${confirmationMessage.lastFour}?"
        } else {
            "Are you sure you want to make a payment of $${confirmationMessage.amount.toFloat()/100}" +
                    " including the fee of $${confirmationMessage.fee!!.toFloat()/100} " +
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


    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        println("barcodeResult $barcodeResult")
        showToast("Barcode Request Successful $barcodeResult")
    }

    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        println("tokenize payment method results $paymentMethodToken")
        showToast("Payment Method Tokenization Complete: ${paymentMethodToken.paymentMethodId}")
    }

}
