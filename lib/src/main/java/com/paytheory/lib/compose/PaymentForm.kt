package com.paytheory.lib.compose

import android.accounts.NetworkErrorException
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paytheory.lib.ErrorCode
import com.paytheory.lib.PTError
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.model.PaymentViewModel.PaymentState
import com.paytheory.lib.valid.Validator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Composable function to display a Payment Form.
 *
 * @param payable An instance of [Payable] interface to handle payment actions.
 * @param configuration Configuration parameters for the payment form, defined in [PayTheoryConfiguration].
 * @param modifier Modifier to apply to the payment form composable.
 */
fun PaymentForm(
    payable: Payable,
    configuration: PayTheoryConfiguration,
    modifier: Modifier = Modifier,
) {
    val validator = Validator()

    if (!isNetworkAvailable(LocalContext.current)) {
        throw NetworkErrorException(NO_NETWORK_CONNECTION)
    }

    val packageName = LocalContext.current.applicationContext.packageName

    val viewModel: PaymentViewModel = viewModel {
        PaymentViewModel(
            packageName,
            configuration,
            payable
        )
    }
    val paymentViewState by viewModel.paymentState.collectAsState()
    var clearFormTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = paymentViewState) {
        when (paymentViewState) {

            is PaymentState.Loading ->{
                Log.d("PaymentForm", "Loading")
            }
            is PaymentState.Success -> {
                Log.d("PaymentForm", "Success")
                clearFormTrigger = true
            }
            is PaymentState.Error ->{
                Log.d("PaymentForm", "Error")
            }
            is PaymentState.Idle -> {
                Log.d("PaymentForm", "Idle")
            }
            is PaymentState.ValidAndReady ->{
                Log.d("PaymentForm", "ValidAndReady")
            }
            is PaymentState.Processing ->{
                Log.d("PaymentForm", "Processing")
            }
        }
    }
    LaunchedEffect(key1 = clearFormTrigger){
        if (clearFormTrigger){
            viewModel.clearSensitiveData()
            Log.d("PaymentForm", "clearFormTrigger")
            clearFormTrigger = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SelectionContainer {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (configuration.paymentMethodType == PaymentMethodType.CARD == true) {
                    if (configuration.requireAccountName) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {

                            NameOnAccountInput(viewModel, validator, modifier)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        if (!configuration.requireBillingAddress && (configuration.payorInfo == null)) {
                            CardNumberInput(modifier, viewModel, validator)
                            ExpirationInput(modifier, viewModel, validator)
                        } else {
                            CardNumberInput(modifier, viewModel, validator)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {


                        if (!configuration.requireBillingAddress && (configuration.payorInfo == null)) {
                            CvcInput(modifier, viewModel, validator)
                            PostalCodeInput(validator, viewModel, modifier)
                        } else {
                            ExpirationInput(modifier, viewModel, validator)
                            CvcInput(modifier, viewModel, validator)
                        }

                    }
                }
                else if (configuration.paymentMethodType == PaymentMethodType.ACH == true) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        NameOnAccountInput(viewModel, validator, modifier)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        BankAccountNumber(viewModel, validator, modifier)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        BankRoutingNumber(viewModel, validator, modifier)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {


                        BankAccountTypeChooser(
                            modifier,
                            configuration,
                            viewModel
                        )
                    }
                }

                if (configuration.requireBillingAddress == true == true) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AddressLine1Input(viewModel, validator, modifier)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AddressLine2Input(viewModel, modifier)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CityInput(viewModel, validator, modifier)
                        RegionInput(viewModel, validator, modifier)
                        PostalCodeInput(validator, viewModel, modifier)
                    }
                }
            }
        }
        when (paymentViewState) {
            is PaymentState.Loading ->{
                PayTheoryButton (
                    onClick = viewModel::submitPayment,
                    enabled = viewModel.isValidAndReady,) {
                    Text("Loading")
                }
            }
            is PaymentState.Success -> {
                PayTheoryButton(
                    onClick = viewModel::submitPayment,
                    enabled = viewModel.isValidAndReady,
                ) {
                    Text("Success")
                }
            }
            is PaymentState.Error ->{
                payable.handleError(PTError(ErrorCode.TokenFailed,viewModel.errorMessage))
                PayTheoryButton (
                    onClick = viewModel::submitPayment,
                    enabled = viewModel.isValidAndReady,) {
                    Text("Error")
                }
            }
            is PaymentState.Idle -> {
                PayTheoryButton(
                    onClick = viewModel::submitPayment,
                    enabled = viewModel.isValidAndReady,
                ) {
                    Text("Submit Payment")
                }
            }
            is PaymentState.ValidAndReady ->{
                payable.handleReady(true)
                PayTheoryButton (
                    onClick = viewModel::submitPayment,
                    enabled = viewModel.isValidAndReady
                ) {
                    Text("Submit Payment")
                }
            }

            PaymentState.Processing ->{
                payable.handlePaymentStart(viewModel.configuration.paymentMethodType.toString())
                PayTheoryButton (
                    onClick = viewModel::submitPayment,
                    enabled = viewModel.isValidAndReady,) {
                    Text("Processing")
                }
            }
        }

    }
}

