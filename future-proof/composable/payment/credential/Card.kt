package com.paytheory.android.sdk.composable.payment.credential

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.paytheory.android.sdk.composable.payment.control.PayTheoryInput

@Composable
fun Card() {
    Column {
        PayTheoryInput("Card Number")
        PayTheoryInput("CVV")
    }
}