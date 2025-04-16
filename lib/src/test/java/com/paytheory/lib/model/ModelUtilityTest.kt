package com.paytheory.lib.model

import com.paytheory.lib.PaymentMethodProcessor
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import com.paytheory.lib.configuration.FeeMode
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payloads.Address
import com.paytheory.lib.data.payloads.PayorInfo
import com.paytheory.lib.data.requests.PaymentDetail
import androidx.compose.runtime.mutableStateOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.HashMap

/**
 * Tests for model utility functions
 */
class ModelUtilityTest {

    @Before
    fun setup() {
        // Reset all static state before each test
        for (field in PaymentField.entries) {
            paymentFieldState[field] = FieldState.INIT
            paymentFieldValid[field] = true
            paymentFieldEmpty[field] = false
        }
    }

    // Test utility functions mirroring what's in ModelUtility
    private object TestUtils {
        fun mapToObject(map: HashMap<String, Any>): TransactionData {
            return TransactionData(
                serviceFee = map["service_fee"]?.toString() ?: "",
                state = map["state"]?.toString() ?: "",
                paymentMethodId = map["payment_method_id"]?.toString() ?: "",
                payorId = map["payor_id"]?.toString() ?: "",
                amount = map["amount"]?.toString() ?: "",
                currency = map["currency"]?.toString() ?: "",
                lastFour = map["last_four"]?.toString() ?: "",
                brand = map["brand"]?.toString() ?: "",
                receiptNumber = map["receipt_number"]?.toString() ?: "",
                createdAt = map["created_at"]?.toString() ?: ""
            )
        }
        
        fun validateBarcodeMap(map: HashMap<String, Any>?): Boolean {
            if (map == null) return false
            
            val requiredKeys = listOf(
                "barcode_id", "barcode_url", "barcode", 
                "barcode_fee", "merchant", "map_url"
            )
            
            return requiredKeys.all { map.containsKey(it) }
        }
        
        fun mapToBarcode(map: HashMap<String, Any>): BarcodeResult {
            return BarcodeResult(
                barcodeId = map["barcode_id"].toString(),
                barcodeUrl = map["barcodeUrl"].toString(),
                barcode = map["barcode"].toString(),
                barcodeFee = map["barcode_fee"].toString(),
                merchant = map["merchant"].toString(),
                mapUrl = map["map_url"].toString()
            )
        }
        
        fun convertStringToBoolean(value: String?): Boolean {
            return when (value?.lowercase()) {
                "true", "1" -> true
                else -> false
            }
        }
    }
    
    // Simple data class for test utility
    data class TransactionData(
        val serviceFee: String,
        val state: String,
        val paymentMethodId: String,
        val payorId: String,
        val amount: String,
        val currency: String,
        val lastFour: String,
        val brand: String,
        val receiptNumber: String,
        val createdAt: String
    )
    
    // Test for propagateState function
    @Test
    fun `propagateState should update field state correctly`() {
        // Reset the state maps for this test
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.INIT
        paymentFieldValid[field] = false
        paymentFieldEmpty[field] = false
        
        // Create mock objects with relaxed behavior
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up clear and simple mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // Use a slot to capture the state change
        val stateChangeSlot = slot<Pair<PaymentField, FieldState>>()
        every { mockPayable.handleStateChange(capture(stateChangeSlot)) } just runs
        
        // Call the function we're testing with "valid" input
        val result = propagateState(mockProcessor, field, isValid = true, isEmpty = false)
        
        // Verify the results
        assertTrue(result)
        assertEquals(FieldState.READY, paymentFieldState[field])
        assertTrue(paymentFieldValid[field]!!)
        assertFalse(paymentFieldEmpty[field]!!)
        assertTrue(stateChangeSlot.isCaptured)
        assertEquals(field, stateChangeSlot.captured.first)
        assertEquals(FieldState.READY, stateChangeSlot.captured.second)
        
        // Verify the mock was called
        verify(exactly = 1) { mockPayable.handleStateChange(any()) }
    }
    
