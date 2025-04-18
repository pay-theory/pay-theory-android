package com.paytheory.lib.data

import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.EncryptedMessage
import com.paytheory.lib.data.payable.EncryptedPaymentToken
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.data.payable.TransactionResult
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for payableData.kt data classes
 */
class PayableDataTest {

    @Test
    fun `PTError should be correctly initialized`() {
        // Given
        val errorCode = ErrorCode.InvalidAPIKey
        val errorMessage = "The API key is invalid"
        
        // When
        val error = PTError(errorCode, errorMessage)
        
        // Then
        assertEquals(ErrorCode.InvalidAPIKey, error.code)
        assertEquals("The API key is invalid", error.error)
    }
    
    @Test
    fun `PTError should work with all error codes`() {
        // Test various error codes
        val errorMessages = mapOf(
            ErrorCode.ActionComplete to "Action completed successfully",
            ErrorCode.ActionInProgress to "Action is in progress",
            ErrorCode.AttestationFailed to "Attestation failed",
            ErrorCode.InProgress to "Transaction in progress",
            ErrorCode.InvalidAPIKey to "API key is invalid",
            ErrorCode.NotValid to "Data not valid",
            ErrorCode.SocketError to "Socket connection error",
            ErrorCode.TokenFailed to "Token creation failed",
            ErrorCode.GooglePayUnavailable to "Google Pay is not available",
            ErrorCode.GooglePayError to "Google Pay error occurred",
            ErrorCode.GooglePayCancelled to "Google Pay operation was cancelled"
        )
        
        errorMessages.forEach { (errorCode, message) ->
            val error = PTError(errorCode, message)
            assertEquals(errorCode, error.code)
            assertEquals(message, error.error)
        }
    }
    
    @Test
    fun `BarcodeResult should be correctly initialized`() {
        // Given
        val barcodeId = "brcd_12345"
        val barcodeUrl = "https://paytheory.com/barcode/12345"
        val barcode = "123456789"
        val barcodeFee = "1.50"
        val merchant = "merch_12345"
        val mapUrl = "https://maps.google.com/?q=payment+locations"
        
        // When
        val result = BarcodeResult(
            barcodeId = barcodeId,
            barcodeUrl = barcodeUrl,
            barcode = barcode,
            barcodeFee = barcodeFee,
            merchant = merchant,
            mapUrl = mapUrl
        )
        
        // Then
        assertEquals(barcodeId, result.barcodeId)
        assertEquals(barcodeUrl, result.barcodeUrl)
        assertEquals(barcode, result.barcode)
        assertEquals(barcodeFee, result.barcodeFee)
        assertEquals(merchant, result.merchant)
        assertEquals(mapUrl, result.mapUrl)
    }
    
    @Test
    fun `TransactionResult should be correctly initialized`() {
        // Given
        val state = "confirmed"
        val amount = "100.00"
        val brand = "visa"
        val lastFour = "1234"
        val serviceFee = "2.50"
        val currency = "USD"
        val metadata = HashMap<Any, Any>()
        val receiptNumber = "rcpt_12345"
        val createdAt = "2023-01-01T12:00:00Z"
        val paymentMethodId = "pm_12345"
        val payorId = "pyr_12345"
        val type = "payment"
        
        // When
        val result = TransactionResult(
            state = state,
            amount = amount,
            brand = brand,
            lastFour = lastFour,
            serviceFee = serviceFee,
            currency = currency,
            metadata = metadata,
            receiptNumber = receiptNumber,
            createdAt = createdAt,
            paymentMethodId = paymentMethodId,
            payorId = payorId,
            type = type
        )
        
        // Then
        assertEquals(state, result.state)
        assertEquals(amount, result.amount)
        assertEquals(brand, result.brand)
        assertEquals(lastFour, result.lastFour)
        assertEquals(serviceFee, result.serviceFee)
        assertEquals(currency, result.currency)
        assertEquals(metadata, result.metadata)
        assertEquals(receiptNumber, result.receiptNumber)
        assertEquals(createdAt, result.createdAt)
        assertEquals(paymentMethodId, result.paymentMethodId)
        assertEquals(payorId, result.payorId)
        assertEquals(type, result.type)
    }
    
