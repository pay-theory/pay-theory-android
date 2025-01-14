package com.paytheory.android.sdk.state

/**
 * Class that holds the states of the payment fields
 */
class CardState {
    /**
     * State of card number field
     */
    var cardNumber: StateElement = StateElement()

    /**
     * State of expiration date field
     */
    var expirationDate: StateElement = StateElement()

    /**
     * State of cvv field
     */
    var cvv: StateElement = StateElement()

    /**
     * State of postal code field
     */
    var postalCode: StateElement = StateElement()
}