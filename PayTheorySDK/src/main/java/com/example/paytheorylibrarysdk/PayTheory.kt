package com.example.paytheorylibrarysdk


import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.services.kms.AWSKMSClient
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.model.VerifyRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*


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
        private val paymentAmount: Int,
        private val cardNumber: Long,
        private val cvv: Int,
        private val expirationMonth: Int,
        private val expirationYear: Int,
        private val firstName: String?,
        private val lastName: String?,
        private val addressOne: String?,
        private val addressTwo: String?,
        private val phoneNumber: String?,
        private val country: String?,
        private val emailAddress: String?,
        private val city: String?,
        private val zipCode: String,
        private val state: String?,
        private val customTags: String?
) {


    //TODO
    private val cardBrand = "visa"
    private var client = OkHttpClient()
    private var result = ""







    /**
     * initPayment() - Method called from PayTheory Object that initializes the transaction
     */

    //During init call api to get finix credentials
    fun initPayment() {
        Log.e(
                "PTLib", "Init Payment Started: \nContext = $context \nAPI Key = $apiKey" +
                "\nPayment Amount = $paymentAmount\nCard Number = $cardNumber\nCVV = $cvv " +
                "\nExpiration Month = $expirationMonth \nExpiration Year = $expirationYear\n" +
                "First Name = $firstName \nLast Name = $lastName\n" +
                "Address One = $addressOne\n" +
                "Address Two = $addressTwo\n" +
                "Phone Number = $phoneNumber\n" +
                "Country = $country\n" +
                "Email Address = $emailAddress\n" +
                "City = $city\n" +
                "Zip Code = $zipCode \nState = $state \nCustom Tags = $customTags"
        )



        if (validation(this.cardNumber.toString())) {

            challenge()


//            authentication("12345")


//                if (authentication("12345") == ""){
//                    Log.e("PTLib", "authentication failed")
//                }
//                else {
//                    Log.e("PTLib", "authentication complete $authResult")
//                }
//


//            if (kms()) {
//                Log.e("PTLib", "KMS SUCCESS")
//            }
//            else {
//                Log.e("PTLib", "KMS Failed")
//            }
//
//            if (idempotency()) {
//                Log.e("PTLib", "idempotency SUCCESS")
//            }
//            else {
//                Log.e("PTLib", "idempotency Failed")
//            }


//TODO
//            val sig: Signature = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures[0]
//            Log.e("App", "Signature : $sig ,  ${sig.toCharsString()}")
//            val sigs: Array<Signature> = context.packageManager.getPackageInfo(context.packageName, PackageManager.).signatures
//            for (sig in sigs) {
//                Log.e("App", "Signature : " + sig.hashCode())
//            }

        } else {
            Log.e("PTLib", "Validation Failed")
        }
    }


    private fun challenge() {
        val request = Request.Builder()
                .url("https://dev.tags.api.paytheorystudy.com/challenge")
                .header("X-API-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }
                    val jsonData: String? = response.body?.string()
                    val challengeJSONResponse = JSONObject(jsonData)
                    val challengeResponseString = challengeJSONResponse.getString("challenge")
                    Log.e("PTLib", "Challenge JsonResponse $challengeJSONResponse")
                    Log.e("PTLib", "Challenge jsonResponse String: $challengeJSONResponse")
                    Log.e("PTLib", "Challenge Response String: $challengeResponseString")

                    Looper.prepare()
                    authentication(challengeResponseString)
                    Looper.loop()

                }
            }
        })
    }

    /**
     * authentication() - Method called to start authentication
     */
    private fun authentication(nonce: String) {


        //Call google play services to verify google play is available
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            Log.e("PT-Lib", "Google Play Service Available.")

//            var safetyNetResponse = SafetyNet.getClient(context).attest(nonce.toByteArray(), "AIzaSyCtRWLrt0I67VhmJV3cue-18ENmxZ8MXGo")
//
//            Log.e("PTLib", "SafetyNet Response: ${safetyNetResponse.result.jwsResult}")

            SafetyNet.getClient(context).attest(
                    nonce.toByteArray(),
                    "AIzaSyCtRWLrt0I67VhmJV3cue-18ENmxZ8MXGo"
            )   //TODO - remove api key and hide it
                    .addOnSuccessListener(Activity()) {


                        // Indicates communication with the service was successful. Use response.getJwsResult() to get the result data.
                        val response = it.jwsResult

                        Log.e("PTLib", "SafetyNet Response: $response")

//                        Log.e("PTLib", "Attestation Response Status: ${payTheoryAttest(response)}")
                        //TODO - Send the JWS object back to your server for validation and use using a secure connection.
                        payTheoryIdempotency(nonce, response)
                    }
                    .addOnFailureListener(Activity()) { e ->
                        // An error occurred while communicating with the service.
                        if (e is ApiException) {
                            // An error with the Google Play services API contains some additional details.
                            val apiException = e as ApiException
                            Log.e("PTLib", "Api Exception Error: $apiException")

                            // You can retrieve the status code using the apiException.statusCode property.
                        } else {
                            // A different, unknown type of error occurred.
                            Log.e("PTLib", "Unknown Error: " + e.message)

                        }

                    }

        } else {
            // Prompt user to update Google Play services.
            Log.e("PT-Lib", "Update Google Play services.")

        }
        //Create Nonce
