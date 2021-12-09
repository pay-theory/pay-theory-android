package com.paytheory.android.sdk.nacl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair


private val lazySodium = LazySodiumAndroid(SodiumAndroid())
val boxLazy = lazySodium as Box.Lazy

/**
 * Function to generate KeyPair
 */
fun generateLocalKeyPair(): KeyPair {
    return boxLazy.cryptoBoxKeypair()
}

/**
 * Function to encrypt message
 * @param message the message to be encrypted
 * @param publicKey the key used to encrypt
 */
fun encryptBox(message: String, publicKey: Key): String {
    return boxLazy.cryptoBoxSealEasy(
        message,
        publicKey
    )
}