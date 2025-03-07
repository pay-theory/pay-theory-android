package com.paytheory.lib.compose.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CreditCardNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.takeIf { it.length <= 16 } ?: text.text.take(16)
        val formatted = trimmed.chunked(4).joinToString(" ")

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