//        val nonce = (java.util.UUID.randomUUID().toString() + idempotency + paymentAmount).toByteArray()
//        Log.e("PTLib", "Nonce: $nonce")

    }

    private fun payTheoryIdempotency(nonce: String, attestation: String) {
        val jsonObject = JSONObject()
        jsonObject.put("currency", "USD")
        jsonObject.put("amount", paymentAmount)
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
        Log.e("PTLib", "payTheoryIdempotency JSON Body: $jsonObject")
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonObject.toString().toRequestBody(mediaType)

        val request = Request.Builder()
                .method("POST", body)
                .url("https://dev.tags.api.paytheorystudy.com/idempotency")
                .addHeader("x-api-key", apiKey)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()

            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {

                        throw IOException("Unexpected code $response")
                    } else {

                        for ((name, value) in response.headers) {
                            println("$name: $value")
                        }
                        val jsonData: String? = response.body?.string()
                        val payTheoryIdempotencyJSONResponse = JSONObject(jsonData)
                        Log.e("PTLib", "payTheoryIdempotencyJSONResponse $payTheoryIdempotencyJSONResponse")
                        val signature = payTheoryIdempotencyJSONResponse.getString("signature")
                        val response = payTheoryIdempotencyJSONResponse.getString("response")
                        val credId = payTheoryIdempotencyJSONResponse.getString("credId")


                        val decodedBytes = Base64.getDecoder().decode(credId)

                        val credArray: List<String> = String(decodedBytes).split(":")

                        Log.e("PTLib", "decodedCredId ${(String(decodedBytes))}")
                        Log.e("PTLib", "credArray[0] ${credArray[0]}")
                        Log.e("PTLib", "credArray[0] ${credArray[1]}")
                        Log.e("PTLib", "credArray[0] ${credArray[2]}")
                        var awsCreds = BasicSessionCredentials(credArray[0], credArray[1], credArray[2])

                        var kmsClient = AWSKMSClient(awsCreds)
                        var decryptRequest = DecryptRequest()



                        decryptRequest.encryptionAlgorithm = "RSAES_OAEP_SHA_256"
                        decryptRequest.keyId = "c731e986-c849-4534-9367-a004f6ca272c"
                        decryptRequest.withCiphertextBlob(ByteBuffer.wrap(Base64.getDecoder().decode(response)))

                        val decryptResponse = kmsClient.decrypt(decryptRequest)


                        val converted = String(decryptResponse.plaintext.array(), charset("UTF-8"))
                        val convertedJSONResponse = JSONObject(converted)
                        val idempotency = convertedJSONResponse.getString("idempotency")
                        val token = convertedJSONResponse.getString("token")
                        val payment = convertedJSONResponse.getJSONObject("payment")
                        val currency = payment.getString("currency")
                        val amount = payment.getString("amount")
                        val merchant = payment.getString("merchant")
                        val convenienceFee = payment.getString("convenience_fee")

                        Log.e("PTLib", "decryptResponse.plaintext $converted")
                        Log.e("PTLib", "decryptResponse $decryptResponse")

                        val signatureBuff3 = (ByteBuffer.wrap(Base64.getDecoder().decode(signature)))
                        val responseBuff3 = (ByteBuffer.wrap(Base64.getDecoder().decode(response)))

                        var verifyRequest = VerifyRequest()

                        verifyRequest.keyId = "9c25fd5d-fd5e-4f02-83ce-a981f1824c4f" //hard coded
                        verifyRequest.signature = signatureBuff3 // 64 to bytebuffer
                        verifyRequest.messageType = "RAW"
                        verifyRequest.message = responseBuff3 //idempotency response // 64 to bytebuffer
                        verifyRequest.signingAlgorithm = "ECDSA_SHA_384"

                        val verifiedResponse = kmsClient.verify(verifyRequest)
                        Log.e("PTLib", "verifiedResponse $verifiedResponse")

//                        Log.e(
//                                "PT-Lib",
//                                "Card: First Six - ${
//                                    cardNumber.toString().take(6)
//                                }, Brand - $cardBrand, Receipt Number - $idempotency, Amount - ${(paymentAmount.toDouble() + convenienceFee.toDouble()).toInt()}, Convenience Fee - $convenienceFee"
//                        )

                        Looper.prepare()
                        confirmAlert(amount.toInt(), convenienceFee, cardBrand, cardNumber, context, token, merchant,currency, idempotency)
                        Looper.loop()


                    }
                }
            }
        })

    }


