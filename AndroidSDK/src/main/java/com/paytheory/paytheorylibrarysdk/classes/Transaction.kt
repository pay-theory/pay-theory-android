package com.paytheory.paytheorylibrarysdk.classes

import ChallengeResponse
import IdempotencyPostData
import IdempotencyResponse
import PaymentData
import PaymentPostData
import PaymentResponse
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.gson.annotations.SerializedName
import com.paytheory.paytheorylibrarysdk.classes.api.ApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


/**
 * Transaction Class is created after data validation and click listener is activated.
 * This hold all pay theory logic to process payments.
 */
class Transaction(
    private val context: Context,
    private val apiKey: String,
    private val payment: PaymentData,
    private val tags: Map<String,String> = HashMap<String,String>(),
    private val buyerOptions: Map<String,String> = HashMap<String,String>()
) {

    private val GOOGLE_API = "AIzaSyDDn2oOEQGs-1ETypHoa9MIkJZZtjEAYBs"

    private var attestationResult: String =""
    private var challengeResult: String =""
    private var idempotencyList: ArrayList<IdempotencyResponse> = ArrayList<IdempotencyResponse>()


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


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun challengeApiCall(context: Context){
        if(UtilMethods.isConnectedToInternet(context)){
            UtilMethods.showLoading(context)

            val observable = ApiService.challengeApiCall().doChallenge()

            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ challenge: ChallengeResponse ->
                    UtilMethods.hideLoading()

                    challengeResult = challenge.challenge

                    callSafetyNet(challengeResult)

                }, { error ->
                    UtilMethods.hideLoading()
                    UtilMethods.showLongToast(context, error.message.toString())
                }
                )
        }else{
            UtilMethods.showLongToast(context, "No Internet Connection!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callSafetyNet(challenge: String) {
        SafetyNet.getClient(context).attest(challenge.toByteArray(), GOOGLE_API)
            .addOnSuccessListener({
                attestationResult = it.jwsResult
                idempotencyApiCall(context)
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun idempotencyApiCall(context: Context){
        if(UtilMethods.isConnectedToInternet(context)){
            UtilMethods.showLoading(context)

            val observable = ApiService.idempotencyApiCall().postIdempotency(IdempotencyPostData(attestationResult, challengeResult, 5000))

            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ idempotency: IdempotencyResponse ->
                    idempotencyList.add(idempotency)
                    paymentApiCall(context)
                }, { error ->
                    UtilMethods.hideLoading()
                    Log.e("payment",error.message.toString())
                    UtilMethods.showLongToast(context, error.message.toString())
                }
                )
        }else{
            UtilMethods.showLongToast(context, "No Internet Connection!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun paymentApiCall(context: Context){
        if(UtilMethods.isConnectedToInternet(context)){
            UtilMethods.showLoading(context)
            val idempotency: IdempotencyResponse = idempotencyList.first()
            val challenger = String(Base64.getDecoder().decode(idempotency.challenge))
            val observable = ApiService.paymentApiCall().postIdempotency(
                PaymentPostData(
                    payment,
                    idempotency.response,
                    idempotency.signature,
                    idempotency.credId,
                    challenger,
                    tags,
                    buyerOptions
                ))
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ payment: PaymentResponse ->
                    UtilMethods.hideLoading()

                    //TODO communicate successfully payment back to host activity
                    Log.i("payment",payment.paymentDetailReference)

                }, { error ->
                    UtilMethods.hideLoading()

                    Log.e("payment",error.message.toString())
                    UtilMethods.showLongToast(context, error.message.toString())
                }
                )
        }else{
            UtilMethods.showLongToast(context, "No Internet Connection!")
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun init() {
        challengeApiCall(context)
//        Log.d("Pay Theory", "Init transaction")
//        CoroutineScope(IO).launch {
//
//            //Challenge Api
//            val challengeResult = async {
//                challenge()
//            }.await()
//            if (challengeComplete) {
//                Log.d("Pay Theory", "Challenge Result: $challengeResult")
//
//                //Google Api
//                val googleApiResult = async {
//                    googleApi()
//                }.await()
//                if (googleVerifyComplete) {
//                    Log.d("Pay Theory", "Google Api Result: $googleApiResult")
//
//                    //Attestation Api
//                    val attestationResponse = async {
//                        attestation(challengeResult)
//                    }.await()
//                    if (!attestationResponse.isNullOrBlank()) {
//                        Log.d("Pay Theory", "Attestation Api Result: $attestationResponse")
//
//
//                        if (!attestationResponse.isNullOrEmpty() && googleVerifyComplete && challengeComplete) {
//                            //Pay Theory Idempotency
//                            val idempotencyResponse = async {
//                                payTheoryIdempotency(challengeResponse, attestationResponse)
//                            }.await()
//
//                            if (!idempotencyResponse.isNullOrBlank() && idempotencyComplete) {
//
//
//                                Log.d("Pay Theory", "Idempotency Result: $idempotencyResponse")
//
//                                if (payment.type == "CARD" && payment.feeMode == "service_fee") {
//                                    Handler(Looper.getMainLooper()).post {
//                                        confirmCard(
//                                            payment.amount,
//                                            payment.convenienceFee,
//                                            getCardType(payment.cardNumber.toString()),
//                                            payment.cardNumber,
//                                            context
//                                        )
//                                    }
//                                } else if (payment.type == "ACH" && payment.feeMode == "service_fee"){
//                                    Handler(Looper.getMainLooper()).post {
//                                        confirmACH(
//                                            payment.amount,
//                                            payment.convenienceFee,
//                                            payment.achAccountNumber,
//                                            context
//                                        )
//                                    }
//                                } else {
//                                    userConfirmation = true
//                                }
//
//                                while (userConfirmation == null) {
//                                    delay(500)
//                                }
//                                if (userConfirmation == true) {
//                                    transactionResponse = async {
//                                        payment(challengeResult, idempotency)
//                                    }.await()
//                                    Log.d(
//                                        "Pay Theory",
//                                        "Transaction Response: $transactionResponse"
//                                    )
//
//                                } else {
//                                    Log.d(
//                                        "Pay Theory",
//                                        "User Confirmed Transaction: $userConfirmation"
//                                    )
//                                }
//
//                            } else {
//                                Log.d("Pay Theory", "ERROR: Idempotency Failed")
//                                val message = "Idempotency failed"
//                                transactionResponse =
//                                    "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                                        payment.cardNumber.toString().takeLast(
//                                            4
//                                        )
//                                    }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
//                            }
//                        } else {
//                            Log.d("Pay Theory", "ERROR: Validation Failed")
//                            val message = "Validation failed"
//                            transactionResponse =
//                                "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                                    payment.cardNumber.toString().takeLast(
//                                        4
//                                    )
//                                }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
//                        }
//
//                    } else {
//                        Log.d("Pay Theory", "ERROR: Attestation failed")
//                        val message = "Attestation failed"
//                        transactionResponse =
//                            "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                                payment.cardNumber.toString().takeLast(
//                                    4
//                                )
//                            }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
//                    }
//
//
//                } else {
//                    Log.d("Pay Theory", "ERROR: Google verification failed")
//                    val message = "Google verification failed"
//                    transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                        payment.cardNumber.toString().takeLast(
//                            4
//                        )
//                    }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
//                }
//            } else {
//                Log.d("Pay Theory", "ERROR: Challenge failed")
//                val message = "Challenge failed"
//                transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                    payment.cardNumber.toString().takeLast(
//                        4
//                    )
//                }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"${message}\"}"
//            }
//        }
//        while (transactionResponse == "") {
//            delay(5000)
//        }
//        return transactionResponse
    }


//    fun payment(challengeResult: String, idempotency: String): String {
//
//        try {
//
//
//        val paymentBody = JSONObject()
//        val buyerOptionsAddress = JSONObject()
//        val buyerOptionsJson = JSONObject()
//        try {
//            if (buyerOptions != null) {
//                if (!buyerOptions.firstName.isNullOrBlank()) {
//                    buyerOptionsJson.put("first_name", buyerOptions.firstName)
//                }
//                if (!buyerOptions.lastName.isNullOrBlank()) {
//                    buyerOptionsJson.put("last_name", buyerOptions.lastName)
//                }
//                if (!buyerOptions.phoneNumber.isNullOrBlank()) {
//                    buyerOptionsJson.put("phone", buyerOptions.phoneNumber)
//                }
//                if (!buyerOptions.email.isNullOrBlank()) {
//                    buyerOptionsJson.put("email", buyerOptions.email)
//                }
//                if (!buyerOptions.addressOne.isNullOrBlank()) {
//                    buyerOptionsAddress.put("line1", buyerOptions.addressOne)
//                }
//                if (!buyerOptions.zipCode.isNullOrBlank()) {
//                    buyerOptionsAddress.put("postal_code", buyerOptions.zipCode)
//                }
//                if (!buyerOptions.addressTwo.isNullOrBlank()) {
//                    buyerOptionsAddress.put("line2", buyerOptions.addressTwo)
//                }
//                if (!buyerOptions.city.isNullOrBlank()) {
//                    buyerOptionsAddress.put("city", buyerOptions.city)
//                }
//                if (!buyerOptions.country.isNullOrBlank()) {
//                    buyerOptionsAddress.put("country", buyerOptions.country)
//                }
//                if (!buyerOptions.state.isNullOrBlank()) {
//                    buyerOptionsAddress.put("region", buyerOptions.state)
//                }
//            }
//
//            val paymentJsonObject = JSONObject()
//
//            if (payment.type == "CARD") {
//                paymentJsonObject.put("expiration_month", payment.cardExpMon)
//                paymentJsonObject.put("expiration_year", payment.cardExpYear)
//                paymentJsonObject.put("security_code", "${payment.cardCvv}")
//                paymentJsonObject.put("number", "${payment.cardNumber}")
//                paymentJsonObject.put("type", "PAYMENT_CARD")
//            } else {
//                paymentJsonObject.put("account_number", "${payment.achAccountNumber}")
//                paymentJsonObject.put("account_type", payment.achAccountType)
//                paymentJsonObject.put("bank_code", payment.achRoutingNumber)
//                paymentJsonObject.put("name", "${payment.firstName} ${payment.lastName}")
//                paymentJsonObject.put("type", "BANK_ACCOUNT")
//            }
//
//
//
//            if (!idempotency.isNullOrBlank()) {
//                val tagsJson = JSONObject()
//                tagsJson.put("key", "pt-platform:android $idempotency")
//                buyerOptionsJson.put("personal_address", buyerOptionsAddress)
//                paymentBody.put("challenge", challengeResult)
//                paymentBody.put("response", idempotencyResponseData)
//                paymentBody.put("credId", idempotencyCredIdData)
//                paymentBody.put("signature", idempotencySignatureData)
//                paymentBody.put("payment", paymentJsonObject)
//                paymentBody.put("tags", tagsJson)
//                paymentBody.put("buyer-options", buyerOptionsJson)
//            }
//            if (!payment.tagsKey.isNullOrBlank() && !payment.tagsValue.isNullOrBlank()) {
//                val tagsJson = JSONObject()
//                tagsJson.put(payment.tagsKey, payment.tagsValue.toString())
//                paymentBody.put("tags", tagsJson)
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//        val paymentRequest = Request.Builder()
//            .method(
//                "POST",
//                paymentBody.toString()
//                    .toRequestBody("application/json; charset=utf-8".toMediaType())
//            )
//            .addHeader("x-api-key", apiKey)
//            .url("https://dev.attested.api.paytheorystudy.com/payment")
//            .build()
//
//        val paymentResponse = client.newCall(paymentRequest).execute()
//        val paymentJsonData: String? = paymentResponse.body?.string()
//        val paymentJsonObject = JSONObject(paymentJsonData)
//
//        Log.d("Pay Theory", "Payment Response: $paymentJsonData")
//
//        transactionResponse = if (paymentJsonObject.getString("state") != "error" && paymentResponse.isSuccessful) {
//
//            if (payment.type == "CARD") {
//                transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                    payment.cardNumber.toString().takeLast(
//                        4
//                    )
//                }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"created_at\":\"${paymentJsonObject.getString(
//                    "created_at"
//                )
//                }\", \"amount\": ${paymentJsonObject.getString("amount")}, \"convenience_fee\": ${paymentJsonObject.getString(
//                    "service_fee"
//                )
//                }, \"state\":\"${paymentJsonObject.getString("state")}\", \"tags\": { \"${payment.tagsKey}\" : \"${payment.tagsValue}\"} }"
//            }
//            if (payment.type == "ACH") {
//                transactionResponse = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                    payment.achAccountNumber.toString().takeLast(
//                        4
//                    )
//                }\", \"created_at\":\"${paymentJsonObject.getString("created_at")
//                }\", \"amount\": ${paymentJsonObject.getString("amount")}, \"convenience_fee\": ${paymentJsonObject.getString(
//                    "service_fee"
//                )
//                }, \"state\":\"${paymentJsonObject.getString("state")}\", \"tags\": { \"${payment.tagsKey}\" : \"${payment.tagsValue}\"} }"
//            }
//            transactionResponse
//
//        } else if (paymentJsonObject.getString("state") == "error") {
//            returnResponse(paymentJsonObject.getString("reason"))
//        } else {
//            Log.d("Pay Theory", "Payment Request Failed")
//            returnResponse("Server Error")
//        }
//
//        } catch (error: Error){
//            transactionResponse = returnResponse(error.toString())
//        }
//        return transactionResponse
//    }
//
//    private fun returnResponse(message: String): String {
//        var response = ""
//
//        if (payment.type == "CARD") {
//            response = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                payment.cardNumber.toString().takeLast(
//                    4
//                )
//            }\", \"brand\":\"${getCardType(payment.cardNumber.toString())}\", \"state\":\"${transactionState}\", \"type\":\"$message\"}"
//        }
//        if (payment.type == "ACH") {
//            response = "{ \"receipt_number\":\"$idempotency\", \"last_four\":\"${
//                payment.achAccountNumber.toString().takeLast(
//                    4
//                )
//            }\", \"state\":\"${transactionState}\", \"type\":\"$message\"}"
//        }
//        return response
//    }
}



