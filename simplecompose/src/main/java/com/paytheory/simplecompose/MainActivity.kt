package com.paytheory.simplecompose


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue // Only needed if using mutable state
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.simplecompose.ui.theme.JetsnacksampleTheme

class MainActivity : ComponentActivity(), Payable {
    private val viewModel: PaymentResultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetsnacksampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SimplePayment(
                        payable = this@MainActivity,
                        viewModel = viewModel, // Pass ViewModel
                        modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    // Update Payable callbacks to interact with ViewModel
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        viewModel.updateState(PaymentResultState.Success(successfulTransactionResult))
        Log.d("MainActivity", "handleSuccess: $successfulTransactionResult")
    }

    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        viewModel.updateState(PaymentResultState.Failure(failedTransactionResult))
        Log.d("MainActivity", "handleFailure: $failedTransactionResult")
    }

    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        viewModel.updateState(PaymentResultState.TokenSuccess(paymentMethodToken))
        Log.d("MainActivity", "handleTokenizeSuccess: $paymentMethodToken")
    }

    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        viewModel.updateState(PaymentResultState.BarcodeSuccess(barcodeResult))
        Log.d("MainActivity", "handleBarcodeSuccess: $barcodeResult")
    }

    override fun handleError(error: PTError) {
        viewModel.updateState(PaymentResultState.Error(error))
        Log.d("MainActivity", "handleError: $error")
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



    override fun handleStateChange(fieldState: Pair<PaymentViewModel.PaymentField, PaymentViewModel.FieldState>) {
        Log.d("MainActivity", "handleStateChange: $fieldState")
    }

}

@Composable
fun SimplePayment(
    payable: Payable,
    viewModel: PaymentResultViewModel,
    modifier: Modifier = Modifier
) {
    val apiKey = LocalContext.current.getString(R.string.api_key)
    val ptConfig = PayTheoryConfiguration(
        apiKey = apiKey,
        amount = 1000000,
        paymentMethodType = PaymentMethodType.ACH,
        requireBillingAddress = false,
        requireAccountName = false,
        outlined = true
    )

    val paymentState by viewModel.paymentState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PaymentForm(
            payable = payable,
            configuration = ptConfig,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        // Display payment state
        when (val state = paymentState) {
            is PaymentResultState.Success -> {
                Text(
                    text = "Payment Success: $${state.result.amount}",
                    modifier = Modifier.padding(8.dp)
                )
            }
            is PaymentResultState.Failure -> {
                Text(
                    text = "Payment Failed: ${state.result.state}",
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp)
                )
            }
            is PaymentResultState.TokenSuccess -> {
                Text("Token Saved: ****${state.token.lastFour}")
            }
            is PaymentResultState.BarcodeSuccess -> {
                Text("Barcode: ${state.barcode.barcode}")
            }
            is PaymentResultState.Error -> {
                Text("Error: ${state.error.error}", color = Color.Red)
            }
            PaymentResultState.Idle -> {} // Initial state, no UI
        }
    }
}