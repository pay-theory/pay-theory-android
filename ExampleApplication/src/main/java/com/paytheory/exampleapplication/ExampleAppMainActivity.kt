package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.classes.PayTheoryActivity

class ExampleAppMainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val COLLECT_BILLING_ADDRESS = "Billing-Address"
        val COLLECT_BILLING_ADDRESS_TRUE = "True"
        val COLLECT_BILLING_ADDRESS_FALSE = "False"
        val FEE_MODE = "Fee-Mode"
        val FEE_MODE_SURCHARGE = "surcharge"
        val FEE_MODE_SERVICE = "service_fee"
        val PAYMENT_AMOUNT = "Payment-Amount"
        val BUYER_OPTIONS = "Buyer-Options"
        val BUYER_OPTIONS_TRUE = "True"
        val BUYER_OPTIONS_FALSE = "False"
        val API_KEY = "Api-Key"
        val TAG_KEY = "Tag-Key"
        val TAG_VALUE = "Tag-Value"


        // Button that will start PayTheoryActivity
        var submitButton = findViewById<Button>(R.id.submitButton)

        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
        submitButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)

            //Set COLLECT_BILLING_ADDRESS (COLLECT_BILLING_ADDRESS_TRUE or COLLECT_BILLING_ADDRESS_FALSE)
            intent.putExtra(COLLECT_BILLING_ADDRESS, COLLECT_BILLING_ADDRESS_TRUE)

            //Set FEE_MODE (FEE_MODE_SURCHARGE or FEE_MODE_SERVICE)
            intent.putExtra(FEE_MODE, FEE_MODE_SURCHARGE)

            //Set PAYMENT_AMOUNT in cents ($50.25 = "5025")
            intent.putExtra(PAYMENT_AMOUNT, "5025")

            //Set API_KEY
            intent.putExtra(API_KEY, "My-API-Key")

            //Set TAG_KEY and TAG_VALUE
            intent.putExtra(TAG_KEY, "Custom Tag Key")
            intent.putExtra(TAG_VALUE, "Custom Tag Value")

            //Set BUYER_OPTIONS (BUYER_OPTIONS_TRUE or BUYER_OPTIONS_FALSE)
            intent.putExtra(BUYER_OPTIONS, BUYER_OPTIONS_TRUE)

            //Set BUYER_OPTIONS data
            intent.putExtra("First-Name", "Buyer")
            intent.putExtra("Last-Name", "Options")
            intent.putExtra("Address-One", "123 Options Lane")
            intent.putExtra("Address-Two", "Apt 1")
            intent.putExtra("City", "Cincinnati")
            intent.putExtra("State", "OH")
            intent.putExtra("Country", "USA")
            intent.putExtra("Zip-Code", "45236")
            intent.putExtra("Phone-Number", "513-123-1234")
            intent.putExtra("Email-Address", "test@paytheory.com")

            //Start PayTheoryActivity
            startActivityForResult(intent, 1);
        }

    }
    // This method is called when the PayTheoryActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Pay Theory", "Result Code $resultCode and Request Code $requestCode and Activity.RESULT_OK ${Activity.RESULT_OK} and data $data")
        if (resultCode == -1 && requestCode == 1 ) {
            // Get String data from PayTheoryActivity
            val returnString = data!!.getStringExtra("result")
            Log.d("Pay Theory", "Here is the result data string : $returnString")
            if (returnString != null) {
                showToast(returnString)
            }

        }
    }

    private fun showToast(message: String){
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()
    }

}
