package com.paytheory.android.sdk.state

/*
* Modernization
* States use state elements to support ValidAndEmpty Protocol
* States are accessible from PayTheoryFragment with the syntax:
* payTheoryFragment.card.number.isValid()
* payTheoryFragment.card.number.isEmpty()
* */

class CardState {
    var cardNumber: StateElement = StateElement()
    var expirationDate: StateElement = StateElement()
    var cvv: StateElement = StateElement()
    var postalCode: StateElement = StateElement()
    var cardholderName: StateElement = StateElement()
    var billingAddressDetails: StateElement = StateElement()
}