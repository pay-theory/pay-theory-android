package com.paytheory.android.testsdk.fragment

import com.paytheory.android.sdk.PayTheoryConfiguration
import com.paytheory.android.sdk.configuration.PaymentMethodType
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.testsdk.R

/**
 * Fragment that handles tokenization
 */
class TokenizeFragment: BaseFragment() {


    /**
     * Function that handles tokenization configuration
     */
    override fun onStart() {
        super.onStart()
        //Create submit button
        val submitButton: PayTheoryButton = requireView().findViewById(R.id.submit)

        //Create PayTheoryFragment
        val payTheoryFragment = getChildFragmentManager().findFragmentById(R.id.payTheoryBankFragment) as PayTheoryFragment

        val configuration = PayTheoryConfiguration(submitButton, apiKey)
        configuration.paymentMethodType = PaymentMethodType.CARD
        configuration.requireAccountName = true
        configuration.requireBillingAddress = true
        configuration.metadata = metadata
        configuration.payorInfo = payorInfo

        payTheoryFragment.configureTokenize(
            configuration
        )


        submitButton.setOnClickListener{
            payTheoryFragment.tokenize()
        }
    }
}