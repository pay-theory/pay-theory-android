package com.paytheory.lib.data

import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PaymentData
import com.paytheory.lib.data.payloads.PaymentMethodData
import com.paytheory.lib.data.payloads.PayorInfo
import com.paytheory.lib.data.requests.ActionRequest
import com.paytheory.lib.data.requests.CashRequest
import com.paytheory.lib.data.requests.HostTokenRequest
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.requests.PaymentMethodTokenData
import com.paytheory.lib.data.requests.TokenizeRequest
import com.paytheory.lib.data.requests.TransferPartOneRequest
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for requests.kt data classes
 */
class RequestsTest {

    @Test
    fun `ActionRequest should be correctly initialized`() {
        // Given
        val action = "tokenize:card"
        val encoded = "encoded_data_here"
        val publicKey = "public_key_here"
        val sessionKey = "session_key_here"
        
        // When
        val request = ActionRequest(
            action = action,
            encoded = encoded,
            publicKey = publicKey,
            sessionKey = sessionKey
        )
        
        // Then
        assertEquals(action, request.action)
        assertEquals(encoded, request.encoded)
        assertEquals(publicKey, request.publicKey)
        assertEquals(sessionKey, request.sessionKey)
    }
    
    @Test
    fun `PaymentDetail should be correctly initialized with card payment details`() {
        // Given
        val type = "CARD"
        val timing = 1633123456789L
        val amount = 1099
        val currency = "USD"
        val name = "John Doe"
        val number = "4111111111111111"
        val securityCode = "123"
        val expirationMonth = "01"
        val expirationYear = "25"
        val fee_mode = "MERCHANT_FEE"
        val address = Address(
            line1 = "123 Main St",
            line2 = "Apt 101",
            city = "Springfield",
            region = "OH",
            postal_code = "12345"
        )
        val payorInfo = PayorInfo(
            first_name = "John",
            last_name = "Doe",
            email = "john@example.com",
            phone = "123-456-7890",
            address = address
        )
        
        // When
        val paymentDetail = PaymentDetail(
            type = type,
            timing = timing,
            amount = amount,
            currency = currency,
            name = name,
            number = number,
            security_code = securityCode,
            expiration_month = expirationMonth,
            expiration_year = expirationYear,
            fee_mode = fee_mode,
            address = address,
            payorInfo = payorInfo
        )
        
        // Then
        assertEquals(type, paymentDetail.type)
        assertEquals(timing, paymentDetail.timing)
        assertEquals(amount, paymentDetail.amount)
        assertEquals(currency, paymentDetail.currency)
        assertEquals(name, paymentDetail.name)
        assertEquals(number, paymentDetail.number)
        assertEquals(securityCode, paymentDetail.security_code)
        assertEquals(expirationMonth, paymentDetail.expiration_month)
        assertEquals(expirationYear, paymentDetail.expiration_year)
        assertEquals(fee_mode, paymentDetail.fee_mode)
        assertEquals(address, paymentDetail.address)
        assertEquals(payorInfo, paymentDetail.payorInfo)
    }
    
    @Test
    fun `PaymentDetail should be correctly initialized with ACH payment details`() {
        // Given
        val type = "ACH"
        val timing = 1633123456789L
        val amount = 1099
        val currency = "USD"
        val name = "John Doe"
        val accountType = "CHECKING"
        val accountNumber = "123456789012"
        val bankCode = "123456789"
        val fee_mode = "MERCHANT_FEE"
        val address = Address(
            line1 = "123 Main St",
            line2 = "Apt 101",
            city = "Springfield",
            region = "OH",
            postal_code = "12345"
        )
        
        // When
        val paymentDetail = PaymentDetail(
            type = type,
            timing = timing,
            amount = amount,
            currency = currency,
            name = name,
            account_type = accountType,
            account_number = accountNumber,
            bank_code = bankCode,
            fee_mode = fee_mode,
            address = address,
            payorInfo = null
        )
        
        // Then
        assertEquals(type, paymentDetail.type)
        assertEquals(timing, paymentDetail.timing)
        assertEquals(amount, paymentDetail.amount)
        assertEquals(currency, paymentDetail.currency)
        assertEquals(name, paymentDetail.name)
        assertEquals(accountType, paymentDetail.account_type)
        assertEquals(accountNumber, paymentDetail.account_number)
        assertEquals(bankCode, paymentDetail.bank_code)
        assertEquals(fee_mode, paymentDetail.fee_mode)
        assertEquals(address, paymentDetail.address)
        assertNull(paymentDetail.payorInfo)
    }
    
