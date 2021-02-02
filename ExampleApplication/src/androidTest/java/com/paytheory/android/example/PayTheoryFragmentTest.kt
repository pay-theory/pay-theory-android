//package com.paytheory.android.sdk
//
//import android.os.Bundle
//import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
//import androidx.fragment.app.testing.launchFragmentInContainer
//import com.paytheory.android.sdk.fragments.PayTheoryFragment
//import org.junit.Test
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4ClassRunner::class)
//class PayTheoryFragmentTest{
//
//    @Test
//    fun test1() {
//
//        //Create bundle for pay theory fragment
//        val bundle = Bundle()
//        bundle.putString(PayTheoryFragment.API_KEY, "pt-sandbox-dev-f992c4a57b86cb16aefae30d0a450237")
//        bundle.putInt(PayTheoryFragment.AMOUNT,5000)
//        bundle.putBoolean(PayTheoryFragment.ACCOUNT_NAME_ENABLED,true)
//
//        //launch fragment with bundle
//        val scenario = launchFragmentInContainer<PayTheoryFragment>(
//            fragmentArgs = bundle
//        )
//
//
//
//    }
//
//}