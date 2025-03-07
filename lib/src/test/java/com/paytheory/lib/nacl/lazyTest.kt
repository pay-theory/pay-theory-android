package com.paytheory.lib.nacl
//
//import com.goterl.lazysodium.exceptions.SodiumException
//import com.goterl.lazysodium.interfaces.Box
//import com.goterl.lazysodium.utils.Base64MessageEncoder
//import com.goterl.lazysodium.utils.Key
//import com.goterl.lazysodium.utils.KeyPair
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNotNull
//import org.junit.Before
//import org.junit.Test
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.MockitoAnnotations
//import org.mockito.kotlin.any
//import org.mockito.kotlin.verify
//import java.util.Base64
//
//class NaclTest {
//
//    @Mock
//    private lateinit var mockBoxLazy: Box.Lazy
//
//    @Mock
//    private lateinit var mockKeyPair: KeyPair
//
//    @Mock
//    private lateinit var mockKey: Key
//
//    private lateinit var testMessage: String
//    private lateinit var testBase64SocketPublicKey: String
//    private lateinit var testNonce: ByteArray
//    private lateinit var testMessageOnlyByteArray: ByteArray
//    private lateinit var testMessageOnlyString: String
//    private lateinit var testKeyBytes: ByteArray
//
//    @Before
//    fun setUp() {
//        MockitoAnnotations.openMocks(this)
//        //Mock keyPair
//        `when`(mockBoxLazy.cryptoBoxKeypair()).thenReturn(mockKeyPair)
//        //Mock key
//        testKeyBytes = ByteArray(32) { it.toByte() }
//        `when`(mockKey.asBytes).thenReturn(testKeyBytes)
//        `when`(mockKey.asHexString).thenReturn("key")
//
//        testMessage = "testMessage"
//        testBase64SocketPublicKey = Base64.getEncoder().encodeToString(testKeyBytes)
//        testNonce = ByteArray(24) { it.toByte() }
//        testMessageOnlyByteArray = ByteArray(32) { (it + 24).toByte() }
//        testMessageOnlyString = "48656c6c6f20776f726c64"
//
//        // Mock static object
//        keyPair = mockKeyPair
//        val field = ::boxLazy.javaClass.getDeclaredField("INSTANCE")
//        field.isAccessible = true
//        field.set(null, mockBoxLazy)
//    }
//
//    @Test
//    fun `generateLocalKeyPair generates a key pair`() {
//        val result = generateLocalKeyPair()
//        assertNotNull(result)
//        assertEquals(mockKeyPair, result)
//        verify(mockBoxLazy).cryptoBoxKeypair()
//    }
//
//    @Test
//    fun `encryptBox encrypts a message`() {
//        val mockEncryptedMessage = "encryptedMessage"
//        `when`(mockBoxLazy.cryptoBoxSealEasy(any<String>(), any<Key>())).thenReturn(mockEncryptedMessage)
//
//        val result = encryptBox(testMessage, mockKey)
//
//        assertEquals(mockEncryptedMessage, result)
//        verify(mockBoxLazy).cryptoBoxSealEasy(testMessage, mockKey)
//    }
//
//    @Test
//    fun `decryptBox decrypts a message`() {
//        val mockDecryptedMessage = "decryptedMessage"
//        val encoder = Base64MessageEncoder()
//        val messageWithNonceByteArray = testNonce + testMessageOnlyByteArray
//        val encodedMessage = encoder.encode(messageWithNonceByteArray)
//
//        `when`(mockBoxLazy.cryptoBoxOpenEasy(any(), any(), any())).thenReturn(mockDecryptedMessage)
//        `when`(lazySodium.sodiumBin2Hex(any())).thenReturn(testMessageOnlyString)
//
//        val result = decryptBox(encodedMessage, testBase64SocketPublicKey)
//
//        assertEquals(mockDecryptedMessage, result)
//        verify(mockBoxLazy).cryptoBoxOpenEasy(testMessageOnlyString, testNonce, KeyPair(mockKey, keyPair.secretKey))
//    }
//
//    @Test
//    fun `decryptBox decrypts an empty message`() {
//        val mockDecryptedMessage = ""
//        val encoder = Base64MessageEncoder()
//        val messageWithNonceByteArray = ByteArray(24)
//        val encodedMessage = encoder.encode(messageWithNonceByteArray)
//
//        `when`(mockBoxLazy.cryptoBoxOpenEasy(any(), any(), any())).thenReturn(mockDecryptedMessage)
//        `when`(lazySodium.sodiumBin2Hex(any())).thenReturn("")
//
//        val result = decryptBox(encodedMessage, testBase64SocketPublicKey)
//
//        assertEquals(mockDecryptedMessage, result)
//        verify(mockBoxLazy).cryptoBoxOpenEasy("", messageWithNonceByteArray, KeyPair(mockKey, keyPair.secretKey))
//    }
//
//    @Test(expected = SodiumException::class)
//    fun `decryptBox throws SodiumException on invalid message`() {
//        val invalidMessage = "invalidBase64"
//        decryptBox(invalidMessage, testBase64SocketPublicKey)
//    }
//
//    @Test(expected = IllegalArgumentException::class)
//    fun `decryptBox throws IllegalArgumentException on invalid public key`(){
//        val validMessage = Base64MessageEncoder().encode(testNonce + testMessageOnlyByteArray)
//        decryptBox(validMessage,"invalidPublicKey")
//    }
//
//    @Test
//    fun `decryptBox handles zero length nonce`(){
//        val encoder = Base64MessageEncoder()
//        val messageWithNonceByteArray = ByteArray(0) + testMessageOnlyByteArray
//        val encodedMessage = encoder.encode(messageWithNonceByteArray)
//
//        `when`(mockBoxLazy.cryptoBoxOpenEasy(any(), any(), any())).thenReturn("message")
//        `when`(lazySodium.sodiumBin2Hex(any())).thenReturn(testMessageOnlyString)
//
//        val result = decryptBox(encodedMessage, testBase64SocketPublicKey)
//
//        assertEquals("message",result)
//    }
//
//    @Test
//    fun `decryptBox handles zero length message`(){
//        val encoder = Base64MessageEncoder()
//        val messageWithNonceByteArray = testNonce + ByteArray(0)
//        val encodedMessage = encoder.encode(messageWithNonceByteArray)
//
//        `when`(mockBoxLazy.cryptoBoxOpenEasy(any(), any(), any())).thenReturn("")
//        `when`(lazySodium.sodiumBin2Hex(any())).thenReturn("")
//
//        val result = decryptBox(encodedMessage, testBase64SocketPublicKey)
//
//        assertEquals("",result)
//    }
//
//    @Test
//    fun `encryptBox handles empty message`() {
//        val mockEncryptedMessage = ""
//        `when`(mockBoxLazy.cryptoBoxSealEasy(any<String>(), any<Key>())).thenReturn(mockEncryptedMessage)
//
//        val result = encryptBox("", mockKey)
//
//        assertEquals(mockEncryptedMessage, result)
//        verify(mockBoxLazy).cryptoBoxSealEasy("", mockKey)
//    }
//
//    @Test
//    fun `generateLocalKeyPair handles null key pair`() {
//        `when`(mockBoxLazy.cryptoBoxKeypair()).thenReturn(null)
//
//        val result = generateLocalKeyPair()
//        assertEquals(null, result)
//    }
//
//}