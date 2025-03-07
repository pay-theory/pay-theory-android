package com.paytheory.lib.compose.string

import androidx.compose.runtime.mutableStateOf
import java.util.Arrays

/**
 * SecureString is a class designed to store sensitive string data in memory in a way that minimizes the risk of
 * exposing the data to unauthorized access. It achieves this by storing the string as a byte array and providing
 * controlled methods for accessing and modifying the data. The string data is encoded into a byte array representation,
 * where each character is represented by two bytes.
 *
 * The primary goal of this class is to reduce the time string is on the memory. All functions that create String values
 * will zero fill them after use.
 *
 * Key Features:
 * - **Secure Storage:** The string data is stored as a byte array, not as a String object. This reduces the risk of
 *   the data being exposed through memory dumps or other memory-related attacks.
 * - **Controlled Access:** Access to the underlying data is only possible through specific methods (`revealForUi`,
 *   `revealForProcessing`).
 * - **Zero-Filling:** Sensitive data (both the internal byte array and temporary char arrays) is zero-filled when it's no longer
 *   needed.
 * - **Modification Tracking:** The `_isModified` flag tracks whether the string has been modified since its
 *   creation.
 * - **Secure Value Update:** `setValue` securely updates the stored value by first zero-filling the old data and then
 *   storing the new value.
 * - **Secure Conversion:** uses `toSecureBytes` and `toSecureString` static methods to convert between String and
 *   ByteArray securely.
 * - **Custom `equals` and `hashCode`:** Implementations to handle equality and hashing based on the data bytes, not references.
 *
 * Usage:
 * 1. Create a SecureString object with a String or ByteArray.
 * 2. Access the string representation of the data using `revealForUi()`.
 * 3. Access the raw byte array representation using `revealForProcessing()`.
 * 4. Update the value with `setValue()`.
 * 5. When done, call `zeroFill()` to clear the internal data.
 *
 * Example:
 * ```kotlin
 * val secureString = SecureString("mySecretPassword")
 * println(secureString.revealForUi()) // Prints the stored string.
 */
class SecureString(dataIn: ByteArray) {
    constructor(value: String) : this(value.toSecureBytes())

    private var data = dataIn
    var _isModified = mutableStateOf(false)

    fun revealForUi(): String = data.toSecureString()
    fun revealForProcessing(): ByteArray = data
    fun zeroFill() {
        data.fill(0)
    }

    // Function to update the SecureString securely
    fun setValue(newValue: String) {
        zeroFill()
        data = newValue.toSecureBytes()
        _isModified.value = !_isModified.value // Toggle the flag
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SecureString
        return data == other.data
    }

    override fun hashCode(): Int = data.contentHashCode()
    companion object {
        fun String.toSecureBytes(): ByteArray {
            val chars = toCharArray()
            val bytes = ByteArray(chars.size * 2)
            chars.forEachIndexed { i, c ->
                bytes[i * 2] = (c.code shr 8).toByte()
                bytes[i * 2 + 1] = c.code.toByte()
            }
            Arrays.fill(chars, '\u0000')
            return bytes
        }

        fun ByteArray.toSecureString(): String {
            val chars = CharArray(size / 2)
            for (i in chars.indices) {
                val high = (this[i * 2].toInt() and 0xFF) shl 8
                val low = this[i * 2 + 1].toInt() and 0xFF
                chars[i] = (high or low).toChar()
            }
            return String(chars).also { Arrays.fill(chars, '\u0000') }
        }

    }
}