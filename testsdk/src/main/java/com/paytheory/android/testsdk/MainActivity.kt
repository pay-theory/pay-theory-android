@file:Suppress("SameParameterValue")

package com.paytheory.android.testsdk

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.paytheory.android.sdk.BarcodeResult
import com.paytheory.android.sdk.ConfirmationMessage
import com.paytheory.android.sdk.FailedTransactionResult
import com.paytheory.android.sdk.PTError
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.Payment
import com.paytheory.android.sdk.PaymentMethodTokenResults
import com.paytheory.android.sdk.SuccessfulTransactionResult
import com.paytheory.android.sdk.view.PayTheoryBarcode
import com.paytheory.android.testsdk.fragment.BankPaymentFragment
import com.paytheory.android.testsdk.fragment.CardPaymentFragment
import com.paytheory.android.testsdk.fragment.CashPaymentFragment
import com.paytheory.android.testsdk.fragment.TokenizeFragment

/**
 * Demo Activity class using Pay Theory Android SDK
 */
class MainActivity : AppCompatActivity(), Payable {
    private var currentFragment : Fragment? = null
    private var activeFragmentType : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bankPaymentFragment = BankPaymentFragment()
        val cardPaymentFragment = CardPaymentFragment()
        val cashPaymentFragment = CashPaymentFragment()
        val tokenizeFragment = TokenizeFragment()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.page_card->setCurrentFragment(cardPaymentFragment,"card")
                R.id.page_bank->setCurrentFragment(bankPaymentFragment,"bank")
                R.id.page_token->setCurrentFragment(tokenizeFragment,"token")
                R.id.page_cash->setCurrentFragment(cashPaymentFragment,"cash")
            }
            true
        }
        bottomNavigationView.selectedItemId = R.id.page_card
    }

    /**
     * Function to set current fragment in view
     * @param fragment fragment to display
     * @param fragmentType type of fragment
     */
    private fun setCurrentFragment(fragment:Fragment, fragmentType: String) {
        val responseTextTextView = findViewById<TextView>(R.id.responseMessage)
        runOnUiThread { responseTextTextView.text = ""}
        currentFragment = fragment
        activeFragmentType = fragmentType
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.paymentFrame, fragment, fragmentType)
            commit()
        }
    }


    /**
     * Function to show progress indicator
     */
    fun showProgress() {
        val progress: CircularProgressIndicator =
            findViewById<CircularProgressIndicator>(R.id.progress_circular)
        progress.visibility = View.VISIBLE
    }

    /**
     * Function to hide progress indicator
     */
    fun hideProgress() {
        val progress: CircularProgressIndicator =
            findViewById<CircularProgressIndicator>(R.id.progress_circular)
        progress.visibility = View.GONE
    }

    /**
     * Inherited from Payable interface, handles ready state
     */
    override fun handleReady(isReady: Boolean) {
        runOnUiThread(Runnable {
            hideProgress()
        })
    }

    /**
     * Inherited from Payable interface, indicates payment has been initiated
     */
    override fun handlePaymentStart(paymentType: String) {
        runOnUiThread(Runnable {
            showProgress()
        })
    }

    /**
     * Inherited from Payable interface, indicates tokenization has been initiated
     */
    override fun handleTokenStart(paymentType: String) {
        runOnUiThread(Runnable {
            showProgress()
        })
    }

    /**
     * Inherited from Payable interface, handles successful transaction
     */
    @SuppressLint("UnsafeIntentLaunch")
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        runOnUiThread(Runnable {
            hideProgress()
        })
        println(successfulTransactionResult)

        val responseTextTextView = findViewById<TextView>(R.id.responseMessage)
        runOnUiThread { responseTextTextView.text = successfulTransactionResult.toString()}
    }

    /**
     * Inherited from Payable interface, handles failed transaction
     */
    @SuppressLint("UnsafeIntentLaunch")
    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        runOnUiThread(Runnable {
            hideProgress()
        })
        println(failedTransactionResult)
        val responseTextTextView = findViewById<TextView>(R.id.responseMessage)
        runOnUiThread { responseTextTextView.text = failedTransactionResult.toString()}
    }


    /**
     * Inherited from Payable interface, handles payment confirmation
     */
    override fun confirmation(confirmationMessage: ConfirmationMessage, payment: Payment) {
        //Handle payment confirmation
    }

    override fun handleError(error: PTError) {
        runOnUiThread(Runnable {
            hideProgress()
        })
        System.err.println(error)
        val responseTextTextView = findViewById<TextView>(R.id.responseMessage)
        runOnUiThread { responseTextTextView.text = error.toString()}
    }

    /**
     * Inherited from Payable interface, handles successful barcode scan
     */
    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        runOnUiThread(Runnable {
            hideProgress()
        })
        println(barcodeResult)

        findViewById<PayTheoryBarcode>(R.id.payTheoryBarcode).displayBarcode(barcodeResult)
        val responseTextTextView = findViewById<TextView>(R.id.responseMessage)
        runOnUiThread { responseTextTextView.text = barcodeResult.toString()}
    }

    /**
     * Inherited from Payable interface, handles successful tokenization
     */
    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        runOnUiThread(Runnable {
            hideProgress()
        })
        println(paymentMethodToken)
        val responseTextTextView = findViewById<TextView>(R.id.responseMessage)
        runOnUiThread { responseTextTextView.text = paymentMethodToken.toString()}
    }
}