package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.BuildConfig
import org.junit.Test

/**
 * Class to test configuration
 */
class BuildConfigTests {

    /**
     * Function to test build configuration class
     */
    @Test
    fun buildConfigTests() {
        val buildConfig = BuildConfig()

        assert(buildConfig is BuildConfig)
    }

}