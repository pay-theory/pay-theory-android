package com.paytheory.android.sdk.state

/**
 * Interface that defines methods for checking the validity and emptiness of an object.
 */
interface ValidAndEmpty {
    /**
     * Checks if the object is valid.
     *
     * @return True if the object is valid, false otherwise.
     */
    fun isValid(): Boolean

    /**
     * Checks if the object is empty.
     *
     * An object is considered empty if it does not contain any meaningful data.
     * For example, an empty string or an empty list would be considered empty.
     *
     * @return True if the object is empty, false otherwise.
     */
    fun isEmpty(): Boolean
}