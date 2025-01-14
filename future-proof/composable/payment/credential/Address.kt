package com.paytheory.android.sdk.composable.payment.credential

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.paytheory.android.sdk.composable.payment.control.PayTheoryInput

@Composable
fun Address(isFullAddress: Boolean) {
    Column {
        if (isFullAddress) {
            PayTheoryInput("Card Number")
            PayTheoryInput("CVV")
        }
        PayTheoryInput("Card Number")
        PayTheoryInput("CVV")
    }
}