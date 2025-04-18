package com.paytheory.lib.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for JsonUtils
 * 
 * The JsonUtils class handles sensitive payment data in JSON format, so thorough testing
 * is essential for security and PCI compliance.
 */
class JsonUtilsTest {

    @Test
    fun testParseSafeJson_validJson() {
        // Valid JSON with payment data
        val jsonStr = """{"card_number":"4111111111111111","expiration":"1225","cvv":"123"}"""
        
        // Parse JSON safely
        val jsonObject = JsonUtils.parseSafeJson(jsonStr)
        
        // Verify parsing
        assertNotNull(jsonObject)
        assertEquals("4111111111111111", jsonObject.get("card_number").asString)
        assertEquals("1225", jsonObject.get("expiration").asString)
        assertEquals("123", jsonObject.get("cvv").asString)
    }
    
    @Test
    fun testParseSafeJson_emptyJson() {
        // Empty JSON
        val jsonStr = "{}"
        
        // Parse JSON safely
        val jsonObject = JsonUtils.parseSafeJson(jsonStr)
        
        // Verify parsing
        assertNotNull(jsonObject)
        assertEquals(0, jsonObject.size())
    }
    
    @Test
    fun testParseSafeJson_invalidJson() {
        // Invalid JSON
        val jsonStr = "{invalid_json"
        
        // Parse JSON safely - should return empty object
        val jsonObject = JsonUtils.parseSafeJson(jsonStr)
        
        // Verify parsing
        assertNotNull(jsonObject)
        assertEquals(0, jsonObject.size())
    }
    
    @Test
    fun testParseSafeJson_nullInput() {
        // Null input
        val jsonObject = JsonUtils.parseSafeJson(null)
        
        // Verify parsing
        assertNotNull(jsonObject)
        assertEquals(0, jsonObject.size())
    }
    
    @Test
    fun testGetStringOrDefault_existingKey() {
        // Create a JSON object with payment data
        val jsonObject = JsonParser.parseString("""{"card_number":"4111111111111111","cvv":"123"}""").asJsonObject
        
        // Get existing string value
        val cardNumber = JsonUtils.getStringOrDefault(jsonObject, "card_number", "default")
        
        // Verify value
        assertEquals("4111111111111111", cardNumber)
    }
    
    @Test
    fun testGetStringOrDefault_missingKey() {
        // Create a JSON object
        val jsonObject = JsonParser.parseString("""{"card_number":"4111111111111111"}""").asJsonObject
        
        // Get non-existent value, should return default
        val cvv = JsonUtils.getStringOrDefault(jsonObject, "cvv", "default")
        
        // Verify default is returned
        assertEquals("default", cvv)
    }
    
    @Test
    fun testGetStringOrDefault_wrongType() {
        // Create a JSON object with mixed types
        val jsonObject = JsonParser.parseString("""{"card_number":"4111111111111111","amount":1000}""").asJsonObject
        
        // Try to get number as string, should return default
        val amount = JsonUtils.getStringOrDefault(jsonObject, "amount", "default")
        
        // Verify default is returned
        assertEquals("default", amount)
    }
    
    @Test
    fun testGetIntOrDefault_existingKey() {
        // Create a JSON object with payment data
        val jsonObject = JsonParser.parseString("""{"amount":1000,"fee":25}""").asJsonObject
        
        // Get existing int value
        val amount = JsonUtils.getIntOrDefault(jsonObject, "amount", 0)
        
        // Verify value
        assertEquals(1000, amount)
    }
    
    @Test
    fun testGetIntOrDefault_missingKey() {
        // Create a JSON object
        val jsonObject = JsonParser.parseString("""{"amount":1000}""").asJsonObject
        
        // Get non-existent value, should return default
        val fee = JsonUtils.getIntOrDefault(jsonObject, "fee", 50)
        
        // Verify default is returned
        assertEquals(50, fee)
    }
    
