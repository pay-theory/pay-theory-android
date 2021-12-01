package com.paytheory.android.example

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.paytheory.android.sdk.*

/**
 * Example Activity
 */
class MainActivity : AppCompatActivity(), Payable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

    override fun confirmation(message: String, transaction: Transaction) {
        print(message)

        this.let {
            val builder = AlertDialog.Builder(it)
            builder?.setMessage("Are you sure you want to make a payment on VISA card beginning with 424242")
                ?.setTitle("Confirm transaction")

            builder.apply {
                setPositiveButton("Yes"
                ) { _, _ ->
                    // User clicked yes
                    transaction.completeTransfer(message)
                }
                setNegativeButton("No"
                ) { _, _ ->
                    // User clicked no
                    transaction.disconnect()

                }
            }
            builder.create()
            builder.show()
        }


    }


    }
}