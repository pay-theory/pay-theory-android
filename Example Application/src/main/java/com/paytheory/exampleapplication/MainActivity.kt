package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

//        //Buyer-Options = True , Display = "Card-Only"
//        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//        toPaymentButton.setOnClickListener {
//            val intent = Intent(this, PayTheoryActivity::class.java)
//            //Set payment amount value in pennies
//            intent.putExtra("Payment-Amount", "4000")
//            //Set api-key value
//            intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
//            //Set Display
//            intent.putExtra("Display", "Card-Only")
////            intent.putExtra("Display", "Card-Account")
//
//            //Set buyer options as false and provide buyer info
//            intent.putExtra("Buyer-Options", "True")
//            //Set Buyer Options data
//            intent.putExtra("First-Name", "Buyer")
//            intent.putExtra("Last-Name", "Options")
//            intent.putExtra("Address-One", "123 Options Lane")
//            intent.putExtra("Address-Two", "Apt 1")
//            intent.putExtra("City", "Cincinnati")
//            intent.putExtra("State", "OH")
//            intent.putExtra("Country", "USA")
//            intent.putExtra("Zip-Code", "45236")
//            intent.putExtra("Phone-Number", "513-123-1234")
//            intent.putExtra("Email-Address", "test@paytheory.com")
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1);
//        }




//        //Buyer-Options = True , Display = "Card-Account"
//        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//        toPaymentButton.setOnClickListener {
//            val intent = Intent(this, PayTheoryActivity::class.java)
//            //Set payment amount value in pennies
//            intent.putExtra("Payment-Amount", "3000")
//            //Set api-key value
//            intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
//            //Set Display
//            intent.putExtra("Display", "Card-Account")
//
//            //Set buyer options as false and provide buyer info
//            intent.putExtra("Buyer-Options", "True")
//            //Set Buyer Options data
//            intent.putExtra("First-Name", "Buyer")
//            intent.putExtra("Last-Name", "Options")
//            intent.putExtra("Address-One", "123 Options Lane")
//            intent.putExtra("Address-Two", "Apt 1")
//            intent.putExtra("City", "Cincinnati")
//            intent.putExtra("State", "OH")
//            intent.putExtra("Country", "USA")
//            intent.putExtra("Zip-Code", "45236")
//            intent.putExtra("Phone-Number", "513-123-1234")
//            intent.putExtra("Email-Address", "test@paytheory.com")
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1);
//        }





//        //Buyer-Options = null , Display = "Card-Account"
//        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//        toPaymentButton.setOnClickListener {
//            val intent = Intent(this, PayTheoryActivity::class.java)
//            //Set payment amount value in pennies
//            intent.putExtra("Payment-Amount", "5000")
//            //Set api-key value
//            intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
//            //Set Display
//            intent.putExtra("Display", "Card-Account")
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1);
//        }

        //Buyer-Options = null , Display = "Card-Only"
        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
        toPaymentButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            //Set payment amount value in pennies
            intent.putExtra("Payment-Amount", "5000")
            //Set api-key value
            intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
            //Set Display
            intent.putExtra("Display", "Card-Only")
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
                Log.e("Main Activity", "Here is the result data string : $returnString")
                if (returnString != null) {
                    showToast(returnString)
                }
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
