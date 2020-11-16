package com.paytheory.paytheorylibrary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var newButton = findViewById<Button>(R.id.toPayment)

        newButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            startActivity(intent)
        }
    }
}