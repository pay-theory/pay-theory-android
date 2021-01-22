package com.paytheory.paytheorylibrarysdk.classes

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.paytheory.paytheorylibrarysdk.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * PayTheoryActivity Class is an AppCompatActivity. Activity used when inputting data to submit payment
 */
class PayTheoryActivity : AppCompatActivity() {



    companion object {
        const val COLLECT_BILLING_ADDRESS = "Billing-Address"
        const val PAYMENT_TYPE = "Payment-Type"
        const val PAYMENT_TYPE_CARD = "CARD"
        const val PAYMENT_TYPE_ACH = "ACH"
        const val FEE_MODE = "Fee-Mode"
        const val DEFAULT_FEE_MODE = "surcharge"
        const val PAYMENT_AMOUNT = "Payment-Amount"
        const val API_KEY = "Api-Key"
        var feeMode = ""
        var paymentType = ""
        var achType: String = "CHECKING"
    }
    override fun onBackPressed() {
        setResult(5, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getStringExtra(PAYMENT_AMOUNT).isNullOrBlank()) {
            val returnIntent = Intent()
            //If "Display" not selected or input
            val errorMessage = "Activity intent \"Payment-Amount\" not valid"
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        if (intent.getStringExtra(API_KEY).isNullOrBlank()) {
            val returnIntent = Intent()
            //If "Display" not selected or input
            val errorMessage = "Activity intent \"Api-Key\" must be supplied"
            Log.d("Pay Theory", errorMessage)
            returnIntent.putExtra("result", errorMessage)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
        if (intent.hasExtra(PAYMENT_TYPE)) {
            paymentType = intent.getStringExtra(PAYMENT_TYPE).toString()
        }
        if ((!intent.hasExtra(PAYMENT_TYPE)) && paymentType == "") {
            paymentType = PAYMENT_TYPE_CARD
        }

        feeMode = if(intent.getStringExtra(FEE_MODE).toString().isNullOrBlank()) {
            DEFAULT_FEE_MODE
        }else {
            intent.getStringExtra(FEE_MODE).toString()
        }

        if (paymentType == PAYMENT_TYPE_CARD && intent.getStringExtra(
                COLLECT_BILLING_ADDRESS
            ) == "True") {
            setContentView(R.layout.activity_pay_theory_card_full_account)

            val btn = findViewById<Button>(R.id.submitButton)
            val btnToACH = findViewById<Button>(R.id.toACH)
            val fullNameView = findViewById<FullNameEditText>(R.id.fullNameEditText)
            val addressOneView = findViewById<AddressOneEditText>(R.id.addressOneEditText)
            val addressTwoView = findViewById<AddressTwoEditText>(R.id.addressTwoEditText)
            val cityView = findViewById<CityEditText>(R.id.cityEditText)
            val zipCodeView = findViewById<ZipEditText>(R.id.zipEditText)
            val stateView = findViewById<StateEditText>(R.id.stateEditText)
            val creditCardView = findViewById<CreditCardEditText>(R.id.creditCardEditText)
            val cvvView = findViewById<CVVEditText>(R.id.cvvEditText)
            val expiration = findViewById<ExpirationEditText>(R.id.expirationEditText)

            btnToACH.setOnClickListener{
                intent.putExtra(PAYMENT_TYPE, PAYMENT_TYPE_ACH)
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                startActivity(intent)
                finish()
            }

            expiration.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(
                    p0: CharSequence?,
                    start: Int,
                    removed: Int,
                    added: Int
                ) {
                    if (start == 1 && start + added == 2 && p0?.contains('/') == false) {
                        expiration.setText("$p0/")
                        expiration.setSelection(expiration.text!!.length);
                    } else if (start == 3 && start - removed == 2 && p0?.contains('/') == true) {
                        expiration.setText(p0.toString().replace("/", ""))
                        expiration.setSelection(expiration.text!!.length);
                    }
                }
            })

            creditCardView.addTextChangedListener(object : TextWatcher {
                private var current = ""
                private val nonDigits = Regex("[^\\d]")

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    if (s.toString() != current) {
                        val userInput = s.toString().replace(nonDigits, "")
                        if (userInput.length <= 16) {
                            current = userInput.chunked(4).joinToString(" ")
                            s.filters = arrayOfNulls<InputFilter>(0)
                        }
                        s.replace(0, s.length, current, 0, current.length)
                    }
                }
            })

            btn.setOnClickListener {
//                val number
//                val cardNumber
//                lateinit var cardString: String
//                lateinit var cardNumber: Long
//
//                if (creditCardView.text.toString().isNullOrEmpty()) {
//                    showToast("Card Number Required")
//                }  else if (creditCardView.text.toString().length < 17) {
//                    showToast("Card Number Invalid")
//                } else if(!(creditCardView.text.toString().isNullOrEmpty()) && creditCardView.text.toString().length >= 17) {
//                    cardString = creditCardView.text.toString()
//                    cardNumber = cardString.replace("\\s".toRegex(), "").toLong()
//                } else if (!cardValidation(cardNumber.toString())) {
//                    showToast("Card Number Invalid")
//                }



                if (creditCardView.text.toString().isNullOrEmpty()) {
                    showToast("Card Number Required")
                }  else if (creditCardView.text.toString().length < 17) {
                    showToast("Card Number Invalid")
                } else if(!cardValidation(
                        creditCardView.text.toString().replace(
                            "\\s".toRegex(),
                            ""
                        )
                    )) {
                    showToast("Card Number Invalid")
                } else if (cvvView.text.toString().isNullOrEmpty()) {
                    showToast("CVV Number Required")
                } else if (cvvView.text.toString().length <= 2) {
                    showToast("CVV Number Must Be 3 Digits")
                } else if (expiration.text.toString().isNullOrEmpty()) {
                    showToast("Expiration Month Required")
                } else if (expiration.text.toString().length < 5) {
                    showToast("Expiration Must Be 4 Digits")
                } else if (zipCodeView.text.toString().isNullOrEmpty()) {
                    showToast("Zip Code Required")
                } else if (zipCodeView.text.toString().length <= 4) {
                    showToast("Zip Code Must Be 5 Digits")
                } else if (fullNameView.text.toString().isNullOrEmpty()) {
                    showToast("Name Required")
                } else if (addressOneView.text.toString().isNullOrEmpty()) {
                    showToast("Address One Required")
                } else if (cityView.text.toString().isNullOrEmpty()) {
                    showToast("City Required")
                } else if (stateView.text.toString().isNullOrEmpty()) {
                    showToast("State Required")
                } else {
                    val number = creditCardView.text.toString()
                    val cardNumber = number.replace("\\s".toRegex(), "").toLong()
                    val cvv = cvvView.text.toString()
                    val expirationString = expiration.text.toString()
                    val expirationMonth = expirationString.take(2).toInt()
                    val expirationYear = ("20" + expirationString.takeLast(2)).toInt()


                    var name: String = fullNameView.text.toString()
                    var parts  = name.split(" ").toMutableList()
                    val firstName = parts.firstOrNull()
                    parts.removeAt(0)
                    val lastName = parts.joinToString(" ")

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
                        achType,

                        intent.getStringExtra(PAYMENT_AMOUNT)!!.toInt(),
                        paymentType,
                        //add fee mode
                        feeMode,
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
                            intent.getStringExtra(API_KEY)!!,
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
                            intent.getStringExtra(API_KEY)!!,
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
        } else if (paymentType == PAYMENT_TYPE_CARD && intent.getStringExtra(
                COLLECT_BILLING_ADDRESS
            ) != "True") {
            setContentView(R.layout.activity_pay_theory_card)

            val btn = findViewById<Button>(R.id.submitButton)
            val creditCardView = findViewById<CreditCardEditText>(R.id.creditCardEditText)
            val cvvView = findViewById<CVVEditText>(R.id.cvvEditText)
            val expiration = findViewById<ExpirationEditText>(R.id.expirationEditText)
            val btnToACH = findViewById<Button>(R.id.toACH)


            btnToACH.setOnClickListener{
                intent.putExtra(PAYMENT_TYPE, PAYMENT_TYPE_ACH)
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                startActivity(intent)
                finish()
            }

            expiration.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {}

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(
                    p0: CharSequence?,
                    start: Int,
                    removed: Int,
                    added: Int
                ) {
                    if (start == 1 && start + added == 2 && p0?.contains('/') == false) {
                        expiration.setText("$p0/")
                        expiration.setSelection(expiration.text!!.length);
                    } else if (start == 3 && start - removed == 2 && p0?.contains('/') == true) {
                        expiration.setText(p0.toString().replace("/", ""))
                        expiration.setSelection(expiration.text!!.length);
                    }
                }
            })

            creditCardView.addTextChangedListener(object : TextWatcher {
                private var current = ""
                private val nonDigits = Regex("[^\\d]")

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    if (s.toString() != current) {
                        val userInput = s.toString().replace(nonDigits, "")
                        if (userInput.length <= 16) {
                            current = userInput.chunked(4).joinToString(" ")
                            s.filters = arrayOfNulls<InputFilter>(0)
                        }
                        s.replace(0, s.length, current, 0, current.length)
                    }
                }
            })

