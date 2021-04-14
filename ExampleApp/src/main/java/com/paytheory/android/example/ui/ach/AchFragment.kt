package com.paytheory.android.example.ui.ach

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
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.PaymentType
import com.paytheory.android.sdk.fragments.PayTheoryFragment

class AchFragment : Fragment() {

    private lateinit var achViewModel: AchViewModel
    private val payTheoryFragment = PayTheoryFragment()
    val apiKey = "pt-sandbox-abel-cc3dfd66a18dd51dca3930eede3b8489"
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        achViewModel =
                ViewModelProvider(this).get(AchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_ach, container, false)
        val textView: TextView = root.findViewById(R.id.text_ach)
        achViewModel.text.observe(viewLifecycleOwner, {
            textView.text = it
        })
        this.childFragmentManager.beginTransaction()
            .add(R.id.payTheoryContainer, payTheoryFragment)
            .commit()

        return root
    }

    override fun onStart() {
        super.onStart()

        payTheoryFragment.configure(apiKey,5000, PaymentType.BANK, false, false, FeeMode.SURCHARGE)
    }
}