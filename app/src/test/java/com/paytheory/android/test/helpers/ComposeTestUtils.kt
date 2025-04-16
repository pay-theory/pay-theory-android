package com.paytheory.android.test.helpers

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith

/**
 * Base class for testing Compose UI components
 * 
 * Provides a configured ComposeContentTestRule and helper methods
 */
@RunWith(AndroidJUnit4::class)
abstract class ComposeTest {
    
    /**
     * Rule for testing compose UI components
     */
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * Sets the content to be tested
     * 
     * @param content the composable to test
     */
    fun setContent(content: @Composable () -> Unit) {
        composeTestRule.setContent(content)
    }
    
    /**
     * Waits for the UI to become idle
     */
    fun waitForIdle() {
        composeTestRule.waitForIdle()
    }
    
    /**
     * Runs a test with the given composable content
     * 
     * @param content the composable to test
     * @param test the test to run with the ComposeContentTestRule
     */
    fun runComposeTest(
        content: @Composable () -> Unit,
        test: ComposeContentTestRule.() -> Unit
    ) {
        composeTestRule.setContent(content)
        composeTestRule.waitForIdle()
        composeTestRule.test()
    }
}

/**
 * Helper functions for testing Compose components
 */
object ComposeTestUtils {
    
    /**
     * Creates a test environment for a composable
     * 
     * @param content the composable to test
     * @param block the test code to execute with the rule
     */
    fun testComposable(
        content: @Composable () -> Unit,
        block: ComposeContentTestRule.() -> Unit
    ) {
        val rule = createComposeRule()
        rule.setContent(content)
        rule.waitForIdle()
        rule.block()
    }
    
    /**
     * Captures a state during testing
     * 
     * @param T the type of state to capture
     * @param initialValue the initial value
     * @return a MutableStateCaptor that can capture state changes
     */
    fun <T> captureState(initialValue: T): MutableStateCaptor<T> {
        return MutableStateCaptor(initialValue)
    }
    
    /**
     * A class that can capture and hold state for testing
     */
    class MutableStateCaptor<T>(initialValue: T) {
        private var _value: T = initialValue
        val value: T get() = _value
        val history = mutableListOf<T>()
        
        fun capture(newValue: T) {
            _value = newValue
            history.add(newValue)
        }
        
        fun reset(value: T = _value) {
            _value = value
            history.clear()
        }
    }
} 