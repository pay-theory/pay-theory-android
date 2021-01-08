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
        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

//        //Buyer-Options = True , Display = "Card-Only", Tags = "My Custom Tags"
//        toPaymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//            val intent = Intent(this, PayTheoryActivity::class.java)
//
//            //Set Fee Mode ("surcharge" or "service-fee")
//            intent.putExtra("Fee-Mode", "surcharge")
//
//            //Set Payment Amount in cents ($50.25 = "5025")
//            intent.putExtra("Payment-Amount", "5025")
//
//            //Set Api-Key
//            intent.putExtra("Api-Key", "MY API KEY")
//
//            //Set Display type ("Card-Only" or "Card-Account")
//            intent.putExtra("Display", "Card-Only")
//
//            //Set Custom Tags for payments
//            intent.putExtra("Tags-Key", "My Custom Tags")
//            intent.putExtra("Tags-Value", "My Custom Tags Value")
//
//            //Set Buyer Options ("True" or "False")
//            intent.putExtra("Buyer-Options", "True")
//
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
//
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1);
//        }




        //Buyer-Options = True , Display = "Card-Account"
        toPaymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
            val intent = Intent(this, PayTheoryActivity::class.java)
                    //Set Fee Mode ("surcharge" or "service-fee")
            intent.putExtra("Fee-Mode", "surcharge")
            //Set Payment Amount in cents ($50.25 = "5025")
            intent.putExtra("Payment-Amount", "3000")
            //Set Api-Key
            intent.putExtra("Api-Key", "MY API KEY")
            //Set Display type ("Card-Only" or "Card-Account")
            intent.putExtra("Display", "Card-Account")
            //Set Custom Tags for payments
            intent.putExtra("Tags", "My Custom Tags")
            //Set Buyer Options ("True" or "False")
            intent.putExtra("Buyer-Options", "True")
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
            startActivityForResult(intent, 1)
        }





//        //Buyer-Options = null , Display = "Card-Account"
//        toPaymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//            val intent = Intent(this, PayTheoryActivity::class.java)
//                    //Set Fee Mode ("surcharge" or "service-fee")
//            intent.putExtra("Fee-Mode", "surcharge")
//            //Set Payment Amount in cents ($50.25 = "5025")
//            intent.putExtra("Payment-Amount", "5000")
//            //Set Api-Key
//            intent.putExtra("Api-Key", "MY API KEY")
//            //Set Display type ("Card-Only" or "Card-Account")
//            intent.putExtra("Display", "Card-Account")
//            //Set Custom Tags for payments
//            intent.putExtra("Tags", "My Custom Tags")
//            //Set Buyer Options ("True" or "False")
//            intent.putExtra("Buyer-Options", "False")
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1);
//        }





//        //Buyer-Options = null , Display = "Card-Only"
//        toPaymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//            val intent = Intent(this, PayTheoryActivity::class.java)
//                    //Set Fee Mode ("surcharge" or "service-fee")
//            intent.putExtra("Fee-Mode", "surcharge")
//            //Set Payment Amount in cents ($50.25 = "5025")
//            intent.putExtra("Payment-Amount", "5000")
//            //Set Api-Key
//            intent.putExtra("Api-Key", "MY API KEY")
//            //Set Display type ("Card-Only" or "Card-Account")
//            intent.putExtra("Display", "Card-Only")
//            //Set Custom Tags for payments
//            intent.putExtra("Tags", "My Custom Tags")
//            //Set Buyer Options ("True" or "False")
//            intent.putExtra("Buyer-Options", "False")
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1);
//        }





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
            }
//            else if(Activity.RESULT_CANCELED == 0){
//                // Get String data from PayTheoryActivity
//                val returnString = data!!.getStringExtra("result")
//                Log.d("MPay Theory, "Here is the result data string : $returnString")
//                if (returnString != null) {
//                    showToast(returnString)
//                }
//            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()
    }
}
