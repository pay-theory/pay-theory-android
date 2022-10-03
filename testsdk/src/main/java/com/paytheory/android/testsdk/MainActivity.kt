package com.paytheory.android.testsdk

import Address
import PayorInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.paytheory.android.sdk.*
import com.paytheory.android.sdk.configuration.FeeMode
import com.paytheory.android.sdk.configuration.TransactionType
import com.paytheory.android.sdk.fragments.PayTheoryFragment

class MainActivity : AppCompatActivity(), Payable {
    private val apiKey = "austin-paytheorylab-d7dbe665f5565fe8ae8a23eab45dd285"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Create PayTheoryFragment
        val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment

        //Set optional PayorInfo configuration
        val payorInfo = PayorInfo(
            "John",
            "Doe",
            "johndoe@paytheory.com",
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
        val metadata: HashMap<Any,Any> = hashMapOf(
            "studentId" to "student_1859034",
            "courseId" to "course_1859034"
        )

        //Keep in try catch for any additional errors
        try {
            //PayTheoryFragment configuration for card payments
            payTheoryFragment.configure(
                apiKey = apiKey,
                amount = 5050,
                transactionType = TransactionType.CARD,
                feeMode = FeeMode.INTERCHANGE,
                metadata = metadata,
                payorInfo = payorInfo
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Inherited from Payable interface
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        println(successfulTransactionResult)
    }

    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        println(failedTransactionResult)
    }

    override fun handleError(error: Error) {
        println(error)
    }

    override fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction) {
        println(confirmationMessage)
    }


    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        println(barcodeResult)
    }

    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        println(paymentMethodToken)
    }
}