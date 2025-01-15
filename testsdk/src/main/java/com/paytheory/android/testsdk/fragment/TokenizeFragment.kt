package com.paytheory.android.testsdk.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paytheory.android.sdk.PayTheoryConfiguration
import com.paytheory.android.sdk.PayTheoryMerchantActivity
import com.paytheory.android.sdk.configuration.PaymentMethodType
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.testsdk.R

/**
 * Fragment that handles tokenization
 */
class TokenizeFragment: BaseFragment() {

    /**
     * Function that handles creating the view for the fragment
     * @param inflater layout inflater for the fragment
     * @param container parent view group for the fragment
     * @param savedInstanceState saved state of the fragment
     * @return view of the fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_token, container, false)
    }
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
        configuration.metadata = metadata
        configuration.payorInfo = payorInfo
        configuration.requireAccountName = true
        configuration.requireBillingAddress = true

        payTheoryFragment.configureTokenize(
            configuration,
            requireActivity() as PayTheoryMerchantActivity
        )


        submitButton.setOnClickListener{
            payTheoryFragment.tokenize()
        }
    }
}