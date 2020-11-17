package com.paytheory.exampleapplication

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var newButton = findViewById<Button>(R.id.toPayment)

        newButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)


            startActivityForResult(intent, 1);
        }
    }
    // This method is called when the second activity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check that it is the SecondActivity with an OK result
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

                // Get String data from Intent
                val returnString = data!!.getStringExtra("keyName")
                Log.e("Main Activity","Here is the return string : $returnString")
            }
        }
    }
}
