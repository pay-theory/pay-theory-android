package com.paytheory.paytheorylibrarysdk.classes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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

    var achPaymentType: String = "Card"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        if (intent.getStringExtra("Payment-Amount").isNullOrBlank()) {
            val returnIntent = Intent()
            //If "Display" not selected or input
            val errorMessage = "Activity intent \"Payment-Amount\" not valid"
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        if (intent.getStringExtra("Api-Key").isNullOrBlank()) {
            val returnIntent = Intent()
            //If "Display" not selected or input
            val errorMessage = "Activity intent \"Api-Key\" must be supplied"
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        if (intent.getStringExtra("Payment-Type") == "Card" && intent.getStringExtra("Full-Account-Details") == "True") {
            setContentView(R.layout.activity_pay_theory_card_full_account)

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

//            firstNameView.setText("Some")
//            lastNameView.setText("Body")
//            addressOneView.setText("1234 Test Lane")
//            addressTwoView.setText("Apt 2")
//            cityView.setText("Cincinnati")
//            zipCodeView.setText("45236")
//            stateView.setText("OH")
//            creditCardView.setText("5597069690181758")
//            cvvView.setText("424")
//            expirationMonthView.setText("04")
//            expirationYearView.setText("2024")

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
                    val cvv = cvvView.text.toString()
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
                        null,
                        null,
                        achPaymentType,

                        intent.getStringExtra("Payment-Amount")!!.toInt(),
                        intent.getStringExtra("Payment-Type")!!.toString(),
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
                            if (intent.hasExtra("First-Name")) intent.getStringExtra("First-Name") else "",
                            if (intent.hasExtra("Last-Name")) intent.getStringExtra("Last-Name") else "",
                            if (intent.hasExtra("Address-One")) intent.getStringExtra("Address-One") else "",
                            if (intent.hasExtra("Address-Two")) intent.getStringExtra("Address-Two") else "",
                            if (intent.hasExtra("City")) intent.getStringExtra("City") else "",
                            if (intent.hasExtra("State")) intent.getStringExtra("State") else "",
                            if (intent.hasExtra("Country")) intent.getStringExtra("Country") else "",
                            if (intent.hasExtra("Zip-Code")) intent.getStringExtra("Zip-Code") else "",
                            if (intent.hasExtra("Phone-Number")) intent.getStringExtra("Phone-Number") else "",
                            if (intent.hasExtra("Email-Address")) intent.getStringExtra("Email-Address") else "",
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
        } else if (intent.getStringExtra("Payment-Type") == "Card" && intent.getStringExtra("Full-Account-Details") != "True") {
            setContentView(R.layout.activity_pay_theory_card)

            val btn = findViewById<Button>(R.id.submitButton)
            val creditCardView = findViewById<CreditCardEditText>(R.id.creditCardEditText)
            val cvvView = findViewById<CVVEditText>(R.id.cvvEditText)
            val expirationMonthView = findViewById<ExpMonthText>(R.id.expirationMonthEditText)
            val expirationYearView = findViewById<ExpYearText>(R.id.expirationYearEditText)

//            creditCardView.setText("5597069690181758")
//            cvvView.setText("424")
//            expirationMonthView.setText("04")
//            expirationYearView.setText("2024")

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
                    val cvv = cvvView.text.toString()
                    val expirationMonth = expirationMonthView.text.toString().toInt()
                    val expirationYear = expirationYearView.text.toString().toInt()

                    val payment = Payment(
                        cardNumber,
                        expirationMonth,
                        expirationYear,
                        cvv,
                        null,
                        null,
                        achPaymentType,
                        intent.getStringExtra("Payment-Amount")!!.toInt(),
                        intent.getStringExtra("Payment-Type")!!.toString(),
                        intent.getStringExtra("Fee-Mode")!!,
                        intent.getStringExtra("Tags-Key"),
                        intent.getStringExtra("Tags-Value"),
                    )
                    if (intent.getStringExtra("Buyer-Options") == "True") {
                        val buyerOptions = BuyerOptions(
                            if (intent.hasExtra("First-Name")) intent.getStringExtra("First-Name") else "",
                            if (intent.hasExtra("Last-Name")) intent.getStringExtra("Last-Name") else "",
                            if (intent.hasExtra("Address-One")) intent.getStringExtra("Address-One") else "",
                            if (intent.hasExtra("Address-Two")) intent.getStringExtra("Address-Two") else "",
                            if (intent.hasExtra("City")) intent.getStringExtra("City") else "",
                            if (intent.hasExtra("State")) intent.getStringExtra("State") else "",
                            if (intent.hasExtra("Country")) intent.getStringExtra("Country") else "",
                            if (intent.hasExtra("Zip-Code")) intent.getStringExtra("Zip-Code") else "",
                            if (intent.hasExtra("Phone-Number")) intent.getStringExtra("Phone-Number") else "",
                            if (intent.hasExtra("Email-Address")) intent.getStringExtra("Email-Address") else "",
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
        } else if (intent.getStringExtra("Payment-Type") == "ACH" && intent.getStringExtra("Full-Account-Details") == "True") {
            setContentView(R.layout.activity_pay_theory_ach_full_account)

            setPaymentTypeSpinner()

            val btn = findViewById<Button>(R.id.submitButton)
            val accountNumberView = findViewById<ACHAccountNumber>(R.id.accountNumberEditText)
            val routingNumberView = findViewById<ACHRoutingNumber>(R.id.routingNumberEditText)
            val firstNameView = findViewById<FirstNameEditText>(R.id.firstNameEditText)
            val lastNameView = findViewById<LastNameEditText>(R.id.lastNameEditText)
            val addressOneView = findViewById<AddressOneEditText>(R.id.addressOneEditText)
            val addressTwoView = findViewById<AddressTwoEditText>(R.id.addressTwoEditText)
            val cityView = findViewById<CityEditText>(R.id.cityEditText)
            val zipCodeView = findViewById<ZipEditText>(R.id.zipEditText)
            val stateView = findViewById<StateEditText>(R.id.stateEditText)

//            accountNumberView.setText("12345678910")
//            routingNumberView.setText("789456124")
//            firstNameView.setText("Some")
//            lastNameView.setText("Body")
//            addressOneView.setText("1234 Test Lane")
//            addressTwoView.setText("Apt 2")
//            cityView.setText("Cincinnati")
//            zipCodeView.setText("45236")
//            stateView.setText("OH")


            btn.setOnClickListener {
                if (accountNumberView.text.toString().isNullOrEmpty()) {
                    showToast("Account Number Required")
                } else if (routingNumberView.text.toString().length <= 8 || routingNumberView.text.toString().length >= 10) {
                    showToast("Routing Number Must Be 9 Digits")
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
                    val accountNumber = accountNumberView.text.toString().toLong()
                    val routingNumber = routingNumberView.text.toString().toInt()
                    val firstName = firstNameView.text.toString()
                    val lastName = lastNameView.text.toString()
                    val addressOne = addressOneView.text.toString()
                    val addressTwo = addressTwoView.text.toString()
                    val city = cityView.text.toString()
                    val state = stateView.text.toString()
                    val zipCode = zipCodeView.text.toString()

                    val payment = Payment(
                        null,
                        null,
                        null,
                        null,
                        accountNumber,
                        routingNumber,
                        achPaymentType,
                        intent.getStringExtra("Payment-Amount")!!.toInt(),
                        intent.getStringExtra("Payment-Type")!!.toString(),
                        intent.getStringExtra("Fee-Mode")!!,
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
                            if (intent.hasExtra("First-Name")) intent.getStringExtra("First-Name") else "",
                            if (intent.hasExtra("Last-Name")) intent.getStringExtra("Last-Name") else "",
                            if (intent.hasExtra("Address-One")) intent.getStringExtra("Address-One") else "",
                            if (intent.hasExtra("Address-Two")) intent.getStringExtra("Address-Two") else "",
                            if (intent.hasExtra("City")) intent.getStringExtra("City") else "",
                            if (intent.hasExtra("State")) intent.getStringExtra("State") else "",
                            if (intent.hasExtra("Country")) intent.getStringExtra("Country") else "",
                            if (intent.hasExtra("Zip-Code")) intent.getStringExtra("Zip-Code") else "",
                            if (intent.hasExtra("Phone-Number")) intent.getStringExtra("Phone-Number") else "",
                            if (intent.hasExtra("Email-Address")) intent.getStringExtra("Email-Address") else "",
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
        } else if (intent.getStringExtra("Payment-Type") == "ACH" && intent.getStringExtra("Full-Account-Details") != "True") {
            setContentView(R.layout.activity_pay_theory_ach)

            setPaymentTypeSpinner()

            val btn = findViewById<Button>(R.id.submitButton)
            val accountNumberView = findViewById<ACHAccountNumber>(R.id.accountNumberEditText)
            val routingNumberView = findViewById<ACHRoutingNumber>(R.id.routingNumberEditText)
            val firstNameView = findViewById<FirstNameEditText>(R.id.firstNameEditText)
            val lastNameView = findViewById<LastNameEditText>(R.id.lastNameEditText)

//            accountNumberView.setText("12345678910")
//            routingNumberView.setText("789456124")


            btn.setOnClickListener {
                if (accountNumberView.text.toString().isNullOrEmpty()) {
                    showToast("Account Number Required")
                } else if (routingNumberView.text.toString().length <= 8 || routingNumberView.text.toString().length >= 10) {
                    showToast("Routing Number Must Be 9 Digits")
                } else if (firstNameView.text.toString().isNullOrEmpty()) {
                    showToast("First Name Required")
                } else if (lastNameView.text.toString().isNullOrEmpty()) {
                    showToast("Last Name Required")
                } else {
                    val accountNumber = accountNumberView.text.toString().toLong()
                    val routingNumber = routingNumberView.text.toString().toInt()
                    val firstName = firstNameView.text.toString()
                    val lastName = lastNameView.text.toString()

                    val payment = Payment(
                        null,
                        null,
                        null,
                        null,
                        accountNumber,
                        routingNumber,
                        achPaymentType,
                        intent.getStringExtra("Payment-Amount")!!.toInt(),
                        intent.getStringExtra("Payment-Type")!!.toString(),
                        intent.getStringExtra("Fee-Mode")!!,
                        intent.getStringExtra("Tags-Key"),
                        intent.getStringExtra("Tags-Value"),
                        firstName,
                        lastName
                    )
                    if (intent.getStringExtra("Buyer-Options") == "True") {

                        val buyerOptions = BuyerOptions(
                            if (intent.hasExtra("First-Name")) intent.getStringExtra("First-Name") else "",
                            if (intent.hasExtra("Last-Name")) intent.getStringExtra("Last-Name") else "",
                            if (intent.hasExtra("Address-One")) intent.getStringExtra("Address-One") else "",
                            if (intent.hasExtra("Address-Two")) intent.getStringExtra("Address-Two") else "",
                            if (intent.hasExtra("City")) intent.getStringExtra("City") else "",
                            if (intent.hasExtra("State")) intent.getStringExtra("State") else "",
                            if (intent.hasExtra("Country")) intent.getStringExtra("Country") else "",
                            if (intent.hasExtra("Zip-Code")) intent.getStringExtra("Zip-Code") else "",
                            if (intent.hasExtra("Phone-Number")) intent.getStringExtra("Phone-Number") else "",
                            if (intent.hasExtra("Email-Address")) intent.getStringExtra("Email-Address") else "",
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
            val errorMessage =
                "Pay Theory activity intents not set up correctly. Please review your on click listener intents."
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(
            this, message,
            Toast.LENGTH_LONG
        ).show()
    }


    private fun cardValidation(cardNumber: String): Boolean {
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

    private fun setPaymentTypeSpinner() {
        val spinner: Spinner = findViewById(R.id.payment_type_spinner)

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.payment_type_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {

                achPaymentType = spinner.selectedItem as String
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                Log.d("Pay Theory", "Current selection is nothing")
            }
        }
    }
}






