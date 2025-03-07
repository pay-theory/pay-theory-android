package com.paytheory.lib.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.R
import com.paytheory.lib.compose.inputs.SecureBankNumberField
import com.paytheory.lib.compose.inputs.SecureCardNumberField
import com.paytheory.lib.compose.inputs.SecureCvcField
import com.paytheory.lib.compose.inputs.SecureExpirationField
import com.paytheory.lib.compose.inputs.SecureStandardTextField
import com.paytheory.lib.model.PaymentViewModel
import com.paytheory.lib.valid.Validator


@Composable
internal fun RowScope.RegionInput(
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
internal fun RowScope.CityInput(
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
internal fun AddressLine2Input(
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
internal fun AddressLine1Input(
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
internal fun BankRoutingNumber(
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
internal fun BankAccountNumber(
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
internal fun RowScope.PostalCodeInput(
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
internal fun RowScope.CvcInput(
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
internal fun RowScope.ExpirationInput(
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
internal fun RowScope.CardNumberInput(
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
internal fun NameOnAccountInput(
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun BankAccountTypeChooser(
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