package com.paytheory.android.sdk.composable.payment.control

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PayTheoryButton(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Filled")
    }
}