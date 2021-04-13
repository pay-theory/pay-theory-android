package com.paytheory.android.example.validation

import android.content.Intent
import android.view.View
import android.widget.EditText
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.paytheory.android.example.MainActivity
import com.paytheory.android.sdk.R
import com.paytheory.android.sdk.view.PayTheoryEditText
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Class that is used to test the formatting for edit text fields
 */
@RunWith(AndroidJUnit4::class)
class FormattingTest {
    @get:Rule
    var mActivityTestRule: ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java,
        true,
        false
    )

    /**
     * Launching activity with an intent
     */
    @Before
    fun setUp() {
        val intent = Intent()
        mActivityTestRule.launchActivity(intent)

        //Turning off the auto fill to remove errors
        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_cvv)?.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO
        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_number)?.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO
        mActivityTestRule.activity.findViewById<EditText>(R.id.cc_expiration)?.importantForAutofill =
            View.IMPORTANT_FOR_AUTOFILL_NO
    }

    /**
     * Testing credit card number formating
     */
    @Test
    fun card_number_formatting() {
        Espresso.onView(ViewMatchers.withId(R.id.cc_number))
            .perform(
                ViewActions.typeText("4242424242424242")
            )

        Espresso.onView(ViewMatchers.withId(R.id.cc_number))
            .check(ViewAssertions.matches(ViewMatchers.withText("4242 4242 4242 4242")))
    }

    /**
     * Testing credit card cvv formating
     */
    @Test
    fun cvv_formatting() {
        Espresso.onView(ViewMatchers.withId(R.id.cc_cvv))
            .perform(
                ViewActions.typeText("042")
            )

        Espresso.onView(ViewMatchers.withId(R.id.cc_cvv))
            .check(ViewAssertions.matches(ViewMatchers.withText("042")))
    }


    /**
     * Testing credit card expiration formating
     */
    @Test
    fun expiration_formatting() {
        Espresso.onView(ViewMatchers.withId(R.id.cc_expiration))
            .perform(
                ViewActions.typeText("0424")
            )


        Espresso.onView(ViewMatchers.withId(R.id.cc_expiration))
            .check(ViewAssertions.matches(ViewMatchers.withText("04/24")))
    }

    /**
     * Testing credit card number formating
     */
    @Test
    fun card_number_invalid() {
        Espresso.onView(ViewMatchers.withId(R.id.cc_number))
            .perform(
                ViewActions.typeText("4567891256789")
            )

        assertEquals(mActivityTestRule.activity.findViewById<PayTheoryEditText>(R.id.cc_number).error, "invalid credit card number")
    }

    /**
     * Testing credit card cvv formating
     */
    @Test
    fun cvv_invalid() {
        Espresso.onView(ViewMatchers.withId(R.id.cc_cvv))
            .perform(
                ViewActions.typeText("02")
            )

        assertEquals(mActivityTestRule.activity.findViewById<PayTheoryEditText>(R.id.cc_cvv).error, "invalid CVV")
    }

    /**
     * Testing credit card expiration formating
     */
    @Test
    fun expiration_invalid() {
        Espresso.onView(ViewMatchers.withId(R.id.cc_expiration))
            .perform(
                ViewActions.typeText("0")
            )

        assertEquals(mActivityTestRule.activity.findViewById<PayTheoryEditText>(R.id.cc_expiration).error, "invalid expiration number")
    }
}