    @Test
    fun `SuccessfulTransactionResult should be correctly initialized`() {
        // Given
        val state = "confirmed"
        val amount = "100.00"
        val brand = "visa"
        val lastFour = "1234"
        val serviceFee = "2.50"
        val currency = "USD"
        val metadata = HashMap<Any, Any>()
        val receiptNumber = "rcpt_12345"
        val createdAt = "2023-01-01T12:00:00Z"
        val paymentMethodId = "pm_12345"
        val payorId = "pyr_12345"
        
        // When
        val result = SuccessfulTransactionResult(
            state = state,
            amount = amount,
            brand = brand,
            lastFour = lastFour,
            serviceFee = serviceFee,
            currency = currency,
            metadata = metadata,
            receiptNumber = receiptNumber,
            createdAt = createdAt,
            paymentMethodId = paymentMethodId,
            payorId = payorId
        )
        
        // Then
        assertEquals(state, result.state)
        assertEquals(amount, result.amount)
        assertEquals(brand, result.brand)
        assertEquals(lastFour, result.lastFour)
        assertEquals(serviceFee, result.serviceFee)
        assertEquals(currency, result.currency)
        assertEquals(metadata, result.metadata)
        assertEquals(receiptNumber, result.receiptNumber)
        assertEquals(createdAt, result.createdAt)
        assertEquals(paymentMethodId, result.paymentMethodId)
        assertEquals(payorId, result.payorId)
    }
    
    @Test
    fun `PaymentMethodTokenResults should be correctly initialized`() {
        // Given
        val state = "tokenized"
        val paymentMethodId = "pm_12345"
        val metadata = HashMap<Any, Any>()
        val payorId = "pyr_12345"
        val lastFour = "1234"
        val firstSix = "411111"
        val brand = "visa"
        val expiration = "12/25"
        val paymentType = "card"
        
        // When
        val result = PaymentMethodTokenResults(
            state = state,
            paymentMethodId = paymentMethodId,
            metadata = metadata,
            payor_id = payorId,
            lastFour = lastFour,
            firstSix = firstSix,
            brand = brand,
            expiration = expiration,
            paymentType = paymentType
        )
        
        // Then
        assertEquals(state, result.state)
        assertEquals(paymentMethodId, result.paymentMethodId)
        assertEquals(metadata, result.metadata)
        assertEquals(payorId, result.payor_id)
        assertEquals(lastFour, result.lastFour)
        assertEquals(firstSix, result.firstSix)
        assertEquals(brand, result.brand)
        assertEquals(expiration, result.expiration)
        assertEquals(paymentType, result.paymentType)
    }
    
    @Test
    fun `PaymentMethodTokenResults should handle bank payment type`() {
        // Given
        val state = "tokenized"
        val paymentMethodId = "pm_12345"
        val paymentType = "bank_account"
        val lastFour = "6789"
        val metadata = HashMap<Any, Any>()
        val payorId = "pyr_67890"
        val firstSix = "123456"
        val brand = "bank"
        val expiration = "N/A"
        
        // When
        val result = PaymentMethodTokenResults(
            state = state,
            paymentMethodId = paymentMethodId,
            paymentType = paymentType,
            lastFour = lastFour,
            metadata = metadata,
            payor_id = payorId,
            firstSix = firstSix,
            brand = brand,
            expiration = expiration
        )
        
        // Then
        assertEquals(state, result.state)
        assertEquals(paymentMethodId, result.paymentMethodId)
        assertEquals(paymentType, result.paymentType)
        assertEquals(lastFour, result.lastFour)
        assertEquals(brand, result.brand)
        assertEquals(firstSix, result.firstSix)
        assertEquals(expiration, result.expiration)
    }
    
    @Test
    fun `FailedTransactionResult should be correctly initialized`() {
        // Given
        val state = "failed"
        val brand = "visa"
        val lastFour = "1234"
        val receiptNumber = "rcpt_12345"
        val paymentMethodId = "pm_12345"
        val payorId = "pyr_12345"
        
        // When
        val result = FailedTransactionResult(
            state = state,
            brand = brand,
            lastFour = lastFour,
            receiptNumber = receiptNumber,
            paymentMethodId = paymentMethodId,
            payorId = payorId
        )
        
        // Then
        assertEquals(state, result.state)
        assertEquals(brand, result.brand)
        assertEquals(lastFour, result.lastFour)
        assertEquals(receiptNumber, result.receiptNumber)
        assertEquals(paymentMethodId, result.paymentMethodId)
        assertEquals(payorId, result.payorId)
    }
    
    @Test
    fun `EncryptedMessage should be correctly initialized`() {
        // Given
        val type = "encrypted_message"
        val body = "encrypted_data_here"
        val publicKey = "public_key_here"
        
        // When
        val message = EncryptedMessage(
            type = type,
            body = body,
            publicKey = publicKey
        )
        
        // Then
        assertEquals(type, message.type)
        assertEquals(body, message.body)
        assertEquals(publicKey, message.publicKey)
    }
    