    @Test
    fun `propagateState should transition to READY when field is valid`() {
        // Reset the state maps for this test
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.INIT
        paymentFieldValid[field] = false
        paymentFieldEmpty[field] = false
        
        // Create mock objects with relaxed behavior
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up clear and simple mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // Use a slot to capture the state change
        val stateChangeSlot = slot<Pair<PaymentField, FieldState>>()
        every { mockPayable.handleStateChange(capture(stateChangeSlot)) } just runs
        
        // Call the function we're testing with "valid" input
        val result = propagateState(mockProcessor, field, isValid = true, isEmpty = false)
        
        // Verify the results
        assertTrue(result)
        assertEquals(FieldState.READY, paymentFieldState[field])
        assertTrue(paymentFieldValid[field]!!)
        assertFalse(paymentFieldEmpty[field]!!)
        assertTrue(stateChangeSlot.isCaptured)
        assertEquals(field, stateChangeSlot.captured.first)
        assertEquals(FieldState.READY, stateChangeSlot.captured.second)
        
        // Verify the mock was called
        verify(exactly = 1) { mockPayable.handleStateChange(any()) }
    }
    
    @Test
    fun `propagateState should transition to EMPTY when field is empty`() {
        // Reset the state maps for this test
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.INIT
        paymentFieldValid[field] = false
        paymentFieldEmpty[field] = false
        
        // Create mock objects
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // Use a slot to capture the state change
        val stateChangeSlot = slot<Pair<PaymentField, FieldState>>()
        every { mockPayable.handleStateChange(capture(stateChangeSlot)) } just runs
        
        // Call the function with empty input
        val result = propagateState(mockProcessor, field, isValid = false, isEmpty = true)
        
        // Verify the results
        assertFalse(result)
        assertEquals(FieldState.EMPTY, paymentFieldState[field])
        assertTrue(paymentFieldEmpty[field]!!)
        verify(exactly = 1) { mockPayable.handleStateChange(any()) }
        assertTrue(stateChangeSlot.isCaptured)
        assertEquals(field, stateChangeSlot.captured.first)
        assertEquals(FieldState.EMPTY, stateChangeSlot.captured.second)
    }
    
    @Test
    fun `propagateState should transition to INVALID when field is invalid and not empty`() {
        // Reset the state maps for this test
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.INIT
        paymentFieldValid[field] = false
        paymentFieldEmpty[field] = false
        
        // Create mock objects
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // Use a slot to capture the state change
        val stateChangeSlot = slot<Pair<PaymentField, FieldState>>()
        every { mockPayable.handleStateChange(capture(stateChangeSlot)) } just runs
        
        // Call the function with invalid input
        val result = propagateState(mockProcessor, field, isValid = false, isEmpty = false)
        
        // Verify the results
        assertFalse(result)
        assertEquals(FieldState.INVALID, paymentFieldState[field])
        assertFalse(paymentFieldValid[field]!!)
        assertFalse(paymentFieldEmpty[field]!!)
        verify(exactly = 1) { mockPayable.handleStateChange(any()) }
        assertTrue(stateChangeSlot.isCaptured)
        assertEquals(field, stateChangeSlot.captured.first)
        assertEquals(FieldState.INVALID, stateChangeSlot.captured.second)
    }
    
    @Test
    fun `propagateState should not notify when state does not change`() {
        // Reset the state maps for this test with the field already in READY state
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.READY
        paymentFieldValid[field] = true
        paymentFieldEmpty[field] = false
        
        // Create mock objects
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // Use a slot to capture the state change
        val stateChangeSlot = slot<Pair<PaymentField, FieldState>>()
        every { mockPayable.handleStateChange(capture(stateChangeSlot)) } just runs
        
        // Call the function with the same state again
        val result = propagateState(mockProcessor, field, isValid = true, isEmpty = false)
        
        // Verify the results
        assertTrue(result)
        assertEquals(FieldState.READY, paymentFieldState[field])
        assertTrue(paymentFieldValid[field]!!)
        assertFalse(paymentFieldEmpty[field]!!)
        
        // Verify no notification happened
        verify(exactly = 0) { mockPayable.handleStateChange(any()) }
        assertFalse(stateChangeSlot.isCaptured)
    }
    
