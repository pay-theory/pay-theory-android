package com.paytheory.android.sdk.nacl

import android.os.Build
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair
import java.util.*


private val lazySodium = LazySodiumAndroid(SodiumAndroid())
private val boxLazy = lazySodium as Box.Lazy
private val keyPair = boxLazy.cryptoBoxKeypair()
private val secretKey = keyPair.secretKey
private val publicKey = keyPair.publicKey

/**
 * Function to generate KeyPair
 */
fun generateLocalKeyPair(): String {

    val returnedPublicKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Base64.getEncoder().encodeToString(publicKey.asBytes)
    } else {
        android.util.Base64.encodeToString(publicKey.asBytes,android.util.Base64.DEFAULT)
    }

    return returnedPublicKey
}

/**
 * Function to encrypt message
 * @param message the message to be encrypted
 * @param publicKey the key used to encrypt
 */
fun encryptBox(message: String): String {
    return boxLazy.cryptoBoxSealEasy(
        message,
        publicKey
    )
}

fun decryptBox(message: String): String {
    val message = boxLazy.cryptoBoxSealOpenEasy(
        message,
        KeyPair(publicKey, secretKey)
    )
    return message
}