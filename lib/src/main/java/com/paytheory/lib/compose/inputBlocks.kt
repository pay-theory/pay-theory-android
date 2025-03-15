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

/**
 * Composable function for rendering a region/state input field within a row.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
@Composable
internal fun RowScope.RegionInput(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
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

/**
 * Composable function for rendering a city input field within a row.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
@Composable
internal fun RowScope.CityInput(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
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

/**
 * Composable function for rendering an optional address line 2 input field.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 */
@Composable
internal fun AddressLine2Input(
    modifier: Modifier,
    viewModel: PaymentViewModel
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

/**
 * Composable function for rendering the primary address input field.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
@Composable
internal fun AddressLine1Input(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
) {
    SecureStandardTextField(
        label = stringResource(id = R.string.address_1),
        value = viewModel.addressLine1.value,
        onValueChange = viewModel::updateAddressLine1,
        isValid = validator::isNotEmpty,
        modifier = modifier.fillMaxWidth(),
        isOutlined = viewModel.configuration.outlined,
        clearKey = viewModel.clearCount.intValue
    )
}

/**
 * Composable function for rendering a bank routing number input field.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
@Composable
internal fun BankRoutingNumber(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
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

/**
 * Composable function for rendering a bank account number input field.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
@Composable
internal fun BankAccountNumber(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator,
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

/**
 * Composable function for rendering a postal code input field within a row.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
@Composable
internal fun RowScope.PostalCodeInput(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
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

/**
 * Composable function for rendering a card CVC input field within a row.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
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

/**
 * Composable function for rendering a card expiration date input field within a row.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
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

/**
 * Composable function for rendering a card number input field within a row.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
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

/**
 * Composable function for rendering a name on account input field.
 *
 * @param modifier The modifier to be applied to the input field
 * @param viewModel The PaymentViewModel instance managing the payment form state
 * @param validator The validator instance for input validation
 */
@Composable
internal fun NameOnAccountInput(
    modifier: Modifier,
    viewModel: PaymentViewModel,
    validator: Validator
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

/**
 * Composable function for rendering a bank account type dropdown selector.
 * 
 * This component provides a dropdown menu for selecting between checking and savings account types.
 * The selection is cleared when the clearKey (viewModel.clearCount.intValue) changes.
 *
 * @param modifier The modifier to be applied to the dropdown
 * @param configuration The PayTheory configuration instance
 * @param viewModel The PaymentViewModel instance managing the payment form state
 */
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

    LaunchedEffect(key1 = viewModel.clearCount.intValue) {
        viewModel.updateBankAccountType("")
    }

    val textFieldValue = remember(viewModel.bankAccountType.value) {
        mutableStateOf(TextFieldValue(viewModel.bankAccountType.value))
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
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
                        viewModel.updateBankAccountType(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}