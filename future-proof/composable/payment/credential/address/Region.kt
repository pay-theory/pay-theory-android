package com.paytheory.android.sdk.composable.payment.credential.address

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import com.paytheory.android.sdk.composable.payment.control.PayTheoryInput

@Composable
fun Region() {
    var isValid: Boolean = false
    var isEmpty: Boolean = true
    Row {
        PayTheoryInput("State", fun (input: String): Boolean {
            if (input.isNotEmpty()) {
                isValid = true
                isEmpty = false
            }
            return isValid
        })
    }
}