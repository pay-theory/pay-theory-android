package com.paytheory.lib.compose.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordFilterTransformationTest {

    private val transformation = PasswordFilterTransformation()

    @Test
    fun testFilter_emptyString() {
        val result = transformation.filter(AnnotatedString(""))
        assertEquals("", result.text.text)
        assertEquals(OffsetMapping.Identity, result.offsetMapping)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
    }

    @Test
    fun testFilter_nonEmptyString() {
        val testString = "password123"
        val result = transformation.filter(AnnotatedString(testString))
        assertEquals("•••••••••••", result.text.text)
        assertEquals(OffsetMapping.Identity, result.offsetMapping)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
        assertEquals(1, result.offsetMapping.originalToTransformed(1))
        assertEquals(1, result.offsetMapping.transformedToOriginal(1))
        assertEquals(testString.length, result.offsetMapping.originalToTransformed(testString.length))
        assertEquals(testString.length, result.offsetMapping.transformedToOriginal(testString.length))
    }

    @Test
    fun testFilter_specialCharacters() {
        val testString = "!@#$%^&*()"
        val result = transformation.filter(AnnotatedString(testString))
        assertEquals("••••••••••", result.text.text)
        assertEquals(OffsetMapping.Identity, result.offsetMapping)
        assertEquals(testString.length, result.offsetMapping.originalToTransformed(testString.length))
        assertEquals(testString.length, result.offsetMapping.transformedToOriginal(testString.length))
    }
    @Test
    fun testFilter_annotatedStringWithSpans() {
        val testString = "password123"
        val annotatedString = AnnotatedString.Builder(testString).apply {
            addStyle(androidx.compose.ui.text.SpanStyle(color = androidx.compose.ui.graphics.Color.Red), 0, 4)
            addStringAnnotation("link", "https://www.example.com", 5, 9)
        }.toAnnotatedString()

        val result = transformation.filter(annotatedString)
        assertEquals("•••••••••••", result.text.text)
        assertEquals(0, result.text.spanStyles.size)
        assertEquals(0, result.text.getStringAnnotations(0, testString.length).size)
    }
}