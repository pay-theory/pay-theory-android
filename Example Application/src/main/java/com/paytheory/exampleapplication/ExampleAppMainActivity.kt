package com.paytheory.exampleapplication

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.classes.PayTheoryActivity


class ExampleAppMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        createSpinner()

    }

    fun createSpinner(){
        val spinner: Spinner = findViewById(R.id.payment_spinner)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.payment_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }



        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {

                val currentSelection = spinner.selectedItem


                if (currentSelection == "ACH"){
                    Log.d("Pay Theory", "Current selection is $currentSelection")
                    setOnClickListener(currentSelection as String)
                }

                else {
                    Log.d("Pay Theory", "Current selection is $currentSelection")
                    setOnClickListener(currentSelection as String)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                Log.d("Pay Theory", "Current selection is nothing")
            }
        }
    }

    //Payment Type should be ("Card" or "ACH")
    fun setOnClickListener(paymentType : String) {


       // Button that will start PayTheoryActivity
        var paymentButton = findViewById<Button>(R.id.toPayment)

        paymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
            val intent = Intent(this, PayTheoryActivity::class.java)

            //Set Full-Account-Details ("True" or "False")
            intent.putExtra("Full-Account-Details", "True")

            //Payment Type is set ("Card" or "ACH")
            intent.putExtra("Payment-Type", paymentType)

            //Set Fee Mode ("surcharge" or "service-fee")
            intent.putExtra("Fee-Mode", "surcharge")

            //Set Payment Amount in cents ($50.25 = "5025")
            intent.putExtra("Payment-Amount", "5025")

            //Set Api-Key
            intent.putExtra("Api-Key", "MY API KEY")


            //Set Custom Tags for payments
            intent.putExtra("Tags-Key", "My Custom Tags")
            intent.putExtra("Tags-Value", "My Custom Tags Value")

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
            startActivityForResult(intent, 1);
        }




//        //Button that will start PayTheoryActivity
//        var paymentButton = findViewById<Button>(R.id.toPayment)
//
//        paymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//            val intent = Intent(this, PayTheoryActivity::class.java)
//
//            //Set Full-Account-Details ("True" or "False") // Defaults to False
//            intent.putExtra("Full-Account-Details", "False")
//
//            //Payment Type is set ("Card" or "ACH")
//            intent.putExtra("Payment-Type", paymentType)
//
//            //Set Fee Mode ("surcharge" or "service-fee")
//            intent.putExtra("Fee-Mode", "surcharge")
//            //Set Payment Amount in cents ($50.25 = "5025")
//            intent.putExtra("Payment-Amount", "3000")
//            //Set Api-Key
//            intent.putExtra("Api-Key", "MY-API-KEY")
//
//
//            //Set Custom Tags for payments
//            intent.putExtra("Tags", "My Custom Tags")
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
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1)
//        }






//Button that will start PayTheoryActivity
//        var paymentButton = findViewById<Button>(R.id.toPayment)
//
//        paymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//            val intent = Intent(this, PayTheoryActivity::class.java)
//
//            //Set Full-Account-Details ("True" or "False")
//            intent.putExtra("Full-Account-Details", "True")
//            //Payment Type is set ("Card" or "ACH")
//            intent.putExtra("Payment-Type", paymentType)
//                    //Set Fee Mode ("surcharge" or "service-fee")
//            intent.putExtra("Fee-Mode", "surcharge")
//            //Set Payment Amount in cents ($50.25 = "5025")
//            intent.putExtra("Payment-Amount", "5000")
//            //Set Api-Key
//            intent.putExtra("Api-Key", "MY API KEY")
//
//
//
//            //Set Custom Tags for payments
//            intent.putExtra("Tags", "My Custom Tags")
//            //Set Buyer Options ("True" or "False")
//            intent.putExtra("Buyer-Options", "False")
//            //Start PayTheoryActivity
//            startActivityForResult(intent, 1);
//        }




//      //Button that will start PayTheoryActivity
//        var paymentButton = findViewById<Button>(R.id.toPayment)
//
//        //Buyer-Options = null , Display = "Card-Only"
//        paymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
//            val intent = Intent(this, PayTheoryActivity::class.java)
//            //Set Full-Account-Details ("True" or "False")
//            intent.putExtra("Full-Account-Details", "False")
//            //Payment Type is set ("Card" or "ACH")
//            intent.putExtra("Payment-Type", paymentType)
//                    //Set Fee Mode ("surcharge" or "service-fee")
//            intent.putExtra("Fee-Mode", "surcharge")
//            //Set Payment Amount in cents ($50.25 = "5025")
//            intent.putExtra("Payment-Amount", "5000")
//            //Set Api-Key
//            intent.putExtra("Api-Key", "MY API KEY")
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
            } else {
                showToast("Error getting result data")
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
