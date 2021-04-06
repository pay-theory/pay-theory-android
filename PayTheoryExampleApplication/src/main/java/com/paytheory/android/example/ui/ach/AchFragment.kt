package com.paytheory.android.example.ui.ach

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.example.R

class AchFragment : Fragment() {

    private lateinit var achViewModel: AchViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        achViewModel =
                ViewModelProvider(this).get(AchViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_ach, container, false)
        val textView: TextView = root.findViewById(R.id.text_ach)
        achViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}