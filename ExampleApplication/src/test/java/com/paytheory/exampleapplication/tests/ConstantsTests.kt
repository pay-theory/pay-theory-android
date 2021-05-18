package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.Constants
import org.junit.Test


/**
 * Class that is used to test Constants
 */
class ConstantsTests {
    /**
     *
     */
    @Test
    fun constantsTest() {
        val constants = Constants("paytheory")


        assert(constants.API_BASE_PATH == "https://paytheory.token.service.paytheorystudy.com/")
        assert(constants.NO_INTERNET_ERROR == "No internet connection")

    }
}