package com.paytheory.android.sdk.state

/**
 * Data class that holds state of cash payment fields
 */
class CashState {
    /**
     * Variable that holds state of payer name field
     */
    var payerName: StateElement = StateElement()
    /**
     * Variable that holds state of contact information field
     */
    var contactInformation: StateElement = StateElement()
}