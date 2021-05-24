package com.paytheory.exampleapplication.tests

import android.text.Editable
import android.text.SpannableStringBuilder
import com.paytheory.android.sdk.validation.CVVFormattingTextWatcher
import com.paytheory.android.sdk.validation.CreditCardFormattingTextWatcher
import com.paytheory.android.sdk.validation.ExpirationFormattingTextWatcher
import com.paytheory.android.sdk.view.PayTheoryEditText
import org.junit.Test
import org.mockito.Mockito


/**
 * Class that is used to test validation
 */
class ValidationTests {

    /**
     *
     */
    @Test
    fun creditCardFormattingTextWatcherTest() {
        val editText = Mockito.mock(PayTheoryEditText::class.java)
        val editable: Editable = SpannableStringBuilder("4242424242424242")
        val textWatcher = CreditCardFormattingTextWatcher(editText)

        textWatcher.onTextChanged(Mockito.mock(CharSequence::class.java), 0,0,0)
        textWatcher.beforeTextChanged(Mockito.mock(CharSequence::class.java), 0,0,0)
        textWatcher.afterTextChanged(editable)

        editText.text?.let { assert(it.equals("4242424242424242") ) }
    }

    /**
     *
     */
    @Test
    fun cVVFormattingTextWatcherTests() {
        val editText = Mockito.mock(PayTheoryEditText::class.java)
        val editable: Editable = SpannableStringBuilder("042")
        val textWatcher = CVVFormattingTextWatcher(editText)

        textWatcher.onTextChanged(Mockito.mock(CharSequence::class.java), 0,0,0)
        textWatcher.beforeTextChanged(Mockito.mock(CharSequence::class.java), 0,0,0)
        textWatcher.afterTextChanged(editable)

        editText.text?.let { assert(it.equals("042") ) }
    }

    /**
     *
     */
    @Test
    fun expirationFormattingTextWatcherTests() {
        val editText = Mockito.mock(PayTheoryEditText::class.java)
        val editable: Editable = SpannableStringBuilder("042024")
        val textWatcher = ExpirationFormattingTextWatcher(editText)

        textWatcher.onTextChanged(Mockito.mock(CharSequence::class.java), 0,0,0)
        textWatcher.beforeTextChanged(Mockito.mock(CharSequence::class.java), 0,0,0)
        textWatcher.afterTextChanged(editable)

        editText.text?.let { assert(it.equals("042024") ) }
    }
}