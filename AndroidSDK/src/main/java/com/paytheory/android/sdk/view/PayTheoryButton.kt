package com.paytheory.android.sdk.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

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

    fun disable(){
        this.isEnabled = false
    }

    fun enable(){
        this.isEnabled = true
    }

}