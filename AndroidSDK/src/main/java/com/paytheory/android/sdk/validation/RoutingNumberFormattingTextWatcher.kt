package com.paytheory.android.sdk.validation

import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Class that will add text watchers to an AppCompatEditText
 * @param pt custom AppCompatEditText that will be watched
 */
class RoutingNumberFormattingTextWatcher(var payTheoryFragment: PayTheoryFragment, var pt: PayTheoryEditText) : TextWatcher {

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        if (s.isEmpty()) {
            return
        }
        val isValid= s.toString().length == 9
        if (isValid) {
            payTheoryFragment.bankRoutingNumberValid = true
        } else {
            payTheoryFragment.bankRoutingNumberValid = false
            pt.error = "Invalid routing number"
        }
    }

}