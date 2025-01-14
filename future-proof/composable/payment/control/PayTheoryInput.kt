package com.paytheory.android.sdk.composable.payment.control

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


@Composable
fun PayTheoryInput(
    hint: String,
    validationFunction: (input: String) -> Boolean,
    defaultValue: String = ""
) {
    var text by remember { mutableStateOf(defaultValue) }
    var visible: Boolean = false

    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(hint) }
    )
}