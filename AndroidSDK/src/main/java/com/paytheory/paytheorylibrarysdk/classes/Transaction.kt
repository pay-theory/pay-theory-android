package com.paytheory.paytheorylibrarysdk.classes

import ChallengeResponse
import IdempotencyPostData
import IdempotencyResponse
import PaymentPostData
import PaymentResponse
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.safetynet.SafetyNet
import com.paytheory.paytheorylibrarysdk.classes.api.ApiService
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
            .addOnSuccessListener {
                attestationResult = it.jwsResult
                idempotencyApiCall(context)
            }
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
    }

}



