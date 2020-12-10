//package com.paytheory.paytheorylibrarysdk
//
//import android.util.Log
//import androidx.test.platform.app.InstrumentationRegistry
//import com.paytheory.paytheorylibrarysdk.paytheory.BuyerOptions
//import com.paytheory.paytheorylibrarysdk.paytheory.Payment
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//import org.junit.Assert.assertNotNull
//import org.junit.FixMethodOrder
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.junit.runners.MethodSorters
//import org.robolectric.RobolectricTestRunner
//
//
///**
// * Example local unit test, which will execute on the development machine (host).
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@RunWith(RobolectricTestRunner::class)
//class PayTheoryTests {
//    private var client = OkHttpClient()
//    private var context = InstrumentationRegistry.getInstrumentation().targetContext
//    private var cardPayment = Payment(7, 6, 5, 4, 3, "2", "1")
//    private var buyerOptions = BuyerOptions(
//        "firstName",
//        "lastName",
//        "addressOne",
//        "addressTwo",
//        "city",
//        "state",
//        "country",
//        "zipCode",
//        "phoneNumber",
//        "email"
//    )
//    private var payTheory = Transaction(context, "pt-sandbox-dev-d9de9154964990737db2f80499029dd6", cardPayment, buyerOptions)
//    private var failedPayTheory = Transaction(context, "wrong api", cardPayment, buyerOptions)
//
//    @Test
//    fun test1ChallengeCall() {
//        val request = Request.Builder()
//            .url("https://dev.tags.api.paytheorystudy.com/challenge")
//            .header("X-API-Key", payTheory.apiKey)
//            .build()
//
//        val response = client.newCall(request).execute()
//        val jsonData: String? = response.body?.string()
//        val challengeJSONResponse = JSONObject(jsonData)
//        assertNotNull(challengeJSONResponse)
//        if (jsonData != null) {
//            assert(jsonData.contains("challenge"))
//        }
//    }
//
//    @Test
//    fun test2FailedChallengeCall() {
//        val request = Request.Builder()
//            .url("https://dev.tags.api.paytheorystudy.com/challenge")
//            .header("X-API-Key", failedPayTheory.apiKey)
//            .build()
//
//        val response = client.newCall(request).execute()
//        val jsonData: String? = response.body?.string()
//        Log.e("PTLib", "Challenge response body $jsonData")
//        val challengeJSONResponse = JSONObject(jsonData)
//        if (jsonData != null) {
//            assert(jsonData.contains("message"))
//        }
//        assertNotNull(challengeJSONResponse)
//        assert(challengeJSONResponse.getString("message") == "Forbidden")
//    }
//
//    //TODO - set up unit tests with coroutines
////    @Test
////    fun test3PayTheoryIdempotency() {
////        var payTheoryIdempotency: String
////        var nonce = payTheory.challenge()
////        CoroutineScope(Dispatchers.IO).launch {
////            val attestationResponse = async {
////                payTheory.attestation(nonce)
////            }.await()
////            payTheoryIdempotency = payTheory.payTheoryIdempotency(nonce, attestationResponse)
////        }
////        assert(payTheoryIdempotency.contains("response"))
////        assert(payTheoryIdempotency.contains("signature"))
////        assert(payTheoryIdempotency.contains("credId"))
////
////        assert(payTheoryIdempotency.isNotBlank())
////    }
////
////    @Test
////    fun test3FailedPayTheoryIdempotency() {
////        var nonce = payTheory.challenge()
////        CoroutineScope(Dispatchers.IO).launch {
////            val attestationResponse = async {
////                payTheory.attestation(nonce)
////            }.await()
////            var payTheoryIdempotency = payTheory.payTheoryIdempotency(nonce, attestationResponse)
////
////
////            assert(payTheoryIdempotency.contains("response"))
////            assert(payTheoryIdempotency.contains("signature"))
////            assert(payTheoryIdempotency.contains("credId"))
////
////            assert(payTheoryIdempotency.isNotBlank())
////        }
////    }
//
//
//
//
//}