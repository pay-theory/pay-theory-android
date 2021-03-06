package com.paytheory.android.validation

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.paytheory.android.example.MainActivity
import com.paytheory.android.sdk.UtilMethods
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Class that testing the utility methods
 */
@RunWith(AndroidJUnit4::class)
class UtilMethodsTest{
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
     * testing connection to internet function
     */
    @Test
    fun isConnectedToInternet(){
        val context = mActivityTestRule.activity.applicationContext

        val util = UtilMethods.isConnectedToInternet(context)

        assert(util)
    }
}