package com.paytheory.android.example

import com.paytheory.android.example.activity.MainActivityTests
import com.paytheory.android.example.validation.FormattingTest
import com.paytheory.android.example.validation.UtilMethodsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(MainActivityTests::class,FormattingTest::class, UtilMethodsTest::class)
class TestSuite