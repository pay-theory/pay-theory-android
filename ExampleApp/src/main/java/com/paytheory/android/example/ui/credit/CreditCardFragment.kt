package com.paytheory.android.example.ui.credit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.example.R


class CreditCardFragment : Fragment() {
    private lateinit var creditCardViewModel: CreditCardViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        creditCardViewModel =
            ViewModelProvider(this).get(CreditCardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_credit, container, false)
        val textView: TextView = root.findViewById(R.id.textView)
        val payTheoryFragment: Fragment? = requireActivity().supportFragmentManager.findFragmentById(R.id.payTheoryFragmentCredit)
        creditCardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        creditCardViewModel.arguments.observe(viewLifecycleOwner, Observer {
            payTheoryFragment?.arguments = it
        })
        return root
    }
}