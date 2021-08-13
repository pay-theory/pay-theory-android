package com.paytheory.android.example

import Address
import BuyerOptions
import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val payTheoryFragment = this.supportFragmentManager
            .findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        val buyerOptions = BuyerOptions("Jim", "Smith", "jim.smith@gmail.com", "513-123-4567",
            Address("123 Testing Lane", "Apt 2", "Cincinnati", "OH", "45236", "USA"))

        payTheoryFragment.configure(apiKey,2965, PaymentType.CASH, false, false, FeeMode.SURCHARGE, buyerOptions)

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
}
