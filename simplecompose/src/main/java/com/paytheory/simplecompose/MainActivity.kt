package com.paytheory.simplecompose


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paytheory.lib.BarcodeResult
import com.paytheory.lib.FailedTransactionResult
import com.paytheory.lib.PTError
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.PaymentMethodTokenResults
import com.paytheory.lib.SuccessfulTransactionResult
import com.paytheory.lib.compose.PaymentForm
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.simplecompose.ui.theme.JetsnacksampleTheme

class MainActivity : ComponentActivity(), Payable {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetsnacksampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SimplePayment(
                        payable = this@MainActivity,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun handleReady(isReady: Boolean) {
        Log.d("MainActivity", "handleReady: $isReady")
    }

    override fun handlePaymentStart(paymentType: String) {
        Log.d("MainActivity", "handlePaymentStart: $paymentType")
    }

    override fun handleTokenStart(paymentType: String) {
        Log.d("MainActivity", "handleTokenStart: $paymentType")
    }

    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        Log.d("MainActivity", "handleSuccess: $successfulTransactionResult")
    }

    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        Log.d("MainActivity", "handleFailure: $failedTransactionResult")
    }

    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        Log.d("MainActivity", "handleBarcodeSuccess: $barcodeResult")
    }

    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        Log.d("MainActivity", "handleTokenizeSuccess: $paymentMethodToken")
    }

    override fun handleError(error: PTError) {
        Log.d("MainActivity", "handleError: $error")
    }
}

@Composable
fun SimplePayment(payable: Payable, modifier: Modifier = Modifier) {
    val apiKey = LocalContext.current.getString(R.string.api_key)
    val ptConfig = PayTheoryConfiguration(
        apiKey = apiKey,
        amount = 1000000,
        paymentMethodType = PaymentMethodType.CARD,
        requireBillingAddress = false,
        requireAccountName = false,
        outlined = false
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PaymentForm(
            payable = payable,
            configuration =ptConfig,
            modifier = Modifier.padding( horizontal = 8.dp, vertical = 4.dp ),
        )
    }
}