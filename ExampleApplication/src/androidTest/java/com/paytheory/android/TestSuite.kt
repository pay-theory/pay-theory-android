package com.paytheory.android

import com.paytheory.android.activity.MainActivityTests
import com.paytheory.android.validation.FormattingTest
import com.paytheory.android.validation.UtilMethodsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Class that runs a test suite for all testing
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(MainActivityTests::class, FormattingTest::class, UtilMethodsTest::class)
class TestSuite