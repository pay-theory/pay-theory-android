package com.example.paytheorylibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.paytheorylibrarysdk.*
import com.example.paytheorylibrarysdk.paytheory.PayTheory
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val btn = findViewById<Button>(R.id.submitButton)
        val firstNameView = findViewById<FirstNameEditText>(R.id.firstNameEditText)
        val lastNameView = findViewById<LastNameEditText>(R.id.lastNameEditText)
        val addressOneView = findViewById<AddressOneEditText>(R.id.addressOneEditText)
        val addressTwoView = findViewById<AddressTwoEditText>(R.id.addressTwoEditText)
        val phoneNumberView = findViewById<PhoneNumberEditText>(R.id.phoneNumberEditText)
        val countryView = findViewById<CountryEditText>(R.id.countryEditText)
        val emailAddressView = findViewById<EmailAddressEditText>(R.id.emailAddressEditText)
        val cityView = findViewById<CityEditText>(R.id.cityEditText)
        val zipCodeView = findViewById<ZipEditText>(R.id.zipEditText)
        val stateView = findViewById<StateEditText>(R.id.stateEditText)
//        val customTagsView = findViewById<Custom>(R.id.EditText)
        val creditCardView = findViewById<CreditCardEditText>(R.id.creditCardEditText)
        val cvvView = findViewById<CVVEditText>(R.id.cvvEditText)
//        val expirationView = findViewById<ExpEditText>(R.id.expEditText)
        val expirationMonthView = findViewById<ExpMonthText>(R.id.expirationMonthEditText)
        val expirationYearView = findViewById<ExpYearText>(R.id.expirationYearEditText)
        val amountView = findViewById<AmountEditText>(R.id.amountEditText)
        val resultView = findViewById<TextView>(R.id.responseText)

        firstNameView.setText("Some")
        lastNameView.setText("Body")
        addressOneView.setText("1234 Test Lane")
        addressTwoView.setText("Apt 2")
        phoneNumberView.setText("5131111111")
        countryView.setText("USA")
        emailAddressView.setText("test@gmail.com")
        cityView.setText("Cincinnati")
        zipCodeView.setText("45236")
        stateView.setText("OH")
        creditCardView.setText("4242424242424242")
        cvvView.setText("424")
        expirationMonthView.setText("04")
        expirationYearView.setText("2024")
        amountView.setText("12.22")





        //Click listener for submit button
        btn.setOnClickListener {
            val apiKey = "pt-sandbox-dev-d9de9154964990737db2f80499029dd6";
            val amount = ((amountView.text.toString().toDouble()) * 100).toInt()
            val cardNumber = creditCardView.text.toString().toLong()
            val cvv = cvvView.text.toString().toInt()
            val expirationMonth = expirationMonthView.text.toString().toInt()
            val expirationYear = expirationYearView.text.toString().toInt()
            val firstName = firstNameView.text.toString()
            val lastName = lastNameView.text.toString()
            val addressOne = addressOneView.text.toString()
            val addressTwo = addressTwoView.text.toString()
            val phoneNumber = phoneNumberView.text.toString()
            val country = countryView.text.toString()
            val emailAddress = emailAddressView.text.toString()
            val city = cityView.text.toString()
            val zipCode = zipCodeView.text.toString()
            val state = stateView.text.toString()
            val customTags = "TODO Custom Tags"

            val payTheoryObject = PayTheory(this, apiKey, amount, cardNumber, cvv,
                    expirationMonth, expirationYear, firstName, lastName, addressOne, addressTwo,
                    phoneNumber, country, emailAddress, city, zipCode, state, customTags, resultView)



//            GlobalScope.launch(Dispatchers.Main) {
//                val queue = async(Dispatchers.IO) {
//                    payTheoryObject.initPayment()
//                }
//                val result = queue.await()
//            }

            Handler(Looper.getMainLooper()).post {
                payTheoryObject.initPayment()
            }

//
//            GlobalScope.launch {
//                suspend {
//                    Log.d("coroutineScope", "#runs on ${Thread.currentThread().name}")
//                    payTheoryObject.initPayment()
//                    withContext(Dispatchers.Main) {
//                        Log.d("coroutineScope", "#runs on ${Thread.currentThread().name}")
//                    }
//                }.invoke()
//            }


//            val thread = Thread {
//                payTheoryObject.initPayment()
//                println("${Thread.currentThread()} has run.")
//            }
//            thread.start()



//            runBlocking {
//                val result = initPayment().await()
//                println(result)
//            }


//            val myScope = CoroutineScope(Dispatchers.Main)
//            myScope.launch {
//                withContext(IO) {
//                    payTheoryObject.initPayment()
//                }
//
//            }



//            runOnUiThread {
//                payTheoryObject.initPayment()
//            }

            
//           GlobalScope.launch {
//               val operation = async(IO) {
//                   payTheoryObject.initPayment()
//
//               }
//               operation.await()
//               runOnUiThread {
//                   val result = payTheoryObject.getResult()
//                   resultView.text = ("Final Results: $result")
//               }
//            }



//                runOnUiThread {
//                    resultView.text = ("Final Results: $${finalResults}")
//                }

        }

    }
}