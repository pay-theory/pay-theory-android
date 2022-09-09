package com.paytheory.android.sdk.nacl

import android.os.Build
import android.util.Log
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair
import java.util.*


private val lazySodium = LazySodiumAndroid(SodiumAndroid())
private val boxLazy = lazySodium as Box.Lazy
private lateinit var keyPair: KeyPair
//private val secretKey = keyPair.secretKey
//private val publicKey = keyPair.publicKey

///**
// * Function to generate KeyPair
// */
//fun generateLocalKeyPair(): String {
//
//    val returnedPublicKey = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        Base64.getEncoder().encodeToString(publicKey.asBytes)
//    } else {
//        android.util.Base64.encodeToString(publicKey.asBytes,android.util.Base64.DEFAULT)
//    }
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        Log.d("PAYTHEORY", Base64.getEncoder().encodeToString(publicKey.asBytes))
//    }
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        Log.d("PAYTHEORY", Base64.getEncoder().encodeToString(secretKey.asBytes))
//    }
//
//    return returnedPublicKey
//}

/**
 * Function to generate KeyPair
 */
fun generateLocalKeyPair(): KeyPair {
    keyPair = boxLazy.cryptoBoxKeypair()
    Log.d("PT- generateLocalKeyPair SECRET KEY", Base64.getEncoder().encodeToString(keyPair.secretKey.asBytes))
    Log.d("PT- generateLocalKeyPair PUBLIC KEY", Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes))
    return keyPair
}


///**
// * Function to encrypt message
// * @param message the message to be encrypted
// * @param publicKey the key used to encrypt
// */
//fun encryptBox(message: String): String {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        Log.d("PAYTHEORY", Base64.getEncoder().encodeToString(publicKey.asBytes))
//    }
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        Log.d("PAYTHEORY", Base64.getEncoder().encodeToString(secretKey.asBytes))
//    }
//
//    return boxLazy.cryptoBoxSealEasy(
//        message,
//        publicKey
//    )
//}

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
    Log.d("PT- decryptBox SECRET KEY", Base64.getEncoder().encodeToString(keyPair.secretKey.asBytes))
    Log.d("PT- decryptBox PUBLIC KEY", Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes))
    val message = boxLazy.cryptoBoxSealOpenEasy(
        message,
        keyPair
    )
    return message
}