    @Test
    fun `PaymentDetail should be correctly initialized with Google Pay details`() {
        // Given
        val type = "wallet"
        val timing = 1633123456789L
        val amount = 1099
        val currency = "USD"
        val walletType = "GOOGLE_PAY" 
        val digitalWalletPayload = "google_pay_token_here"
        val fee_mode = "MERCHANT_FEE"
        val merchant = "merchant_id_here"
        val serviceFee = "2.50"
        
        // When
        val paymentDetail = PaymentDetail(
            type = type,
            timing = timing,
            amount = amount,
            currency = currency,
            walletType = walletType,
            digitalWalletPayload = digitalWalletPayload,
            fee_mode = fee_mode,
            merchant = merchant,
            service_fee = serviceFee,
            payorInfo = null
        )
        
        // Then
        assertEquals(type, paymentDetail.type)
        assertEquals(timing, paymentDetail.timing)
        assertEquals(amount, paymentDetail.amount)
        assertEquals(currency, paymentDetail.currency)
        assertEquals(walletType, paymentDetail.walletType)
        assertEquals(digitalWalletPayload, paymentDetail.digitalWalletPayload)
        assertEquals(fee_mode, paymentDetail.fee_mode)
        assertEquals(merchant, paymentDetail.merchant)
        assertEquals(serviceFee, paymentDetail.service_fee)
        assertNull(paymentDetail.payorInfo)
    }
    
    @Test
    fun `Address should be correctly initialized with all parameters`() {
        // Given
        val line1 = "123 Main St"
        val line2 = "Apt 101"
        val city = "Springfield"
        val region = "OH"
        val postalCode = "12345"
        
        // When
        val address = Address(
            line1 = line1,
            line2 = line2,
            city = city,
            region = region,
            postal_code = postalCode
        )
        
        // Then
        assertEquals(line1, address.line1)
        assertEquals(line2, address.line2)
        assertEquals(city, address.city)
        assertEquals(region, address.region)
        assertEquals(postalCode, address.postal_code)
    }
    
    @Test
    fun `Address should be correctly initialized with minimal parameters`() {
        // Given
        val line1 = "123 Main St"
        val city = "Springfield"
        val region = "OH"
        val postalCode = "12345"
        
        // When - line2 is empty string, not null
        val address = Address(
            line1 = line1,
            line2 = "",
            city = city,
            region = region,
            postal_code = postalCode
        )
        
        // Then
        assertEquals(line1, address.line1)
        assertEquals("", address.line2)
        assertEquals(city, address.city)
        assertEquals(region, address.region)
        assertEquals(postalCode, address.postal_code)
    }
    
    @Test
    fun `PayorInfo should be correctly initialized with all parameters`() {
        // Given
        val firstName = "John"
        val lastName = "Doe"
        val email = "john@example.com"
        val phone = "123-456-7890"
        val address = Address(
            line1 = "123 Main St",
            line2 = "",
            city = "Springfield",
            region = "OH",
            postal_code = "12345"
        )
        
        // When
        val payorInfo = PayorInfo(
            first_name = firstName,
            last_name = lastName,
            email = email,
            phone = phone,
            address = address
        )
        
        // Then
        assertEquals(firstName, payorInfo.first_name)
        assertEquals(lastName, payorInfo.last_name)
        assertEquals(email, payorInfo.email)
        assertEquals(phone, payorInfo.phone)
        assertEquals(address, payorInfo.address)
    }
    
    @Test
    fun `PayorInfo should be correctly initialized with minimal parameters`() {
        // Given
        val firstName = "John"
        val lastName = "Doe"
        val minimalAddress = Address(
            line1 = "123 Main St", 
            line2 = "",
            city = "Springfield", 
            region = "OH", 
            postal_code = "12345"
        )
        
        // When - email and phone are null but address is required
        val payorInfo = PayorInfo(
            first_name = firstName,
            last_name = lastName,
            email = null,
            phone = null,
            address = minimalAddress
        )
        
        // Then
        assertEquals(firstName, payorInfo.first_name)
        assertEquals(lastName, payorInfo.last_name)
        assertNull(payorInfo.email)
        assertNull(payorInfo.phone)
        assertNotNull(payorInfo.address)
    }
    