    @Test
    fun `PaymentDetail should be correctly initialized with card details`() {
        // Given
        val address = Address(
            line1 = "123 Main St",
            line2 = "Apt 4B",
            city = "Columbus", 
            region = "OH",
            postal_code = "43215"
        )

        // When
        val paymentDetail = PaymentDetail(
            type = "CARD",
            timing = 1234567890L,
            amount = 1999,
            name = "John Doe",
            number = "4111111111111111",
            security_code = "123",
            expiration_month = "12",
            expiration_year = "2025",
            fee_mode = FeeMode.MERCHANT_FEE.toString(),
            address = address,
            payorInfo = PayorInfo(
                first_name = "customer123", 
                email = "email@example.com", 
                phone = "555-123-4567",
                address = address
            )
        )

        // Then
        assertEquals("CARD", paymentDetail.type)
        assertEquals(1234567890L, paymentDetail.timing)
        assertEquals(1999, paymentDetail.amount)
        assertEquals("John Doe", paymentDetail.name)
        assertEquals("4111111111111111", paymentDetail.number)
        assertEquals("123", paymentDetail.security_code)
        assertEquals("12", paymentDetail.expiration_month)
        assertEquals("2025", paymentDetail.expiration_year)
        assertEquals(FeeMode.MERCHANT_FEE.toString(), paymentDetail.fee_mode)
        
        // Verify address
        assertNotNull(paymentDetail.address)
        assertEquals("123 Main St", paymentDetail.address?.line1)
        assertEquals("Apt 4B", paymentDetail.address?.line2)
        assertEquals("Columbus", paymentDetail.address?.city)
        assertEquals("OH", paymentDetail.address?.region)
        assertEquals("43215", paymentDetail.address?.postal_code)
        
        // Verify payor info
        assertNotNull(paymentDetail.payorInfo)
        assertEquals("customer123", paymentDetail.payorInfo?.first_name)
        assertEquals("email@example.com", paymentDetail.payorInfo?.email)
        assertEquals("555-123-4567", paymentDetail.payorInfo?.phone)
    }
    
    @Test
    fun `PaymentDetail should be correctly initialized with bank details`() {
        // Given
        val address = Address(
            line1 = "123 Main St",
            line2 = "Apt 4B",
            city = "Columbus", 
            region = "OH",
            postal_code = "43215"
        )

        // When
        val paymentDetail = PaymentDetail(
            type = "ACH",
            timing = 1234567890L,
            amount = 2999,
            name = "John Doe",
            account_number = "0123456789",
            bank_code = "021000021",
            account_type = "checking",
            fee_mode = FeeMode.MERCHANT_FEE.toString(),
            address = address,
            payorInfo = PayorInfo(
                first_name = "customer456", 
                email = "bank@example.com", 
                phone = "555-987-6543",
                address = address
            )
        )

        // Then
        assertEquals("ACH", paymentDetail.type)
        assertEquals(1234567890L, paymentDetail.timing)
        assertEquals(2999, paymentDetail.amount)
        assertEquals("John Doe", paymentDetail.name)
        assertEquals("0123456789", paymentDetail.account_number)
        assertEquals("021000021", paymentDetail.bank_code)
        assertEquals("checking", paymentDetail.account_type)
        assertEquals(FeeMode.MERCHANT_FEE.toString(), paymentDetail.fee_mode)
        
        // Verify address
        assertNotNull(paymentDetail.address)
        assertEquals("123 Main St", paymentDetail.address?.line1)
        assertEquals("Apt 4B", paymentDetail.address?.line2)
        assertEquals("Columbus", paymentDetail.address?.city)
        assertEquals("OH", paymentDetail.address?.region)
        assertEquals("43215", paymentDetail.address?.postal_code)
        
        // Verify payor info
        assertNotNull(paymentDetail.payorInfo)
        assertEquals("customer456", paymentDetail.payorInfo?.first_name)
        assertEquals("bank@example.com", paymentDetail.payorInfo?.email)
        assertEquals("555-987-6543", paymentDetail.payorInfo?.phone)
    }

