package com.paytheory.android.sdk.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

/**
 * PayTheoryButton Class is used to create a submit button to handle the transact or tokenize function
 */
class PayTheoryButton: AppCompatButton {

    constructor(context: Context) : super(context){
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
    defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        this.isEnabled = false
    }

    /**
     * Function to disable use PayTheoryButton
     */
    fun disable(){
        this.isEnabled = false
    }

    /**
     * Function to enable use PayTheoryButton
     */
    fun enable(){
        this.isEnabled = true
    }

}