package com.paytheory.android.testsdk.fragment

import com.paytheory.android.sdk.PayTheoryConfiguration
import com.paytheory.android.sdk.configuration.PaymentMethodType
import com.paytheory.android.sdk.fragments.PayTheoryFragment
import com.paytheory.android.sdk.view.PayTheoryButton
import com.paytheory.android.testsdk.R

/**
 * Fragment that handles bank payments
 */
class BankPaymentFragment() : BaseFragment() {

    /**
     * Function that handles fragment setup and configuration of PayTheoryFragment
     */
    override fun onStart() {
        super.onStart()
        //Create submit button
        val submitButton: PayTheoryButton = requireView().findViewById(R.id.submit)

        //Create PayTheoryFragment
        val payTheoryFragment = getChildFragmentManager().findFragmentById(R.id.payTheoryBankFragment) as PayTheoryFragment

        val configuration = PayTheoryConfiguration(submitButton, apiKey)
        configuration.paymentMethodType = PaymentMethodType.BANK
        configuration.metadata = metadata
        configuration.payorInfo = payorInfo
        configuration.amount = 15000
        configuration.accountCode = "Test Account Code"
        configuration.reference = "Test Reference"
        configuration.sendReceipt = true
        configuration.receiptDescription = "Android Payment Receipt Test"

        //Keep in try catch for any additional errors
        try {
            payTheoryFragment.configurePayment(
                configuration
            )

            submitButton.setOnClickListener{
                payTheoryFragment.transact()
            }

            payTheoryFragment.card.cardNumber.isValid()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}