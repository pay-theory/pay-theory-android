package com.paytheory.android.sdk.state

/**
 * class that holds state of ACH fields
 */
class ACHState {
    /**
     * State element for account name field
     */
    var accountName: StateElement = StateElement()
    /**
     * State element for account number field
     */
    var accountNumber: StateElement = StateElement()
    /**
     * State element for account name field
     */
    var accountType: StateElement = StateElement()
    /**
     * State element for routing number field
     */
    var routingNumber: StateElement = StateElement()
}