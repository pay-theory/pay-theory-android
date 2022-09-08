package com.paytheory.android.sdk.nacl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair


private val lazySodium = LazySodiumAndroid(SodiumAndroid())
val boxLazy = lazySodium as Box.Lazy
private val keyPair = boxLazy.cryptoBoxKeypair()
//private val secretKey = keyPair.secretKey
//private val publicKey = keyPair.publicKey

/**
 * Function to generate KeyPair
 */
fun generateLocalKeyPair(): KeyPair {
    return keyPair
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

fun decryptBox(message: String): String {
    val message = boxLazy.cryptoBoxSealOpenEasy(
        message,
        keyPair
    )
    return message
}