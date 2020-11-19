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
        //Swtich to turn on or off Buyer Options
        var toggleBuyerOptions = findViewById<ToggleButton>(R.id.toggleButton)
        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)


        if (toggleBuyerOptions.isChecked){
            //On Click Listener to start PayTheoryActivity with Buyer Options
            toPaymentButton.setOnClickListener {
                val intent = Intent(this, PayTheoryActivity::class.java)
                //Set payment amount value in pennies
                intent.putExtra("Payment-Amount", "5050")
                //Set api-key value
                intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
                //Set buyer options as false and provide buyer info
                intent.putExtra("Buyer-Options", "true")
                //Start PayTheoryActivity
                startActivityForResult(intent, 1);
            }
        } else {
            //On Click Listener to start PayTheoryActivity without Buyer Options
            toPaymentButton.setOnClickListener {
                val intent = Intent(this, PayTheoryActivity::class.java)
                //Set payment amount value in pennies
                intent.putExtra("Payment-Amount", "5050")
                //Set api-key value
                intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
                //Set buyer options as false and provide buyer info
                intent.putExtra("Buyer-Options", "false")
                //Set first name
                intent.putExtra("First-Name", "firstName")
                intent.putExtra("Last-Name", "lastName")
                intent.putExtra("Address-One", "addressOne")
                intent.putExtra("Address-Two", "addressTwo")
                intent.putExtra("City", "city")
                intent.putExtra("State", "state")
                intent.putExtra("Country", "country")
                intent.putExtra("Zip-Code", "zipCode")
                intent.putExtra("Phone-Number", "phoneNumber")
                intent.putExtra("Email-Address", "emailAddress")
                //Start PayTheoryActivity
                startActivityForResult(intent, 1);
            }
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
                Toast.makeText(
                    this, returnString,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
