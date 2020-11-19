package com.paytheory.paytheorylibrarysdk.paytheory

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PayTheoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        val theme :Resources.Theme = intent.getParcelableExtra("Theme")!!
//        //Allows the style of an activity if setTheme is added
////        setTheme(R.style.DarkTheme)
//        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_theory)

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
        val creditCardView = findViewById<CreditCardEditText>(R.id.creditCardEditText)
        val cvvView = findViewById<CVVEditText>(R.id.cvvEditText)
        val expirationMonthView = findViewById<ExpMonthText>(R.id.expirationMonthEditText)
        val expirationYearView = findViewById<ExpYearText>(R.id.expirationYearEditText)
//        val amountView = findViewById<AmountEditText>(R.id.amountEditText)
        //        val expirationView = findViewById<ExpEditText>(R.id.expEditText)
        //        val customTagsView = findViewById<Custom>(R.id.EditText)

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
//        amountView.setText("12.22")

        btn.setOnClickListener {
//            val apiKey = "pt-sandbox-dev-d9de9154964990737db2f80499029dd6";
//            val amount = ((amountView.text.toString().toDouble()) * 100).toInt()
            val cardNumber = creditCardView.text.toString().toLong()
            val cvv = cvvView.text.toString().toInt()
            val expirationMonth = expirationMonthView.text.toString().toInt()
            val expirationYear = expirationYearView.text.toString().toInt()
            val firstName = firstNameView.text.toString()
            val lastName = lastNameView.text.toString()
            val addressOne = addressOneView.text.toString()
            val addressTwo = addressTwoView.text.toString()
            val city = cityView.text.toString()
            val state = stateView.text.toString()
            val country = countryView.text.toString()
            val zipCode = zipCodeView.text.toString()
            val phoneNumber = phoneNumberView.text.toString()
            val emailAddress = emailAddressView.text.toString()
//            val customTags = "TODO Custom Tags"

            val payment = CardPayment(cardNumber, expirationMonth, expirationYear, cvv, intent.getStringExtra("Payment-Amount")!!
                .toInt())
            val buyerOptions = BuyerOptions(
                firstName,
                lastName,
                addressOne,
                addressTwo,
                city,
                state,
                country,
                zipCode,
                phoneNumber,
                emailAddress
            )
            val payTheory = PayTheory(
                this,
                intent.getStringExtra("Api-Key")!!,
                payment,
                buyerOptions
            )


            val returnIntent = Intent()
            CoroutineScope(IO).launch {
                val transactResult = async {
                    payTheory.init()
                }.await()
                while (transactResult == "") {
                    delay(500)
                }
                Log.e("PT2", "Transact Result : $transactResult")
                returnIntent.putExtra("result", transactResult)
                setResult(1, returnIntent);
                finish()
            }
        }
    }
}


