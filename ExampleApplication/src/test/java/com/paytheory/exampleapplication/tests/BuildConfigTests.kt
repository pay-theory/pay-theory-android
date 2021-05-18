package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.BuildConfig
import org.junit.Test

class BuildConfigTests {

    /**
     *
     */
    @Test
    fun buildConfigTests() {
        val buildConfig = BuildConfig()

        assert(buildConfig is BuildConfig)
    }

}