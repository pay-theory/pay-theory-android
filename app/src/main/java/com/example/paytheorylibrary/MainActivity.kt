package com.example.paytheorylibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.paytheorylibrarysdk.*
import com.example.paytheorylibrarysdk.paytheory.BuyerOptions
import com.example.paytheorylibrarysdk.paytheory.CardPayment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    //    val apiKey = "pt-sandbox-dev-d9de9154964990737db2f80499029dd6";
//    val amount = 1550
//    val cardNumber = 4242424242424242
//    val cvv = 242
//    val expirationMonth = 12
//    val expirationYear = 24
//    val firstName = "Some"
//    val lastName = "Body"
//    val addressOne = "101 Test Drive"
//    val addressTwo= "Apartment 2"
//    val phoneNumber = "5131111111"
//    val country= "USA"
//    val emailAddress = "TestEmail@gmail.com"
//    val city = "Cincinnati"
//    val zipCode = "45236"
//    val state = "OH"
//    val customTags = "CustomTagsTest"

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


            val payment = CardPayment("4242424242424242", "04", "24", "424", 5000)
            val buyerOptions = BuyerOptions("Some", "Body", "104 Testing", "", "Cincinnati", "OH", "USA", "45140", "5133333333", "TestEmail@gmail.com")

            val payTheory = PayTheory(this, "pt-sandbox-dev-d9de9154964990737db2f80499029dd6", payment, buyerOptions)

            payTheory.init()

            suspend fun setTextOnMainThread(input:String) {
                withContext(Dispatchers.Main){
                    val newText = resultView.text.toString() + "\n$input"
                    resultView.text = newText
                }

            }












//            GlobalScope.launch(Dispatchers.Main) {
//                val queue = async(Dispatchers.IO) {
//                    payTheoryObject.initPayment()
//                }
//                val result = queue.await()
//            }

//            Handler(Looper.getMainLooper()).post {
//                payTheoryObject.initPayment()
//            }

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