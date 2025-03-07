package com.paytheory.lib.compose.inputs

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import com.paytheory.lib.compose.string.SecureString

/**
 * A composable wrapper for either [OutlinedTextField] or [TextField] based on the `isOutlined` parameter.
 * This component handles displaying and updating a text field's value, with options for styling, validation, and input configuration.
 * It uses a [SecureString] for internal value handling, ensuring sensitive data is stored securely.
 *
 * @param value The current text to be displayed in the text field, wrapped in a [SecureString].
 * @param onValueChange The callback that is triggered when the text field's text changes.
 *                      It receives the new text as a [String].
 * @param modifier Modifier to be applied to the text field.
 * @param enabled Controls the enabled state of the text field. When `false`, the text field will be neither editable nor focusable,
 *                the input of the text field will not be selectable, visually text field will appear in the disabled UI state.
 * @param label The optional label to be displayed inside the text field container.
 * @param isError Indicates if the text field's current value is in an error state. If set to `true`, the text field's border and
 *                label will be displayed in an error color.
 * @param visualTransformation The visual transformation to be applied to the text field's text. This is useful for masking input,
 *                             such as password fields. Defaults to [VisualTransformation.None].
 * @param keyboardOptions Software keyboard options that should be provided for this text field.
 *                        Defaults to [KeyboardOptions.Default].
 * @param singleLine When set to `true`, the text field will be constrained to a single line. When `false`, the text field can span multiple lines.
 *                   Defaults to `false`.
 * @param maxLines The maximum number of lines that can be displayed in the text field. This parameter is ignored if `singleLine` is `true`.
 */
@Composable
fun TextFieldWrapper(
    value: SecureString,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isOutlined: Boolean = true
) {

    if (isOutlined) {
        OutlinedTextField(
            value = value.revealForUi(),
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled
        )
    } else {
        TextField(
            value = value.revealForUi(),
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,

            singleLine = singleLine,
            enabled = enabled
        )
    }
}

/**
 * A composable function that wraps either an [OutlinedTextField] or a [TextField] based on the [isOutlined] parameter.
 *
 * This function provides a convenient way to switch between outlined and filled text fields without duplicating
 * the parameter set.
 *
 * @param value The text field's current value.
 * @param onValueChange Callback that is triggered when the input service updates the text. An updated text comes as a
 *        parameter of the callback.
 * @param modifier Modifier to be applied to the text field.
 * @param enabled Controls the enabled state of the text field. When `false`, the text field will be neither
 *        editable nor focusable, the input of the text field will not be selectable,
 *        and the text field will appear in the disabled UI state.
 * @param label The optional label to be displayed inside the text field container. The default
 *        text color used is determined by the theme.
 * @param isError Indicates if the text field's current value is in error. If set to `true`, the
 *        text field will display the error state.
 * @param visualTransformation Transforms the visual representation of the input [value]. For example, you can
 *        hide the actual value for password fields. By default, no transformation is applied.
 * @param keyboardOptions Software keyboard options that should be provided to the software keyboard when the
 *        text field is focused.
 * @param singleLine When set to `true`, this forces the text field to be in a single line mode. By default, the
 *        text field can have multiple lines.
 * @param maxLines The maximum number of lines the text field can display. This is ignored when [singleLine] is
 *        `true`. By default, the text field will expand to fit the content.
 */
@Composable
fun TextFieldWrapper(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isOutlined: Boolean = true
) {

    if (isOutlined) {
        OutlinedTextField(
            value = value,
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled
        )
    } else {
        TextField(
            value = value,
            isError = isError,
            onValueChange = onValueChange,
            modifier = modifier,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            label = label,
            maxLines = maxLines,
            singleLine = singleLine,
            enabled = enabled
        )
    }
}