package com.paytheory.android.sdk.nacl

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
//private val nonce = lazySodium.nonce(Box.NONCEBYTES)
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
//fun encryptBox(message: String, publicKey: Key): String {
//    return lazySodium.cryptoBoxEasy(message, nonce, keyPair)
//
//}

fun encryptBox(message: String, publicKey: Key): String {
    return boxLazy.cryptoBoxSealEasy(
        message,
        publicKey
    )
}


//This is decryption from tags-secure-socket
//def decrypt_sealed_box(private_key, hex_message):
//decoded_message = bytes.fromhex(hex_message)
//
//server_private_key = PrivateKey(base64.b64decode(private_key))
//
//box = SealedBox(server_private_key)
//
//return str(box.decrypt(decoded_message), 'UTF-8')

//This is decryption from secure-tags-lib
//export const decrypt = (
//secretOrSharedKey,
//messageWithNonce
//) => {
//    const messageWithNonceAsUint8Array = decodeBase64(messageWithNonce);
//    const nonce = messageWithNonceAsUint8Array.slice(0, box.nonceLength);
//    const message = messageWithNonceAsUint8Array.slice(
//            box.nonceLength,
//    messageWithNonce.length
//    );
//
//    const decrypted = box.open.after(message, nonce, secretOrSharedKey);
//
//    if (!decrypted) {
//        throw new Error('Could not decrypt message');
//    }
//
//    const base64DecryptedMessage = encodeUTF8(decrypted);
//    return JSON.parse(base64DecryptedMessage);
//};

fun decryptBox(message: String): String {
    Log.d("PT- decryptBox SECRET KEY", Base64.getEncoder().encodeToString(keyPair.secretKey.asBytes))
    Log.d("PT- decryptBox PUBLIC KEY", Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes))

    val decodedMessageWithNonce = String(Base64.getDecoder().decode(message))
    Log.d("PT- decodedMessageWithNonce", decodedMessageWithNonce)

    val nonce = lazySodium.nonce(24)
//    val nonce = decodedMessageWithNonce.take(24).toByteArray()
    Log.d("PT- nonce", nonce.toString())

    val messageOnly = decodedMessageWithNonce.drop(24)
    Log.d("PT- messageOnly", messageOnly)

//    Log.d("PT- message", message)
//    val first24ofmessage = message.substring(0,24)
//    Log.d("first24ofmessage", first24ofmessage)
//    val trythisnonce = lazySodium.bytes(first24ofmessage)
//    Log.d("first24ofmessage", first24ofmessage)
//    val lastofmessage = message.substring(24,message.length)
//    Log.d("lastofmessage", lastofmessage)








//    val decodedBytes = Base64.getDecoder().decode(message)
//    val messageWithNonce = String(decodedBytes)
//    Log.d("messageWithNonce", messageWithNonce)

//    val noncenew = messageWithNonce.take(24)
//    Log.d("noncenew", noncenew)

//    val messagenew = messageWithNonce.substring(24,messageWithNonce.length)
//    Log.d("messagenew", messagenew)
//
//    val messageWithNonce2 = Base64.getDecoder().decode(message)
//
//    val noncenew = messageWithNonce2.slice(0..24).toByteArray()
//    Log.d("noncenew", noncenew.toString())
//
//    val messagenew = messageWithNonce.slice(24..messageWithNonce.length)
//    Log.d("messagenew", messagenew)



//    val nonce = messageWithNonce.take(24)
//    val nonce2 = messageWithNonce.subSequence(0, 24).toString()

//    Log.d("nonce", nonce)
//    Log.d("nonce2", nonce2)

//    val message = boxLazy.cryptoBoxSealOpenEasy(
//        message,
//        keyPair
//    )

    val decrypedMessage = boxLazy.cryptoBoxOpenEasy(message, nonce, keyPair)

//    val decrypedMessage = boxLazy.cryptoBoxEasyAfterNm(
//        messageOnly,
//        nonce,
//        Base64.getEncoder().encodeToString(keyPair.secretKey.asBytes)
//    )

//    val decrypedMessage = "hello"

    return decrypedMessage
}