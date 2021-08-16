package com.paytheory.android.example.ui.credit

import Address
import BuyerOptions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.example.R
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.PaymentType
import com.paytheory.android.sdk.fragments.PayTheoryFragment

/**
 * Example Fragment
 */
class CreditCardFragment : Fragment() {

    private lateinit var creditCardViewModel: CreditCardViewModel
    val apiKey = "My-Api-Key"
    private val payTheoryFragment = PayTheoryFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        creditCardViewModel =
            ViewModelProvider(this).get(CreditCardViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_credit, container, false)
        val textView: TextView = root.findViewById(R.id.text_credit)
        creditCardViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        this.childFragmentManager.beginTransaction()
            .add(R.id.payTheoryContainer, payTheoryFragment)
            .commit()

        return root
    }

    override fun onStart() {
        super.onStart()

        val buyerOptions = BuyerOptions("Jim", "Smith", "jim.smith@gmail.com", "513-123-4567",
            Address("123 Testing Lane", "Apt 2", "Cincinnati", "OH", "45236", "USA"))

        val tags = hashMapOf("pay-theory-account-code" to "ABC12345", "pay-theory-reference" to "12345ABC")

        payTheoryFragment.configure(apiKey,5000, PaymentType.CREDIT, false, false, FeeMode.SERVICE_FEE, buyerOptions, tags)
    }


}