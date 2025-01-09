package com.paytheory.android.sdk.state

/*
* Modernization
* ValidAndEmpty Interface provides ValidAndEmpty protocol
* */

interface ValidAndEmpty {
    /** Returns true if the object is valid, false otherwise. */ fun isValid(): Boolean
    fun isEmpty(): Boolean
}