//    private fun kms(): Boolean {
//        var verified = false
//        //            verifier.grantTokens
////
////
////            verifier.
////            val request = verifier.withKeyId("12345")
////            request.toString()
//
//
//        //            val publicKeyRequest = createPublicKeyRequest
////            val publicKeyRequest = createPublicKeyRequest.withKeyId("arn:aws:kms:us-east-1:291752019718:key/b8544a95-c387-460a-86dd-7e93ca3c5407")
//        val thread = Thread {
//            try {
//                val publicKey = kmsClient.getPublicKey(createPublicKeyRequest)
//                Log.e("PTLib", publicKey.toString())
//                verified = true
//            } catch (e: Exception) {
//                Log.e("PTLib", e.message!!)
//                verified = false
//            }
//        }
//        thread.start()
//        return verified
//    }


//    private fun payTheoryAttest(attestation: String) {
//        val jsonObject = JSONObject()
//        jsonObject.put("type", "android")
//        try {
//            if (attestation != null) {
//                jsonObject.put("attestation", "$attestation")
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//        Log.e("PTLib", "Attestation JSON Body: $jsonObject")
//        val mediaType = "application/json; charset=utf-8".toMediaType()
//        val body = jsonObject.toString().toRequestBody(mediaType)
//
//        val request = Request.Builder()
//                .method("POST", body)
//                .url("https://dev.tags.api.paytheorystudy.com/attest")
//                .header("X-API-Key", apiKey)
//                .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//                    val jsonData: String? = response.body?.string()
//
//                    Log.e("PTLib", "Attestation Response Body: $jsonData")
//
//                    val jsonDataResponse = JSONObject(jsonData)
////                    if (jsonDataResponse.getString("success") == "true") {
////
////                    }
//                }
//            }
//        })
//
//    }


    /**
     *
     */


    /**
     * confirmAlert() - Method called to allow user to confirm or cancel transaction initialization
     */
    private fun confirmAlert(
            paymentAmount: Int, convenienceFee: String,
            cardBrand: String, cardNumber: Long, context: Context, token: String, merchantId: String, currency: String, idempotency: String
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

            transact(token, merchantId, currency, idempotency)

        }
        builder.setNegativeButton("NO") { dialog, which ->
            cancel()
            dialog.dismiss()

        }
        val alert = builder.create()
        alert.show()

    }


    /**
     * confirmAlert() - Method called to cancel transaction initialization
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
            if (phoneNumber != null) {
                entityJSONObject.put("phone", "$phoneNumber")
            }
            if (firstName != null) {
                entityJSONObject.put("first_name", "$firstName")
            }
            if (lastName != null) {
                entityJSONObject.put("last_name", "$lastName")
            }
            if (emailAddress != null) {
                entityJSONObject.put("email", "$emailAddress")
            }
            if (addressOne != null) {
                personalAddressJsonObject.put("line1", "$addressOne")
            }
            if (zipCode != null) {
                personalAddressJsonObject.put("postal_code", "$zipCode")
            }
            if (addressTwo != null) {
                personalAddressJsonObject.put("line2", "$addressTwo")
            }
            if (city != null) {
                personalAddressJsonObject.put("city", "$city")
            }
            if (country != null) {
                personalAddressJsonObject.put("country", "$country")
            }

            if (state != null) {
                personalAddressJsonObject.put("region", "$state")
            }
            if (idempotency != null) {
                val identityTagsJsonObject = JSONObject()
                identityTagsJsonObject.put("key", "pt-platform:android $idempotency")
                entityJSONObject.put("entity" , personalAddressJsonObject)

//            identityJsonObject.put("tags", idempotency)
                identityJsonObject.put("tags", identityTagsJsonObject)
                identityJsonObject.put("entity", entityJSONObject)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val identityBody = identityJsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.e("PT-Lib", "Identity Call JSON body : $identityJsonObject")

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
        Log.e("PT-Lib", "Identity Call Response: $identityJsonResponse")


        //TODO - Set up check if authorization response throws back an error
        if (identityJsonResponse.getString("id") != null) {

            val paymentJsonObject = JSONObject()
            try {
                paymentJsonObject.put(
                        "identity",
                        "${identityJsonResponse.getString("id")}"
                )
                paymentJsonObject.put("expiration_month", "$expirationMonth")
                paymentJsonObject.put("expiration_year", "$expirationYear")
                paymentJsonObject.put("security_code", "$cvv")
                paymentJsonObject.put("number", "$cardNumber")
                paymentJsonObject.put("type", "PAYMENT_CARD")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val paymentBody = paymentJsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            Log.e("PT-Lib", "Payment Card Call JSON body : $paymentJsonObject")


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

            Log.e("PT-Lib", "Payment Call Response: $paymentJsonResponse")
            if (paymentJsonResponse.getString("id") != null) {
                val amountDouble = (paymentAmount.toDouble())
                val amountInCents = 100 * amountDouble


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

                val authBody = authJsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                Log.e("PT-Lib", "Authorization Call JSON body : $authJsonObject")

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
                Log.e("PT-Lib", "Authorization Call Response: $authJSONResponse")

                if (authJSONResponse.getString("state") == "SUCCEEDED") {
                    Log.e("PT-Lib", "Request Succeeded")
                    val amountDouble = (paymentAmount.toDouble())
                    val amountInCents = 100 * amountDouble

                    val capAuthJsonObject = JSONObject()
                    try {
                        capAuthJsonObject.put("capture_amount", amountInCents)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    val capAuthBody =
                            capAuthJsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    Log.e("PT-Lib", "JSON body : $capAuthJsonObject")

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
                        Log.e("PT-Lib", "Request Succeeded")
                        Log.e(
                                "PT-Lib",
                                "Authorization Capture Body: $capAuthJSONResponse"
                        )
//                            val status = capAuthJSONResponse.getString("state")
//                            val paymentAmount = (capAuthJSONResponse.getString("amount")
//                                .toDouble()) / 100

                       this.result = capAuthJSONResponse.toString()
//                        setNewText("Payment Complete:\nStatus: $status\nPayment Amount: $$paymentAmount", resultView)
//                        makeToast(paymentAmount, status, context)

                        //TODO
//                            printToMain(capAuthJSONResponse, context)
                    } else {
                        Log.e("PT-Lib", "Capture Authorization Request Failed")
                    }
                } else {
                    Log.e("PT-Lib", "Create Authorization Request Failed")
                }
            } else {
                Log.e("PT-Lib", "Payment Call Request Failed")
            }
        } else {
            Log.e("PT-Lib", "Identity Call Request Failed")
        }
//                        return "No Results"


    }

    /**
     * validation() - Method called to validate the card number
     */
    private fun validation(cardNumber: String): Boolean {
        var checksum: Int = 0
        for (i in cardNumber.length - 1 downTo 0 step 2) {
            checksum += cardNumber[i] - '0'
        }
        for (i in cardNumber.length - 2 downTo 0 step 2) {
            val n: Int = (cardNumber[i] - '0') * 2
            checksum += if (n > 9) n - 9 else n
        }
        return if (checksum % 10 == 0 && cardNumber.isNotEmpty()) {
            Log.e("PTLib", "$cardNumber is a valid credit card number")
            true
        } else {
            Log.e("PTLib", "$cardNumber is an invalid credit card number")
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle("Alert")
            alertDialog.setMessage("$cardNumber is an invalid credit card number")
            alertDialog.setButton(
                    AlertDialog.BUTTON_NEUTRAL, "OK"
            ) { dialog, which -> dialog.dismiss() }
            alertDialog.show()
            false
        }
    }


}





