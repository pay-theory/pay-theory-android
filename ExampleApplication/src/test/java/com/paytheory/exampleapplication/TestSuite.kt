package com.paytheory.exampleapplication


import com.paytheory.exampleapplication.tests.*
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Class that runs a test suite for all testing
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(ApiTests::class, BuildConfigTests::class, ConfigurationTests::class,
    ConstantsTests::class, PayableTests::class,
    ReactorTests::class, UtilMethodsTests::class, ValidationTests::class, ViewTests::class, WebsocketTests::class )
class TestSuite