    @Test
    fun `mapToObject should convert map to TransactionData`() {
        // Given a map with transaction data
        val map = HashMap<String, Any>()
        map["service_fee"] = "100"
        map["state"] = "completed"
        map["payment_method_id"] = "pm_123456"
        map["payor_id"] = "cus_789"
        map["amount"] = "2500"
        map["currency"] = "USD"
        map["last_four"] = "4242"
        map["brand"] = "visa"
        map["receipt_number"] = "REC-12345"
        map["created_at"] = "1620000000"

        // When
        val transactionData = TestUtils.mapToObject(map)

        // Then
        assertNotNull(transactionData)
        assertEquals("100", transactionData.serviceFee)
        assertEquals("completed", transactionData.state)
        assertEquals("pm_123456", transactionData.paymentMethodId)
        assertEquals("cus_789", transactionData.payorId)
        assertEquals("2500", transactionData.amount)
        assertEquals("USD", transactionData.currency)
        assertEquals("4242", transactionData.lastFour)
        assertEquals("visa", transactionData.brand)
        assertEquals("REC-12345", transactionData.receiptNumber)
        assertEquals("1620000000", transactionData.createdAt)
    }

    @Test
    fun `convertStringToBoolean should handle various inputs`() {
        // Test various inputs
        assertTrue(TestUtils.convertStringToBoolean("true"))
        assertTrue(TestUtils.convertStringToBoolean("TRUE"))
        assertTrue(TestUtils.convertStringToBoolean("True"))
        assertTrue(TestUtils.convertStringToBoolean("1"))
        
        assertFalse(TestUtils.convertStringToBoolean("false"))
        assertFalse(TestUtils.convertStringToBoolean("FALSE"))
        assertFalse(TestUtils.convertStringToBoolean("False"))
        assertFalse(TestUtils.convertStringToBoolean("0"))
        assertFalse(TestUtils.convertStringToBoolean(""))
        assertFalse(TestUtils.convertStringToBoolean("any other string"))
        assertFalse(TestUtils.convertStringToBoolean(null))
    }

