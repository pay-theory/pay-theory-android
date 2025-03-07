package com.paytheory.lib.compose.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A [VisualTransformation] that formats a credit card number with spaces.
 *
 * This class is responsible for transforming the input text of a credit card number
 * to display it in a user-friendly format, typically with spaces separating groups
 * of four digits. It also provides offset mapping to correctly handle cursor
 * positioning and text selection within the transformed text.
 *
 * Example:
 * Input: "1234567890123456"
 * Output: "1234 5678 9012 3456"
 *
 * Input: "12345678"
 * Output: "1234 5678"
 *
 * Input: "123"
 * Output: "123"
 */
class CreditCardNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {

        val trimmed = text.text.take(16)
        val formatted = trimmed.chunked(4).joinToString(" ").trim().take(19)

        return TransformedText(
            AnnotatedString(formatted),
            CreditCardOffsetMapping
        )
    }

    private object CreditCardOffsetMapping : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            return when {
                offset <= 4 -> offset
                offset <= 8 -> offset + 1
                offset <= 12 -> offset + 2
                else -> offset + 3
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            return when {
                offset <= 4 -> offset
                offset <= 9 -> offset - 1
                offset <= 14 -> offset - 2
                else -> offset - 3
            }
        }
    }
}