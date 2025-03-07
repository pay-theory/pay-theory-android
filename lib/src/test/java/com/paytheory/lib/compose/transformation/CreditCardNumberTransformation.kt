package com.paytheory.lib.compose.transformation

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class CreditCardNumberTransformationTest(
    private val input: String,
    private val expectedFormatted: String,
    private val expectedOriginalToTransformed: Map<Int, Int>,
    private val expectedTransformedToOriginal: Map<Int, Int>
) {

    private val transformation = CreditCardNumberTransformation()

    @Test
    fun testFilter() {
        val result = transformation.filter(AnnotatedString(input))
        assertEquals(expectedFormatted, result.text.text)

        expectedOriginalToTransformed.forEach { (original, transformed) ->
            assertEquals(
                transformed,
                result.offsetMapping.originalToTransformed(original)
            )
        }

        expectedTransformedToOriginal.forEach { (transformed, original) ->
            assertEquals(
                original,
                result.offsetMapping.transformedToOriginal(transformed)
            )
        }
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{index}: input={0}, formatted={1}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    "",
                    "",
                    mapOf(0 to 0),
                    mapOf(0 to 0)
                ),
                arrayOf(
                    "123",
                    "123",
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3),
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3)
                ),
                arrayOf(
                    "1234",
                    "1234",
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 4),
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 4)
                ),
                arrayOf(
                    "12345",
                    "1234 5",
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 6),
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 4)
                ),
                arrayOf(
                    "12345678",
                    "1234 5678",
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 6, 6 to 7, 7 to 8, 8 to 9),
                    mapOf(0 to 0, 1 to 1, 2 to 2, 3 to 3, 4 to 4, 6 to 5, 7 to 6, 8 to 7, 9 to 8)
                ),
                arrayOf(
                    "123456789",
                    "1234 5678 9",
                    mapOf(0 to 0, 4 to 4, 5 to 6, 8 to 9, 9 to 11),
                    mapOf(0 to 0, 4 to 4, 6 to 5, 9 to 8, 10 to 8)
                ),
                arrayOf(
                    "123456789012",
                    "1234 5678 9012",
                    mapOf(0 to 0, 4 to 4, 5 to 6, 8 to 9, 9 to 11, 12 to 14),
                    mapOf(0 to 0, 4 to 4, 6 to 5, 9 to 8, 10 to 8, 13 to 11)
                ),
                arrayOf(
                    "1234567890123",
                    "1234 5678 9012 3",
                    mapOf(0 to 0, 4 to 4, 5 to 6, 8 to 9, 9 to 11, 12 to 14, 13 to 16),
                    mapOf(0 to 0, 4 to 4, 6 to 5, 9 to 8, 10 to 8, 13 to 11, 14 to 12)
                ),
                arrayOf(
                    "1234567890123456",
                    "1234 5678 9012 3456",
                    mapOf(0 to 0, 4 to 4, 5 to 6, 8 to 9, 9 to 11, 12 to 14, 13 to 16, 16 to 19),
                    mapOf(0 to 0, 4 to 4, 6 to 5, 9 to 8, 10 to 8, 13 to 11, 14 to 12, 19 to 16)
                )
            )
        }
    }
}