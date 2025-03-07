package com.paytheory.lib.compose.string

import androidx.compose.runtime.mutableStateOf
import java.util.Arrays

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