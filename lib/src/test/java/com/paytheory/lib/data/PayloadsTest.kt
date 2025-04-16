package com.paytheory.lib.data

import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PayorInfo
import com.paytheory.lib.data.payloads.PaymentData
import com.paytheory.lib.data.payloads.PaymentMethodData
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for payloads data classes
 */
class PayloadsTest {

    @Test
    fun `Address should be correctly initialized`() {
        // Given
        val line1 = "123 Main St"
        val line2 = "Apt 4B"
        val city = "Columbus"
        val region = "OH"
        val postal_code = "43215"
        val country = "US"
        
        // When
        val address = Address(
            line1 = line1,
            line2 = line2,
            city = city,
            region = region,
            postal_code = postal_code,
            country = country
        )
        
        // Then
        assertEquals(line1, address.line1)
        assertEquals(line2, address.line2)
        assertEquals(city, address.city)
        assertEquals(region, address.region)
        assertEquals(postal_code, address.postal_code)
        assertEquals(country, address.country)
    }
    
    @Test
    fun `Address should set default country to US when not provided`() {
        // When
        val address = Address(
            line1 = "123 Main St",
            city = "Columbus",
            region = "OH",
            postal_code = "43215"
        )
        
        // Then
        assertEquals("US", address.country)
    }
    
    @Test
    fun `PayorInfo should be correctly initialized`() {
        // Given
        val first_name = "John"
        val last_name = "Doe"
        val email = "john.doe@example.com"
        val phone = "6145551234"
        val address = Address(
            line1 = "123 Main St",
            city = "Columbus",
            region = "OH",
            postal_code = "43215"
        )
        
        // When
        val payorInfo = PayorInfo(
            first_name = first_name,
            last_name = last_name,
            email = email,
            phone = phone,
            address = address
        )
        
        // Then
        assertEquals(first_name, payorInfo.first_name)
        assertEquals(last_name, payorInfo.last_name)
        assertEquals(email, payorInfo.email)
        assertEquals(phone, payorInfo.phone)
        assertEquals(address, payorInfo.address)
    }
    
    @Test
    fun `PayorInfo should accept null values for optional fields`() {
        // Given
        val first_name = "John"
        val last_name = "Doe"
        val address = Address(
            line1 = "123 Main St",
            city = "Columbus",
            region = "OH",
            postal_code = "43215"
        )
        
        // When
        val payorInfo = PayorInfo(
            first_name = first_name,
            last_name = last_name,
            address = address
        )
        
        // Then
        assertEquals(first_name, payorInfo.first_name)
        assertEquals(last_name, payorInfo.last_name)
        assertNull(payorInfo.email)
        assertNull(payorInfo.phone)
    }
    
    @Test
    fun `PaymentMethodData should be correctly initialized for card`() {
        // Given
        val number = "4111111111111111"
        val expiration_month = "12"
        val expiration_year = "2030"
        val security_code = "123"
        val type = "card"
        
        // When
        val paymentMethodData = PaymentMethodData(
            number = number,
            expiration_month = expiration_month,
            expiration_year = expiration_year,
            security_code = security_code,
            type = type
        )
        
        // Then
        assertEquals(number, paymentMethodData.number)
        assertEquals(expiration_month, paymentMethodData.expiration_month)
        assertEquals(expiration_year, paymentMethodData.expiration_year)
        assertEquals(security_code, paymentMethodData.security_code)
        assertEquals(type, paymentMethodData.type)
        assertNull(paymentMethodData.account_number)
        assertNull(paymentMethodData.bank_code)
        assertNull(paymentMethodData.account_type)
    }
    
    @Test
    fun `PaymentMethodData should be correctly initialized for bank account`() {
        // Given
        val account_number = "123456789"
        val bank_code = "123456789"
        val account_type = "checking"
        val type = "bank"
        
        // When
        val paymentMethodData = PaymentMethodData(
            account_number = account_number,
            bank_code = bank_code,
            account_type = account_type,
            type = type
        )
        
        // Then
        assertEquals(account_number, paymentMethodData.account_number)
        assertEquals(bank_code, paymentMethodData.bank_code)
        assertEquals(account_type, paymentMethodData.account_type)
        assertEquals(type, paymentMethodData.type)
        assertNull(paymentMethodData.number)
        assertNull(paymentMethodData.expiration_month)
        assertNull(paymentMethodData.expiration_year)
        assertNull(paymentMethodData.security_code)
    }
    
    @Test
    fun `PaymentData should be correctly initialized`() {
        // Given
        val currency = "USD"
        val amount = 1000
        val fee_mode = "service_fee"
        
        // When
        val paymentData = PaymentData(
            currency = currency,
            amount = amount,
            fee_mode = fee_mode
        )
        
        // Then
        assertEquals(currency, paymentData.currency)
        assertEquals(amount, paymentData.amount)
        assertEquals(fee_mode, paymentData.fee_mode)
    }
    
    @Test
    fun `PaymentData should accept null values for optional fields`() {
        // Given
        val amount = 1000
        
        // When
        val paymentData = PaymentData(
            currency = null,
            amount = amount,
            fee_mode = null
        )
        
        // Then
        assertEquals(amount, paymentData.amount)
        assertNull(paymentData.currency)
        assertNull(paymentData.fee_mode)
    }
} 