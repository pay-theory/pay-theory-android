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

    override fun paymentComplete(transactionResult: TransactionResult) {
        showToast("payment successful on account XXXX${transactionResult.lastFour}")
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
    override fun paymentConfirmation(confirmationData: ConfirmationMessage, transaction: Transaction) {
        Log.d("Pay Theory Demo", confirmationData.toString())

        val confirmationTextView = dialog!!.findViewById(R.id.popup_window_text) as TextView
        confirmationTextView.text = if (confirmationData.bin.card_brand == "ACH") {
            "Are you sure you want to make a payment of $${confirmationData.payment.amount.toFloat()/100}" +
                    " including the fee of $${confirmationData.payment.service_fee!!.toFloat()/100} " +
                    "on account ending in ${confirmationData.bin.last_four}?"
        } else {
            "Are you sure you want to make a payment of $${confirmationData.payment.amount.toFloat()/100}" +
                    " including the fee of $${confirmationData.payment.service_fee!!.toFloat()/100} " +
                    "on ${confirmationData.bin.card_brand} account beginning with ${confirmationData.bin.first_six}?"
        }

        val yesBtn = dialog!!.findViewById(R.id.btn_yes) as Button
        val noBtn = dialog!!.findViewById(R.id.btn_no) as Button

        yesBtn.setOnClickListener {
            dialog!!.dismiss()
            transaction.completeTransfer()
        }

        noBtn.setOnClickListener {
            dialog!!.dismiss()
            showToast("payment canceled on account beginning with ${confirmationData.bin.first_six}")
            transaction.disconnect()
        }

        runOnUiThread {
            dialog!!.show()
        }
    }

}
