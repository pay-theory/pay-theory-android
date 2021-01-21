package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.classes.PayTheoryActivity

class ExampleAppMainActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button that will start PayTheoryActivity
        var submitButton = findViewById<Button>(R.id.submitButton)

        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
        submitButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)

            //Set COLLECT_BILLING_ADDRESS (COLLECT_BILLING_ADDRESS_TRUE or COLLECT_BILLING_ADDRESS_FALSE)
            intent.putExtra(COLLECT_BILLING_ADDRESS, COLLECT_BILLING_ADDRESS_TRUE)

            //Set Fee Mode (FEE_MODE_SURCHARGE or FEE_MODE_SERVICE)
            intent.putExtra(FEE_MODE, FEE_MODE_SURCHARGE)

            //Set Payment Amount in cents ($50.25 = "5025")
            intent.putExtra(PAYMENT_AMOUNT, "5025")

            //Set Api-Key
            intent.putExtra(API_KEY, "MY-API-KEY")

            //Set Custom Tags for payments
            intent.putExtra(TAG_KEY, "My Custom Tags")
            intent.putExtra(TAG_VALUE, "My Custom Tags Value")

            //Set Buyer Options (BUYER_OPTIONS_TRUE or BUYER_OPTIONS_FALSE)
            intent.putExtra(BUYER_OPTIONS, BUYER_OPTIONS_TRUE)

            //Set Buyer Options data
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
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Get String data from PayTheoryActivity
                val returnString = data!!.getStringExtra("result")
                Log.d("Pay Theory", "Here is the result data string : $returnString")
                if (returnString != null) {
                    showToast(returnString)
                }
            } else {
                showToast("No result data")
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
