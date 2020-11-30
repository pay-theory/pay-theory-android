package com.paytheory.paytheorylibrarysdk

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
import com.paytheory.paytheorylibrarysdk.paytheory.BuyerOptions
import com.paytheory.paytheorylibrarysdk.paytheory.CardPayment
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


class PayTheory(
    private val context: Context,
    val apiKey: String,
    private val cardPayment: CardPayment,
    private val buyerOptions: BuyerOptions? = null
) {
    var client = OkHttpClient()
    var challengeResponse = ""
//    var safetyNetResult = "" TODO
    var googleApiAvailability = false
    var attestationResponse: String? = null
    var idempotencyResponse: String? = null
    var idempotencyResponseString: String = ""
    var idempotencySignatureString: String = ""
    var idempotencyCredIdString: String = ""
    var kmsResult: Boolean = false
    var token = ""
    var idempotency = ""
    var merchantId = ""

    var userConfirmation: Boolean? = null
    var payTheoryTransactResponse = ""

    suspend fun init(): String {
        Log.e("PT2", "Init PT2")
        CoroutineScope(IO).launch {

            //Challenge Api
            val challengeResult = async{
                challenge()
            }.await()
            Log.e("PT2", "Printing challenge $challengeResult")


            //Google Api
            val googleApiResult = async{
                googleApi()
            }.await()
            Log.e("PT2", "Printing Google Api Result $googleApiResult")


            //Attestation Api
            val attestationResponse = async{
                attestation(challengeResult)
            }.await()
            if(attestationResponse == null ){
                Log.e("PT2", "Attestation failed")
            }
            else{
                Log.e("PT2", "Printing Attestation Result $attestationResponse")
            }



            if (!attestationResponse.isNullOrEmpty() || googleApiAvailability || !challengeResponse.isNullOrEmpty()){
                //Pay Theory Idempotency
                val payTheoryIdempotencyResponse = async{
                    payTheoryIdempotency(challengeResponse, attestationResponse)
                }.await()

                Log.e("PT2", "Printing Pay Theory Idempotency Result $payTheoryIdempotencyResponse")

                //KMS
                val kmsResult = async{
                    kms(
                        idempotencyResponseString,
                        idempotencySignatureString,
                        idempotencyCredIdString
                    )
                }.await()

                Log.e("PT2", "Printing KMS Result $kmsResult")
            } else {
                Log.e("PT2", "Validation Failed")
            }

            if (kmsResult){
                Handler(Looper.getMainLooper()).post {
                    confirmAlert(
                        cardPayment.amount,
                        cardPayment.convenienceFee,
                        getCardType(cardPayment.cardNumber.toString()),
                        cardPayment.cardNumber,
                        context
                    )
                }

            } else{
                Log.e("PT2", "Verification failed")
            }

            while (userConfirmation == null) {
                delay(500)
            }
            if (userConfirmation == true){
                payTheoryTransactResponse = async{
                    transact(token, merchantId, cardPayment.currency, idempotency)
                }.await()
                Log.e("PT2", "payTheoryTransactResponse : $payTheoryTransactResponse")

            } else {
                Log.e("PT2", "User Confirmation : $userConfirmation")
            }
        }
        while (payTheoryTransactResponse == ""){
            delay(5000)
        }
        Log.e(
            "PT2",
            "payTheoryTransactResponse returned back to function call : $payTheoryTransactResponse"
        )
        return payTheoryTransactResponse
    }

    fun challenge(): String {
        val request = Request.Builder()
            .url("https://dev.tags.api.paytheorystudy.com/challenge")
            .header("X-API-Key", apiKey)
            .build()

        val response = client.newCall(request).execute()
        val jsonData: String? = response.body?.string()
        Log.e("PTLib", "Challenge response body $jsonData")
        val challengeJSONResponse = JSONObject(jsonData)
        return if(challengeJSONResponse.has("challenge")){
            val challengeResponseString = challengeJSONResponse.getString("challenge")
            challengeResponse = challengeJSONResponse.getString("challenge")
            Log.e("PTLib", "Challenge response $challengeResponseString")
            challengeResponseString
        }
        else {
            challengeJSONResponse.getString("message")
        }


    }

    fun googleApi(): Boolean {
        //Call google play services to verify google play is available
        return if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            Log.e("PT2", "Google Play Service Available.")
            googleApiAvailability = true
            true
        } else {
            googleApiAvailability = false
            false
        }
    }

    suspend fun attestation(nonce: String): String? {

        if (Looper.myLooper()==null){
            Looper.prepare()
        }

        var attestationTask = SafetyNet.getClient(context).attest(
            nonce.toByteArray(),
            "AIzaSyCtRWLrt0I67VhmJV3cue-18ENmxZ8MXGo"
        )
        while(!attestationTask.isComplete){
            delay(3000)
        }
        attestationResponse = attestationTask.result.jwsResult
        return attestationTask.result.jwsResult
    }

    fun payTheoryIdempotency(nonce: String, attestation: String?): String {
        val jsonObject = JSONObject()
        jsonObject.put("currency", "USD")
        jsonObject.put("amount", cardPayment.amount)
        jsonObject.put("type", "android")
        try {
            if (nonce != null) {
                jsonObject.put("nonce", "$nonce")
            }
            if (attestation != null) {
                jsonObject.put("attestation", "$attestation")
            }
            if (!cardPayment.feeMode.isNullOrBlank()) {
                jsonObject.put("fee_mode", "${cardPayment.feeMode}")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.e("PT2", "payTheoryIdempotency JSON Body: $jsonObject")

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .method("POST", body)
            .url("https://dev.tags.api.paytheorystudy.com/idempotency")
            .addHeader("x-api-key", apiKey)
            .build()

        val response = client.newCall(request).execute()
        val jsonData: String? = response.body?.string()
        Log.e("PTLib", "Idempotency response body ${jsonData}")
        val idempotencyJSONResponse = JSONObject(jsonData)
        return if(idempotencyJSONResponse.has("response") && idempotencyJSONResponse.has("signature") && idempotencyJSONResponse.has(
                "credId"
            )){
            idempotencyResponse = idempotencyJSONResponse.toString()
            idempotencyResponseString = idempotencyJSONResponse.getString("response")
            idempotencySignatureString = idempotencyJSONResponse.getString("signature")
            idempotencyCredIdString = idempotencyJSONResponse.getString("credId")

            Log.e("PTLib", "Idempotency response $idempotencyResponse")
            idempotencyResponse.toString()
        }
        else {
            idempotencyJSONResponse.getString("message")
            return ""
        }
    }

    fun kms(response: String, signature: String, credId: String): Boolean {
        val decodedBytes = android.util.Base64.decode(credId, DEFAULT)
        val credArray: List<String> = String(decodedBytes).split(":")

        Log.e("PTLib", "decodedCredId ${(String(decodedBytes))}")
        Log.e("PTLib", "credArray[0] ${credArray[0]}")
        Log.e("PTLib", "credArray[0] ${credArray[1]}")
        Log.e("PTLib", "credArray[0] ${credArray[2]}")
        var awsCreds =
            BasicSessionCredentials(credArray[0], credArray[1], credArray[2])

        var kmsClient = AWSKMSClient(awsCreds)
        var decryptRequest = DecryptRequest()

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
        cardPayment.currency = payment.getString("currency")
        cardPayment.amount = payment.getString("amount").toInt()
        cardPayment.convenienceFee = payment.getString("service_fee")

        Log.e("PTLib", "decryptResponse.plaintext $converted")
        Log.e("PTLib", "decryptResponse $decryptResponse")

        val signatureBuff3 =
            (ByteBuffer.wrap(android.util.Base64.decode(signature, DEFAULT)))
        val responseBuff3 = (ByteBuffer.wrap(android.util.Base64.decode(response, DEFAULT)))

        var verifyRequest = VerifyRequest()

        verifyRequest.keyId = "9c25fd5d-fd5e-4f02-83ce-a981f1824c4f" //hard coded
        verifyRequest.signature = signatureBuff3 // 64 to bytebuffer
        verifyRequest.messageType = "RAW"
        verifyRequest.message =
            responseBuff3 //idempotency response // 64 to bytebuffer
        verifyRequest.signingAlgorithm = "ECDSA_SHA_384"

        val verifiedResponse = kmsClient.verify(verifyRequest)
        Log.e("PT2", "verifiedResponse $verifiedResponse")
        return if (verifiedResponse.isSignatureValid){
            kmsResult = true
            Log.e("PT2", "Verified Response is True")
            kmsResult
        }
        else{
            kmsResult = false
            Log.e("PT2", "Verified Response is False")
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
            "Are you sure you want to make a payment of $${paymentAmount.toDouble() / 100}, including a fee of $${convenienceFee.toDouble() / 100} on $cardBrand card beginning with ${
                cardNumber.toString().take(6)
            }"
        )
        builder.setPositiveButton("YES") { dialog, which ->
            userConfirmation = true
            dialog.dismiss()
            Log.e(
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
        Log.e("PTLib", "Cancel complete")
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
                if (buyerOptions.phoneNumber !== null) {
                    entityJSONObject.put("phone", "${buyerOptions.phoneNumber}")
                }
                if (buyerOptions.firstName !== null) {
                    entityJSONObject.put("first_name", "${buyerOptions.firstName}")
                }
                if (buyerOptions.lastName !== null) {
                    entityJSONObject.put("last_name", "${buyerOptions.lastName}")
                }
                if (buyerOptions.email !== null) {
                    entityJSONObject.put("email", "${buyerOptions.email}")
                }
                if (buyerOptions.addressOne !== null) {
                    personalAddressJsonObject.put("line1", "${buyerOptions.addressOne}")
                }
                if (buyerOptions.zipCode !== null) {
                    personalAddressJsonObject.put("postal_code", "${buyerOptions.zipCode}")
                }
                if (buyerOptions.addressTwo !== null) {
                    personalAddressJsonObject.put("line2", "${buyerOptions.addressTwo}")
                }
                if (buyerOptions.city !== null) {
                    personalAddressJsonObject.put("city", "${buyerOptions.city}")
                }
                if (buyerOptions.country !== null) {
                    personalAddressJsonObject.put("country", "${buyerOptions.country}")
                }
                if (buyerOptions.state !== null) {
                    personalAddressJsonObject.put("region", "${buyerOptions.state}")
                }
            }
            if (idempotency !== null) {
                val identityTagsJsonObject = JSONObject()
                identityTagsJsonObject.put("key", "pt-platform:android $idempotency")
                entityJSONObject.put("entity", personalAddressJsonObject)

//            identityJsonObject.put("tags", idempotency)
                identityJsonObject.put("tags", identityTagsJsonObject)
                identityJsonObject.put("entity", entityJSONObject)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val identityBody = identityJsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.e("PT2", "Identity Call JSON body : $identityJsonObject")



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
        Log.e("PT2", "Identity Call Response: $identityJsonResponse")

        //TODO - Set up check if authorization response throws back an error
        if (identityJsonResponse.getString("id") != null) {

            val addressJsonObject = JSONObject()
            addressJsonObject.put("city", "${cardPayment.cardCity}")
            addressJsonObject.put("region", "${cardPayment.cardState}")
            addressJsonObject.put("postal_code", "${cardPayment.cardZip}")
            addressJsonObject.put("line1", "${cardPayment.cardAddressOne}")
            addressJsonObject.put("line2", "${cardPayment.cardAddressTwo}")

            val paymentJsonObject = JSONObject()
            try {
                paymentJsonObject.put(
                    "identity",
                    "${identityJsonResponse.getString("id")}"
                )
                if (!cardPayment.cardFirstName.isNullOrBlank()){
                    paymentJsonObject.put(
                        "name",
                        "${cardPayment.cardFirstName} ${cardPayment.cardLastName}"
                    )
                }
                if (!cardPayment.cardAddressOne.isNullOrBlank() || !cardPayment.cardAddressTwo.isNullOrBlank()){
                    paymentJsonObject.put("address", addressJsonObject)
                }

                paymentJsonObject.put("expiration_month", "${cardPayment.cardExpMon}")
                paymentJsonObject.put("expiration_year", "${cardPayment.cardExpYear}")
                paymentJsonObject.put("security_code", "${cardPayment.cardCvv}")
                paymentJsonObject.put("number", "${cardPayment.cardNumber}")
                paymentJsonObject.put("type", "PAYMENT_CARD")

            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val paymentBody = paymentJsonObject.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            Log.e("PT2", "Payment Card Call JSON body : $paymentJsonObject")


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

            Log.e("PT2", "Payment Call Response: $paymentJsonResponse")
           if(!paymentJsonResponse.has("_embedded"))
//            if (paymentJsonResponse.getString("id") != null)
                { //TODO - check if failed
                val authJsonObject = JSONObject()
                try {
                    authJsonObject.put(
                        "source",
                        "${paymentJsonResponse.getString("id")}"
                    )
                    authJsonObject.put("merchant_identity", merchantId)
                    authJsonObject.put("amount", cardPayment.amount)
                    authJsonObject.put("currency", currency)
                    if(!cardPayment.tags.isNullOrBlank()){
                        val tagsJson = JSONObject()
                        tagsJson.put("tags", cardPayment.tags.toString())
                        authJsonObject.put("tags", tagsJson)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                val authBody = authJsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())
                Log.e("PT2", "Authorization Call JSON body : $authJsonObject")

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
                Log.e("PT2", "Authorization Call Response: $authJSONResponse")

                if (authJSONResponse.getString("state") == "SUCCEEDED") {
                    Log.e("PT2", "Request Succeeded")

                    val capAuthJsonObject = JSONObject()
                    try {
                        capAuthJsonObject.put("capture_amount", cardPayment.amount)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    val capAuthBody =
                        capAuthJsonObject.toString()
                            .toRequestBody("application/json; charset=utf-8".toMediaType())
                    Log.e("PT2", "JSON body : $capAuthJsonObject")

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


                    //check if payment was complete or denied
                    if (capAuthJSONResponse.getString("state") == "SUCCEEDED") {

                        Log.e("PT2", "Request Succeeded")
                        Log.e(
                            "PT2",
                            "Authorization Capture Body: $capAuthJSONResponse"
                        )
                        payTheoryTransactResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${cardPayment.cardNumber.toString().takeLast(
                            4
                        )}\", \"brand\":\"${getCardType(cardPayment.cardNumber.toString())}\", \"created_at\":\"${capAuthJSONResponse.getString(
                            "created_at"
                        )}\", \"amount\": ${cardPayment.amount}, \"convenience_fee\": ${cardPayment.convenienceFee}, \"state\":\"${capAuthJSONResponse.getString(
                            "state"
                        )}\", \"tags\":{ \"pay-theory-environment\":\":\"test\",\"pt-number\":\"pt-env-XXXXXX\", \"YOUR_TAG_KEY\": \"${cardPayment.tags.toString()}\" }"
                        return payTheoryTransactResponse
                    } else {
                        Log.e("PT2", "Capture Authorization Request Failed")
                    }
                } else {
                    Log.e("PT2", "Create Authorization Request Failed")
                }
            } else {
               var embedded = paymentJsonResponse.getJSONObject("_embedded")
               var error = embedded.getJSONArray("errors")
               var errorJson = error.getJSONObject(0)
               var messageString = errorJson.getString("message")
               payTheoryTransactResponse = messageString
               Log.e("PT2", "Payment Call Request Failed")

               return payTheoryTransactResponse

            }
        } else {
            Log.e("PT2", "Identity Call Request Failed")
        }
        return payTheoryTransactResponse
    }


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
}



