package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.system.Os.link
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.paytheory.paytheorylibrarysdk.fragments.PayTheoryFragment


class ExampleAppMainActivity : FragmentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment)

        val payTheoryArgs = Bundle()
        payTheoryArgs.putString(PayTheoryFragment.API_KEY, "pt-sandbox-dev-f992c4a57b86cb16aefae30d0a450237")
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

}
