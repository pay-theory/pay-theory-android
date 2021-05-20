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
//
////    Error: java.lang.UnsatisfiedLinkError: Native library (com/sun/jna/win32-x86-64/jnidispatch.dll) not found in resource path (C:\Users\abeld\AppData\Local\Temp\classpath1211720529.jar)
////    java.lang.NoClassDefFoundError: Could not initialize class com.paytheory.android.sdk.nacl.LazyKt
//
//    val keyPair = generateLocalKeyPair()
//
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