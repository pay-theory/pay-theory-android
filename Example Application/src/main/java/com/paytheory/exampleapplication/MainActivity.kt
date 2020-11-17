package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)
        toPaymentButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            startActivityForResult(intent, 1);
        }
    }
    // This method is called when the PayTheoryActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Get String data from PayTheoryActivity
                val returnString = data!!.getStringExtra("keyName")
                Log.e("Main Activity","Here is the result data string : $returnString")
            }
        }
    }
}
