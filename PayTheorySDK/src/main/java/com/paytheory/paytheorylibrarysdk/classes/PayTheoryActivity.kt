package com.paytheory.paytheorylibrarysdk.classes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PayTheoryActivity Class is an AppCompatActivity. Activity used when inputting data to submit payment
 */
class PayTheoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getStringExtra("Payment-Amount").isNullOrBlank()){
            val returnIntent = Intent()
            //If "Display" not selected or input
            val errorMessage = "Pay Theory \"Payment-Amount\" not valid"
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        if (intent.getStringExtra("Api-Key").isNullOrBlank()){
            val returnIntent = Intent()
            //If "Display" not selected or input
            val errorMessage = "Pay Theory \"Api-Key\" must be supplied"
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        if (intent.getStringExtra("Display") == "Card-Account") {
            setContentView(R.layout.activity_pay_theory_credit_card_account)

            val btn = findViewById<Button>(R.id.submitButton)
            val firstNameView = findViewById<FirstNameEditText>(R.id.firstNameEditText)
            val lastNameView = findViewById<LastNameEditText>(R.id.lastNameEditText)
            val addressOneView = findViewById<AddressOneEditText>(R.id.addressOneEditText)
            val addressTwoView = findViewById<AddressTwoEditText>(R.id.addressTwoEditText)
            val cityView = findViewById<CityEditText>(R.id.cityEditText)
            val zipCodeView = findViewById<ZipEditText>(R.id.zipEditText)
            val stateView = findViewById<StateEditText>(R.id.stateEditText)
            val creditCardView = findViewById<CreditCardEditText>(R.id.creditCardEditText)
            val cvvView = findViewById<CVVEditText>(R.id.cvvEditText)
            val expirationMonthView = findViewById<ExpMonthText>(R.id.expirationMonthEditText)
            val expirationYearView = findViewById<ExpYearText>(R.id.expirationYearEditText)

            firstNameView.setText("Some")
            lastNameView.setText("Body")
            addressOneView.setText("1234 Test Lane")
            addressTwoView.setText("Apt 2")
            cityView.setText("Cincinnati")
            zipCodeView.setText("45236")
            stateView.setText("OH")
            creditCardView.setText("5597069690181758")
            cvvView.setText("424")
            expirationMonthView.setText("04")
            expirationYearView.setText("2024")

            btn.setOnClickListener {
                if (creditCardView.text.toString().isNullOrEmpty()) {
                    showToast("Card Number Required")
                } else if (!cardValidation(creditCardView.text.toString())) {
                    showToast("Card Number Invalid")
                } else if (creditCardView.text.toString().length < 13) {
                    showToast("Card Number Invalid")
                } else if (cvvView.text.toString().isNullOrEmpty()) {
                    showToast("CVV Number Required")
                } else if (cvvView.text.toString().length <= 2) {
                    showToast("CVV Number Must Be 3 Digits")
                } else if (expirationMonthView.text.toString().isNullOrEmpty()) {
                    showToast("Expiration Month Required")
                }  else if (expirationMonthView.text.toString().length <= 1) {
                    showToast("Expiration Month Must Be 2 Digits")
                } else if (expirationYearView.text.toString().isNullOrEmpty()) {
                    showToast("Expiration Year Required")
                } else if (expirationYearView.text.toString().length <= 3) {
                    showToast("Expiration Year Must Be 4 Digits")
                } else if (zipCodeView.text.toString().isNullOrEmpty()) {
                    showToast("Zip Code Required")
                } else if (zipCodeView.text.toString().length <= 4) {
                    showToast("Zip Code Must Be 5 Digits")
                } else if (firstNameView.text.toString().isNullOrEmpty()) {
                    showToast("First Name Required")
                } else if (lastNameView.text.toString().isNullOrEmpty()) {
                    showToast("Last Name Required")
                } else if (addressOneView.text.toString().isNullOrEmpty()) {
                    showToast("Address One Required")
                } else if (cityView.text.toString().isNullOrEmpty()) {
                    showToast("City Required")
                } else if (stateView.text.toString().isNullOrEmpty()) {
                    showToast("State Required")
                } else {
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
                    val zipCode = zipCodeView.text.toString()

                    val payment = Payment(
                        cardNumber,
                        expirationMonth,
                        expirationYear,
                        cvv,
                        intent.getStringExtra("Payment-Amount")!!.toInt(),
                        //add fee mode
                        intent.getStringExtra("Fee-Mode")!!,
                        //add tags if intent is there
                        intent.getStringExtra("Tags-Key"),
                        intent.getStringExtra("Tags-Value"),
                        firstName,
                        lastName,
                        addressOne,
                        addressTwo,
                        city,
                        state,
                        zipCode

                    )
                    if (intent.getStringExtra("Buyer-Options") == "True") {
                        val buyerOptions = BuyerOptions(
                            intent.getStringExtra("First-Name")!!,
                            intent.getStringExtra("Last-Name")!!,
                            intent.getStringExtra("Address-One")!!,
                            intent.getStringExtra("Address-Two")!!,
                            intent.getStringExtra("City")!!,
                            intent.getStringExtra("State")!!,
                            intent.getStringExtra("Country")!!,
                            intent.getStringExtra("Zip-Code")!!,
                            intent.getStringExtra("Phone-Number")!!,
                            intent.getStringExtra("Email-Address")!!
                        )
                        val payTheory = Transaction(
                            this,
                            intent.getStringExtra("Api-Key")!!,
                            payment,
                            buyerOptions
                        )

                        showToast("Processing payment please wait...")

                        val returnIntent = Intent()
                        CoroutineScope(IO).launch {
                            val payTheoryResult = async {
                                payTheory.init()
                            }.await()
                            while (payTheoryResult == "") {
                                delay(500)
                            }
                            Log.d("Pay Theory", "Transact Result : $payTheoryResult")
                            returnIntent.putExtra("result", payTheoryResult)
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    } else {
                        //If buyer options is false or null
                        val payTheory = Transaction(
                            this,
                            intent.getStringExtra("Api-Key")!!,
                            payment
                        )

                        showToast("Processing payment please wait...")

                        val returnIntent = Intent()
                        CoroutineScope(IO).launch {
                            val payTheoryResult = async {
                                payTheory.init()
                            }.await()
                            while (payTheoryResult == "") {
                                delay(500)
                            }
                            Log.d("Pay Theory", "Transact Result : $payTheoryResult")
                            returnIntent.putExtra("result", payTheoryResult)
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    }
                }
            }
        } else if (intent.getStringExtra("Display") == "Card-Only") {
            setContentView(R.layout.activity_pay_theory_credit_card)

            val btn = findViewById<Button>(R.id.submitButton)
            val creditCardView = findViewById<CreditCardEditText>(R.id.creditCardEditText)
            val cvvView = findViewById<CVVEditText>(R.id.cvvEditText)
            val expirationMonthView = findViewById<ExpMonthText>(R.id.expirationMonthEditText)
            val expirationYearView = findViewById<ExpYearText>(R.id.expirationYearEditText)

            creditCardView.setText("5597069690181758")
            cvvView.setText("424")
            expirationMonthView.setText("04")
            expirationYearView.setText("2024")

            btn.setOnClickListener {
                if (creditCardView.text.toString().isNullOrEmpty()) {
                    showToast("Card Number Required")
                } else if (!cardValidation(creditCardView.text.toString())) {
                    showToast("Card Number Invalid")
                } else if (creditCardView.text.toString().length < 13) {
                    showToast("Card Number Invalid")
                } else if (cvvView.text.toString().isNullOrEmpty()) {
                    showToast("CVV Number Required")
                } else if (cvvView.text.toString().length <= 2) {
                    showToast("CVV Number Must Be 3 Digits")
                } else if (expirationMonthView.text.toString().isNullOrEmpty()) {
                    showToast("Expiration Month Required")
                } else if (expirationMonthView.text.toString().length <= 1) {
                    showToast("Expiration Month Must Be 2 Digits")
                } else if (expirationYearView.text.toString().isNullOrEmpty()) {
                    showToast("Expiration Year Required")
                } else if (expirationYearView.text.toString().length <= 3) {
                    showToast("Expiration Year Must Be 4 Digits")
                } else {
                    val cardNumber = creditCardView.text.toString().toLong()
                    val cvv = cvvView.text.toString().toInt()
                    val expirationMonth = expirationMonthView.text.toString().toInt()
                    val expirationYear = expirationYearView.text.toString().toInt()

                    val payment = Payment(
                        cardNumber,
                        expirationMonth,
                        expirationYear,
                        cvv,
                        intent.getStringExtra("Payment-Amount")!!.toInt(),
                        intent.getStringExtra("Fee-Mode")!!,
                        intent.getStringExtra("Tags-Key"),
                        intent.getStringExtra("Tags-Value"),
                    )
                    if (intent.getStringExtra("Buyer-Options") == "True") {
                        val buyerOptions = BuyerOptions(
                            intent.getStringExtra("First-Name")!!,
                            intent.getStringExtra("Last-Name")!!,
                            intent.getStringExtra("Address-One")!!,
                            intent.getStringExtra("Address-Two")!!,
                            intent.getStringExtra("City")!!,
                            intent.getStringExtra("State")!!,
                            intent.getStringExtra("Country")!!,
                            intent.getStringExtra("Zip-Code")!!,
                            intent.getStringExtra("Phone-Number")!!,
                            intent.getStringExtra("Email-Address")!!
                        )
                        val payTheory = Transaction(
                            this,
                            intent.getStringExtra("Api-Key")!!,
                            payment,
                            buyerOptions
                        )

                        showToast("Processing payment please wait...")

                        val returnIntent = Intent()
                        CoroutineScope(IO).launch {
                            val payTheoryResult = async {
                                payTheory.init()
                            }.await()
                            while (payTheoryResult == "") {
                                delay(500)
                            }
                            Log.d("Pay Theory", "Transact Result : $payTheoryResult")
                            returnIntent.putExtra("result", payTheoryResult)
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    } else {
                        //If buyer options is false or null
                        val payTheory = Transaction(
                            this,
                            intent.getStringExtra("Api-Key")!!,
                            payment
                        )
                        showToast("Processing payment please wait...")

                        val returnIntent = Intent()
                        CoroutineScope(IO).launch {
                            val payTheoryResult = async {
                                payTheory.init()
                            }.await()
                            while (payTheoryResult == "") {
                                delay(500)
                            }
                            Log.d("Pay Theory", "Pay Theory Result : $payTheoryResult")
                            returnIntent.putExtra("result", payTheoryResult)
                            setResult(Activity.RESULT_OK, returnIntent)
                            finish()
                        }
                    }
                }
            }
        } else {
            val returnIntent = Intent()
            //If "Display" not selected or input
            val errorMessage = "Pay Theory \"Display\" Not Selected"
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
    private fun showToast(message: String){
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()
    }
    private fun cardValidation(cardNumber :String): Boolean {
        var sum = 0
        var alternate = false
        for (i in cardNumber.length - 1 downTo 0) {
            var n: Int = cardNumber.substring(i, i + 1).toInt()
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = n % 10 + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }
}





