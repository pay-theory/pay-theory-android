package com.paytheory.android.example.ui.credit

import Address
import PayorInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.paytheory.android.example.R
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.TransactionType
import com.paytheory.android.sdk.fragments.PayTheoryFragment

/**
 * Example Fragment
 */
class CreditCardFragment : Fragment() {

    private lateinit var creditCardViewModel: CreditCardViewModel
    val apiKey = "My-Api-Key"
    private val payTheoryFragment = PayTheoryFragment()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        creditCardViewModel =
            ViewModelProvider(this)[CreditCardViewModel::class.java]

        val root = inflater.inflate(R.layout.fragment_credit, container, false)
        val textView: TextView = root.findViewById(R.id.text_credit)
        creditCardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        this.childFragmentManager.beginTransaction()
            .add(R.id.payTheoryContainer, payTheoryFragment)
            .commit()

        return root
    }

    override fun onStart() {
        super.onStart()

        //PayorInfo configuration
        val payorInfo = PayorInfo(
            "Abel",
            "Collins",
            "abel@paytheory.com",
            "5135555555", //TODO handle if passed in with dashes
            Address(
                "10549 Reading Rd",
                "Apt 1",
                "Cincinnati",
                "OH",
                "45241",
                "USA")
        )

        //metadata configuration
        val metadata: HashMap<Any,Any> = hashMapOf(
            "studentId" to "student_1859034",
            "courseId" to "course_1859034"
        )


        //PayTheoryFragment configuration for card payments
        payTheoryFragment.configure(
            apiKey = apiKey,
            amount = 2500,
            transactionType = TransactionType.CARD,
            requireAccountName = false,
            requireBillingAddress = false,
            confirmation = true,
            feeMode = FeeMode.INTERCHANGE,
            metadata = metadata,
            payorInfo = payorInfo,
            sendReceipt = true,
            receiptDescription = "Test on Android SDK",
            accountCode = "987654321", //TODO
            reference = "Test v2.7.0 on android",
            paymentParameters = "test-params-2",
//          payorId = "ptl_pay_3CHDGvMHbnscEgq3pbqZp5",
//          invoiceId = "PTL_INV_6BVQ3USX7PXWMXCRKV8SU1"
        )
    }


}