//    fun getResult(): String{
//        val result = this.result
//        return result
//    }

//TODO - CREATE A METHOD TO PRINT TO MAIN ACTIVITY
//private fun printToMain(capAuthJSONResponse: JSONObject, context: Context){
//        Toast.makeText(context,"$capAuthJSONResponse",Toast.LENGTH_SHORT).show();
//}


//    @Throws(AmazonServiceException::class, AmazonClientException::class)
//    fun getPublicKey(getPublicKeyRequest: GetPublicKeyRequest?): GetPublicKeyResult? {
//        val executionContext: ExecutionContext = createExecutionContext(getPublicKeyRequest)
//        val awsRequestMetrics = executionContext.awsRequestMetrics
//        awsRequestMetrics.startEvent(Field.ClientExecuteTime)
//        var request: Request<GetPublicKeyRequest?>? = null
//        var response: Response<GetPublicKeyResult?>? = null
//        return try {
//            awsRequestMetrics.startEvent(Field.RequestMarshallTime)
//            try {
//                request = GetPublicKeyRequestMarshaller().marshall(getPublicKeyRequest)
//                // Binds the request metrics to the current request.
//                request.setAWSRequestMetrics(awsRequestMetrics)
//            } finally {
//                awsRequestMetrics.endEvent(Field.RequestMarshallTime)
//            }
//            val unmarshaller: Unmarshaller<GetPublicKeyResult, JsonUnmarshallerContext> = GetPublicKeyResultJsonUnmarshaller()
//            val responseHandler = JsonResponseHandler<GetPublicKeyResult>(
//                    unmarshaller)
//            response = invoke(request, responseHandler, executionContext)
//            response.getAwsResponse()
//        } finally {
//            awsRequestMetrics.endEvent(Field.ClientExecuteTime)
//            endClientExecution(awsRequestMetrics, request, response, LOGGING_AWS_REQUEST_METRIC)
//        }
//    }


