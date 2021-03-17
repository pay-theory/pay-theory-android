package com.paytheory.android.sdk

import IdempotencyPostData
import IdempotencyResponse
import PaymentPostData
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.safetynet.SafetyNet
import com.paytheory.android.sdk.api.ApiService
import com.paytheory.android.sdk.api.ChallengeResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList


/**
 * Transaction Class is created after data validation and click listener is activated.
 * This hold all pay theory logic to process payments.
 */
class Transaction(
    private val context: Context,
    private val apiKey: String,
    private val payment: Any,
    private var tags: Map<String, String> = HashMap<String, String>(),
    private val buyerOptions: Map<String, String> = HashMap<String, String>(),
    private val amount: Int
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

    private fun buildApiHeaders(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        headerMap["X-API-Key"] = apiKey
        return headerMap
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun challengeApiCall(context: Context){
        if(UtilMethods.isConnectedToInternet(context)){

            val observable = ApiService.challengeApiCall().doChallenge(buildApiHeaders())

            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ challenge: ChallengeResponse ->

                    challengeResult = challenge.challenge

                    callSafetyNet(challengeResult)

                }, { error ->
                    if (context is Payable) {
                        context.paymentError(PaymentError(error.message!!))
                    }
                }
                )
        }else{
            if (context is Payable) {
                context.paymentError(PaymentError(Constants.NO_INTERNET_ERROR))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun callSafetyNet(challenge: String) {
        SafetyNet.getClient(context).attest(challenge.toByteArray(), GOOGLE_API)
            .addOnSuccessListener {
                attestationResult = it.jwsResult
                idempotencyApiCall(context)
            }.addOnFailureListener {
                if (context is Payable) {
                    context.paymentError(PaymentError(it.message!!))
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun idempotencyApiCall(context: Context){
        if(UtilMethods.isConnectedToInternet(context)){

            val observable = ApiService.idempotencyApiCall().postIdempotency(
                buildApiHeaders(),
                IdempotencyPostData(attestationResult, challengeResult, amount)
            )

            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ idempotency: IdempotencyResponse ->
                    idempotencyList.add(idempotency)
                    paymentApiCall(context)
                }, { error ->
                    if (context is Payable) {
                        context.paymentError(PaymentError(error.message!!))
                    }
                }
                )
        }else{
            if (context is Payable) {
                context.paymentError(PaymentError(Constants.NO_INTERNET_ERROR))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CheckResult")
    private fun paymentApiCall(context: Context){
        if(UtilMethods.isConnectedToInternet(context)){
            val idempotency: IdempotencyResponse = idempotencyList.first()
            tags += "pt-number" to idempotency.idempotency
            tags += "pay-theory-environment" to Constants.ENV
            val challenger = String(Base64.getDecoder().decode(idempotency.challenge))
            val observable = ApiService.paymentApiCall().postIdempotency(
                buildApiHeaders(),
                PaymentPostData(
                    payment,
                    idempotency.response,
                    idempotency.signature,
                    idempotency.credId,
                    challenger,
                    tags,
                    buyerOptions
                )
            )
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ paymentResult: PaymentResult ->
                    if (context is Payable) {
                        paymentResult.created_at?.let { context.paymentComplete(paymentResult) }
                        paymentResult.type?.let { context.paymentFailed(paymentResult) }
                    }
                }, { error ->

                    if (context is Payable) {
                        context.paymentError(PaymentError(error.message!!))

                    }
                }
                )
        }else{
            if (context is Payable) {
                context.paymentError(PaymentError(Constants.NO_INTERNET_ERROR))
            }
        }
    }

    /**
     * Initiate transaction
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun init() {
        challengeApiCall(context)
    }

}



