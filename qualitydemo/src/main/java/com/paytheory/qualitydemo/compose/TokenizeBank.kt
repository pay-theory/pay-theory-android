package com.paytheory.qualitydemo.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.compose.PaymentForm
import com.paytheory.lib.configuration.PaymentMethodAction
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.model.FieldState
import com.paytheory.lib.model.PaymentFieldViewModel
import com.paytheory.lib.model.PaymentProcessViewModel
import com.paytheory.lib.model.PaymentResultState
import com.paytheory.qualitydemo.R

/**
 * Composable function for displaying a simple payment form and handling payment states.
 * This is an example of how to use the PayTheory payment library in a Compose function.
 * @param payable An instance of [Payable] for handling payment callbacks.
 * @param processModel The [PaymentProcessViewModel] for managing the payment process state.
 * @param fieldModel The [PaymentFieldViewModel] for managing the state of payment fields.
 * @param modifier Modifier for styling the composable.
 */
@Composable
fun TokenizeBank(
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
        paymentMethodType = PaymentMethodType.ACH,
        requireBillingAddress = false,
        requireAccountName = false,
        paymentMethodAction = PaymentMethodAction.TOKEN
    )

    //collecting the payment state for results
    val paymentState by processModel.paymentState.collectAsState()

    // collecting the state of the individual fields
    val nameOnAccountState by fieldModel.nameState.collectAsState()
    val bankAccountState by fieldModel.bankAccountState.collectAsState()
    val bankRoutingState by fieldModel.bankRoutingState.collectAsState()
    val bankAccountType by fieldModel.bankAccountTypeState.collectAsState()

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
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is PaymentResultState.Failure -> {
                Text(
                    text = "Payment Failed: ${state.result.state}",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            is PaymentResultState.TokenSuccess -> {
                Text("Token Saved: ****${state.token.lastFour}",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary)
            }
            is PaymentResultState.BarcodeSuccess -> {
                Text("Barcode: ${state.barcode.barcode}",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary)
            }
            is PaymentResultState.Error -> {
                Text("Error: ${state.error.error}",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error)
            }
            is PaymentResultState.Idle -> {
                Text("Ready",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary)
            } // Initial state, no UI
            is PaymentResultState.Loading -> {
                Text("Loading",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary)
            }

            is PaymentResultState.Processing -> {
                Text("Processing",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary)
            }
        }

        when (val state = nameOnAccountState) {
            FieldState.EMPTY -> {
                Text(
                    text = "Name on Account: $nameOnAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            FieldState.READY -> {
                Text(
                    text = "Bank Account Number: $bankAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            FieldState.INVALID -> {
                Text(
                    text = "Bank Routing Number: $bankRoutingState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            FieldState.INIT -> {
                Text(
                    text = "Bank Account Type: $bankAccountType",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        when (val state = nameOnAccountState) {
            FieldState.EMPTY -> {
                Text(
                    text = "Name on Account: $nameOnAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            FieldState.READY -> {
                Text(
                    text = "Name on Account: $nameOnAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            FieldState.INVALID -> {
                Text(
                    text = "Name on Account: $nameOnAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            FieldState.INIT -> {
                Text(
                    text = "Name on Account: $nameOnAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        when (val state = bankAccountState) {
            FieldState.EMPTY -> {
                Text(
                    text = "Bank Account Number: $bankAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            FieldState.READY -> {
                Text(
                    text = "Bank Account Number: $bankAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            FieldState.INVALID -> {
                Text(
                    text = "Bank Account Number: $bankAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            FieldState.INIT -> {
                Text(
                    text = "Bank Account Number: $bankAccountState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        when (val state = bankRoutingState) {
            FieldState.EMPTY -> {
                Text(
                    text = "Bank Routing Number: $bankRoutingState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            FieldState.READY -> {
                Text(
                    text = "Bank Routing Number: $bankRoutingState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            FieldState.INVALID -> {
                Text(
                    text = "Bank Routing Number: $bankRoutingState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            FieldState.INIT -> {
                Text(
                    text = "Bank Routing Number: $bankRoutingState",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}