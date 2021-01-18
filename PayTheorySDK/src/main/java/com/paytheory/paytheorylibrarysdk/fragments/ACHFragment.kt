package com.paytheory.paytheorylibrarysdk.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.paytheory.paytheorylibrarysdk.R

/**
 * CardFragment is a simple Fragment subclass.
 * Used to create Card Only fields
 */
class ACHFragment : Fragment(){
    /**
     * Display Card Only fields
     * @return fragment_pay_theory_credit_card layout
     */
    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pay_theory_ach, container, false)
    }
}
