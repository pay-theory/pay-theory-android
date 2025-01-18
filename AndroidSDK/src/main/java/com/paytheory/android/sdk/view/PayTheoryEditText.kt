package com.paytheory.android.sdk.view

import android.content.Context

import android.util.AttributeSet

/**
 * PayTheory Edit Text is an extended EditText class providing a foundation for
 * the different text editors needed
 */
/**
 * PayTheory Edit Text
 */
@Suppress("EmptyMethod")
open class PayTheoryEditText : com.google.android.material.textfield.TextInputEditText{

    /**
     * Constructor for PayTheoryEditText
     * @param context Context of application
     */
    constructor(context: Context) : super(context){
        init()
    }

    /**
     * Constructor for PayTheoryEditText
     * @param context Context of application
     * @param attrs Attribute set of application
     */
    constructor(context: Context, attrs : AttributeSet) : super(context,attrs){
        init()
    }

    /**
     * Constructor for PayTheoryEditText
     * @param context Context of application
     * @param attrs Attribute set of application
     * @param defStyleAttr Style attribute of application
     */
    constructor(context: Context,  attrs: AttributeSet , defStyleAttr : Int) : super(context, attrs, defStyleAttr){
        init()
    }
    private fun init() {
        // no-op comment in an unused listener function
    }
}