package com.paytheory.android.sdk.view

import android.content.Context
import android.util.AttributeSet

/**
 * PayTheoryButton Class is used to create a submit button to handle the transact or tokenize function
 */
class PayTheoryButton: com.google.android.material.button.MaterialButton {



    /**
     * constructor function
     * @param context Context of application
     */
    constructor(context: Context) : super(context){
        init()
    }

    /**
     * constructor function
     * @param context Context of application
     * @param attrs attributes of PayTheoryButton
     *
     */
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init()
    }

    /**
     * constructor function
     * @param context Context of application
     * @param attrs attributes of PayTheoryButton
     * @param defStyleAttr default style attributes of PayTheoryButton
     *
     */
    constructor(
        context: Context,
        attrs: AttributeSet?,
    defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {

        init()
    }

    /**
     * Function to initial PayTheoryButton
     */
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