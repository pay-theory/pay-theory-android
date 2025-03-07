package com.paytheory.lib.compose.string

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SecureStringWrapperTest {

    private lateinit var secureString: SecureString
    private lateinit var secureStringWrapper: SecureStringWrapper

    @Before
    fun setup() {
        secureString = SecureString("initial_string")
        secureStringWrapper = SecureStringWrapper(secureString, TextRange(0))
    }

    @Test
    fun testInitialValues() {
        assertEquals("initial_string", secureStringWrapper.visibleValue)
        assertEquals("initial_string", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(0), secureStringWrapper._selection)
    }

    @Test
    fun testUpdateValue() {
        secureStringWrapper.updateValue("new_string", 5)
        assertEquals("new_string", secureStringWrapper.visibleValue)
        assertEquals("new_string", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(5), secureStringWrapper._selection)
    }

    @Test
    fun testSecureValueGetter() {
        assertEquals(secureString, secureStringWrapper.secureValue)
    }

    @Test
    fun testSecureStateGetter_withSelection() {
        val expectedTextFieldValue = TextFieldValue("initial_string", TextRange(0))
        assertEquals(expectedTextFieldValue.text, secureStringWrapper.secureState.text)
        assertEquals(expectedTextFieldValue.selection, secureStringWrapper.secureState.selection)
    }

    @Test
    fun testSecureStateGetter_withoutSelection() {
        val secureString = SecureString("initial_string")
        val secureStringWrapper = SecureStringWrapper(secureString, null)
        val expectedTextFieldValue = TextFieldValue("initial_string")
        assertEquals(expectedTextFieldValue.text, secureStringWrapper.secureState.text)
        assertEquals(expectedTextFieldValue.selection, secureStringWrapper.secureState.selection)
    }
    @Test
    fun testInitialValues_nullSelection() {
        val secureStringWrapper = SecureStringWrapper(secureString, null)
        assertEquals("initial_string", secureStringWrapper.visibleValue)
        assertEquals("initial_string", secureStringWrapper.secureValue.revealForUi())
        assertNull(secureStringWrapper._selection)
    }

    @Test
    fun testUpdateValue_nullInitialSelection() {
        val secureString = SecureString("initial_string")
        val secureStringWrapper = SecureStringWrapper(secureString, null)
        secureStringWrapper.updateValue("new_string", 5)
        assertEquals("new_string", secureStringWrapper.visibleValue)
        assertEquals("new_string", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(5), secureStringWrapper._selection)
    }

    @Test
    fun testUpdateValue_differentString() {
        secureStringWrapper.updateValue("new_string_2", 7)
        assertEquals("new_string_2", secureStringWrapper.visibleValue)
        assertEquals("new_string_2", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(7), secureStringWrapper._selection)
    }

    @Test
    fun testUpdateValue_multipleTimes() {
        secureStringWrapper.updateValue("first_update", 3)
        assertEquals("first_update", secureStringWrapper.visibleValue)
        assertEquals("first_update", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(3), secureStringWrapper._selection)

        secureStringWrapper.updateValue("second_update", 6)
        assertEquals("second_update", secureStringWrapper.visibleValue)
        assertEquals("second_update", secureStringWrapper.secureValue.revealForUi())
        assertEquals(TextRange(6), secureStringWrapper._selection)
    }

    @Test
    fun testSecureStateGetter_emptyString() {
        secureStringWrapper.updateValue("", 0)
        val expectedTextFieldValue = TextFieldValue("", TextRange(0))
        assertEquals(expectedTextFieldValue.text, secureStringWrapper.secureState.text)
        assertEquals(expectedTextFieldValue.selection, secureStringWrapper.secureState.selection)
    }
}