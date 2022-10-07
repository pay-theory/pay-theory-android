package com.paytheory.android.sdk.validation

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryEditText

/**
 * Text watcher class to validate buyer contact edit text field
 */
class CashBuyerContactTextWatcher(var payTheoryFragment: PayTheoryFragment, var pt: PayTheoryEditText) : TextWatcher {

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op comment in an unused listener function
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op comment in an unused listener function
    }

    override fun afterTextChanged(s: Editable) {
        val isValid = isValidEmail(s.toString())
        if (isValid) {
            payTheoryFragment.cashBuyerValid = true
        } else {
            payTheoryFragment.cashBuyerValid = false
            pt.error = "Invalid email or phone number"
        }
    }

    private fun isValidEmail(target: CharSequence): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches() || Patterns.PHONE.matcher(target).matches()
        }
    }
}