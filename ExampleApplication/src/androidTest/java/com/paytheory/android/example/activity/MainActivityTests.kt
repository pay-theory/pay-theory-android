package com.paytheory.android.example.activity

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import androidx.test.rule.ActivityTestRule
import com.paytheory.android.example.MainActivity
import com.paytheory.android.example.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/*
Set up your test environment
To avoid flakiness, we highly recommend that you turn off system animations on the virtual or physical devices used for testing. On your device, under Settings > Developer options, disable the following 3 settings:

Window animation scale
Transition animation scale
Animator duration scale
 */

/**
 * Class that is used to test the example activity
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTests {

    @get:Rule
    var mActivityTestRule : ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java,
        true,
        false
    )

    /**
     * Launching activity with an intent
     */
    @Before
    fun setUp(){
        val intent = Intent()
        mActivityTestRule.launchActivity(intent)
    }



    /**
     * Running a transaction in test environment
     */
    @Test
    fun transaction() {

        //Running this on UI thread because only the original thread that created a view hierarchy can touch its views.
        runOnUiThread {
            val accountNameEditText =
                mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.account_name)
            accountNameEditText.visibility = View.VISIBLE

            val ccNumberEditText =
                mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.cc_number)
            ccNumberEditText.visibility = View.VISIBLE

            val ccCVVEditText =
                mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.cc_cvv)
            ccCVVEditText.visibility = View.VISIBLE

            val ccExpirationEditText =
                mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.cc_expiration)
            ccExpirationEditText.visibility = View.VISIBLE
        }

    //Turning off the auto fill to remove errors
        mActivityTestRule.activity.findViewById<EditText>(R.id.account_name)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_number)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_cvv)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_expiration)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO


        onView(withId(R.id.account_name))
            .perform(typeText("Some Body"))

        onView(withId(R.id.cc_number))
            .perform(typeText("4242424242424242"))

        onView(withId(R.id.cc_cvv))
            .perform(typeText("042"))

        onView(withId(R.id.cc_expiration))
            .perform(typeText("0424"))

        onView(withId(R.id.cc_number))
        .check(matches(withText("4242 4242 4242 4242")))

        onView(withId(R.id.cc_cvv))
        .check(matches(withText("042")))

        onView(withId(R.id.cc_expiration))
        .check(matches(withText("04/24")))

        onView(withId(R.id.account_name))
        .check(matches(withText("Some Body")))


//    var fragment = mActivityTestRule.activity.supportFragmentManager.findFragmentById(R.id.PayTheoryFragment)
    



        Log.d("Testing", mActivityTestRule.activity.applicationContext.packageName)

//                onView(withId(R.id.submitButton))
//            .perform(click(), closeSoftKeyboard())
//        //Read override methods and assert the value
//        Log.d("Testing", mActivityTestRuleOne.activity.


    }


//
////    payTheoryArgs.putBoolean(PayTheoryFragment.ACCOUNT_NAME_ENABLED,false)
//    @Test
//    fun `cc_transaction_with_accountNameDisabled_billingAddressDisabled`() {
//
//        //Running this on UI thread because only the original thread that created a view hierarchy can touch its views.
//        runOnUiThread(Runnable {
//            var ccNumberEditText = mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.cc_number)
//            ccNumberEditText.visibility = View.VISIBLE
//
//            var ccCVVEditText = mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.cc_cvv)
//            ccCVVEditText.visibility = View.VISIBLE
//
//            var ccExpirationEditText = mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.cc_expiration)
//            ccExpirationEditText.visibility = View.VISIBLE
//        })
//
//        //Turning off the auto fill to remove errors
//        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_number)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
//        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_cvv)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
//        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_expiration)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
//
//
//        //Replacing Text
//        onView(withId(R.id.cc_number))
//            .perform(replaceText("4242424242424242"))
//
//        onView(withId(R.id.cc_cvv))
//            .perform(replaceText("042"))
//
//        onView(withId(R.id.cc_expiration))
//            .perform(replaceText("0424"))
//
//
//        Log.d("Testing", mActivityTestRule.activity.applicationContext.packageName)
//
//    }
//
////    payTheoryArgs.putBoolean(PayTheoryFragment.ACCOUNT_NAME_ENABLED,true)
////    payTheoryArgs.putBoolean(PayTheoryFragment.USE_ACH,true)
//    @Test
//    fun `ach_transaction_with_accountName_billingAddressDisabled`() {
//
//    //Running this on UI thread because only the original thread that created a view hierarchy can touch its views.
//    runOnUiThread(Runnable {
//        var accountNameEditText = mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.account_name)
//        accountNameEditText.visibility = View.VISIBLE
//
//        var achAccountNumberEditText = mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.ach_account_number)
//        achAccountNumberEditText.visibility = View.VISIBLE
//
//        var achRoutingNumberEditText = mActivityTestRule.activity.findViewById<AppCompatEditText>(R.id.ach_routing_number)
//        achRoutingNumberEditText.visibility = View.VISIBLE
//
//    })
//
//    //Turning off the auto fill to remove errors
//    mActivityTestRule.activity.findViewById<EditText>(R.id.account_name)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
//    mActivityTestRule.activity.findViewById<EditText>(R.id.ach_account_number)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
//    mActivityTestRule.activity.findViewById<EditText>(R.id.ach_routing_number)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
//
//
//        onView(withId(R.id.account_name))
//            .perform(replaceText("Some Body"))
//
//        onView(withId(R.id.ach_account_number))
//            .perform(replaceText("12345678910"))
//
//        onView(withId(R.id.ach_routing_number))
//            .perform(replaceText("789456124"))
//
//
//    Log.d("Testing", mActivityTestRule.activity.applicationContext.packageName)
////                onView(withId(R.id.submitButton))
////            .perform(click(), closeSoftKeyboard())
////        //Read override methods and assert the value
////        Log.d("Testing", mActivityTestRuleOne.activity.
//
//    }














