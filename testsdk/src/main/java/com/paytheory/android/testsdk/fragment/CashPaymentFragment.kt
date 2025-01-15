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
 * Fragment that handles cash payments
 */
class CashPaymentFragment: BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cash_barcode, container, false)
    }

    /**
     * Function that handles fragment setup and configuration of payment
     */
    override fun onStart() {
        super.onStart()
        //Create submit button

        val submitButton: PayTheoryButton = requireView().findViewById(R.id.submit)

        //Create PayTheoryFragment
        val payTheoryFragment = getChildFragmentManager().findFragmentById(R.id.payTheoryBankFragment) as PayTheoryFragment

        val configuration = PayTheoryConfiguration(submitButton, apiKey)
        configuration.paymentMethodType = PaymentMethodType.CASH
        configuration.metadata = metadata
        configuration.payorInfo = payorInfo
        configuration.amount = 15000
        configuration.accountCode = "Test Account Code"
        configuration.reference = "Test Reference"
        configuration.sendReceipt = true
        configuration.receiptDescription = "Android Payment Receipt Test"

        payTheoryFragment.configurePayment(
            configuration,
            requireActivity() as PayTheoryMerchantActivity
        )

        submitButton.setOnClickListener{
            payTheoryFragment.transact()
        }
    }
}