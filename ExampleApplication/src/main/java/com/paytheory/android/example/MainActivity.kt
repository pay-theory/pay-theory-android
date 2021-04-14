package com.paytheory.android.example

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.PaymentError
import com.paytheory.android.sdk.PaymentResult
import com.paytheory.android.sdk.PaymentResultFailure
import com.paytheory.android.sdk.configuration.PaymentType
import com.paytheory.android.sdk.data.LiveDataViewModel
import com.paytheory.android.sdk.fragments.PayTheoryFragment

/**
 * Example activity class
 */
class MainActivity : FragmentActivity() , Payable {
    val apiKey = "pt-sandbox-abel-cc3dfd66a18dd51dca3930eede3b8489"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val payTheoryFragment = this.supportFragmentManager
            .findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        val buyerOptions = HashMap<String, Any>()
        buyerOptions["first_name"] = "Some"
        buyerOptions["last_name"] = "Body"
        buyerOptions["line_1"] = "123 Testing Lane"
        buyerOptions["line_2"] = "Apt 2"
        buyerOptions["city"] = "Cincinnati"
        buyerOptions["region"] = "Ohio"
        buyerOptions["postal_code"] = "45236"


        payTheoryFragment.configure(apiKey,5000, PaymentType.CREDIT, false, false)

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

    override fun paymentFailed(paymentFailure: PaymentResultFailure) {
        showToast("payment failed on account XXXX${paymentFailure.last_four} ${paymentFailure.type}")
    }

    override fun paymentError(paymentError: PaymentError) {
        showToast("an error occurred ${paymentError.reason}")
    }

}
