package com.paytheory.lib.model

enum class BankFields {
    NAME_ON_ACCOUNT,
    BANK_ACCOUNT_NUMBER,
    BANK_ROUTING_NUMBER,
    BANK_ACCOUNT_TYPE
}
enum class CreditCardFields {
    CARD_NUMBER,
    CARD_EXPIRATION,
    CARD_CVC
}
enum class AddressFields {
    ADDRESS_LINE1,
    CITY,
    REGION,
    POSTAL_CODE
}
enum class FieldState {
    EMPTY,
    READY,
    INVALID,
    INIT
}
enum class PaymentField {
    NAME_ON_ACCOUNT,
    BANK_ACCOUNT_NUMBER,
    BANK_ROUTING_NUMBER,
    BANK_ACCOUNT_TYPE,
    CARD_NUMBER,
    CARD_EXPIRATION,
    CARD_CVC,
    ADDRESS_LINE1,
    ADDRESS_LINE2,
    CITY,
    REGION,
    POSTAL_CODE
}

val paymentFieldValid: HashMap<PaymentField, Boolean> = hashMapOf(
    Pair<PaymentField, Boolean>(PaymentField.NAME_ON_ACCOUNT, true),
    Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_NUMBER, true),
    Pair<PaymentField, Boolean>(PaymentField.BANK_ROUTING_NUMBER, true),
    Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_TYPE, true),
    Pair<PaymentField, Boolean>(PaymentField.CARD_NUMBER, true),
    Pair<PaymentField, Boolean>(PaymentField.CARD_EXPIRATION, true),
    Pair<PaymentField, Boolean>(PaymentField.CARD_CVC, true),
    Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE1, true),
    Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE2, true),
    Pair<PaymentField, Boolean>(PaymentField.CITY, true),
    Pair<PaymentField, Boolean>(PaymentField.REGION, true),
    Pair<PaymentField, Boolean>(PaymentField.POSTAL_CODE, true)
)
val paymentFieldEmpty: HashMap<PaymentField, Boolean> = hashMapOf(
    Pair<PaymentField, Boolean>(PaymentField.NAME_ON_ACCOUNT, false),
    Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_NUMBER, false),
    Pair<PaymentField, Boolean>(PaymentField.BANK_ROUTING_NUMBER, false),
    Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_TYPE, false),
    Pair<PaymentField, Boolean>(PaymentField.CARD_NUMBER, false),
    Pair<PaymentField, Boolean>(PaymentField.CARD_EXPIRATION, false),
    Pair<PaymentField, Boolean>(PaymentField.CARD_CVC, false),
    Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE1, false),
    Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE2, false),
    Pair<PaymentField, Boolean>(PaymentField.CITY, false),
    Pair<PaymentField, Boolean>(PaymentField.REGION, false),
    Pair<PaymentField, Boolean>(PaymentField.POSTAL_CODE, false)
)
val paymentFieldState: HashMap<PaymentField, FieldState> = hashMapOf(
    Pair<PaymentField, FieldState>(PaymentField.NAME_ON_ACCOUNT, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.BANK_ACCOUNT_NUMBER, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.BANK_ROUTING_NUMBER, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.BANK_ACCOUNT_TYPE, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.CARD_NUMBER, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.CARD_EXPIRATION, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.CARD_CVC, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.ADDRESS_LINE1, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.ADDRESS_LINE2, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.CITY, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.REGION, FieldState.INIT),
    Pair<PaymentField, FieldState>(PaymentField.POSTAL_CODE, FieldState.INIT)
)