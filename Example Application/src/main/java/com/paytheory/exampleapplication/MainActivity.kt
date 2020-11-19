package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

        //On Click Listener to start PayTheoryActivity
        toPaymentButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            //Set payment amount value in pennies
            intent.putExtra("Payment-Amount", "5050")
            //Set api-key value
            intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
            //Start PayTheoryActivity
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
                Log.e("Main Activity", "Here is the result data string : $returnString")
            }
        }
    }
}
