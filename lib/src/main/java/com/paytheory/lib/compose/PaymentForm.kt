package com.paytheory.lib.compose

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
    fieldModifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
) {
    val validator = Validator()

    if (!isNetworkAvailable(LocalContext.current)) {
        payable.handleError(PTError(ErrorCode.TokenFailed,NO_NETWORK_CONNECTION))
//        throw NetworkErrorException(NO_NETWORK_CONNECTION)
    } else {
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

        LaunchedEffect(key1 = clearFormTrigger){
            if (clearFormTrigger){
                viewModel.clearSensitiveData()
                Log.d("PaymentForm", "clearFormTrigger")
                clearFormTrigger = false
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

                                NameOnAccountInput(fieldModifier, viewModel, validator)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            if (!configuration.requireBillingAddress && (configuration.payorInfo == null)) {
                                CardNumberInput(fieldModifier, viewModel, validator)
                                ExpirationInput(fieldModifier, viewModel, validator)
                            } else {
                                CardNumberInput(fieldModifier, viewModel, validator)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {


                            if (!configuration.requireBillingAddress && (configuration.payorInfo == null)) {
                                CvcInput(fieldModifier, viewModel, validator)
                                PostalCodeInput(fieldModifier, viewModel, validator)
                            } else {
                                ExpirationInput(fieldModifier, viewModel, validator)
                                CvcInput(fieldModifier, viewModel, validator)
                            }

                        }
                    }
                    else if (configuration.paymentMethodType == PaymentMethodType.ACH == true) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            NameOnAccountInput(fieldModifier, viewModel, validator)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            BankAccountNumber(fieldModifier, viewModel, validator)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            BankRoutingNumber(fieldModifier, viewModel, validator)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {


                            BankAccountTypeChooser(
                                fieldModifier,
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
                            AddressLine1Input(fieldModifier, viewModel, validator)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AddressLine2Input(fieldModifier, viewModel)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CityInput(fieldModifier, viewModel, validator)
                            RegionInput(fieldModifier, viewModel, validator)
                            PostalCodeInput(fieldModifier, viewModel, validator)
                        }
                    }
                }
            }
            when (paymentViewState) {
                is PaymentState.Loading ->{
//                    payable.handleReady(false)
                    PayTheoryButton (
                        enabled = viewModel.isValidAndReady,
                        onClick = viewModel::submitPayment,
                        content = {
                            Text("Loading")
                        },
                        modifier = buttonModifier
                    )
                }
                is PaymentState.Success -> {
                    PayTheoryButton(
                        enabled = viewModel.isValidAndReady,
                        onClick = viewModel::submitPayment,
                        content = {
                            Text("Success")
                        },
                        modifier = buttonModifier
                    )
                }
                is PaymentState.Error ->{
                    payable.handleError(PTError(ErrorCode.TokenFailed,viewModel.errorMessage))
                    PayTheoryButton (
                        enabled = viewModel.isValidAndReady,
                        onClick = viewModel::submitPayment,
                        content = {
                            Text("Error")
                        },
                        modifier = buttonModifier
                    )
                }
                is PaymentState.Idle -> {
//                    payable.handleReady(viewModel.isValidAndReady)
                    PayTheoryButton(
                        enabled = viewModel.isValidAndReady,
                        onClick = viewModel::submitPayment,
                        content = {
                            Text("Submit Payment")
                        },
                        modifier = buttonModifier
                    )
                }
                is PaymentState.ValidAndReady ->{
//                    payable.handleReady(viewModel.isValidAndReady)
                    PayTheoryButton(
                        enabled = viewModel.isValidAndReady,
                        onClick = viewModel::submitPayment,
                        modifier = buttonModifier,
                        content = {
                            Text("Submit Payment")
                        }
                    )
                }

                PaymentState.Processing ->{
                    PayTheoryButton (
                        onClick = viewModel::submitPayment,
                        enabled = viewModel.isValidAndReady,
                        modifier = buttonModifier,
                        content = {
                            Text("Processing")
                        }
                    )
                }
            }
        }
    }
}


