package com.paytheory.android.testsdk.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.paytheory.android.sdk.data.Address
import com.paytheory.android.sdk.data.PayorInfo
import com.paytheory.android.testsdk.R

/**
 * Base fragment class for shared functionality
 */
open class BaseFragment : Fragment() {
    var apiKey = ""
    //Set optional PayorInfo configuration
    var payorInfo = PayorInfo(
        "John",
        "Doe",
        "abel@paytheory.com",
        "5135555555",
        Address(
            "10549 Reading Rd",
            "Apt 1",
            "Cincinnati",
            "OH",
            "45241",
            "USA"
        )
    )

    //Set optional metadata configuration
    var metadata: HashMap<Any,Any> = hashMapOf(
        "studentId" to "student_1859034",
        "courseId" to "course_1859034"
    )

    /**
     * Function that is called when fragment is created
     * @param savedInstanceState saved state of the fragment
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiKey = resources.getString(R.string.api_key)
    }

    /**
     * Function that handles creating the view for the fragment
     * @param inflater layout inflater for the fragment
     * @param container parent view group for the fragment
     * @param savedInstanceState saved state of the fragment
     * @return view of the fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment, container, false)
    }
}