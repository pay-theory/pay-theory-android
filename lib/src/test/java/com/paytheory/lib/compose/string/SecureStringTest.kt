package com.paytheory.lib.compose.string

import com.paytheory.lib.compose.string.SecureString.Companion.toSecureBytes
import com.paytheory.lib.compose.string.SecureString.Companion.toSecureString
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test class for the SecureString.kt file.
 */
class SecureStringTest {

    private lateinit var secureString: SecureString

    @Before
    fun setup() {
        secureString = SecureString("test_string")
    }

    @Test
    fun testRevealForUi() {
        assertEquals("test_string", secureString.revealForUi())
    }

    @Test
    fun testRevealForProcessing() {
        val expectedBytes = "test_string".toSecureBytes()
        val actualBytes = secureString.revealForProcessing()
        assertArrayEquals(expectedBytes, actualBytes)
    }

    @Test
    fun testClear() {
        secureString.zeroFill()
        val allZeros = ByteArray(secureString.revealForProcessing().size)
        assertArrayEquals(allZeros, secureString.revealForProcessing())
        assertEquals(allZeros.toSecureString(),secureString.revealForUi())
    }

    @Test
    fun testResetValue() {
        secureString.resetValue()
        assertEquals("", secureString.revealForUi())
        val expectedBytes = ByteArray(0)
        assertArrayEquals(expectedBytes, secureString.revealForProcessing())
    }

    @Test
    fun testResetValue_modifiedFlag() {
        val initialModified = secureString._isModified.value
        secureString.resetValue()
        assertNotEquals(initialModified, secureString._isModified.value)
    }

    @Test
    fun testConstructorWithString() {
        val secureString = SecureString("hello")
        assertEquals("hello", secureString.revealForUi())
    }

    @Test
    fun testConstructorWithByteArray() {
        val byteArray = "hello".toSecureBytes()
        val secureString = SecureString(byteArray)
        assertEquals("hello", secureString.revealForUi())
        assertArrayEquals(byteArray, secureString.revealForProcessing())
    }

    @Test
    fun testEquals_sameObject() {
        assertTrue(secureString == secureString)
    }

    @Test
    fun testEquals_sameValue() {
        val otherSecureString = SecureString("test_string")
        assertTrue(secureString.revealForUi() == otherSecureString.revealForUi())
        assertEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testEquals_differentValue() {
        val otherSecureString = SecureString("different_string")
        assertFalse(secureString == otherSecureString)
        assertNotEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testEquals_differentType() {
        assertFalse(secureString.toString() == "test_string")
    }


    @Test
    fun testHashCode_sameValue() {
        val otherSecureString = SecureString("test_string")
        assertEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testHashCode_differentValue() {
        val otherSecureString = SecureString("different_string")
        assertNotEquals(secureString.hashCode(), otherSecureString.hashCode())
    }

    @Test
    fun testGetValue() {
        assertEquals("test_string", secureString.revealForUi())
    }

    @Test
    fun testToSecureBytes_emptyString() {
        val bytes = "".toSecureBytes()
        assertEquals(0, bytes.size)
    }

    @Test
    fun testToSecureBytes_nonEmptyString() {
        val bytes = "hello".toSecureBytes()
        assertEquals(10, bytes.size)
    }

    @Test
    fun testToSecureString_emptyByteArray() {
        val string = ByteArray(0).toSecureString()
        assertEquals("", string)
    }

    @Test
    fun testToSecureString_nonEmptyByteArray() {
        val bytes = "hello".toSecureBytes()
        val string = bytes.toSecureString()
        assertEquals("hello", string)
    }

//    @Test
//    fun testClear_allZeros(){
//        secureString.clear()
//        val expectedBytes = ByteArray(secureString.revealForProcessing().size){0.toByte()}
//        assertArrayEquals(expectedBytes, secureString.revealForProcessing())
//    }
}