    @Test
    fun testGetIntOrDefault_wrongType() {
        // Create a JSON object with mixed types
        val jsonObject = JsonParser.parseString("""{"amount":1000,"card_number":"4111111111111111"}""").asJsonObject
        
        // Try to get string as int, should return default
        val cardNumber = JsonUtils.getIntOrDefault(jsonObject, "card_number", 0)
        
        // Verify default is returned
        assertEquals(0, cardNumber)
    }
    
    @Test
    fun testGetBooleanOrDefault_existingKey() {
        // Create a JSON object with payment data
        val jsonObject = JsonParser.parseString("""{"tokenize":true,"save_card":false}""").asJsonObject
        
        // Get existing boolean value
        val tokenize = JsonUtils.getBooleanOrDefault(jsonObject, "tokenize", false)
        
        // Verify value
        assertTrue(tokenize)
    }
    
    @Test
    fun testGetBooleanOrDefault_missingKey() {
        // Create a JSON object
        val jsonObject = JsonParser.parseString("""{"tokenize":true}""").asJsonObject
        
        // Get non-existent value, should return default
        val saveCard = JsonUtils.getBooleanOrDefault(jsonObject, "save_card", true)
        
        // Verify default is returned
        assertTrue(saveCard)
    }
    
    @Test
    fun testGetBooleanOrDefault_wrongType() {
        // Create a JSON object with mixed types
        val jsonObject = JsonParser.parseString("""{"tokenize":true,"amount":1000}""").asJsonObject
        
        // Try to get number as boolean, should return default
        val amount = JsonUtils.getBooleanOrDefault(jsonObject, "amount", true)
        
        // Verify default is returned
        assertTrue(amount)
    }
    
    @Test
    fun testGetNestedObjectOrEmpty_existingKey() {
        // Create a JSON object with nested objects
        val jsonObject = JsonParser.parseString(
            """{"billing_address":{"street":"123 Main St","city":"Anytown"}}"""
        ).asJsonObject
        
        // Get existing nested object
        val address = JsonUtils.getNestedObjectOrEmpty(jsonObject, "billing_address")
        
        // Verify nested object
        assertNotNull(address)
        assertEquals("123 Main St", address.get("street").asString)
        assertEquals("Anytown", address.get("city").asString)
    }
    
    @Test
    fun testGetNestedObjectOrEmpty_missingKey() {
        // Create a JSON object
        val jsonObject = JsonParser.parseString("""{"billing_address":{"street":"123 Main St"}}""").asJsonObject
        
        // Get non-existent nested object, should return empty object
        val shippingAddress = JsonUtils.getNestedObjectOrEmpty(jsonObject, "shipping_address")
        
        // Verify empty object is returned
        assertNotNull(shippingAddress)
        assertEquals(0, shippingAddress.size())
    }
    
    @Test
    fun testGetNestedObjectOrEmpty_wrongType() {
        // Create a JSON object with mixed types
        val jsonObject = JsonParser.parseString("""{"billing_address":{"street":"123 Main St"},"amount":1000}""").asJsonObject
        
        // Try to get non-object as object, should return empty object
        val amount = JsonUtils.getNestedObjectOrEmpty(jsonObject, "amount")
        
        // Verify empty object is returned
        assertNotNull(amount)
        assertEquals(0, amount.size())
    }
    
    @Test
    fun testToJsonString() {
        // Create a test data class and instance
        data class PaymentInfo(val card_number: String, val expiration: String, val cvv: String)
        val paymentInfo = PaymentInfo("4111111111111111", "1225", "123")
        
        // Convert to JSON string
        val jsonStr = JsonUtils.toJsonString(paymentInfo)
        
        // Verify conversion
        assertTrue(jsonStr.contains("\"card_number\":\"4111111111111111\""))
        assertTrue(jsonStr.contains("\"expiration\":\"1225\""))
        assertTrue(jsonStr.contains("\"cvv\":\"123\""))
    }
    
