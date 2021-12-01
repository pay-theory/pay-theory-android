package com.paytheory.android.example

import Address
import BuyerOptions
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    var payTheoryFragment : PayTheoryFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        payTheoryFragment = this.supportFragmentManager
            .findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        val buyerOptions = BuyerOptions("Jim", "Smith", "jim.smith@gmail.com", "513-123-4567",
            Address("123 Testing Lane", "Apt 2", "Cincinnati", "OH", "45236", "USA"))

        val tags = hashMapOf("pay-theory-account-code" to "ABC12345", "pay-theory-reference" to "12345ABC")

        payTheoryFragment!!.configure(apiKey,8500, PaymentType.CREDIT, false, false, false, FeeMode.SURCHARGE, buyerOptions, tags)

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
