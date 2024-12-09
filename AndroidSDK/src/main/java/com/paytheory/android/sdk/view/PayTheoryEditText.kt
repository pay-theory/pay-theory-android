package com.paytheory.android.sdk.view

import android.content.Context

import android.util.AttributeSet

/**
 * PayTheory Edit Text is an extended EditText class providing a foundation for
 * the different text editors needed
 */
@Suppress("EmptyMethod")
open class PayTheoryEditText : androidx.appcompat.widget.AppCompatEditText{

    companion object {
        /**
         * Custom validation function
         * @param incoming String to validate
         * @param validator Lambda accepting the incoming String value and returning a Boolean result
         */
        fun validate(incoming: String, validator: (String) -> Boolean): Boolean {
            return validator(incoming)
        }
    }

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs : AttributeSet) : super(context,attrs){
        init()
    }

    constructor(context: Context,  attrs: AttributeSet , defStyleAttr : Int) : super(context, attrs, defStyleAttr){
        init()
    }
    private fun init() {
        // no-op comment in an unused listener function
    }
}