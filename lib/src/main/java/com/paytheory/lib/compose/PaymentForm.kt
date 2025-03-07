package com.paytheory.lib.compose

import android.accounts.NetworkErrorException
import android.content.Context
import android.icu.util.TimeZone
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paytheory.lib.ErrorCode
import com.paytheory.lib.PTError
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.R
import com.paytheory.lib.compose.inputs.SecureBankNumberField
import com.paytheory.lib.compose.inputs.SecureCardNumberField
import com.paytheory.lib.compose.inputs.SecureCvcField
import com.paytheory.lib.compose.inputs.SecureExpirationField
import com.paytheory.lib.compose.inputs.SecureStandardTextField
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.model.PaymentViewModel.PaymentState
import com.paytheory.lib.valid.Validator


private const val NO_NETWORK_CONNECTION = "No valid network connection"




private fun isNetworkAvailable(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } == true
    }


/**
 * Creates payTheoryData object for transfer requests
 * @param configuration PayTheoryConfiguration object
 * @return HashMap<Any, Any> of pay theory data for the request
 */
fun createPayTheoryData(configuration: PayTheoryConfiguration): HashMap<Any, Any> {
    //create pay_theory_data object for host:transfer_part1 action request
    val payTheoryData = hashMapOf<Any, Any>()

    payTheoryData["skip_validation"] = configuration.skipTokenizeValidation

    payTheoryData["send_receipt"] = configuration.sendReceipt
    if (configuration.sendReceipt == true) {

        if (configuration.receiptDescription.isNotBlank()){
            payTheoryData["receipt_description"] = configuration.receiptDescription
        }
    }
    // if paymentParameters is given add to pay_theory_data
    if (configuration.paymentParameters.isNotBlank()) {
        payTheoryData["payment_parameters"] = configuration.paymentParameters
    }
    // if payorId is given add to pay_theory_data
    if (configuration.payorId.isNotBlank()) {
        payTheoryData["payorId"] = configuration.payorId
    }
    // if invoiceId is given add to pay_theory_data
    if (configuration.invoiceId.isNotBlank()) {
        payTheoryData["invoice_id"] = configuration.invoiceId
    }
    // if account_code is given add to pay_theory_data
    if (configuration.accountCode.isNotBlank()) {
        payTheoryData["account_code"] = configuration.accountCode
    }
    // if reference is given add to pay_theory_data
    if (configuration.reference.isNotBlank()) {
        payTheoryData["reference"] = configuration.reference
    }

    payTheoryData["fee"] = configuration.serviceFee as Any

    payTheoryData["timezone"] = TimeZone.getDefault().id

    return payTheoryData
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BankAccountTypeChooser(
    modifier: Modifier,
    configuration: PayTheoryConfiguration,
    viewModel: PaymentViewModel
) {
    val options: List<String> = listOf(
        stringResource(id = R.string.checking_account),
        stringResource(id = R.string.savings_account)
    )
    var expanded by remember { mutableStateOf(false) }

    // Clear the selected option when clearKey changes (viewModel.clearCount.intValue changes)
    LaunchedEffect(key1 = viewModel.clearCount.intValue) {
        viewModel.updateBankAccountType("") // Clear the selection in the ViewModel
    }

    // State to track the TextFieldValue, derived from viewModel.bankAccountType
    val textFieldValue = remember(viewModel.bankAccountType.value) {
        mutableStateOf(TextFieldValue(viewModel.bankAccountType.value))
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        // Use either OutlinedTextField or TextField based on the configuration
        val textFieldModifier = Modifier
            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
            .fillMaxWidth()
        if (configuration.outlined) {
            OutlinedTextField(
                value = textFieldValue.value,
                onValueChange = { /* Do nothing, read-only */ },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = textFieldModifier,
                placeholder = { Text(stringResource(id = R.string.account_type)) }
            )
        } else {
            TextField(
                value = textFieldValue.value,
                onValueChange = { /* Do nothing, read-only */ },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = textFieldModifier,
                placeholder = { Text(stringResource(id = R.string.account_type)) }
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        viewModel.updateBankAccountType(option) // Update ViewModel
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
//if (configuration.outlined) {
//    OutlinedTextField(
//        // The `menuAnchor` modifier must be passed to the text field to handle
//        // expanding/collapsing the menu on click. A read-only text field has
//        // the anchor type `PrimaryNotEditable`.
//        modifier = Modifier
//            .fillMaxWidth()
//            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
//        state = textFieldState,
//        readOnly = true,
//        lineLimits = TextFieldLineLimits.SingleLine,
//        label = { Text(stringResource(id = R.string.account_type)) },
//        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//    )
//    ExposedDropdownMenu(
//        modifier = Modifier.fillMaxWidth(),
//        expanded = expanded,
//
//        onDismissRequest = { expanded = false },
//    ) {
//        options.forEach { option ->
//            DropdownMenuItem(
//                text = {
//                    Text(
//                        option
//                    )
//                },
//                onClick = {
//                    viewModel.updateBankAccountType(option)
//                    textFieldState.setTextAndPlaceCursorAtEnd(viewModel.bankAccountType.value)
//                    expanded = false
//                },
//
//                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//            )
//        }
//    }
//} else {
//    TextField(
//        // The `menuAnchor` modifier must be passed to the text field to handle
//        // expanding/collapsing the menu on click. A read-only text field has
//        // the anchor type `PrimaryNotEditable`.
//        modifier = Modifier
//            .fillMaxWidth()
//            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
//        state = textFieldState,
//        readOnly = true,
//        lineLimits = TextFieldLineLimits.SingleLine,
//        label = { Text(stringResource(id = R.string.account_type)) },
//        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//    )
//    ExposedDropdownMenu(
//        modifier = Modifier.fillMaxWidth(),
//        expanded = expanded,
//        onDismissRequest = { expanded = false },
//    ) {
//        options.forEach { option ->
//            DropdownMenuItem(
//                text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
//                onClick = {
//                    textFieldState.setTextAndPlaceCursorAtEnd(option)
//                    expanded = false
//                    viewModel.updateBankAccountType(option)
//                },
//                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
//            )
//        }
//    }
//}

@Composable
private fun RowScope.RegionInput(
    viewModel: PaymentViewModel,
    validator: Validator,
    modifier: Modifier
) {
    SecureStandardTextField(
        label = stringResource(id = R.string.state),
        value = viewModel.region.value,
        onValueChange = viewModel::updateRegion,
        isValid = validator::isNotEmpty,
        modifier = modifier.weight(1f),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun RowScope.CityInput(
    viewModel: PaymentViewModel,
    validator: Validator,
    modifier: Modifier
) {
    SecureStandardTextField(
        label = stringResource(id = R.string.city),
        value = viewModel.city.value,
        onValueChange = viewModel::updateCity,
        isValid = validator::isNotEmpty,
        modifier = modifier.weight(2f),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun AddressLine2Input(
    viewModel: PaymentViewModel,
    modifier: Modifier
) {
    SecureStandardTextField(
        label = stringResource(id = R.string.address_2),
        value = viewModel.addressLine2.value,
        onValueChange = viewModel::updateAddressLine2,
        modifier = modifier.fillMaxWidth(),
        isValid = { true },
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun AddressLine1Input(
    viewModel: PaymentViewModel,
    validator: Validator,
    modifier: Modifier
) {
    SecureStandardTextField(
        label = stringResource(id = R.string.address_1),//"Address Line 1"
        value = viewModel.addressLine1.value,
        onValueChange = viewModel::updateAddressLine1,
        isValid = validator::isNotEmpty,
        modifier = modifier.fillMaxWidth(),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun BankRoutingNumber(
    viewModel: PaymentViewModel,
    validator: Validator,
    modifier: Modifier
) {
    SecureBankNumberField(
        value = viewModel.bankRoutingNumber.value,
        onValueChange = viewModel::updateBankRoutingNumber,
        isRouting = true,
        isValid = validator::isValidBankRoutingNumber,
        modifier = modifier.fillMaxWidth(),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun BankAccountNumber(
    viewModel: PaymentViewModel,
    validator: Validator,
    modifier: Modifier
) {
    SecureBankNumberField(
        value = viewModel.bankAccountNumber.value,
        onValueChange = viewModel::updateBankAccountNumber,
        isValid = validator::isValidBankAccountNumber,
        modifier = modifier.fillMaxWidth(),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun RowScope.PostalCodeInput(
    validator: Validator,
    viewModel: PaymentViewModel,
    modifier: Modifier
) {
    SecureStandardTextField(
        label = "Zip",
        isValid = validator::isValidPostalCode,
        maxLength = 6,
        value = viewModel.postalCode.value,
        onValueChange = viewModel::updatePostalCode,
        modifier = modifier.weight(1f),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun RowScope.CvcInput(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
) {
    SecureCvcField(
        modifier = modifier.weight(1f),
        value = viewModel.cvc.value,
        onValueChange = viewModel::updateCvc,
        isValid = validator::isValidCvc,
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun RowScope.ExpirationInput(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
) {
    SecureExpirationField(
        modifier = modifier.weight(1.25f),
        value = viewModel.expiration.value,
        onValueChange = viewModel::updateExpiration,
        isValid = validator::isValidExpiration,
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun RowScope.CardNumberInput(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
) {
    SecureCardNumberField(
        modifier = modifier.weight(3f),
        value = viewModel.cardNumber.value,
        onValueChange = viewModel::updateCardNumber,
        isValid = validator::isValidCardNumber,
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

@Composable
private fun NameOnAccountInput(
    viewModel: PaymentViewModel,
    validator: Validator,
    modifier: Modifier
) {
    SecureStandardTextField(
        label = stringResource(id = R.string.name_on_account),
        value = viewModel.nameOnAccount.value,
        onValueChange = viewModel::updateNameOnAccount,
        isValid = validator::isNotEmpty,
        modifier = modifier.fillMaxWidth(),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}