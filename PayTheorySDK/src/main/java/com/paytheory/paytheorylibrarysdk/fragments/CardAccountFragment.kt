package com.paytheory.paytheorylibrarysdk.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paytheory.paytheorylibrarysdk.R

/**
 * A simple [Fragment] subclass.
 * Use the [CardAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CardAccountFragment : Fragment() {

    companion object {

        fun newInstance(): CardAccountFragment {
            return CardAccountFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        Log.e("Fragment", "onCreateView")
        return inflater.inflate(R.layout.fragment_pay_theory_credit_card_account, container, false)
    }


}