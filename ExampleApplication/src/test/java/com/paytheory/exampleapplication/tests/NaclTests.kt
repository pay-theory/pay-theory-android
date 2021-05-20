//package com.paytheory.exampleapplication.tests
//
//
//import com.goterl.lazycode.lazysodium.utils.Key
//import com.goterl.lazycode.lazysodium.utils.KeyPair
//import com.paytheory.android.sdk.nacl.boxLazy
//import com.paytheory.android.sdk.nacl.encryptBox
//import com.paytheory.android.sdk.nacl.generateLocalKeyPair
//import org.junit.Test
//
//
///**
// * Class that is used to test Nacl encryption
// */
//class NaclTests {
//
////    private val lazySodium = LazySodiumAndroid(SodiumAndroid())
////    val boxLazy = lazySodium as Box.Lazy
////    val keyPair = boxLazy.cryptoBoxKeypair()
//    val keyPair = generateLocalKeyPair()
//    /**
//     *
//     */
//    @Test
//    fun generateLocalKeyPairTest() {
//        assert(keyPair is KeyPair)
//        assert(keyPair.publicKey != null)
//        assert(keyPair.secretKey != null)
//        assert(keyPair.secretKey is Key)
//        assert(keyPair.secretKey is Key)
//    }
//
//    /**
//     *
//     */
//    @Test
//    fun encryptBoxTest() {
//        val message = "Secret Message"
//
//        val cipher = encryptBox(message, keyPair.publicKey)
//
//        val decrypted: String = boxLazy.cryptoBoxSealOpenEasy(cipher, keyPair)
//
//        assert(decrypted == message)
//
//    }
//}