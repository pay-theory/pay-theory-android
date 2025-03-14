package com.paytheory.simplecompose


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.paytheory.lib.model.PaymentFieldViewModel
import com.paytheory.lib.model.PaymentProcessViewModel
import com.paytheory.lib.model.PaymentResultState
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.simplecompose.ui.theme.JetsnacksampleTheme
import java.text.NumberFormat
import java.util.Locale


/**
 * Main Activity for the Simple Compose app.
 * This activity demonstrates how to integrate the PayTheory payment library into a Compose application.
 * It implements the [Payable] interface to handle payment events and uses ViewModels to manage payment state.
 */
class MainActivity : ComponentActivity(), Payable {
    /**
     * ViewModel for managing the payment process state.
     */
    private val paymentProcessModel: PaymentProcessViewModel by viewModels()
    /**
     * ViewModel for managing the state of individual payment fields.
     */
    private val paymentFieldModel: PaymentFieldViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetsnacksampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SimplePayment(
                        payable = this@MainActivity,
                        processModel = paymentProcessModel, // Pass ViewModel
                        fieldModel = paymentFieldModel, // Pass ViewModel
                        modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    /**
     * Called when a payment transaction is successful.
     * Updates the payment process ViewModel with the success state.
     * @param successfulTransactionResult The result of the successful transaction.
     */
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        paymentProcessModel.updateState(PaymentResultState.Success(successfulTransactionResult))
        Log.d("MainActivity", "handleSuccess: $successfulTransactionResult")
    }

    /**
     * Called when a payment transaction fails.
     * Updates the payment process ViewModel with the failure state.
     * @param failedTransactionResult The result of the failed transaction.
     */
    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        paymentProcessModel.updateState(PaymentResultState.Failure(failedTransactionResult))
        Log.d("MainActivity", "handleFailure: $failedTransactionResult")
    }

    /**
     * Called when a tokenization is successful.
     * Updates the payment process ViewModel with the token success state.
     * @param paymentMethodToken The tokenized payment method result.
     */
    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        paymentProcessModel.updateState(PaymentResultState.TokenSuccess(paymentMethodToken))
        Log.d("MainActivity", "handleTokenizeSuccess: $paymentMethodToken")
    }

    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        paymentProcessModel.updateState(PaymentResultState.BarcodeSuccess(barcodeResult))
        Log.d("MainActivity", "handleBarcodeSuccess: $barcodeResult")
    }

    /**
     * Called when an error occurs during the payment process.
     * Updates the payment process ViewModel with the error state.
     * @param error The PTError that occurred.
     */
    override fun handleError(error: PTError) {
        paymentProcessModel.updateState(PaymentResultState.Error(error))
        Log.d("MainActivity", "handleError: $error")
    }

    /**
     * Called when the payment form is ready for input.
     * Updates the payment process ViewModel to the idle state.
     * @param isReady A boolean indicating whether the form is ready.
     */
    override fun handleReady(isReady: Boolean) {
        paymentProcessModel.updateState(PaymentResultState.Idle)
        Log.d("MainActivity", "handleReady: $isReady")
    }

    /**
     * Called when a payment transaction starts.
     * Updates the payment process ViewModel to the loading state.
     * @param paymentType The type of payment being processed.
     */
    override fun handlePaymentStart(paymentType: String) {
        paymentProcessModel.updateState(PaymentResultState.Loading)
        Log.d("MainActivity", "handlePaymentStart: $paymentType")
    }

    /**
     * Called when a tokenization process starts.
     * Logs the start of the tokenization process.
     * @param paymentType The type of payment being tokenized.
     */
    override fun handleTokenStart(paymentType: String) {
        Log.d("MainActivity", "handleTokenStart: $paymentType")
    }

    /**
     * Called when the state of a payment field changes.
     * Updates the payment field ViewModel with the new state.
     * @param fieldState A pair of the payment field and its new state.
     */

    override fun handleStateChange(fieldState: Pair<PaymentViewModel.PaymentField, PaymentViewModel.FieldState>) {
        paymentFieldModel.updateState(fieldState.first, fieldState.second)
    }

}

fun formatPenniesAsDollars(pennies: Int): String {
    val dollars = pennies.toDouble() / 100
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(dollars)
}