//    fun identity(token: String){
//        //Creating json Object to pass into finix API
//        val identityJsonObject = JSONObject()
//        try {
//            if (zipCode != null) {
//                identityJsonObject.put("postal_code", "$zipCode")
//            }
//            if (phoneNumber != null) {
//                identityJsonObject.put("phone", "$phoneNumber")
//            }
//            if (firstName != null) {
//                identityJsonObject.put("first_name", "$firstName")
//            }
//            if (lastName != null) {
//                identityJsonObject.put("last_name", "$lastName")
//            }
//            if (addressOne != null) {
//                identityJsonObject.put("line1", "$addressOne")
//            }
//            if (addressTwo != null) {
//                identityJsonObject.put("line2", "$addressTwo")
//            }
//            if (city != null) {
//                identityJsonObject.put("city", "$city")
//            }
//            if (country != null) {
//                identityJsonObject.put("country", "$country")
//            }
//            if (emailAddress != null) {
//                identityJsonObject.put("email", "$emailAddress")
//            }
//            if (state != null) {
//                identityJsonObject.put("region", "$state")
//            }
////            jsonObject.put("tags", "${customTag}")
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//
//
//
//        val mediaType = "application/json; charset=utf-8".toMediaType()
//        val identityBody = identityJsonObject.toString().toRequestBody(mediaType)
//        Log.e("PT-Lib", "Identity Call JSON body : $identityJsonObject")
//
////        var random = "1234567"
////        val base64 = Base64.encodeToString(random.toByteArray(), Base64.DEFAULT)
////
////
////        Log.e("PT-Lib", "Authorization Headers: ${Base64.decode(base64, Base64.DEFAULT)}")
////
////        val data = Base64.decode(merchantIdentity, Base64.DEFAULT)
////        val text = String(data, StandardCharsets)
////        Log.e("PT-Lib", "Authorization Headers: $text")
//
//        val authRequest = Request.Builder()
//                .method("POST", identityBody)
//                .header(
//                        "Authorization",
//                        "Basic $token"
//                )
//                .url("https://finix.sandbox-payments-api.com/identities")
//                .build()
//
//
//        val identityResponse = client.newCall(authRequest).execute()
//        val identityJsonData: String? = identityResponse.body?.string()
//        val identityJsonResponse = JSONObject(identityJsonData)
//        Log.e("PT-Lib", "Identity Call Response: $identityJsonResponse")
//
//    }