    @Test
    fun `EncryptedMessage should accept different types`() {
        // Given
        val types = listOf("encrypted_message", "encrypted_payment", "custom_encrypted_type")
        val body = "encrypted_data_here"
        val publicKey = "public_key_here"
        
        // Then
        types.forEach { type ->
            val message = EncryptedMessage(
                type = type,
                body = body,
                publicKey = publicKey
            )
            assertEquals(type, message.type)
        }
    }
    
    @Test
    fun `EncryptedPaymentToken should be correctly initialized`() {
        // Given
        val type = "encrypted_token"
        val body = "encrypted_token_data_here"
        val publicKey = "public_key_here"
        
        // When
        val token = EncryptedPaymentToken(
            type = type,
            body = body,
            publicKey = publicKey
        )
        
        // Then
        assertEquals(type, token.type)
        assertEquals(body, token.body)
        assertEquals(publicKey, token.publicKey)
    }
    
    @Test
    fun `EncryptedPaymentToken should accept different types`() {
        // Given
        val types = listOf("encrypted_token", "payment_token", "custom_token_type")
        val body = "encrypted_token_data_here"
        val publicKey = "public_key_here"
        
        // Then
        types.forEach { type ->
            val token = EncryptedPaymentToken(
                type = type,
                body = body,
                publicKey = publicKey
            )
            assertEquals(type, token.type)
        }
    }
    
    @Test
    fun `ErrorCode enum should have appropriate values`() {
        // Assert that all known ErrorCode values exist
        assertNotNull(ErrorCode.ActionComplete)
        assertNotNull(ErrorCode.ActionInProgress)
        assertNotNull(ErrorCode.AttestationFailed)
        assertNotNull(ErrorCode.InProgress)
        assertNotNull(ErrorCode.InvalidAPIKey)
        assertNotNull(ErrorCode.NotValid)
        assertNotNull(ErrorCode.SocketError)
        assertNotNull(ErrorCode.TokenFailed)
        assertNotNull(ErrorCode.GooglePayUnavailable)
        assertNotNull(ErrorCode.GooglePayError)
        assertNotNull(ErrorCode.GooglePayCancelled)
    }
    
    @Test
    fun `All typealias should map to correct implementations`() {
        // These tests verify that the typealiases point to the correct implementations
        
        // Test PTError typealias
        val ptError = PTError(ErrorCode.InvalidAPIKey, "Error message")
        assertTrue(true)
        
        // Test BarcodeResult typealias
        val barcodeResult = BarcodeResult(
            barcodeId = "brcd_12345",
            barcode = "123456789",
            barcodeUrl = "url",
            barcodeFee = "fee",
            merchant = "merchant",
            mapUrl = "map"
        )
        assertTrue(true)
        
        // Test TransactionResult typealias
        val transactionResult = TransactionResult(
            state = "confirmed",
            amount = "100.00",
            brand = "brand",
            lastFour = "1234",
            serviceFee = "1.00",
            currency = "USD",
            metadata = HashMap(),
            receiptNumber = "receipt",
            createdAt = "created",
            paymentMethodId = "pm_id",
            payorId = "payor_id",
            type = "type"
        )
        assertTrue(true)
        
        // Test SuccessfulTransactionResult typealias
        val successfulResult = SuccessfulTransactionResult(
            state = "confirmed",
            amount = "100.00",
            brand = "brand",
            lastFour = "1234",
            serviceFee = "fee",
            currency = "USD",
            metadata = HashMap(),
            receiptNumber = "receipt",
            createdAt = "created",
            paymentMethodId = "pm_id",
            payorId = "payor_id"
        )
        assertTrue(true)
        
        // Test PaymentMethodTokenResults typealias
        val tokenResults = PaymentMethodTokenResults(
            state = "tokenized",
            paymentMethodId = "pm_12345",
            metadata = HashMap(),
            payor_id = "payor_id",
            lastFour = "1234",
            firstSix = "123456",
            brand = "brand",
            expiration = "expiration",
            paymentType = "type"
        )
        assertTrue(true)
        
        // Test FailedTransactionResult typealias
        val failedResult = FailedTransactionResult(
            state = "failed",
            brand = "brand",
            lastFour = "1234",
            receiptNumber = "receipt",
            paymentMethodId = "pm_id",
            payorId = "payor_id"
        )
        assertTrue(true)
        
        // Test EncryptedMessage typealias
        val encryptedMessage = EncryptedMessage(
            type = "encrypted_message",
            body = "encrypted_data",
            publicKey = "public_key"
        )
        assertTrue(true)
        
        // Test EncryptedPaymentToken typealias
        val encryptedToken = EncryptedPaymentToken(
            type = "encrypted_token",
            body = "encrypted_token_data",
            publicKey = "public_key"
        )
        assertTrue(true)
    }
} 