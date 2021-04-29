package com.paytheory.exampleapplication


import com.paytheory.exampleapplication.api.ApiTests
//import com.paytheory.exampleapplication.unit.UnitTests
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Class that runs a test suite for all testing
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(ApiTests::class)
//@Suite.SuiteClasses(ApiTests::class, UnitTests::class)
class TestSuite