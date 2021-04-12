package com.paytheory.android.example.ui.credit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.example.R
import com.paytheory.android.sdk.configuration.PaymentType
import com.paytheory.android.sdk.fragments.PayTheoryFragment


class CreditCardFragment : Fragment() {

    private lateinit var creditCardViewModel: CreditCardViewModel
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

        payTheoryFragment.configure(context?.resources!!.getString(R.string.api_key),5000, PaymentType.CREDIT, false, false)
    }


}