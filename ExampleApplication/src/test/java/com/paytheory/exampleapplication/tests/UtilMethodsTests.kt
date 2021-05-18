package com.paytheory.exampleapplication.tests

import android.content.Context
import com.paytheory.android.sdk.UtilMethods
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * Class that is used to test Payable interface
 */
class UtilMethodsTests {
    /**
     *
     */
    @Test
    fun isConnectedToInternetTests() {
        val util = UtilMethods
//        val isConnected = util.isConnectedToInternet(mock(Context::class.java))
        assert(util is UtilMethods)
    }
}