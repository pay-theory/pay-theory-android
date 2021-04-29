//package com.paytheory.exampleapplication.unit
//
//import com.goterl.lazycode.lazysodium.utils.Key
//import com.paytheory.android.sdk.nacl.encryptBox
//import com.paytheory.android.sdk.nacl.generateLocalKeyPair
//import org.hamcrest.MatcherAssert.assertThat
//import org.junit.Test
//
///**
// * Unit tests for encryption functions
// */
//class UnitTests {
//
//    var arr = byteArrayOf(0x2E, 0x38)
//    var keyPair = generateLocalKeyPair()
//    var publicKey: Key = keyPair.publicKey
//    var privateKey: Key = keyPair.secretKey
//    var encryptedString = encryptBox("secret message", publicKey)
//
//    @Test
//    fun testEncryptionKeys(){
//        assertThat("key pair is correct object", keyPair.javaClass.name ==  "KeyPair")
//        assertThat("key is correct object", publicKey.javaClass.name ==  "Key")
//        assertThat("key is correct object", privateKey.javaClass.name ==  "Key")
//
//    }
//
//    @Test
//    fun testEncryptionBox(){
//        assertThat("box is correct object", encryptedString.javaClass.name ==  "String")
//    }
//
//
//
//
//}