package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.system.Os.link
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity


class ExampleAppMainActivity : FragmentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val payTheoryManager = this.supportFragmentManager
        val payTheoryFragment = payTheoryManager.findFragmentById(R.id.payTheoryFragment)

        val payTheoryArgs = Bundle()
        payTheoryArgs.putString("api_key", "my-api-key")
        payTheoryArgs.putInt("amount",5000)

        payTheoryFragment!!.arguments = payTheoryArgs
    }


    private fun showToast(message: String){
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()
    }

}
