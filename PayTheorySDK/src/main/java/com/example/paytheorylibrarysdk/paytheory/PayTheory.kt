package com.example.paytheorylibrarysdk

import android.content.Context
import android.os.Looper
import android.util.Base64.DEFAULT
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.model.VerifyRequest
import com.example.paytheorylibrarysdk.paytheory.BuyerOptions
import com.example.paytheorylibrarysdk.paytheory.CardPayment
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
 * Pay Theory Class
 *
 * This class takes all the details required for initiating a payment
 * @property context the context of the activity
 * @property apiKey the API key required to make the call to PayTheory
 * @property paymentAmount the payment amount for the transaction in USD cents
 * @property cardNumber the card number used for the transaction
 * @property cvv the cvv of the card used for the transaction
 * @property expirationMonth the expiration month of the card used for the transaction
 * @constructor creates a PayTheory Object that will be used during initialization of the transaction
 */
class PayTheory(
    private val context: Context,
    private val apiKey: String,
    private val cardPayment: CardPayment,
    private val buyerOptions: BuyerOptions?
) {
    private var client = OkHttpClient()
    private var challengeResponse = ""
    private var safetyNetResult = ""
    private var googleApiAvailability = false
    private var attestationResponse: String? = null
    private var idempotencyResponse: String? = null
    private var idempotencyResponseString: String = ""
    private var idempotencySignatureString: String = ""
    private var idempotencyCredIdString: String = ""
    private var kmsResult: Boolean = false
    private var token = ""
    private var idempotency = ""
    private var merchantId = ""

    fun init(){
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
                    kms(idempotencyResponseString, idempotencySignatureString, idempotencyCredIdString)
                }.await()

                Log.e("PT2", "Printing KMS Result $kmsResult")
            }
            else {
                Log.e("PT2", "Validation Failed")
            }

            if (kmsResult){
                confirmAlert(cardPayment.amount,
                    cardPayment.convenienceFee,
                    "visa", //TODO
                    cardPayment.cardNumber.toLong(),
                    context)
            }
        }
    }


    private fun challenge(): String {
        val request = Request.Builder()
            .url("https://dev.tags.api.paytheorystudy.com/challenge")
            .header("X-API-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
            .build()

        val response = client.newCall(request).execute()
        val jsonData: String? = response.body?.string()
        Log.e("PTLib", "Challenge response body ${jsonData}")
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

    private fun googleApi(): Boolean {
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




    private suspend fun attestation(nonce: String): String? {

        Looper.prepare()
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


    private fun payTheoryIdempotency(nonce: String, attestation: String?): String {
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
        return if(idempotencyJSONResponse.has("response") && idempotencyJSONResponse.has("signature") && idempotencyJSONResponse.has("credId")){
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


    private fun kms(response: String, signature: String, credId: String): Boolean {
//        val decodedBytes = Base64.getDecoder().decode(credId)

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
//                        Base64.getDecoder().decode(response)
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
        cardPayment.convenienceFee = payment.getString("convenience_fee")


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


    /**
     * confirmAlert() - Method called to allow user to confirm or cancel transaction initialization
     */
    private fun confirmAlert(
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
            dialog.dismiss()
            Log.e(
                "PTLib",
                "User Confirmed Yes - $paymentAmount, $convenienceFee, $cardBrand, $cardNumber"
            )
            transact(token, merchantId, cardPayment.currency, idempotency)


        }
        builder.setNegativeButton("NO") { dialog, which ->
            cancel()
            dialog.dismiss()

        }
        val alert = builder.create()
        alert.show()

    }


    /**
     * cancel()
     */
    private fun cancel(): String {
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


    /**
     * transact() - Method called to complete transaction
     */
    private fun transact(token: String, merchantId: String, currency: String, idempotency: String) {


        //Creating json Object to pass into finix API
        val identityJsonObject = JSONObject()
        val personalAddressJsonObject = JSONObject()
        val entityJSONObject = JSONObject()
        try {
            if (buyerOptions != null) {
                if (buyerOptions.phoneNumber != null) {
                    entityJSONObject.put("phone", "$buyerOptions.phoneNumber")
                }
                if (buyerOptions.firstName != null) {
                    entityJSONObject.put("first_name", "$buyerOptions.firstName")
                }
                if (buyerOptions.lastName != null) {
                    entityJSONObject.put("last_name", "$buyerOptions.lastName")
                }
                if (buyerOptions.email != null) {
                    entityJSONObject.put("email", "$buyerOptions.emailAddress")
                }
                if (buyerOptions.addressOne != null) {
                    personalAddressJsonObject.put("line1", "$buyerOptions.addressOne")
                }
                if (buyerOptions.zipCode != null) {
                    personalAddressJsonObject.put("postal_code", "$buyerOptions.zipCode")
                }
                if (buyerOptions.addressTwo != null) {
                    personalAddressJsonObject.put("line2", "$buyerOptions.addressTwo")
                }
                if (buyerOptions.city != null) {
                    personalAddressJsonObject.put("city", "$buyerOptions.city")
                }
                if (buyerOptions.country != null) {
                    personalAddressJsonObject.put("country", "$buyerOptions.country")
                }
                if (buyerOptions.state != null) {
                    personalAddressJsonObject.put("region", "$buyerOptions.state")
                }
            }
            if (idempotency != null) {
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

            val paymentJsonObject = JSONObject()
            try {
                paymentJsonObject.put(
                    "identity",
                    "${identityJsonResponse.getString("id")}"
                )
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
            if (paymentJsonResponse.getString("id") != null) {
                val amountDouble = (cardPayment.amount.toDouble())
                val amountInCents = amountDouble / .01


                val authJsonObject = JSONObject()
                try {
                    authJsonObject.put(
                        "source",
                        "${paymentJsonResponse.getString("id")}"
                    )
                    authJsonObject.put("merchant_identity", merchantId)
                    authJsonObject.put("amount", amountInCents)
                    authJsonObject.put("currency", currency)

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
                    val amountDouble = (cardPayment.amount.toDouble())
                    val amountInCents = 100 * amountDouble

                    val capAuthJsonObject = JSONObject()
                    try {
                        capAuthJsonObject.put("capture_amount", amountInCents)

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
//                            val status = capAuthJSONResponse.getString("state")
//                            val paymentAmount = (capAuthJSONResponse.getString("amount")
//                                .toDouble()) / 100
                        val status = capAuthJSONResponse.getString("state")




                        //TODO
//                            printToMain(capAuthJSONResponse, context)
                    } else {
                        Log.e("PT2", "Capture Authorization Request Failed")
                    }
                } else {
                    Log.e("PT2", "Create Authorization Request Failed")
                }
            } else {
                Log.e("PT2", "Payment Call Request Failed")
            }
        } else {
            Log.e("PT2", "Identity Call Request Failed")
        }
//                        return "No Results"

//        setNewText("Payment Complete:\nPayment Amount: $$paymentAmount", resultView)
//        makeToast(paymentAmount.toDouble(), "Complete", context)

    }





//    private fun attestation(nonce: String): String? {
//
//                Looper.prepare()
//                SafetyNet.getClient(context).attest(
//                nonce.toByteArray(),
//                "AIzaSyCtRWLrt0I67VhmJV3cue-18ENmxZ8MXGo"
//        )
//                .addOnSuccessListener(Activity()) {
//                    // Indicates communication with the service was successful. Use response.getJwsResult() to get the result data.
//                    val response = it.jwsResult
//
//                    Log.e("PTLib", "SafetyNet Response: $response")
//                    attestationResponse = response
////                        Log.e("PTLib", "Attestation Response Status: ${payTheoryAttest(response)}")
//                }
//                .addOnFailureListener(Activity()) { e ->
//                    // An error occurred while communicating with the service.
//                    if (e is ApiException) {
//                        // An error with the Google Play services API contains some additional details.
//                        val apiException = e as ApiException
//                        Log.e("PTLib", "Api Exception Error: $apiException")
//
//                        // You can retrieve the status code using the apiException.statusCode property.
//                    } else {
//                        // A different, unknown type of error occurred.
//                        Log.e("PTLib", "Unknown Error: " + e.message)
//                    }
//                }
//                Looper.loop()
//
//        return attestationResponse
//    }

//        SafetyNet.getClient(context).attest(
//                nonce.toByteArray(),
//                "AIzaSyCtRWLrt0I67VhmJV3cue-18ENmxZ8MXGo"
//        )   //TODO - remove api key and hide it
//                .addOnSuccessListener(Activity()) {
//                    // Indicates communication with the service was successful. Use response.getJwsResult() to get the result data.
//                    val response = it.jwsResult
//
//                    Log.e("PTLib", "SafetyNet Response: $response")
//                    attestationResponse = response
////                        Log.e("PTLib", "Attestation Response Status: ${payTheoryAttest(response)}")
//                    //TODO - Send the JWS object back to your server for validation and use using a secure connection.
//
//                }
//                .addOnFailureListener(Activity()) { e ->
//                    // An error occurred while communicating with the service.
//                    if (e is ApiException) {
//                        // An error with the Google Play services API contains some additional details.
//                        val apiException = e as ApiException
//                        Log.e("PTLib", "Api Exception Error: $apiException")
//
//                        // You can retrieve the status code using the apiException.statusCode property.
//                    } else {
//                        // A different, unknown type of error occurred.
//                        Log.e("PTLib", "Unknown Error: " + e.message)
//
//                    }
//
//                }


//    /**
//     * authentication() - Method called to start authentication
//     */
//    private fun authentication(nonce: String) {
//        //Call google play services to verify google play is available
//        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
//            Log.e("PT2", "Google Play Service Available.")
//            runBlocking {
//                var safetyNetTask = SafetyNet.getClient(context).attest(
//                        nonce.toByteArray(),
//                        "AIzaSyCtRWLrt0I67VhmJV3cue-18ENmxZ8MXGo"
//                )
//
//            }//TODO - remove api key and hide it
//
//
////                    .addOnSuccessListener(Activity()) {
////                        // Indicates communication with the service was successful. Use response.getJwsResult() to get the result data.
////                        val response = it.jwsResult
////                        Log.e("PTLib", "SafetyNet Response: $response")
////                        //TODO - Send the JWS object back to your server for validation and use using a secure connection.
//////                        payTheoryIdempotency(nonce, response)
////                    }
////                    .addOnFailureListener(Activity()) { e ->
////                        // An error occurred while communicating with the service.
////                        if (e is ApiException) {
////                            // An error with the Google Play services API contains some additional details.
////                            val apiException = e as ApiException
////                            Log.e("PTLib", "Api Exception Error: $apiException")
////                            // You can retrieve the status code using the apiException.statusCode property.
////                        } else {
////                            // A different, unknown type of error occurred.
////                            Log.e("PTLib", "Unknown Error: " + e.message)
////                        }
////                    }
//        } else {
//            // Prompt user to update Google Play services.
//            Log.e("PT2", "Update Google Play services.")
//        }
//    }
}