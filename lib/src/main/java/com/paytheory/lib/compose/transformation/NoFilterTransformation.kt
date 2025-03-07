package com.paytheory.lib.compose.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * A [VisualTransformation] that performs no transformation on the input text.
 *
 * This transformation is useful when you want to display the raw text input
 * without any formatting or masking. It simply returns the input text as is,
 * with an identity offset mapping, meaning the cursor position remains the same
 * before and after transformation.
 *
 * Example use cases:
 * - Displaying plain text input.
 * - Situations where no visual masking or formatting is required.
 *
 * @constructor Creates a NoFilterTransformation instance.
 */
class NoFilterTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text,
            OffsetMapping.Identity
        )
    }
}