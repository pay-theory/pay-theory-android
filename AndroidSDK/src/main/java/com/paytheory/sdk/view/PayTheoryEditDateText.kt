package com.paytheory.sdk.view

import android.content.Context

import android.util.AttributeSet
import android.view.View

/**
 * PayTheory Edit Date Text is an extended EditText class providing a date autofill
 */
class PayTheoryEditDateText : PayTheoryEditText{

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs : AttributeSet) : super(context,attrs){
        init()
    }

    constructor(context: Context,  attrs: AttributeSet , defStyleAttr : Int) : super(context, attrs, defStyleAttr){
        init()
    }

    override fun getAutofillType(): Int {
        return View.AUTOFILL_TYPE_DATE
    }

    private fun init() {

    }
}