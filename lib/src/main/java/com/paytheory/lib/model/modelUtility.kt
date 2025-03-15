package com.paytheory.lib.model

import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.data.Address
import com.paytheory.lib.data.PaymentDetail

/**
 * Propagates the state of a payment field based on its validity and emptiness.
 *
 * This function updates the internal state of a payment field (`paymentFieldState`) and related
 * properties (`paymentFieldValid`, `paymentFieldEmpty`) based on whether the field is valid,
 * empty, or neither. It also notifies the payment context about the state change.
 *
 * @param field The payment field whose state is being propagated.
 * @param isValid `true` if the field's current input is considered valid, `false` otherwise.
 * @param isEmpty `true` if the field is currently empty, `false` otherwise.
 * @return `true` if the field is valid, `false` otherwise (same as the `isValid` input).
 *
 * The function operates based on the following logic:
 * 1. **Empty State:** If `isEmpty` is `true` and the field's current state is not `EMPTY`,
 *    it sets the state to `EMPTY`, marks the field as empty, and notifies the context.
 * 2. **Ready State:** If `isValid` is `true` and the field's current state is not `READY`,
 *    it sets the state to `READY`, marks the field as valid, potentially updates the emptiness status,
 *    and notifies the context.
 * 3. **Invalid State:** If `isEmpty` is `false`, `isValid` is `false`, and the field's current
 *    state is not `INVALID`, it sets the state to `INVALID`, marks the field as invalid,
 *    marks the field as not empty, and notifies the context.
 *
 * The context is notified through the `payTheoryPayment.context.handleStateChange` method,
 * which receives a `Pair` containing the field and its new state.
 *
 * Example Scenarios
 *  - The user clears a field: isEmpty will be true, isValid will be false, state will change to EMPTY.
 *  - The user enters a valid input: isEmpty will be false, isValid will be true, state will change to READY.
 *  - The user enters an invalid input: isEmpty will be false, isValid will be false, state will change to INVALID.
 */
internal fun propagateState(
    payTheoryProcessor: PaymentMethodProcessor,
    field: PaymentField,
    isValid: Boolean,
    isEmpty: Boolean): Boolean {
    var fieldState: FieldState? = paymentFieldState[field]

    if (isEmpty && fieldState != FieldState.EMPTY) {
        paymentFieldState[field] = FieldState.EMPTY
        paymentFieldEmpty[field] = true
        payTheoryProcessor.payable.handleStateChange(Pair(field, paymentFieldState[field]!!))
    } else if (fieldState != FieldState.READY && isValid) {
        paymentFieldState[field] = FieldState.READY
        paymentFieldValid[field] = true
        paymentFieldEmpty[field] = isEmpty
        payTheoryProcessor.payable.handleStateChange(Pair(field, paymentFieldState[field]!!))
    } else if (isEmpty == false && fieldState != FieldState.INVALID && isValid == false) {
        paymentFieldState[field] = FieldState.INVALID
        paymentFieldValid[field] = false
        paymentFieldEmpty[field] = false
        payTheoryProcessor.payable.handleStateChange(Pair(field, paymentFieldState[field]!!))
    }
    return isValid
}

internal fun PaymentViewModel.constructCardPayment(): PaymentDetail {
    return PaymentDetail(
        timing = System.currentTimeMillis(),
        amount = configuration.amount,
        type = configuration.paymentMethodType.toString(),
        name = nameOnAccount.value.secureValue.revealForUi(),
        number = cardNumber.value.secureValue.revealForUi(),
        security_code = cvc.value.secureValue.revealForUi(),
        expiration_month = if (expiration.value.secureValue.revealForUi()
                .isNotBlank()
        ) expiration.value.secureValue.revealForUi().split("/")[0] else null,
        expiration_year = if (expiration.value.secureValue.revealForUi()
                .isNotBlank()
        ) expiration.value.secureValue.revealForUi().split("/")[1] else null,
        fee_mode = configuration.feeMode,
        address = Address(
            line1 = addressLine1.value.secureValue.revealForUi(),
            line2 = addressLine2.value.secureValue.revealForUi(),
            city = city.value.secureValue.revealForUi(),
            region = region.value.secureValue.revealForUi(),
            postal_code = postalCode.value.secureValue.revealForUi()
        ),
        payorInfo = configuration.payorInfo
    )
}

internal fun PaymentViewModel.constructBankPayment(
): PaymentDetail {
    return PaymentDetail(
        timing = System.currentTimeMillis(),
        amount = configuration.amount,
        type = configuration.paymentMethodType.toString(),
        account_type = bankAccountType.value,
        name = nameOnAccount.value.secureValue.revealForUi(),
        account_number = bankAccountNumber.value.secureValue.revealForUi(),
        bank_code = bankRoutingNumber.value.secureValue.revealForUi(),
        fee_mode = configuration.feeMode,
        address = Address(
            line1 = addressLine1.value.secureValue.revealForUi(),
            line2 = addressLine2.value.secureValue.revealForUi(),
            city = city.value.secureValue.revealForUi(),
            region = region.value.secureValue.revealForUi(),
            postal_code = postalCode.value.secureValue.revealForUi()
        ),
        payorInfo = configuration.payorInfo
    )
}