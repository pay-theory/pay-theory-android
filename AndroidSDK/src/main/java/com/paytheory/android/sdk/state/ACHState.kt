package com.paytheory.android.sdk.state

/*
* Modernization
* States use state elements to support ValidAndEmpty Protocol
* States are accessible from PayTheoryFragment with the syntax:
* payTheoryFragment.ach.accountNumber.isValid()
* payTheoryFragment.ach.accountNumber.isEmpty()
* */

class ACHState {
    var accountNumber: StateElement = StateElement()
    var routingNumber: StateElement = StateElement()
    var cvv: StateElement = StateElement()
    var postalCode: StateElement = StateElement()
    var accountHolderName: StateElement = StateElement()
    var accountType: StateElement = StateElement()
}