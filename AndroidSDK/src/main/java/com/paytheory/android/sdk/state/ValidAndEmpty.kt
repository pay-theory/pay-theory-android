package com.paytheory.android.sdk.state

/*
* Modernization
* ValidAndEmpty Interface provides ValidAndEmpty protocol
* */

interface ValidAndEmpty {
    fun isValid(): Boolean
    fun isEmpty(): Boolean
}