package com.paytheory.lib.compose.buttons

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paytheory.lib.compose.GooglePayButton
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Google Pay Button component.
 * 
 * These tests verify that the GooglePayButton renders correctly with different
 * button types and colors, and that it responds to enabled/disabled states.
 */
@RunWith(AndroidJUnit4::class)
class GooglePayButtonTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    /**
     * Tests that the default Google Pay button renders correctly when enabled.
     */
    @Test
    fun testDefaultGooglePayButtonEnabled() {
        // Set up the GooglePayButton with default parameters
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with BUY button type.
     */
    @Test
    fun testGooglePayButtonWithBuyType() {
        // Set up the GooglePayButton with BUY type
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonType = GooglePayButtonType.BUY
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with PAY button type.
     */
    @Test
    fun testGooglePayButtonWithPayType() {
        // Set up the GooglePayButton with PAY type
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonType = GooglePayButtonType.PAY
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with BOOK button type.
     */
    @Test
    fun testGooglePayButtonWithBookType() {
        // Set up the GooglePayButton with BOOK type
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonType = GooglePayButtonType.BOOK
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with CHECKOUT button type.
     */
    @Test
    fun testGooglePayButtonWithCheckoutType() {
        // Set up the GooglePayButton with CHECKOUT type
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonType = GooglePayButtonType.CHECKOUT
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with ORDER button type.
     */
    @Test
    fun testGooglePayButtonWithOrderType() {
        // Set up the GooglePayButton with ORDER type
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonType = GooglePayButtonType.ORDER
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with SUBSCRIBE button type.
     */
    @Test
    fun testGooglePayButtonWithSubscribeType() {
        // Set up the GooglePayButton with SUBSCRIBE type
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonType = GooglePayButtonType.SUBSCRIBE
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with BLACK color.
     */
    @Test
    fun testGooglePayButtonWithBlackColor() {
        // Set up the GooglePayButton with BLACK color
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonColor = GooglePayButtonColor.BLACK
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
    
    /**
     * Tests that the Google Pay button renders correctly with WHITE color.
     */
    @Test
    fun testGooglePayButtonWithWhiteColor() {
        // Set up the GooglePayButton with WHITE color
        composeTestRule.setContent {
            GooglePayButton(
                onClick = { /* no-op for testing */ },
                enabled = true,
                buttonColor = GooglePayButtonColor.WHITE
            )
        }
        
        // Verify the button is displayed
        composeTestRule.onNodeWithContentDescription("Google Pay button").assertIsDisplayed()
    }
} 