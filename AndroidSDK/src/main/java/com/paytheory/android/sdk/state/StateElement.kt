package com.paytheory.android.sdk.state

/*
* Modernization
* StateElement implements ValidAndEmpty interface
* */

class StateElement: ValidAndEmpty {
    private var valid: Boolean = false
    private var empty: Boolean = true
    fun setValid(_valid:Boolean) {
        valid = _valid
    }
    fun setEmpty(_empty:Boolean) {
        empty = _empty
    }
    override fun isValid(): Boolean {
        return valid
    }
    override fun isEmpty(): Boolean {
        return empty
    }
}