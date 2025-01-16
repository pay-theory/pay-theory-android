package com.paytheory.android.sdk.state

/**
 * class that holds state of ACH fields
 */
class AddressState {
    /**
     * State element for address line 1 field
     */
    var addressLine1: StateElement = StateElement()
    /**
     * State element for city field
     */
    var city: StateElement = StateElement()
    /**
     * State element for region field
     */
    var region: StateElement = StateElement()
}