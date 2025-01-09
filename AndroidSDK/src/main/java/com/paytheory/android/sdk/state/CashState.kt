package com.paytheory.android.sdk.state

/*
* Modernization
* States use state elements to support ValidAndEmpty Protocol
* States are accessible from PayTheoryFragment with the syntax:
* payTheoryFragment.cash.contactInformation.isValid()
* payTheoryFragment.card.contactInformation.isEmpty()
* */
class CashState {
    var payerName: StateElement = StateElement()
    var contactInformation: StateElement = StateElement()
}