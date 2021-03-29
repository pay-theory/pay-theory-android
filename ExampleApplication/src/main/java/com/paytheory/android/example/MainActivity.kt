package com.paytheory.android.example

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.PaymentError
import com.paytheory.android.sdk.PaymentResult
import com.paytheory.android.sdk.fragments.PayTheoryFragment

/**
 * Example activity class
 */
class MainActivity : FragmentActivity() , Payable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment)

        val payTheoryArgs = Bundle()

        payTheoryArgs.putString(PayTheoryFragment.API_KEY, "pt-sandbox-dev-34a4a8979a9f3de0057d47eb05245255")
        payTheoryArgs.putInt(PayTheoryFragment.AMOUNT, 4200)
        payTheoryArgs.putBoolean(PayTheoryFragment.ACCOUNT_NAME_ENABLED, false)

        val tags: HashMap<String, String> = hashMapOf("test tag" to "testing")
        payTheoryArgs.putSerializable(PayTheoryFragment.TAGS, tags)

        payTheoryFragment!!.arguments = payTheoryArgs
    }

    private fun showToast(message: String){
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun paymentComplete(paymentResult: PaymentResult) {
        showToast("payment successful on account XXXX${paymentResult.last_four}")
    }

    override fun paymentFailed(paymentFailure: PaymentResult) {
        showToast("payment failed on account XXXX${paymentFailure.last_four} ${paymentFailure.type}")
    }

    override fun paymentError(paymentError: PaymentError) {
        showToast("an error occurred ${paymentError.reason}")
    }

}