    @Test
    fun `TokenizeRequest should be correctly initialized`() {
        // Given
        val hostToken = "host_token_123456"
        val paymentMethodData = PaymentMethodData(
            type = "card",
            number = "4111111111111111",
            expiration_month = "01",
            expiration_year = "25",
            security_code = "123"
        )
        val payorInfo = PayorInfo(
            first_name = "John",
            last_name = "Doe",
            address = Address(
                line1 = "123 Main St",
                city = "Springfield",
                region = "OH",
                postal_code = "12345"
            )
        )
        val payTheoryData = HashMap<Any, Any>()
        val metadata = HashMap<Any, Any>()
        val sessionKey = "session_key_here"
        val timing = 1633123456789L
        
        // When
        val request = TokenizeRequest(
            hostToken = hostToken,
            paymentMethodData = paymentMethodData,
            payorInfo = payorInfo,
            payTheoryData = payTheoryData,
            metadata = metadata,
            sessionKey = sessionKey,
            timing = timing
        )
        
        // Then
        assertEquals(hostToken, request.hostToken)
        assertEquals(paymentMethodData, request.paymentMethodData)
        assertEquals(payorInfo, request.payorInfo)
        assertEquals(payTheoryData, request.payTheoryData)
        assertEquals(metadata, request.metadata)
        assertEquals(sessionKey, request.sessionKey)
        assertEquals(timing, request.timing)
    }
    
    @Test
    fun `TransferPartOneRequest should be correctly initialized`() {
        // Given
        val hostToken = "host_token_123456"
        val paymentMethodData = PaymentMethodData(
            type = "card",
            number = "4111111111111111",
            expiration_month = "01",
            expiration_year = "25",
            security_code = "123"
        )
        val paymentData = PaymentData(
            amount = 1000,
            currency = "USD",
            fee_mode = "MERCHANT_FEE"
        )
        val payorInfo = PayorInfo(
            first_name = "John",
            last_name = "Doe",
            address = Address(
                line1 = "123 Main St",
                city = "Springfield",
                region = "OH",
                postal_code = "12345"
            )
        )
        val payTheoryData = HashMap<Any, Any>()
        val metadata = HashMap<Any, Any>()
        val sessionKey = "session_key_here"
        val timing = 1633123456789L
        
        // When
        val request = TransferPartOneRequest(
            hostToken = hostToken,
            paymentMethodData = paymentMethodData,
            paymentData = paymentData,
            payorInfo = payorInfo,
            payTheoryData = payTheoryData,
            metadata = metadata,
            sessionKey = sessionKey,
            timing = timing
        )
        
        // Then
        assertEquals(hostToken, request.hostToken)
        assertEquals(paymentMethodData, request.paymentMethodData)
        assertEquals(paymentData, request.paymentData)
        assertEquals(payorInfo, request.payorInfo)
        assertEquals(payTheoryData, request.payTheoryData)
        assertEquals(metadata, request.metadata)
        assertEquals(sessionKey, request.sessionKey)
        assertEquals(timing, request.timing)
    }
    
    @Test
    fun `CashRequest should be correctly initialized`() {
        // Given
        val hostToken = "host_token_123456"
        val sessionKey = "session_key_123456"
        val paymentDetail = PaymentDetail(
            type = "CARD",
            timing = 1633123456789L,
            amount = 1000,
            currency = "USD",
            name = "John Doe"
        )
        val timing = 1633123456789L
        val payorInfo = PayorInfo(
            first_name = "John",
            last_name = "Doe",
            address = Address(
                line1 = "123 Main St",
                city = "Springfield",
                region = "OH",
                postal_code = "12345"
            )
        )
        val payTheoryData = HashMap<Any, Any>()
        val metadata = HashMap<Any, Any>()
        
        // When
        val request = CashRequest(
            hostToken = hostToken,
            sessionKey = sessionKey,
            paymentDetail = paymentDetail,
            timing = timing,
            payorInfo = payorInfo,
            payTheoryData = payTheoryData,
            metadata = metadata
        )
        
        // Then
        assertEquals(hostToken, request.hostToken)
        assertEquals(sessionKey, request.sessionKey)
        assertEquals(paymentDetail, request.paymentDetail)
        assertEquals(timing, request.timing)
        assertEquals(payorInfo, request.payorInfo)
        assertEquals(payTheoryData, request.payTheoryData)
        assertEquals(metadata, request.metadata)
    }
    
    @Test
    fun `CashRequest should be correctly initialized with null payorInfo`() {
        // Given
        val hostToken = "host_token_123456"
        val sessionKey = "session_key_123456"
        val paymentDetail = PaymentDetail(
            type = "CARD",
            timing = 1633123456789L,
            amount = 1000,
            currency = "USD",
            name = "John Doe"
        )
        val timing = 1633123456789L
        val payTheoryData = HashMap<Any, Any>()
        val metadata = HashMap<Any, Any>()
        
        // When
        val request = CashRequest(
            hostToken = hostToken,
            sessionKey = sessionKey,
            paymentDetail = paymentDetail,
            timing = timing,
            payorInfo = null,
            payTheoryData = payTheoryData,
            metadata = metadata
        )
        
        // Then
        assertEquals(hostToken, request.hostToken)
        assertEquals(sessionKey, request.sessionKey)
        assertEquals(paymentDetail, request.paymentDetail)
        assertEquals(timing, request.timing)
        assertNull(request.payorInfo)
        assertEquals(payTheoryData, request.payTheoryData)
        assertEquals(metadata, request.metadata)
    }
    
