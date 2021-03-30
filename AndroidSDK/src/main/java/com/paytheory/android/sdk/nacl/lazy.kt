package com.paytheory.android.sdk.nacl

import com.google.gson.annotations.SerializedName
import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.SodiumAndroid
import com.goterl.lazycode.lazysodium.interfaces.Box
import com.goterl.lazycode.lazysodium.utils.Key
import com.goterl.lazycode.lazysodium.utils.KeyPair
import java.util.*

private val lazySodium = LazySodiumAndroid(SodiumAndroid())
val boxLazy = lazySodium as Box.Lazy

/**
 * Class used for encrypting WebSocket messages
 * @param cipher cipher text
 * @param nonce nonce
 */
class Encrypted(
    @SerializedName("cipher") val cipher: String,
    @SerializedName("nonce") val nonce: ByteArray,
)

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







//NOT USED

//import com.goterl.lazycode.lazysodium.LazySodiumAndroid.toBin
//import com.goterl.lazycode.lazysodium.interfaces.SecretBox

///**
// * Function used to decode hex into byte array
// */
//fun String.decodeHex(): ByteArray = chunked(2)
//    .map { it.toByte(16) }
//    .toByteArray()
//fun generateNonce(): ByteArray {
//    val nonce = lazySodium.nonce(Box.NONCEBYTES)
//    println("nonce ${Base64.getEncoder().encodeToString(nonce)}")
//    return nonce
//}
//fun unseal(cipher: String, keyPair: KeyPair): String {
//    return boxLazy.cryptoBoxSealOpenEasy(cipher, keyPair)
//}
//fun cryptoKeyPair(remotePublicKey: Key, privateKey: Key): KeyPair {
//    return KeyPair(remotePublicKey, privateKey)
//}







//export const encrypt = (
//secretOrSharedKey,
//json
//) => {
//    const nonce = newNonce();
//    const messageUint8 = decodeUTF8(JSON.stringify(json));
//    const encrypted = box.after(messageUint8, nonce, secretOrSharedKey);
//
//    const fullMessage = new Uint8Array(nonce.length + encrypted.length);
//    fullMessage.set(nonce);
//    fullMessage.set(encrypted, nonce.length);
//
//    const base64FullMessage = encodeBase64(fullMessage);
//    return base64FullMessage;
//};
//
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