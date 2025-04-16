package com.paytheory.lib.compose.utility

import org.junit.Assert.*
import org.junit.Test

class CvcFieldUtilsTest {

    @Test
    fun `formatCvc with empty string returns empty string`() {
        assertEquals("", CvcFieldUtils.formatCvc(""))
    }

    @Test
    fun `formatCvc removes non-digit characters`() {
        assertEquals("123", CvcFieldUtils.formatCvc("1a2b3c"))
        assertEquals("123", CvcFieldUtils.formatCvc("1-2-3"))
        assertEquals("123", CvcFieldUtils.formatCvc(" 1 2 3 "))
    }

    @Test
    fun `formatCvc preserves only digits`() {
        assertEquals("123", CvcFieldUtils.formatCvc("123"))
        assertEquals("1234", CvcFieldUtils.formatCvc("1234"))
    }

    @Test
    fun `isValidCvc with empty string returns false`() {
        assertFalse(CvcFieldUtils.isValidCvc(""))
    }

    @Test
    fun `isValidCvc with 1-2 digits returns false without card network`() {
        assertFalse(CvcFieldUtils.isValidCvc("1"))
        assertFalse(CvcFieldUtils.isValidCvc("12"))
    }

    @Test
    fun `isValidCvc with 3-4 digits returns true without card network`() {
        assertTrue(CvcFieldUtils.isValidCvc("123"))
        assertTrue(CvcFieldUtils.isValidCvc("1234"))
    }

    @Test
    fun `isValidCvc with more than 4 digits returns false without card network`() {
        assertFalse(CvcFieldUtils.isValidCvc("12345"))
    }

    @Test
    fun `isValidCvc with Visa card network validates 3 digits`() {
        assertTrue(CvcFieldUtils.isValidCvc("123", CardNetwork.VISA))
        assertFalse(CvcFieldUtils.isValidCvc("1234", CardNetwork.VISA))
    }

    @Test
    fun `isValidCvc with Mastercard card network validates 3 digits`() {
        assertTrue(CvcFieldUtils.isValidCvc("123", CardNetwork.MASTERCARD))
        assertFalse(CvcFieldUtils.isValidCvc("1234", CardNetwork.MASTERCARD))
    }

    @Test
    fun `isValidCvc with Discover card network validates 3 digits`() {
        assertTrue(CvcFieldUtils.isValidCvc("123", CardNetwork.DISCOVER))
        assertFalse(CvcFieldUtils.isValidCvc("1234", CardNetwork.DISCOVER))
    }

    @Test
    fun `isValidCvc with Amex card network validates 4 digits`() {
        assertTrue(CvcFieldUtils.isValidCvc("1234", CardNetwork.AMEX))
        assertFalse(CvcFieldUtils.isValidCvc("123", CardNetwork.AMEX))
    }

    @Test
    fun `getExpectedCvcLength returns 4 for Amex`() {
        assertEquals(4, CvcFieldUtils.getExpectedCvcLength(CardNetwork.AMEX))
    }

    @Test
    fun `getExpectedCvcLength returns 3 for Visa, Mastercard, Discover`() {
        assertEquals(3, CvcFieldUtils.getExpectedCvcLength(CardNetwork.VISA))
        assertEquals(3, CvcFieldUtils.getExpectedCvcLength(CardNetwork.MASTERCARD))
        assertEquals(3, CvcFieldUtils.getExpectedCvcLength(CardNetwork.DISCOVER))
    }

    @Test
    fun `getExpectedCvcLength returns 3 for null network`() {
        assertEquals(3, CvcFieldUtils.getExpectedCvcLength(null))
    }
} 