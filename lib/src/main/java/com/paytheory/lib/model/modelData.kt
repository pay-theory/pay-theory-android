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
    PaymentField.NAME_ON_ACCOUNT to true,
    PaymentField.BANK_ACCOUNT_NUMBER to true,
    PaymentField.BANK_ROUTING_NUMBER to true,
    PaymentField.BANK_ACCOUNT_TYPE to true,
    PaymentField.CARD_NUMBER to true,
    PaymentField.CARD_EXPIRATION to true,
    PaymentField.CARD_CVC to true,
    PaymentField.ADDRESS_LINE1 to true,
    PaymentField.ADDRESS_LINE2 to true,
    PaymentField.CITY to true,
    PaymentField.REGION to true,
    PaymentField.POSTAL_CODE to true
)
val paymentFieldEmpty: HashMap<PaymentField, Boolean> = hashMapOf(
    PaymentField.NAME_ON_ACCOUNT to false,
    PaymentField.BANK_ACCOUNT_NUMBER to false,
    PaymentField.BANK_ROUTING_NUMBER to false,
    PaymentField.BANK_ACCOUNT_TYPE to false,
    PaymentField.CARD_NUMBER to false,
    PaymentField.CARD_EXPIRATION to false,
    PaymentField.CARD_CVC to false,
    PaymentField.ADDRESS_LINE1 to false,
    PaymentField.ADDRESS_LINE2 to false,
    PaymentField.CITY to false,
    PaymentField.REGION to false,
    PaymentField.POSTAL_CODE to false
)
val paymentFieldState: HashMap<PaymentField, FieldState> = hashMapOf(
    PaymentField.NAME_ON_ACCOUNT to FieldState.INIT,
    PaymentField.BANK_ACCOUNT_NUMBER to FieldState.INIT,
    PaymentField.BANK_ROUTING_NUMBER to FieldState.INIT,
    PaymentField.BANK_ACCOUNT_TYPE to FieldState.INIT,
    PaymentField.CARD_NUMBER to FieldState.INIT,
    PaymentField.CARD_EXPIRATION to FieldState.INIT,
    PaymentField.CARD_CVC to FieldState.INIT,
    PaymentField.ADDRESS_LINE1 to FieldState.INIT,
    PaymentField.ADDRESS_LINE2 to FieldState.INIT,
    PaymentField.CITY to FieldState.INIT,
    PaymentField.REGION to FieldState.INIT,
    PaymentField.POSTAL_CODE to FieldState.INIT
)