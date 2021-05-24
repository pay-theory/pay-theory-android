package com.paytheory.android.activity

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

        Log.d("Testing", mActivityTestRule.activity.applicationContext.packageName)

    }
}