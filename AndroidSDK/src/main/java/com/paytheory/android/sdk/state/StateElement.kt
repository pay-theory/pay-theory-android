package com.paytheory.android.sdk.state

/**
 * Class that represents the state of an element
 */
class StateElement: ValidAndEmpty {
    private var valid: Boolean = false
    private var empty: Boolean = true

    /**
     * Function that sets the validity state of the StateElement
     * @param validIn validity of the element
     */
    fun setValid(validIn:Boolean) {
        valid = validIn
    }

    /**
     * Function that sets the empty state of the StateElement
     * @param emptyIn empty state of the element
     */
    fun setEmpty(emptyIn: Boolean) {
        empty = emptyIn
    }

    /**
     * Function that returns the validity state of the StateElement
     * @return validity state of the element
     */
    override fun isValid(): Boolean {
        return valid
    }

    /**
     * Function that returns the empty state of the StateElement
     * @return empty state of the element
     */
    override fun isEmpty():Boolean {
        return empty
    }


}