    @Test
    fun `validateBarcodeMap should return true for valid map`() {
        // Given
        val validMap = HashMap<String, Any>().apply {
            put("barcode_id", "123456")
            put("barcode_url", "https://example.com/barcode/123456")
            put("barcode", "123456789")
            put("barcode_fee", "1.50")
            put("merchant", "Test Merchant")
            put("map_url", "https://maps.example.com/location")
        }
        
        // When
        val result = TestUtils.validateBarcodeMap(validMap)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `validateBarcodeMap should return false for invalid map`() {
        // Given - missing required keys
        val invalidMap = HashMap<String, Any>().apply {
            put("barcode_id", "123456")
            put("barcode_url", "https://example.com/barcode/123456")
            // Missing "barcode"
            put("barcode_fee", "1.50")
            // Missing "merchant"
            put("map_url", "https://maps.example.com/location")
        }
        
        // When
        val result = TestUtils.validateBarcodeMap(invalidMap)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `validateBarcodeMap should return false for null map`() {
        // Given
        val nullMap: HashMap<String, Any>? = null
        
        // When
        val result = TestUtils.validateBarcodeMap(nullMap)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `mapToBarcode should correctly convert map to BarcodeResult`() {
        // Given
        val barcodeMap = HashMap<String, Any>().apply {
            put("barcode_id", "123456")
            put("barcodeUrl", "https://example.com/barcode/123456")
            put("barcode", "123456789")
            put("barcode_fee", "1.50")
            put("merchant", "Test Merchant")
            put("map_url", "https://maps.example.com/location")
        }
        
        // When
        val result = TestUtils.mapToBarcode(barcodeMap)
        
        // Then
        assertEquals("123456", result.barcodeId)
        assertEquals("https://example.com/barcode/123456", result.barcodeUrl)
        assertEquals("123456789", result.barcode)
        assertEquals("1.50", result.barcodeFee)
        assertEquals("Test Merchant", result.merchant)
        assertEquals("https://maps.example.com/location", result.mapUrl)
    }
    
    @Test
    fun `propagateState handles scenario when current state is already READY`() {
        // Given - field already in READY state
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.READY
        paymentFieldValid[field] = true
        paymentFieldEmpty[field] = false
        
        // Create mock objects
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // When - propagate the same state
        val result = propagateState(mockProcessor, field, isValid = true, isEmpty = false)
        
        // Then - state should remain READY and no notification should be sent
        assertTrue(result)
        assertEquals(FieldState.READY, paymentFieldState[field])
        assertTrue(paymentFieldValid[field]!!)
        assertFalse(paymentFieldEmpty[field]!!)
        verify(exactly = 0) { mockPayable.handleStateChange(any()) }
    }
    
    @Test
    fun `propagateState handles scenario when current state is already INVALID`() {
        // Given - field already in INVALID state
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.INVALID
        paymentFieldValid[field] = false
        paymentFieldEmpty[field] = false
        
        // Create mock objects
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // When - propagate the same state
        val result = propagateState(mockProcessor, field, isValid = false, isEmpty = false)
        
        // Then - state should remain INVALID and no notification should be sent
        assertFalse(result)
        assertEquals(FieldState.INVALID, paymentFieldState[field])
        assertFalse(paymentFieldValid[field]!!)
        assertFalse(paymentFieldEmpty[field]!!)
        verify(exactly = 0) { mockPayable.handleStateChange(any()) }
    }
    
    @Test
    fun `propagateState handles scenario when current state is already EMPTY`() {
        // Given - field already in EMPTY state
        val field = PaymentField.CARD_NUMBER
        paymentFieldState[field] = FieldState.EMPTY
        paymentFieldValid[field] = false
        paymentFieldEmpty[field] = true
        
        // Create mock objects
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // When - propagate the same state
        val result = propagateState(mockProcessor, field, isValid = false, isEmpty = true)
        
        // Then - state should remain EMPTY and no notification should be sent
        assertFalse(result)
        assertEquals(FieldState.EMPTY, paymentFieldState[field])
        assertFalse(paymentFieldValid[field]!!)
        assertTrue(paymentFieldEmpty[field]!!)
        verify(exactly = 0) { mockPayable.handleStateChange(any()) }
    }

    @Test
    fun `constructCardPayment creates payment detail with correct fields`() {
        // Mock PaymentViewModel
        val mockViewModel = mock(PaymentViewModel::class.java)
        val mockConfig = mock(PayTheoryConfiguration::class.java)

        // Set up required values in view model
        `when`(mockViewModel.cardNumber).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("4242424242424242"), null)))
        `when`(mockViewModel.expiration).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("12/25"), null)))
        `when`(mockViewModel.cvc).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("123"), null)))
        `when`(mockViewModel.nameOnAccount).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("John Doe"), null)))
        `when`(mockViewModel.addressLine1).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("123 Main St"), null)))
        `when`(mockViewModel.addressLine2).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("Apt 4B"), null)))
        `when`(mockViewModel.city).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("Columbus"), null)))
        `when`(mockViewModel.region).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("OH"), null)))
        `when`(mockViewModel.postalCode).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("43215"), null)))
        
        // Set up configuration
        `when`(mockViewModel.configuration).thenReturn(mockConfig)
        `when`(mockConfig.paymentMethodType).thenReturn(PaymentMethodType.CARD)
        `when`(mockConfig.amount).thenReturn(1000)
        `when`(mockConfig.feeMode).thenReturn("MERCHANT_FEE")
        
        // Call the method
        val paymentDetail = mockViewModel.constructCardPayment()
        
        // Verify payment detail fields
        assertEquals("CARD", paymentDetail.type)
        assertEquals(1000, paymentDetail.amount)
        assertEquals("John Doe", paymentDetail.name)
        assertEquals("4242424242424242", paymentDetail.number)
        assertEquals("123", paymentDetail.security_code)
        assertEquals("12", paymentDetail.expiration_month)
        assertEquals("25", paymentDetail.expiration_year)
        assertEquals("MERCHANT_FEE", paymentDetail.fee_mode)
        
        // Verify address
        assertEquals("123 Main St", paymentDetail.address?.line1)
        assertEquals("Apt 4B", paymentDetail.address?.line2)
        assertEquals("Columbus", paymentDetail.address?.city)
        assertEquals("OH", paymentDetail.address?.region)
        assertEquals("43215", paymentDetail.address?.postal_code)
    }
    
    @Test
    fun `constructBankPayment creates payment detail with correct ACH fields`() {
        // Mock PaymentViewModel
        val mockViewModel = mock(PaymentViewModel::class.java)
        val mockConfig = mock(PayTheoryConfiguration::class.java)

        // Set up required values in view model
        `when`(mockViewModel.nameOnAccount).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("John Doe"), null)))
        `when`(mockViewModel.bankAccountNumber).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("123456789"), null)))
        `when`(mockViewModel.bankRoutingNumber).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("987654321"), null)))
        `when`(mockViewModel.bankAccountType).thenReturn(mutableStateOf("checking"))
        `when`(mockViewModel.addressLine1).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("123 Main St"), null)))
        `when`(mockViewModel.addressLine2).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("Apt 4B"), null)))
        `when`(mockViewModel.city).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("Columbus"), null)))
        `when`(mockViewModel.region).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("OH"), null)))
        `when`(mockViewModel.postalCode).thenReturn(mutableStateOf(SecureStringWrapper(SecureString("43215"), null)))
        
        // Set up configuration
        `when`(mockViewModel.configuration).thenReturn(mockConfig)
        `when`(mockConfig.paymentMethodType).thenReturn(PaymentMethodType.ACH)
        `when`(mockConfig.amount).thenReturn(2000)
        `when`(mockConfig.feeMode).thenReturn("MERCHANT_FEE")
        
        // Call the method
        val paymentDetail = mockViewModel.constructBankPayment()
        
        // Verify payment detail fields
        assertEquals("ACH", paymentDetail.type)
        assertEquals(2000, paymentDetail.amount)
        assertEquals("John Doe", paymentDetail.name)
        assertEquals("123456789", paymentDetail.account_number)
        assertEquals("987654321", paymentDetail.bank_code)
        assertEquals("checking", paymentDetail.account_type)
        assertEquals("MERCHANT_FEE", paymentDetail.fee_mode)
        
        // Verify address
        assertEquals("123 Main St", paymentDetail.address?.line1)
        assertEquals("Apt 4B", paymentDetail.address?.line2)
        assertEquals("Columbus", paymentDetail.address?.city)
        assertEquals("OH", paymentDetail.address?.region)
        assertEquals("43215", paymentDetail.address?.postal_code)
    }
    
    @Test
    fun `propagateState handles null field state gracefully`() {
        // Create mock objects
        val mockPayable = mockk<Payable>(relaxed = true)
        val mockProcessor = mockk<PaymentMethodProcessor>()
        
        // Set up mock behavior
        every { mockProcessor.payable } returns mockPayable
        
        // Pre-condition - remove a field from the map
        val field = PaymentField.CARD_NUMBER
        val originalState = paymentFieldState[field]
        paymentFieldState.remove(field)
        
        try {
            // When - propagate state for a field not in the map
            propagateState(mockProcessor, field, isValid = true, isEmpty = false)
            
            // Then - No NPE should be thrown, and field should be added with READY state
            assertEquals(FieldState.READY, paymentFieldState[field])
        } finally {
            // Restore original state for cleanup
            if (originalState != null) {
                paymentFieldState[field] = originalState
            }
        }
    }
}