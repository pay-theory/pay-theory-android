package com.paytheory.paytheorylibrarysdk.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paytheory.paytheorylibrarysdk.R

/**
 * CardFullAccountFragment is a simple Fragment subclass.
 * Used to create Card Account fields
 */
class CardFullAccountFragment : Fragment() {

    /**
     * Display Card Account fields
     * @return fragment_pay_theory_credit_card_account layout
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pay_theory_card_full_account, container, false)
    }


}