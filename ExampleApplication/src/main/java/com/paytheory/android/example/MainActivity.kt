package com.paytheory.android.example

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.PaymentError
import com.paytheory.android.sdk.PaymentResult
import com.paytheory.android.sdk.fragments.PayTheoryFragment


class MainActivity : FragmentActivity() , Payable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment)

        val payTheoryArgs = Bundle()
        payTheoryArgs.putString(PayTheoryFragment.API_KEY, "My-Api-Key")
        payTheoryArgs.putInt(PayTheoryFragment.AMOUNT,5000)
        payTheoryArgs.putBoolean(PayTheoryFragment.ACCOUNT_NAME_ENABLED,true)

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