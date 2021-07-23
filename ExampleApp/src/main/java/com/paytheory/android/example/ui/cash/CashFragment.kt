package com.paytheory.android.example.ui.cash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.example.R

/**
 * Example Fragment
 */
class CashFragment : Fragment() {

    private lateinit var cashViewModel: CashViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        cashViewModel =
                ViewModelProvider(this).get(CashViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_cash, container, false)
        val textView: TextView = root.findViewById(R.id.text_cash)
        val messageView: TextView = root.findViewById(R.id.text_cash_message)
        cashViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        cashViewModel.message.observe(viewLifecycleOwner, Observer {
            messageView.text = it
        })
        return root
    }
}