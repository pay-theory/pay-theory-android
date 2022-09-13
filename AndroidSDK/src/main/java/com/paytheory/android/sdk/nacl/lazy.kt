package com.paytheory.android.sdk.nacl

import android.util.Log
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.Box
import com.goterl.lazysodium.utils.Base64MessageEncoder
import com.goterl.lazysodium.utils.Key
import com.goterl.lazysodium.utils.KeyPair
import java.util.*


private val lazySodium = LazySodiumAndroid(SodiumAndroid())
private val boxLazy = lazySodium as Box.Lazy
private lateinit var keyPair: KeyPair
/**
 * Function to generate KeyPair
 */
fun generateLocalKeyPair(): KeyPair {
    keyPair = boxLazy.cryptoBoxKeypair()
    // secret keys should not be logged even in testing
    //Log.d("PT- generateLocalKeyPair SECRET KEY", Base64.getEncoder().encodeToString(keyPair.secretKey.asBytes))
    //Log.d("PT- generateLocalKeyPair PUBLIC KEY", Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes))
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

//Below are examples of secure-tags-lib decrypting the incoming tag-secure-socket messages

//const messagePublicKey = encryption.decodeKey(data.public_key)
//const messageBox = encryption.pairedBox(messagePublicKey, keyPair.secretKey)
//body = encryption.decrypt(messageBox, body)


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

//Above are examples of secure-tags-lib decrypting the incoming tag-secure-socket messages
fun decryptBox(message: String, socketPublicKey: String): String {
    //Take public key passed from tag-secure-socket and base64 decode it and convert to lazysodium key
    val socketPublicKey = Key.fromBase64String(socketPublicKey)

    //base64 decode message with nonce into byte array
    val messageWithNonceByteArray = Base64MessageEncoder().decode(message)
    Log.d("decryptBox", messageWithNonceByteArray.toString())


    //remove first 24 bytes for nonce value
    val nonceOnlyByteArray : ByteArray = messageWithNonceByteArray.copyOfRange(0,24)

    //remove after first 24 bytes for message value
    val messageOnlyByteArray : ByteArray = messageWithNonceByteArray.copyOfRange(24,messageWithNonceByteArray.size)

    //convert message from bytearray into string
    val messageOnlyString = lazySodium.str(messageOnlyByteArray)

    //create key pair with generated secret key and tags-secure-socket public key
    val messageKeys = KeyPair(socketPublicKey, keyPair.secretKey)

    //Different methods to open
    val decrypedMessage = boxLazy.cryptoBoxOpenEasy(messageOnlyString, nonceOnlyByteArray, messageKeys)
//    val decrypedMessage = boxLazy.cryptoBoxSealOpenEasy(messageOnlyString, messageKeys)
//    val decrypedMessage = lazySodium.cryptoSecretBoxOpenEasy(messageOnlyString, nonceOnlyByteArray, keyPair.secretKey)
//    val decrypedMessage = boxLazy.cryptoBoxSealOpenEasy(messageOnlyString, messageKeyPair)

    return decrypedMessage
}





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