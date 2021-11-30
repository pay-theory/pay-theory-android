package com.paytheory.android.example.ui.cash

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
class CashFragment : Fragment() {

    private lateinit var cashViewModel: CashViewModel
    val apiKey = "My-Api-Key"
    private val payTheoryFragment = PayTheoryFragment()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        cashViewModel =
                ViewModelProvider(this).get(CashViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_cash, container, false)
        val textView: TextView = root.findViewById(R.id.text_cash)

        cashViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })

        this.childFragmentManager.beginTransaction()
            .add(R.id.payTheoryContainer, payTheoryFragment)
            .commit()

        return root
    }

    override fun onStart() {
        super.onStart()

        payTheoryFragment.configure(apiKey,7500, PaymentType.CASH, false, false)
    }
}