    @Test
    fun `HostTokenRequest should be correctly initialized`() {
        // Given
        val ptToken = "pt_token_123456"
        val attestation = "attestation_data"
        val timing = 1633123456789L
        val applicationPackageName = "com.paytheory.app"
        val requireAttestation = false
        
        // When
        val request = HostTokenRequest(
            ptToken = ptToken,
            attestation = attestation,
            timing = timing,
            applicationPackageName = applicationPackageName,
            requireAttestation = requireAttestation
        )
        
        // Then
        assertEquals(ptToken, request.ptToken)
        assertEquals(attestation, request.attestation)
        assertEquals(timing, request.timing)
        assertEquals("android", request.origin) // Default value
        assertEquals(applicationPackageName, request.applicationPackageName)
        assertFalse(request.requireAttestation)
    }
    
    @Test
    fun `HostTokenRequest should use default values if not provided`() {
        // Given
        val ptToken = "pt_token_123456"
        val attestation = "attestation_data"
        val timing = 1633123456789L
        val applicationPackageName = "com.paytheory.app"
        
        // When - using default value for requireAttestation
        val request = HostTokenRequest(
            ptToken = ptToken,
            attestation = attestation,
            timing = timing,
            applicationPackageName = applicationPackageName
        )
        
        // Then
        assertEquals(ptToken, request.ptToken)
        assertEquals(attestation, request.attestation)
        assertEquals(timing, request.timing)
        assertEquals("android", request.origin) // Default value
        assertEquals(applicationPackageName, request.applicationPackageName)
        assertTrue(request.requireAttestation) // Default is true per the class
    }
    
    @Test
    fun `PaymentMethodTokenData should be correctly initialized for card`() {
        // Given
        val type = "card"
        val timing = 1633123456789L
        val name = "John Doe"
        val number = "4111111111111111"
        val securityCode = "123"
        val expirationMonth = "01"
        val expirationYear = "25"
        val sessionKey = "session_key_123456"
        val address = Address(
            line1 = "123 Main St",
            city = "Springfield",
            region = "OH",
            postal_code = "12345"
        )
        
        // When
        val tokenData = PaymentMethodTokenData(
            type = type,
            timing = timing,
            name = name,
            number = number,
            security_code = securityCode,
            expiration_month = expirationMonth,
            expiration_year = expirationYear,
            address = address,
            sessionKey = sessionKey
        )
        
        // Then
        assertEquals(type, tokenData.type)
        assertEquals(timing, tokenData.timing)
        assertEquals(name, tokenData.name)
        assertEquals(number, tokenData.number)
        assertEquals(securityCode, tokenData.security_code)
        assertEquals(expirationMonth, tokenData.expiration_month)
        assertEquals(expirationYear, tokenData.expiration_year)
        assertEquals(address, tokenData.address)
        assertEquals(sessionKey, tokenData.sessionKey)
        assertNull(tokenData.accountNumber)
        assertNull(tokenData.account_type)
        assertNull(tokenData.bank_code)
        assertNull(tokenData.payorInfo)
    }
    
    @Test
    fun `PaymentMethodTokenData should be correctly initialized for bank account`() {
        // Given
        val type = "bank"
        val timing = 1633123456789L
        val name = "John Doe"
        val accountNumber = "123456789012"
        val accountType = "checking"
        val bankCode = "123456789"
        val payorInfo = PayorInfo(
            first_name = "John",
            last_name = "Doe",
            address = Address(
                line1 = "123 Main St",
                city = "Springfield",
                region = "OH",
                postal_code = "12345"
            )
        )
        
        // When
        val tokenData = PaymentMethodTokenData(
            type = type,
            timing = timing,
            name = name,
            accountNumber = accountNumber,
            account_type = accountType,
            bank_code = bankCode,
            payorInfo = payorInfo
        )
        
        // Then
        assertEquals(type, tokenData.type)
        assertEquals(timing, tokenData.timing)
        assertEquals(name, tokenData.name)
        assertEquals(accountNumber, tokenData.accountNumber)
        assertEquals(accountType, tokenData.account_type)
        assertEquals(bankCode, tokenData.bank_code)
        assertEquals(payorInfo, tokenData.payorInfo)
        assertNull(tokenData.number)
        assertNull(tokenData.security_code)
        assertNull(tokenData.expiration_month)
        assertNull(tokenData.expiration_year)
        assertNull(tokenData.address)
        assertNull(tokenData.sessionKey)
    }
} 