            btn.setOnClickListener {
                if (creditCardView.text.toString().isNullOrEmpty()) {
                    showToast("Card Number Required")
                }  else if (creditCardView.text.toString().length < 17) {
                    showToast("Card Number Invalid")
                } else if(!cardValidation(
                        creditCardView.text.toString().replace(
                            "\\s".toRegex(),
                            ""
                        )
                    )) {
                    showToast("Card Number Invalid")
                } else if (cvvView.text.toString().isNullOrEmpty()) {
                    showToast("CVV Number Required")
                } else if (cvvView.text.toString().length <= 2) {
                    showToast("CVV Number Must Be 3 Digits")
                } else if (expiration.text.toString().isNullOrEmpty()) {
                    showToast("Expiration Month Required")
                } else if (expiration.text.toString().length < 5) {
                    showToast("Expiration Must Be 4 Digits")
                } else {
                    val number = creditCardView.text.toString()
                    val cardNumber = number.replace("\\s".toRegex(), "").toLong()
                    val cvv = cvvView.text.toString()
                    val expirationString = expiration.text.toString()
                    val expirationMonth = expirationString.take(2).toInt()
                    val expirationYear = ("20" + expirationString.takeLast(2)).toInt()


                    val payment = Payment(
                        cardNumber,
                        expirationMonth,
                        expirationYear,
                        cvv,
                        null,
                        null,
                        achType,
                        intent.getStringExtra(PAYMENT_AMOUNT)!!.toInt(),
                        paymentType,
                        feeMode,
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
                            intent.getStringExtra(API_KEY)!!,
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
                            intent.getStringExtra(API_KEY)!!,
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
        } else if (paymentType == PAYMENT_TYPE_ACH && intent.getStringExtra(
                COLLECT_BILLING_ADDRESS
            ) == "True") {
            setContentView(R.layout.activity_pay_theory_ach_full_account)

            setPaymentTypeSpinner()

            val btn = findViewById<Button>(R.id.submitButton)
            val accountNumberView = findViewById<ACHAccountNumber>(R.id.accountNumberEditText)
            val routingNumberView = findViewById<ACHRoutingNumber>(R.id.routingNumberEditText)
            val fullNameView = findViewById<FullNameEditText>(R.id.fullNameEditText)
            val addressOneView = findViewById<AddressOneEditText>(R.id.addressOneEditText)
            val addressTwoView = findViewById<AddressTwoEditText>(R.id.addressTwoEditText)
            val cityView = findViewById<CityEditText>(R.id.cityEditText)
            val zipCodeView = findViewById<ZipEditText>(R.id.zipEditText)
            val stateView = findViewById<StateEditText>(R.id.stateEditText)
            val btnToCard = findViewById<Button>(R.id.toCard)


            btnToCard.setOnClickListener{
                intent.putExtra(PAYMENT_TYPE, PAYMENT_TYPE_CARD)
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                startActivity(intent)
                finish()
            }
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
                } else if (fullNameView.text.toString().isNullOrEmpty()) {
                    showToast("Name Required")
                } else if (addressOneView.text.toString().isNullOrEmpty()) {
                    showToast("Address One Required")
                } else if (cityView.text.toString().isNullOrEmpty()) {
                    showToast("City Required")
                } else if (stateView.text.toString().isNullOrEmpty()) {
                    showToast("State Required")
                } else {
                    val accountNumber = accountNumberView.text.toString().toLong()
                    val routingNumber = routingNumberView.text.toString().toInt()

                    var name: String = fullNameView.text.toString()
                    var parts  = name.split(" ").toMutableList()
                    val firstName = parts.firstOrNull()
                    parts.removeAt(0)
                    val lastName = parts.joinToString(" ")

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
                        achType,
                        intent.getStringExtra(PAYMENT_AMOUNT)!!.toInt(),
                        paymentType,
                        feeMode,
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
                            intent.getStringExtra(API_KEY)!!,
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
                            intent.getStringExtra(API_KEY)!!,
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
        } else if (paymentType == PAYMENT_TYPE_ACH && intent.getStringExtra(
                COLLECT_BILLING_ADDRESS
            ) != "True") {
            setContentView(R.layout.activity_pay_theory_ach)

            setPaymentTypeSpinner()

            val btn = findViewById<Button>(R.id.submitButton)
            val accountNumberView = findViewById<ACHAccountNumber>(R.id.accountNumberEditText)
            val routingNumberView = findViewById<ACHRoutingNumber>(R.id.routingNumberEditText)
            val fullNameView = findViewById<FullNameEditText>(R.id.fullNameEditText)
            val btnToCard = findViewById<Button>(R.id.toCard)


            btnToCard.setOnClickListener{
                intent.putExtra(PAYMENT_TYPE, PAYMENT_TYPE_CARD)
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                startActivity(intent)
                finish()
            }
//            accountNumberView.setText("12345678910")
//            routingNumberView.setText("789456124")


            btn.setOnClickListener {
                if (accountNumberView.text.toString().isNullOrEmpty()) {
                    showToast("Account Number Required")
                } else if (routingNumberView.text.toString().length <= 8 || routingNumberView.text.toString().length >= 10) {
                    showToast("Routing Number Must Be 9 Digits")
                } else if (fullNameView.text.toString().isNullOrEmpty()) {
                    showToast("Name Required")
                }else {
                    val accountNumber = accountNumberView.text.toString().toLong()
                    val routingNumber = routingNumberView.text.toString().toInt()

                    var name: String = fullNameView.text.toString()
                    var parts  = name.split(" ").toMutableList()
                    val firstName = parts.firstOrNull()
                    parts.removeAt(0)
                    val lastName = parts.joinToString(" ")

                    val payment = Payment(
                        null,
                        null,
                        null,
                        null,
                        accountNumber,
                        routingNumber,
                        achType,
                        intent.getStringExtra(PAYMENT_AMOUNT)!!.toInt(),
                        paymentType,
                        feeMode,
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
                            intent.getStringExtra(API_KEY)!!,
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
                            intent.getStringExtra(API_KEY)!!,
                            payment
                        )
                        showToast("Processing payment please wait...")


                        CoroutineScope(IO).launch {
                            val payTheoryResult = async {
                                payTheory.init()
                            }.await()
                            while (payTheoryResult == "") {
                                delay(500)
                            }
                            Log.d("Pay Theory", "Pay Theory Result : $payTheoryResult")
                            intent.putExtra("result", payTheoryResult)
                            setResult(RESULT_OK, intent)
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
            setResult(RESULT_CANCELED, returnIntent)
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
            R.array.ach_type_array,
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

                achType = spinner.selectedItem as String
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                Log.d("Pay Theory", "Current selection is nothing")
            }
        }
    }
}






