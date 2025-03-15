package com.paytheory.qualitydemo.compose

import java.text.NumberFormat
import java.util.Locale

fun formatPenniesAsDollars(pennies: Int): String {
    val dollars = pennies.toDouble() / 100
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(dollars)
}