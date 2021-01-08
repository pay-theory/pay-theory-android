package com.paytheory.paytheorylibrarysdk.classes

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64.DEFAULT
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.model.VerifyRequest
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
import java.nio.ByteBuffer

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
    private var googleVerify = false
    private var attestationResponse: String? = null
    private var idempotencyResponse: String? = null
    private var idempotencyResponseData: String = ""
    private var idempotencySignatureData: String = ""
    private var idempotencyCredIdData: String = ""
    private var kmsResult: Boolean = false
    private var token = ""
    private var idempotency = "Not Created"
    private var merchantId = ""
    private var userConfirmation: Boolean? = null
    private var transactionResponse = ""
    private var transactionState = "NOT COMPLETE"
    private var challengeComplete: Boolean? = null

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
            if (challengeComplete == true) {
                Log.d("Pay Theory", "Challenge Result: $challengeResult")


            //Google Api
            val googleApiResult = async {
                googleApi()
            }.await()
            Log.d("Pay Theory", "Google Api Result: $googleApiResult")


            //Attestation Api
            val attestationResponse = async {
                attestation(challengeResult)
            }.await()
            if (attestationResponse == null) {
                Log.d("Pay Theory", "ERROR: Attestation failed")
            } else {
                Log.d("Pay Theory", "Attestation Result: $attestationResponse")
            }



            if (!attestationResponse.isNullOrEmpty() || googleVerify || !challengeResponse.isNullOrEmpty()) {
                //Pay Theory Idempotency
                val idempotencyResponse = async {
                    payTheoryIdempotency(challengeResponse, attestationResponse)
                }.await()

                Log.d("Pay Theory", "Idempotency Result: $idempotencyResponse")

                //KMS
                val kmsResult = async {
                    kms(
                        idempotencyResponseData,
                        idempotencySignatureData,
                        idempotencyCredIdData
                    )
                }.await()

                Log.d("Pay Theory", "KMS Result: $kmsResult")
            } else {
                Log.d("Pay Theory", "ERROR: Validation Failed")
            }

            if (kmsResult) {
                Handler(Looper.getMainLooper()).post {
                    confirmAlert(
                        payment.amount,
                        payment.convenienceFee,
                        getCardType(payment.cardNumber.toString()),
                        payment.cardNumber,
                        context
                    )
                }

            } else {
                Log.d("Pay Theory", "ERROR: Verification failed")
            }

            while (userConfirmation == null) {
                delay(500)
            }
            if (userConfirmation == true) {
                transactionResponse = async {
                    transact(token, merchantId, payment.currency, idempotency)
                }.await()
                Log.d("Pay Theory", "Transaction Response: $transactionResponse")

            } else {
                Log.d("Pay Theory", "User Cancelled Transaction: $userConfirmation")
            }

            } else {
                Log.d("Pay Theory", "ERROR: Challenge failed")
                transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                    payment.cardNumber.toString().takeLast(
                        4
                    )
                }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${challengeResult}\"}"
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
        Log.d("PTLib", "Challenge Response: $jsonData")

        if(response.message == "Forbidden"){
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
            Log.d("Pay Theory", "Google Play Service Available.")
            googleVerify = true
            true
        } else {
            googleVerify = false
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
        Log.d("Pay Theory", "payTheoryIdempotency JSON Body: $jsonObject")

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .method("POST", body)
            .url("https://dev.tags.api.paytheorystudy.com/idempotency")
            .addHeader("x-api-key", apiKey)
            .build()

        val response = client.newCall(request).execute()
        val jsonData: String? = response.body?.string()
        Log.d("PTLib", "Idempotency response body ${jsonData}")
        //TODO -  Idempotency response body  <!DOCTYPE of type java.lang.String cannot be converted to JSONObject   <!DOCTYPE html>
        //    <html lang="en">
        //    <head>
        //    <meta charset="utf-8">
        //    <title>Error</title>
        //    </head>
        //    <body>
        //    <pre>Cannot POST /idempotency</pre>
        //    </body>
        //    </html>



        val idempotencyJSONResponse = JSONObject(jsonData)

        //TODO - Idempotency Result: { "receipt_number":"", "last_four":"1758", "brand":"Mastercard", "state":"NOT COMPLETE", "type":"Forbidden"} as string
        return if (idempotencyJSONResponse.has("response") && idempotencyJSONResponse.has("signature") && idempotencyJSONResponse.has(
                "credId"
            )
        ) {
            idempotencyResponse = idempotencyJSONResponse.toString()
            idempotencyResponseData = idempotencyJSONResponse.getString("response")
            idempotencySignatureData = idempotencyJSONResponse.getString("signature")
            idempotencyCredIdData = idempotencyJSONResponse.getString("credId")

            Log.d("PTLib", "Idempotency response $idempotencyResponse")
            idempotencyResponse.toString()
        } else {

            val failError = idempotencyJSONResponse.getString("message")

            transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${payment.cardNumber.toString().takeLast(
                4
            )}\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"$failError\"}"
            return transactionResponse
        }
    }

    fun kms(response: String, signature: String, credId: String): Boolean {
        val decodedBytes = android.util.Base64.decode(credId, DEFAULT)
        val credArray: List<String> = String(decodedBytes).split(":")

        Log.d("PTLib", "decodedCredId ${(String(decodedBytes))}")
        Log.d("PTLib", "credArray[0] ${credArray[0]}")
        Log.d("PTLib", "credArray[0] ${credArray[1]}")
        Log.d("PTLib", "credArray[0] ${credArray[2]}")
        val awsCreds =
            BasicSessionCredentials(credArray[0], credArray[1], credArray[2])

        val kmsClient = AWSKMSClient(awsCreds)
        val decryptRequest = DecryptRequest()

        decryptRequest.encryptionAlgorithm = "RSAES_OAEP_SHA_256"
        decryptRequest.keyId = "c731e986-c849-4534-9367-a004f6ca272c"
        decryptRequest.withCiphertextBlob(
            ByteBuffer.wrap(
                android.util.Base64.decode(response, DEFAULT)
            )
        )

        val decryptResponse = kmsClient.decrypt(decryptRequest)
        val converted = String(decryptResponse.plaintext.array(), charset("UTF-8"))
        val convertedJSONResponse = JSONObject(converted)
        val payment = convertedJSONResponse.getJSONObject("payment")

        idempotency = convertedJSONResponse.getString("idempotency")
        token = convertedJSONResponse.getString("token")
        merchantId = payment.getString("merchant")
        this.payment.currency = payment.getString("currency")
        this.payment.amount = payment.getString("amount").toInt()
        this.payment.convenienceFee = payment.getString("service_fee")

        Log.d("PTLib", "decryptResponse.plaintext $converted")
        Log.d("PTLib", "decryptResponse $decryptResponse")

        val signatureBuff3 =
            (ByteBuffer.wrap(android.util.Base64.decode(signature, DEFAULT)))
        val responseBuff3 = (ByteBuffer.wrap(android.util.Base64.decode(response, DEFAULT)))

        val verifyRequest = VerifyRequest()

        verifyRequest.keyId = "9c25fd5d-fd5e-4f02-83ce-a981f1824c4f" //hard coded
        verifyRequest.signature = signatureBuff3 // 64 to bytebuffer
        verifyRequest.messageType = "RAW"
        verifyRequest.message = responseBuff3 //idempotency response // 64 to bytebuffer
        verifyRequest.signingAlgorithm = "ECDSA_SHA_384"

        val verifiedResponse = kmsClient.verify(verifyRequest)
        Log.d("Pay Theory", "verifiedResponse $verifiedResponse")
        return if (verifiedResponse.isSignatureValid) {
            kmsResult = true
            Log.d("Pay Theory", "Verified Response is True")
            kmsResult
        } else {
            kmsResult = false
            Log.d("Pay Theory", "Verified Response is False")
            kmsResult
        }


    }

    fun confirmAlert(
        paymentAmount: Int,
        convenienceFee: String,
        cardBrand: String,
        cardNumber: Long,
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
        builder.setPositiveButton("YES") { dialog, which ->
            userConfirmation = true
            dialog.dismiss()
            Log.d(
                "PTLib",
                "User Confirmed Yes - $paymentAmount, $convenienceFee, $cardBrand, $cardNumber"
            )
        }
        builder.setNegativeButton("NO") { dialog, which ->
            userConfirmation = false
            cancel()
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
    }

    fun cancel(): String {
        Log.d("PTLib", "Cancel complete")
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage("Cancelled transaction")
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
        return "Payment Cancelled"
    }

    fun transact(token: String, merchantId: String, currency: String, idempotency: String): String {
        val identityJsonObject = JSONObject()
        val personalAddressJsonObject = JSONObject()
        val entityJSONObject = JSONObject()
        try {
            if (buyerOptions != null) {
                if (!buyerOptions.phoneNumber.isNullOrBlank()) {
                    entityJSONObject.put("phone", buyerOptions.phoneNumber)
                }
                if (!buyerOptions.firstName.isNullOrBlank()) {
                    entityJSONObject.put("first_name", buyerOptions.firstName)
                }
                if (!buyerOptions.lastName.isNullOrBlank()) {
                    entityJSONObject.put("last_name", buyerOptions.lastName)
                }
                if (!buyerOptions.email.isNullOrBlank()) {
                    entityJSONObject.put("email", buyerOptions.email)
                }
                if (!buyerOptions.addressOne.isNullOrBlank()) {
                    personalAddressJsonObject.put("line1", buyerOptions.addressOne)
                }
                if (!buyerOptions.zipCode.isNullOrBlank()) {
                    personalAddressJsonObject.put("postal_code", buyerOptions.zipCode)
                }
                if (!buyerOptions.addressTwo.isNullOrBlank()) {
                    personalAddressJsonObject.put("line2", buyerOptions.addressTwo)
                }
                if (!buyerOptions.city.isNullOrBlank()) {
                    personalAddressJsonObject.put("city", buyerOptions.city)
                }
                if (!buyerOptions.country.isNullOrBlank()) {
                    personalAddressJsonObject.put("country", buyerOptions.country)
                }
                if (!buyerOptions.state.isNullOrBlank()) {
                    personalAddressJsonObject.put("region", buyerOptions.state)
                }
            }
            if (!idempotency.isNullOrBlank()) {
                val identityTagsJsonObject = JSONObject()
                identityTagsJsonObject.put("key", "pt-platform:android $idempotency")
                entityJSONObject.put("entity", personalAddressJsonObject)
                identityJsonObject.put("tags", identityTagsJsonObject)
                identityJsonObject.put("entity", entityJSONObject)
            }
            if (!payment.tagsKey.isNullOrBlank() && !payment.tagsValue.isNullOrBlank()) {
                val tagsJson = JSONObject()
                tagsJson.put(payment.tagsKey, payment.tagsValue.toString())
                identityJsonObject.put("tags", tagsJson)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val identityBody = identityJsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.d("Pay Theory", "Identity Call JSON body : $identityJsonObject")


        val authRequest = Request.Builder()
            .method("POST", identityBody)
            .header(
                "Authorization",
                "Basic $token"
            )
            .url("https://finix.sandbox-payments-api.com/identities")
            .build()


        val identityResponse = client.newCall(authRequest).execute()


        val identityJsonData: String? = identityResponse.body?.string()
        val identityJsonResponse = JSONObject(identityJsonData)
        Log.d("Pay Theory", "Identity Call Response: $identityJsonResponse")

        //TODO - Set up check if authorization response throws back an error
        if (!identityJsonResponse.getString("id").isNullOrBlank()) {

            val addressJsonObject = JSONObject()
            addressJsonObject.put("city", "${payment.cardCity}")
            addressJsonObject.put("region", "${payment.cardState}")
            addressJsonObject.put("postal_code", "${payment.cardZip}")
            addressJsonObject.put("line1", "${payment.cardAddressOne}")
            addressJsonObject.put("line2", "${payment.cardAddressTwo}")

            val paymentJsonObject = JSONObject()
            try {
                paymentJsonObject.put(
                    "identity",
                    identityJsonResponse.getString("id")
                )
                if (!payment.cardFirstName.isNullOrBlank()) {
                    paymentJsonObject.put(
                        "name",
                        "${payment.cardFirstName} ${payment.cardLastName}"
                    )
                }
                if (!payment.cardAddressOne.isNullOrBlank() || !payment.cardAddressTwo.isNullOrBlank()) {
                    paymentJsonObject.put("address", addressJsonObject)
                }

                paymentJsonObject.put("expiration_month", "${payment.cardExpMon}")
                paymentJsonObject.put("expiration_year", "${payment.cardExpYear}")
                paymentJsonObject.put("security_code", "${payment.cardCvv}")
                paymentJsonObject.put("number", "${payment.cardNumber}")
                paymentJsonObject.put("type", "PAYMENT_CARD")
                if (!payment.tagsKey.isNullOrBlank() && !payment.tagsValue.isNullOrBlank()) {
                    val tagsJson = JSONObject()
                    tagsJson.put(payment.tagsKey, payment.tagsValue.toString())
                    paymentJsonObject.put("tags", tagsJson)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val paymentBody = paymentJsonObject.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            Log.d("Pay Theory", "Payment Card Call JSON body : $paymentJsonObject")


            val request = Request.Builder()
                .method("POST", paymentBody)
                .header(
                    "Authorization",
                    "Basic $token"
                )
                .url("https://finix.sandbox-payments-api.com/payment_instruments")
                .build()
            val response = client.newCall(request).execute()
            val paymentJsonData: String? = response.body?.string()
            val paymentJsonResponse = JSONObject(paymentJsonData)

            Log.d("Pay Theory", "Payment Call Response: $paymentJsonResponse")
            if (!paymentJsonResponse.has("_embedded"))
            {
                val authJsonObject = JSONObject()
                try {
                    authJsonObject.put(
                        "source",
                        paymentJsonResponse.getString("id")
                    )
                    authJsonObject.put("merchant_identity", merchantId)
                    authJsonObject.put("amount", payment.amount)
                    authJsonObject.put("currency", currency)
                    if (!payment.tagsKey.isNullOrBlank() && !payment.tagsValue.isNullOrBlank()) {
                        val tagsJson = JSONObject()
                        tagsJson.put(payment.tagsKey, payment.tagsValue.toString())
                        authJsonObject.put("tags", tagsJson)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                val authBody = authJsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                Log.d("Pay Theory", "Authorization Call JSON body : $authJsonObject")

                val request = Request.Builder()
                    .method("POST", authBody)
                    .header(
                        "Authorization",
                        "Basic $token"
                    )
                    .url("https://finix.sandbox-payments-api.com/authorizations")
                    .build()

                val authResponse = client.newCall(request).execute()
                val jsonData: String? = authResponse.body?.string()
                val authJSONResponse = JSONObject(jsonData)
                Log.d("Pay Theory", "Authorization Call Response: $authJSONResponse")

                if (authJSONResponse.getString("state") == "SUCCEEDED") {
                    Log.d("Pay Theory", "Request Succeeded")

                    val capAuthJsonObject = JSONObject()
                    try {
                        capAuthJsonObject.put("capture_amount", payment.amount)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    val capAuthBody =
                        capAuthJsonObject.toString()
                            .toRequestBody("application/json; charset=utf-8".toMediaType())
                    Log.d("Pay Theory", "JSON body : $capAuthJsonObject")

                    val request = Request.Builder()
                        .method("PUT", capAuthBody)
                        .header(
                            "Authorization",
                            "Basic $token"
                        )
                        .url(
                            "https://finix.sandbox-payments-api.com/authorizations/${
                                authJSONResponse.getString(
                                    "id"
                                )
                            }"
                        )
                        .build()
                    val capAuthResponse = client.newCall(request).execute()
                    val jsonData: String? = capAuthResponse.body?.string()
                    val capAuthJSONResponse = JSONObject(jsonData)

                    transactionState = capAuthJSONResponse.getString("state")

                    if (transactionState == "SUCCEEDED") {

                        Log.d("Pay Theory", "Request Succeeded")
                        Log.d(
                            "Pay Theory",
                            "Authorization Capture Body: $capAuthJSONResponse"
                        )
                        transactionResponse =
                            "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                                payment.cardNumber.toString().takeLast(
                                    4
                                )
                            }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"created_at\":\"${
                                capAuthJSONResponse.getString(
                                    "created_at"
                                )
                            }\", \"amount\": ${payment.amount}, \"convenience_fee\": ${payment.convenienceFee}, \"state\":\"${transactionState}\", \"tags\":{ \"pay-theory-environment\":\":\"test\",\"pt-number\":\"pt-env-XXXXXX\""

                        if (!payment.tagsKey.isNullOrBlank() && !payment.tagsValue.isNullOrBlank()) {
                            transactionResponse += ", \"${payment.tagsKey.toString()}\": \"${payment.tagsValue.toString()}\" }"
                        } else {
                            transactionResponse += "} }"
                        }


                        return transactionResponse
                    } else {
                        Log.d("Pay Theory", "Capture Authorization Request Failed / Payment Request Failed")
                        val embedded = capAuthJSONResponse.getJSONObject("_embedded")
                        val error = embedded.getJSONArray("errors")
                        val errorJson = error.getJSONObject(0)
                        val failError = errorJson.getString("message")

                        transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                            payment.cardNumber.toString().takeLast(
                                4
                            )
                        }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"$failError\"}"
                        return transactionResponse
                    }
                } else {
                    Log.d("Pay Theory", "Create Authorization Request Failed  / Payment Request Failed")
                    val embedded = authJSONResponse.getJSONObject("_embedded")
                    val error = embedded.getJSONArray("errors")
                    val errorJson = error.getJSONObject(0)
                    val failError = errorJson.getString("message")

                    transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                        payment.cardNumber.toString().takeLast(
                            4
                        )
                    }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"$failError\"}"
                    return transactionResponse
                }
            } else {
                Log.d("Pay Theory", "Payment Instrument Request Failed / Payment Request Failed")
                val embedded = paymentJsonResponse.getJSONObject("_embedded")
                val error = embedded.getJSONArray("errors")
                val errorJson = error.getJSONObject(0)
                val failError = errorJson.getString("message")

                transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
                    payment.cardNumber.toString().takeLast(
                        4
                    )
                }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"$failError\"}"
                return transactionResponse
            }
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



