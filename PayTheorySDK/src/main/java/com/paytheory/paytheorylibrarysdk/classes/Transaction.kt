package com.paytheory.paytheorylibrarysdk.classes

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject

/**
 * Transaction Class is created after data validation and click listener is activated.
 * This hold all pay theory logic to process payments.
 */
class Transaction(
    private val context: Context,
    private val apiKey: String,
    private val payment: Payment,
    private val buyerOptions: BuyerOptions? = null
) {
    private var client = OkHttpClient()
    private var challengeResponse = ""

    private var attestationResponse: String? = null
    private var idempotencyResponse: String? = null
    private var idempotencyResponseData: String = ""
    private var idempotencySignatureData: String = ""
    private var idempotencyCredIdData: String = ""
    private var token = ""
    private var idempotency = ""
    private var merchantId = ""
    private var userConfirmation: Boolean? = null
    private var transactionResponse = ""
    private var transactionState = "NOT COMPLETE"

    private var challengeComplete = false
    private var googleVerifyComplete = false
    private var idempotencyComplete = false


    private fun getCardType(number: String): String {

        val visa = Regex("^4[0-9]{12}(?:[0-9]{3})?$")
        val mastercard = Regex("^5[1-5][0-9]{14}$")
        val amx = Regex("^3[47][0-9]{13}$")
        val diners = Regex("^3(?:0[0-5]|[68][0-9])[0-9]{11}$")
        val discover = Regex("^6(?:011|5[0-9]{2})[0-9]{12}$")

        return when {
            visa.matches(number) -> "Visa"
            mastercard.matches(number) -> "Mastercard"
            amx.matches(number) -> "American Express"
            diners.matches(number) -> "Diners"
            discover.matches(number) -> "Discover"
            else -> "Unknown"
        }
    }

    suspend fun init(): String {
        Log.d("Pay Theory", "Init transaction")
        CoroutineScope(IO).launch {

            //Challenge Api
            val challengeResult = async {
                challenge()
            }.await()
            if (challengeComplete) {
                Log.d("Pay Theory", "Challenge Result: $challengeResult")

                //Google Api
                val googleApiResult = async {
                    googleApi()
                }.await()
                if (googleVerifyComplete) {
                    Log.d("Pay Theory", "Google Api Result: $googleApiResult")

                    //Attestation Api
                    val attestationResponse = async {
                        attestation(challengeResult)
                    }.await()
                    if (!attestationResponse.isNullOrBlank()) {
                        Log.d("Pay Theory", "Attestation Api Result: $attestationResponse")


                        if (!attestationResponse.isNullOrEmpty() && googleVerifyComplete && challengeComplete) {
                            //Pay Theory Idempotency
                            val idempotencyResponse = async {
                                payTheoryIdempotency(challengeResponse, attestationResponse)
                            }.await()

                            if (!idempotencyResponse.isNullOrBlank() && idempotencyComplete) {


                                Log.d("Pay Theory", "Idempotency Result: $idempotencyResponse")

                                if (payment.type == "Card") {
                                    Handler(Looper.getMainLooper()).post {
                                        confirmCard(
                                            payment.amount,
                                            payment.convenienceFee,
                                            getCardType(payment.cardNumber.toString()),
                                            payment.cardNumber,
                                            context
                                        )
                                    }
                                } else {
                                    Handler(Looper.getMainLooper()).post {
                                        confirmACH(
                                            payment.amount,
                                            payment.convenienceFee,
                                            payment.achAccountNumber,
                                            context
                                        )
                                    }
                                }

                                while (userConfirmation == null) {
                                    delay(500)
                                }
                                if (userConfirmation == true) {
                                    transactionResponse = async {
                                        payment(idempotency)
                                    }.await()
                                    Log.d(
                                        "Pay Theory",
                                        "Transaction Response: $transactionResponse"
                                    )

                                } else {
                                    Log.d(
                                        "Pay Theory",
                                        "User Confirmed Transaction: $userConfirmation"
                                    )
                                }

                            } else {
                                Log.d("Pay Theory", "ERROR: Idempotency Failed")
                                val message = "Idempotency failed"
                                transactionResponse =
                                    "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                                        payment.cardNumber.toString().takeLast(
                                            4
                                        )
                                    }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
                            }
                        } else {
                            Log.d("Pay Theory", "ERROR: Validation Failed")
                            val message = "Validation failed"
                            transactionResponse =
                                "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                                    payment.cardNumber.toString().takeLast(
                                        4
                                    )
                                }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
                        }

                    } else {
                        Log.d("Pay Theory", "ERROR: Attestation failed")
                        val message = "Attestation failed"
                        transactionResponse =
                            "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                                payment.cardNumber.toString().takeLast(
                                    4
                                )
                            }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
                    }


                } else {
                    Log.d("Pay Theory", "ERROR: Google verification failed")
                    val message = "Google verification failed"
                    transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                        payment.cardNumber.toString().takeLast(
                            4
                        )
                    }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
                }
            } else {
                Log.d("Pay Theory", "ERROR: Challenge failed")
                val message = "Challenge failed"
                transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                    payment.cardNumber.toString().takeLast(
                        4
                    )
                }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
            }
        }
        while (transactionResponse == "") {
            delay(5000)
        }
        return transactionResponse
    }

    fun challenge(): String {
        val request = Request.Builder()
            .url("https://dev.tags.api.paytheorystudy.com/challenge")
            .header("X-API-Key", apiKey)
            .build()
        val response = client.newCall(request).execute()
        val jsonData: String? = response.body?.string()

        if (response.message == "Forbidden") {
            return response.message
        }
        val challengeJSONResponse = JSONObject(jsonData)
        return if (challengeJSONResponse.has("challenge")) {
            val challengeResponseString = challengeJSONResponse.getString("challenge")
            challengeResponse = challengeJSONResponse.getString("challenge")
            challengeComplete = true
            challengeResponseString
        } else {
            challengeComplete = false
            challengeJSONResponse.getString("message")
        }
    }

    fun googleApi(): Boolean {
        //Call google play services to verify google play is available
        return if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        ) {
            googleVerifyComplete = true
            true
        } else {
            googleVerifyComplete = false
            false
        }
    }

    suspend fun attestation(nonce: String): String? {
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }
        val attestationTask = SafetyNet.getClient(context).attest(
            nonce.toByteArray(),
            "AIzaSyCtRWLrt0I67VhmJV3cue-18ENmxZ8MXGo"
        )
        while (!attestationTask.isComplete) {
            delay(3000)
        }
        attestationResponse = attestationTask.result.jwsResult
        return attestationTask.result.jwsResult
    }

    fun payTheoryIdempotency(nonce: String, attestation: String?): String {
        val jsonObject = JSONObject()
        jsonObject.put("currency", "USD")
        jsonObject.put("amount", payment.amount)
        jsonObject.put("type", "android")
        try {
            if (!nonce.isNullOrBlank()) {
                jsonObject.put("nonce", nonce)
            }
            if (attestation != null) {
                jsonObject.put("attestation", "$attestation")
            }
            if (!payment.feeMode.isNullOrBlank()) {
                jsonObject.put("fee_mode", payment.feeMode)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .method("POST", body)
            .url("https://dev.attested.api.paytheorystudy.com/idempotency")
            .addHeader("x-api-key", apiKey)
            .build()
        val response = client.newCall(request).execute()
        val jsonData: String? = response.body?.string()

        val idempotencyJSONResponse = JSONObject(jsonData)

        Log.d("PayTheory", idempotencyJSONResponse.toString())

        return if (idempotencyJSONResponse.has("response")
        ) {
            idempotencyResponse = idempotencyJSONResponse.toString()
            idempotencyResponseData = idempotencyJSONResponse.getString("response")
            idempotencySignatureData = idempotencyJSONResponse.getString("signature")
            idempotencyCredIdData = idempotencyJSONResponse.getString("credId")
            idempotency = idempotencyJSONResponse.getString("idempotency")
            var newPayment = JSONObject(idempotencyJSONResponse.getString("payment"))
            payment.amount = newPayment.get("amount") as Int
            payment.convenienceFee = newPayment.get("service_fee").toString()
            idempotencyComplete = true
            idempotencyResponse.toString()
        } else if (idempotencyJSONResponse.getString("state") == "error") {

            val failError = idempotencyJSONResponse.getString("reason")

            transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                payment.cardNumber.toString().takeLast(
                    4
                )
            }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${
                idempotencyJSONResponse.getString(
                    "state"
                )
            }\", \"type\":\"$failError\"}"
            return transactionResponse
        } else {
            val failError = "application error"

            transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                payment.cardNumber.toString().takeLast(
                    4
                )
            }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"$failError\"}"
            return transactionResponse
        }
    }

    fun confirmCard(
        paymentAmount: Int,
        convenienceFee: String,
        cardBrand: String,
        cardNumber: Long?,
        context: Context
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm")
        builder.setMessage(
            "Are you sure you want to make a payment of $${
                String.format(
                    "%.2f",
                    (paymentAmount.toDouble() / 100)
                )
            }, including a fee of $${
                String.format(
                    "%.2f",
                    (convenienceFee.toDouble() / 100)
                )
            } on $cardBrand card beginning with ${
                cardNumber.toString().take(6)
            }"
        )
        builder.setPositiveButton("YES") { dialog, _ ->
            userConfirmation = true
            dialog.dismiss()
            Log.d(
                "Pay Theory",
                "User Confirmed Yes - $paymentAmount, $convenienceFee"
            )
        }
        builder.setNegativeButton("NO") { dialog, _ ->
            userConfirmation = false
            cancel()
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    fun confirmACH(
        paymentAmount: Int,
        convenienceFee: String,
        accountNumber: Long?,
        context: Context
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm")
        builder.setMessage(
            "Are you sure you want to make a payment of $${
                String.format(
                    "%.2f",
                    (paymentAmount.toDouble() / 100)
                )
            }, including a fee of $${
                String.format(
                    "%.2f",
                    (convenienceFee.toDouble() / 100)
                )
            } on account beginning with ${
                accountNumber.toString().take(6)
            }"
        )
        builder.setPositiveButton("YES") { dialog, _ ->
            userConfirmation = true
            dialog.dismiss()
            Log.d(
                "Pay Theory",
                "User Confirmed Yes - $paymentAmount, $convenienceFee"
            )
        }
        builder.setNegativeButton("NO") { dialog, _ ->
            userConfirmation = false
            cancel()
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    fun cancel(): String {
        Log.d("Pay Theory", "Cancel complete")
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("Cancelled transaction")
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
        return "Payment Cancelled"
    }

    fun payment(idempotency: String): String {
        val paymentBody = JSONObject()
        val buyerOptionsAddress = JSONObject()
        val buyerOptionsJson = JSONObject()
        try {
            if (buyerOptions != null) {
                if (!buyerOptions.firstName.isNullOrBlank()) {
                    buyerOptionsJson.put("first_name", buyerOptions.firstName)
                }
                if (!buyerOptions.lastName.isNullOrBlank()) {
                    buyerOptionsJson.put("last_name", buyerOptions.lastName)
                }
                if (!buyerOptions.phoneNumber.isNullOrBlank()) {
                    buyerOptionsJson.put("phone", buyerOptions.phoneNumber)
                }
                if (!buyerOptions.email.isNullOrBlank()) {
                    buyerOptionsJson.put("email", buyerOptions.email)
                }
                if (!buyerOptions.addressOne.isNullOrBlank()) {
                    buyerOptionsAddress.put("line1", buyerOptions.addressOne)
                }
                if (!buyerOptions.zipCode.isNullOrBlank()) {
                    buyerOptionsAddress.put("postal_code", buyerOptions.zipCode)
                }
                if (!buyerOptions.addressTwo.isNullOrBlank()) {
                    buyerOptionsAddress.put("line2", buyerOptions.addressTwo)
                }
                if (!buyerOptions.city.isNullOrBlank()) {
                    buyerOptionsAddress.put("city", buyerOptions.city)
                }
                if (!buyerOptions.country.isNullOrBlank()) {
                    buyerOptionsAddress.put("country", buyerOptions.country)
                }
                if (!buyerOptions.state.isNullOrBlank()) {
                    buyerOptionsAddress.put("region", buyerOptions.state)
                }
            }

            val paymentJsonObject = JSONObject()

            if (payment.type == "Card") {
                paymentJsonObject.put("expiration_month", payment.cardExpMon)
                paymentJsonObject.put("expiration_year", payment.cardExpYear)
                paymentJsonObject.put("security_code", "${payment.cardCvv}")
                paymentJsonObject.put("number", "${payment.cardNumber}")
                paymentJsonObject.put("type", "PAYMENT_CARD")
            } else {
                paymentJsonObject.put("account_number", "${payment.achAccountNumber}")
                paymentJsonObject.put("account_type", payment.achAccountType)
                paymentJsonObject.put("bank_code", payment.achRoutingNumber)
                paymentJsonObject.put("name", "${payment.firstName} ${payment.lastName}")
                paymentJsonObject.put("type", "BANK_ACCOUNT")
            }



            if (!idempotency.isNullOrBlank()) {
                val tagsJson = JSONObject()
                tagsJson.put("key", "pt-platform:android $idempotency")
                buyerOptionsJson.put("personal_address", buyerOptionsAddress)
                paymentBody.put("response", idempotencyResponseData)
                paymentBody.put("credId", idempotencyCredIdData)
                paymentBody.put("signature", idempotencySignatureData)
                paymentBody.put("payment", paymentJsonObject)
                paymentBody.put("tags", tagsJson)
                paymentBody.put("buyer-options", buyerOptionsJson)
            }
            if (!payment.tagsKey.isNullOrBlank() && !payment.tagsValue.isNullOrBlank()) {
                val tagsJson = JSONObject()
                tagsJson.put(payment.tagsKey, payment.tagsValue.toString())
                paymentBody.put("tags", tagsJson)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val paymentRequest = Request.Builder()
            .method(
                "POST",
                paymentBody.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .addHeader("x-api-key", apiKey)
            .url("https://dev.attested.api.paytheorystudy.com/payment")
            .build()

        val paymentResponse = client.newCall(paymentRequest).execute()
        val paymentJsonData: String? = paymentResponse.body?.string()
        val paymentJsonObject = JSONObject(paymentJsonData)

        if (paymentJsonObject.getString("state") != "error") {

            transactionResponse = paymentJsonObject.toString()

        } else {
            Log.d("Pay Theory", "Identity Call Request Failed / Payment Request Failed")
            val failError = "Identity Call Request Failed / Payment Request Failed"

            transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                payment.cardNumber.toString().takeLast(
                    4
                )
            }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"$failError\"}"
            return transactionResponse
        }
        return transactionResponse
    }
}