/**
 * Composable function for displaying a simple payment form and handling payment states.
 * This is an example of how to use the PayTheory payment library in a Compose function.
 * @param payable An instance of [Payable] for handling payment callbacks.
 * @param processModel The [PaymentProcessViewModel] for managing the payment process state.
 * @param fieldModel The [PaymentFieldViewModel] for managing the state of payment fields.
 * @param modifier Modifier for styling the composable.
 */
@Composable
fun SimplePayment(
    payable: Payable,
    processModel: PaymentProcessViewModel,
    fieldModel: PaymentFieldViewModel,
    modifier: Modifier = Modifier
) {
    val apiKey = LocalContext.current.getString(R.string.api_key)
    /**
     *  `PayTheoryConfiguration` configures the payment form.
     * `apiKey`: unique identifier for the merchant, located in the app resources.
     * `amount`: The payment amount. 1000000 represents $10000.00.
     * `paymentMethodType`: Set to ACH for ACH transfers.
     * `requireBillingAddress` and `requireAccountName`:  set to false to disable these fields as example
     * `outlined`: if true will use outlined fields
     */
    val ptConfig = PayTheoryConfiguration(
        apiKey = apiKey,
        amount = 1000,
        paymentMethodType = PaymentMethodType.CARD,
        requireBillingAddress = false,
        requireAccountName = false,
        outlined = true
    )

    //collecting the payment state for results
    val paymentState by processModel.paymentState.collectAsState()

    // collecting the state of the individual fields

//    val nameOnAccountState by fieldModel.nameState.collectAsState()
//    val bankAccountNumberState by fieldModel.bankAccountState.collectAsState()
//    val bankAccountRoutingState by fieldModel.bankRoutingState.collectAsState()
//    val bankAccountTypeState by fieldModel.bankAccountTypeState.collectAsState()

    val creditCardState by fieldModel.cardNumberState.collectAsState()
    val cardExpirationState by fieldModel.cardExpirationState.collectAsState()
    val cardCvcState by fieldModel.cardCvcState.collectAsState()
    val postalCodeState by fieldModel.postalCodeState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PaymentForm(
            payable = payable,
            configuration = ptConfig,
            fieldModifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            buttonModifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )

        when (val state = paymentState) {
            is PaymentResultState.Success -> {
                Text(
                    text = "Payment Success: ${formatPenniesAsDollars(state.result.amount.toInt())}",
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
                Text("Token Saved: ****${state.token.lastFour}",
                    modifier = Modifier.padding(8.dp))
            }
            is PaymentResultState.BarcodeSuccess -> {
                Text("Barcode: ${state.barcode.barcode}",
                    modifier = Modifier.padding(8.dp))
            }
            is PaymentResultState.Error -> {
                Text("Error: ${state.error.error}", color = Color.Red,
                    modifier = Modifier.padding(8.dp))
            }
            is PaymentResultState.Idle -> {
                Text("Ready", color = Color.Green,
                    modifier = Modifier.padding(8.dp))
            } // Initial state, no UI
            is PaymentResultState.Loading -> {
                Text("Loading", color = Color.Blue,
                    modifier = Modifier.padding(8.dp))
            }
        }

        when (val state = creditCardState) {
            PaymentViewModel.FieldState.EMPTY -> {
                Text(
                    text = "Credit Card Number: $creditCardState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Blue
                )
            }
            PaymentViewModel.FieldState.READY -> {
                Text(
                    text = "Credit Card Number: $creditCardState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Green
                )
            }
            PaymentViewModel.FieldState.INVALID -> {
                Text(
                    text = "Credit Card Number: $creditCardState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red
                )
            }
            PaymentViewModel.FieldState.INIT -> {
                Text(
                    text = "Credit Card Number: $creditCardState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Magenta
                )
            }
        }

        when (val state = cardExpirationState) {
            PaymentViewModel.FieldState.EMPTY -> {
                Text(
                    text = "Card Expiration: $cardExpirationState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Blue
                )
            }
            PaymentViewModel.FieldState.READY -> {
                Text(
                    text = "Card Expiration: $cardExpirationState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Green
                )
            }
            PaymentViewModel.FieldState.INVALID -> {
                Text(
                    text = "Card Expiration: $cardExpirationState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red
                )
            }
            PaymentViewModel.FieldState.INIT -> {
                Text(
                    text = "Card Expiration: $cardExpirationState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Magenta
                )
            }
        }

        when (val state = cardCvcState) {
            PaymentViewModel.FieldState.EMPTY -> {
                Text(
                    text = "Card CVC State: $cardCvcState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Blue
                )
            }
            PaymentViewModel.FieldState.READY -> {
                Text(
                    text = "Card CVC State: $cardCvcState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Green
                )
            }
            PaymentViewModel.FieldState.INVALID -> {
                Text(
                    text = "Card CVC State: $cardCvcState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red
                )
            }
            PaymentViewModel.FieldState.INIT -> {
                Text(
                    text = "Card CVC State: $cardCvcState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Magenta
                )
            }
        }

        when (val state = postalCodeState) {
            PaymentViewModel.FieldState.EMPTY -> {
                Text(
                    text = "Postal Code: $postalCodeState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Blue
                )
            }
            PaymentViewModel.FieldState.READY -> {
                Text(
                    text = "Postal Code: $postalCodeState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Green
                )
            }
            PaymentViewModel.FieldState.INVALID -> {
                Text(
                    text = "Postal Code: $postalCodeState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Red
                )
            }
            PaymentViewModel.FieldState.INIT -> {
                Text(
                    text = "Postal Code: $postalCodeState",
                    modifier = Modifier.padding(8.dp),
                    color = Color.Magenta
                )
            }
        }

//        when (val state = bankAccountTypeState) {
//            PaymentViewModel.FieldState.EMPTY -> {
//                Text(
//                    text = "Bank Account Type: $bankAccountTypeState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Blue
//                )
//            }
//            PaymentViewModel.FieldState.READY -> {
//                Text(
//                    text = "Bank Account Type: $bankAccountTypeState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Green
//                )
//            }
//            PaymentViewModel.FieldState.INVALID -> {
//                Text(
//                    text = "Bank Account Type: $bankAccountTypeState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Red
//                )
//            }
//            PaymentViewModel.FieldState.INIT -> {
//                Text(
//                    text = "Bank Account Type: $bankAccountTypeState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Magenta
//                )
//            }
//        }
//
//        when (val state = nameOnAccountState) {
//            PaymentViewModel.FieldState.EMPTY -> {
//                Text(
//                    text = "Name on Account: $nameOnAccountState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Blue
//                )
//            }
//            PaymentViewModel.FieldState.READY -> {
//                Text(
//                    text = "Name on Account: $nameOnAccountState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Green
//                )
//            }
//            PaymentViewModel.FieldState.INVALID -> {
//                Text(
//                    text = "Name on Account: $nameOnAccountState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Red
//                )
//            }
//            PaymentViewModel.FieldState.INIT -> {
//                Text(
//                    text = "Name on Account: $nameOnAccountState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Magenta
//                )
//            }
//        }
//
//        when (val state = bankAccountNumberState) {
//            PaymentViewModel.FieldState.EMPTY -> {
//                Text(
//                    text = "Bank Account Number: $bankAccountNumberState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Blue
//                )
//            }
//            PaymentViewModel.FieldState.READY -> {
//                Text(
//                    text = "Bank Account Number: $bankAccountNumberState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Green
//                )
//            }
//            PaymentViewModel.FieldState.INVALID -> {
//                Text(
//                    text = "Bank Account Number: $bankAccountNumberState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Red
//                )
//            }
//            PaymentViewModel.FieldState.INIT -> {
//                Text(
//                    text = "Bank Account Number: $bankAccountNumberState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Magenta
//                )
//            }
//        }
//
//        when (val state = bankAccountRoutingState) {
//            PaymentViewModel.FieldState.EMPTY -> {
//                Text(
//                    text = "Bank Routing Number: $bankAccountRoutingState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Blue
//                )
//            }
//            PaymentViewModel.FieldState.READY -> {
//                Text(
//                    text = "Bank Routing Number: $bankAccountRoutingState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Green
//                )
//            }
//            PaymentViewModel.FieldState.INVALID -> {
//                Text(
//                    text = "Bank Routing Number: $bankAccountRoutingState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Red
//                )
//            }
//            PaymentViewModel.FieldState.INIT -> {
//                Text(
//                    text = "Bank Routing Number: $bankAccountRoutingState",
//                    modifier = Modifier.padding(8.dp),
//                    color = Color.Magenta
//                )
//            }
//        }

    }
}