    @Test
    fun testFromJsonString() {
        // Create a test data class
        data class PaymentInfo(val card_number: String, val expiration: String, val cvv: String)
        
        // Create a JSON string
        val jsonStr = """{"card_number":"4111111111111111","expiration":"1225","cvv":"123"}"""
        
        // Convert to object
        val paymentInfo = JsonUtils.fromJsonString(jsonStr, PaymentInfo::class.java)
        
        // Verify conversion
        assertNotNull(paymentInfo)
        assertEquals("4111111111111111", paymentInfo?.card_number)
        assertEquals("1225", paymentInfo?.expiration)
        assertEquals("123", paymentInfo?.cvv)
    }
    
    @Test
    fun testFromJsonString_invalidJson() {
        // Create a test data class
        data class PaymentInfo(val card_number: String, val expiration: String, val cvv: String)
        
        // Create an invalid JSON string
        val jsonStr = "{invalid_json"
        
        // Convert to object - should return null
        val paymentInfo = JsonUtils.fromJsonString(jsonStr, PaymentInfo::class.java)
        
        // Verify conversion
        assertNull(paymentInfo)
    }
    
    @Test
    fun testFromJsonStringToList() {
        // Create a test data class
        data class CardInfo(val brand: String, val last4: String)
        
        // Create a JSON array string
        val jsonStr = """[{"brand":"visa","last4":"1111"},{"brand":"mastercard","last4":"2222"}]"""
        
        // Convert to list
        val cardList = JsonUtils.fromJsonStringToList(jsonStr, CardInfo::class.java)
        
        // Verify conversion
        assertNotNull(cardList)
        assertEquals(2, cardList.size)
        assertEquals("visa", cardList[0].brand)
        assertEquals("1111", cardList[0].last4)
        assertEquals("mastercard", cardList[1].brand)
        assertEquals("2222", cardList[1].last4)
    }
    
    @Test
    fun testFromJsonStringToList_invalidJson() {
        // Create a test data class
        data class CardInfo(val brand: String, val last4: String)
        
        // Create an invalid JSON string
        val jsonStr = "[invalid_json"
        
        // Convert to list - should return empty list
        val cardList = JsonUtils.fromJsonStringToList(jsonStr, CardInfo::class.java)
        
        // Verify conversion
        assertTrue(cardList.isEmpty())
    }
    
    @Test
    fun testIsValidJson_validJson() {
        // Valid JSON
        val jsonStr = """{"card_number":"4111111111111111"}"""
        
        // Validate JSON
        val isValid = JsonUtils.isValidJson(jsonStr)
        
        // Verify validation
        assertTrue(isValid)
    }
    
    @Test
    fun testIsValidJson_invalidJson() {
        // Invalid JSON
        val jsonStr = "{invalid_json"
        
        // Validate JSON
        val isValid = JsonUtils.isValidJson(jsonStr)
        
        // Verify validation
        assertFalse(isValid)
    }
    
    @Test
    fun testIsValidJson_nullInput() {
        // Null input
        val isValid = JsonUtils.isValidJson(null)
        
        // Verify validation
        assertFalse(isValid)
    }
    
    @Test
    fun testCreateJsonObject() {
        // Create a map with payment data
        val map = mapOf(
            "card_number" to "4111111111111111",
            "expiration" to "1225",
            "cvv" to "123",
            "amount" to 1000,
            "tokenize" to true,
            "null_value" to null
        )
        
        // Convert to JSON object
        val jsonObject = JsonUtils.createJsonObject(map)
        
        // Verify conversion
        assertEquals("4111111111111111", jsonObject.get("card_number").asString)
        assertEquals("1225", jsonObject.get("expiration").asString)
        assertEquals("123", jsonObject.get("cvv").asString)
        assertEquals(1000, jsonObject.get("amount").asInt)
        assertTrue(jsonObject.get("tokenize").asBoolean)
        assertTrue(jsonObject.has("null_value"))
        assertTrue(jsonObject.get("null_value").isJsonNull)
    }
} 