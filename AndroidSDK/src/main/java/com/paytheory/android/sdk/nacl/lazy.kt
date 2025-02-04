package com.paytheory.android.sdk.nacl

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Base64MessageEncoder
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair

/**
 * Object that handles lazy sodium functions
 */
private val lazySodium = LazySodiumAndroid(SodiumAndroid())
private val boxLazy = lazySodium as Box.Lazy
private lateinit var keyPair: KeyPair

/**
 * Function to generate KeyPair
 */
fun generateLocalKeyPair(): KeyPair {
    keyPair = boxLazy.cryptoBoxKeypair()
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

/**
 * Function to decrypt messages
 * @param message the message to be encrypted
 * @param base64socketPublicKey a base 64 encoded key
 * @return the decrypted string
 */
fun decryptBox(message: String, base64socketPublicKey: String): String {
    //Take public key passed from tag-secure-socket and base64 decode it and convert to lazysodium key
    val socketPublicKey = Key.fromBase64String(base64socketPublicKey)
    //base64 decode message with nonce into byte array
    val messageWithNonceByteArray = Base64MessageEncoder().decode(message)
    //remove first 24 bytes for nonce value
    val nonceOnlyByteArray: ByteArray = messageWithNonceByteArray.copyOfRange(0, 24)
    //remove after first 24 bytes for message value
    val messageOnlyByteArray: ByteArray =
        messageWithNonceByteArray.copyOfRange(24, messageWithNonceByteArray.size)
    //convert message from bytearray into string
    val messageOnlyString = lazySodium.sodiumBin2Hex(messageOnlyByteArray)
    //create key pair with generated secret key and tags-secure-socket public key
    val messageKeys = KeyPair(socketPublicKey, keyPair.secretKey)
    return boxLazy.cryptoBoxOpenEasy(messageOnlyString, nonceOnlyByteArray, messageKeys)
}