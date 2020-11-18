package com.paytheory.paytheorylibrarysdk

import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.paytheory.paytheorylibrarysdk.paytheory.BuyerOptions
import com.paytheory.paytheorylibrarysdk.paytheory.CardPayment
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class PayTheoryTests {
    private var client = OkHttpClient()
    private var context = InstrumentationRegistry.getInstrumentation().targetContext
    private var cardPayment = CardPayment(7, 6, 5, 4, 3, "2", "1")
    private var buyerOptions = BuyerOptions(
        "firstName",
        "lastName",
        "addressOne",
        "addressTwo",
        "city",
        "state",
        "country",
        "zipCode",
        "phoneNumber",
        "email"
    )
    var payTheory = PayTheory(context, "pt-sandbox-dev-d9de9154964990737db2f80499029dd6", cardPayment, buyerOptions)

    @Test
    fun testChallengeCall() {
        val request = Request.Builder()
            .url("https://dev.tags.api.paytheorystudy.com/challenge")
            .header("X-API-Key", payTheory.apiKey)
            .build()

        val response = client.newCall(request).execute()
        val jsonData: String? = response.body?.string()
        Log.e("PTLib", "Challenge response body $jsonData")
        val challengeJSONResponse = JSONObject(jsonData)

        assertNotNull(challengeJSONResponse)
    }


}