//    @Test
//    fun `apiservice`() {
//        val apiService = ApiService
//
//        val challengeApiCall = apiService.challengeApiCall()
//
//        Log.d("Testing", challengeApiCall.toString())
//    }



//    @Test
//    fun `transaction_test_billingAddressEnabled_accountNameEnabled_achEnabled`() {
////        Thread.sleep(3000)
//        //Running this on UI thread because only the original thread that created a view hierarchy can touch its views.
////        runOnUiThread(Runnable {
////            var accountNameEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.account_name)
////            accountNameEditText.visibility = View.VISIBLE
////
////            var achAccountNumberEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.ach_account_number)
////            achAccountNumberEditText.visibility = View.VISIBLE
////
////            var achRoutingNumberEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.ach_routing_number)
////            achRoutingNumberEditText.visibility = View.VISIBLE
////
////            var billingAddressOneEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.billing_address_1)
////            billingAddressOneEditText.visibility = View.VISIBLE
////
////            var billingAddressTwoEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.billing_address_2)
////            billingAddressTwoEditText.visibility = View.VISIBLE
////
////            var billingCityEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.billing_city)
////            billingCityEditText.visibility = View.VISIBLE
////
////            var billingStateEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.billing_state)
////            billingStateEditText.visibility = View.VISIBLE
////
////            var billingZipEditText = mActivityTestRuleOne.activity.findViewById<AppCompatEditText>(R.id.billing_zip)
////            billingZipEditText.visibility = View.VISIBLE
////        })
////
////        //Turning off the auto fill to remove errors
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.account_name)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.ach_account_number)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.ach_routing_number)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.billing_address_1)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.billing_address_2)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.billing_city)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.billing_state)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////        mActivityTestRuleOne.activity.findViewById<EditText>(R.id.billing_zip)?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
////
////
////        onView(withId(R.id.account_name))
////            .perform(replaceText("Some Body"))
////
////        onView(withId(R.id.ach_account_number))
////            .perform(replaceText("12345678910"))
////
////        onView(withId(R.id.ach_routing_number))
////            .perform(replaceText("789456124"))
////
////        onView(withId(R.id.billing_address_1))
////            .perform(replaceText("123 Testing Lane"))
////
////        onView(withId(R.id.billing_address_2))
////            .perform(replaceText("Apt 2"))
////
////        onView(withId(R.id.billing_city))
////            .perform(replaceText("Cincinnati"))
////
////        onView(withId(R.id.billing_state))
////            .perform(replaceText("OH"))
////
////        onView(withId(R.id.billing_zip))
////            .perform(replaceText("45040"))
//
//
//    }

//    @Test
//    fun `test`(){
//
//    }





//    @Test
//    fun cvvFormattingTest() {
//
//        onView(withId(R.id.cc_cvv))
//            .perform(clearText(),typeText("042"), closeSoftKeyboard())
//
//        onView(withId(R.id.cc_cvv))
//            .check(matches(withText("042")));
//    }
//
//    @Test
//    fun expirationFormattingTest() {
//
//        onView(withId(R.id.cc_cvv))
//            .perform(clearText(),typeText("0424"), closeSoftKeyboard())
//
//        onView(withId(R.id.cc_cvv))
//            .check(matches(withText("04/24")));


    //        onView(withId(R.id.cc_number))
//            .perform(replaceText("4242424242424242"))
//
//        onView(withId(R.id.cc_number))
//            .check(matches(withText("4242 4242 4242 4242")))

//        Thread.sleep(250)
//        onView(withId(R.id.submitButton))
//            .perform(click(), closeSoftKeyboard())



//        onView(withId(R.id.cc_number))
//            .check(matches(withText("4242 4242 4242 4242")));

//        editText.check(matches(hasErrorText("Cannot be blank!")));
//    }
}

