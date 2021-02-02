package com.paytheory.android.example.validation

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import com.paytheory.android.example.MainActivity
import com.paytheory.android.sdk.UtilMethods
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UtilMethodsTest{
    @get:Rule
    var mActivityTestRule : ActivityTestRule<MainActivity> = ActivityTestRule(
        MainActivity::class.java,
        true,
        false
    )

    @Before
    fun setUp(){
        val intent = Intent()
        mActivityTestRule.launchActivity(intent)
    }

    @Test
    fun isConnectedToInternet(){
        val context = mActivityTestRule.activity.applicationContext

        val util = UtilMethods.isConnectedToInternet(context)

        assertTrue(util)
    }
}