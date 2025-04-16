package com.paytheory.lib.compose.transformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.paytheory.lib.compose.utility.TransformationUtils

/**
 * [VisualTransformation] that masks the input text with a bullet character (•), effectively hiding the original
 * text content. This is commonly used for password fields.
 *
 * Example:
 * If the input text is "password", the transformed text will be "••••••••".
 *
 * This transformation uses [OffsetMapping.Identity] because the cursor position should remain the same
 * relative to the displayed text. For instance, if the cursor is after the 3rd character in the input
 * "password", it will be after the 3rd bullet in the transformed text "••••••••".
 */
class PasswordFilterTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            TransformationUtils.maskAnnotatedString(text, '•'),
            OffsetMapping.Identity
        )
    }
}