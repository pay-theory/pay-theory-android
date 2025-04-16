package com.paytheory.lib.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for JsonUtils helper class.
 * These tests verify JSON parsing, formatting and error handling capabilities.
 */
class JsonUtilsTest {
    
    @Test
    fun `parseSafeJson should parse valid JSON strings`() {
        // Given
        val validJson = """{"key": "value", "nested": {"nestedKey": "nestedValue"}}"""
        
        // When
        val result = JsonUtils.parseSafeJson(validJson)
        
        // Then
        assertNotNull(result)
        assertEquals("value", result.get("key").asString)
        assertTrue(result.has("nested"))
        assertEquals("nestedValue", result.getAsJsonObject("nested").get("nestedKey").asString)
    }
    
    @Test
    fun `parseSafeJson should return empty JsonObject for invalid JSON`() {
        // Given
        val invalidJson = """{"key": "value"""  // Missing closing brace
        
        // When
        val result = JsonUtils.parseSafeJson(invalidJson)
        
        // Then
        assertNotNull(result)
        assertEquals(0, result.size())
    }
    
    @Test
    fun `parseSafeJson should return empty JsonObject for empty string`() {
        // Given
        val emptyJson = ""
        
        // When
        val result = JsonUtils.parseSafeJson(emptyJson)
        
        // Then
        assertNotNull(result)
        assertEquals(0, result.size())
    }
    
    @Test
    fun `parseSafeJson should return empty JsonObject for null input`() {
        // Given
        val nullJson: String? = null
        
        // When
        val result = JsonUtils.parseSafeJson(nullJson)
        
        // Then
        assertNotNull(result)
        assertEquals(0, result.size())
    }
    
    @Test
    fun `getStringOrDefault should return value when present`() {
        // Given
        val json = JsonParser.parseString("""{"key": "value"}""").asJsonObject
        
        // When
        val result = JsonUtils.getStringOrDefault(json, "key", "default")
        
        // Then
        assertEquals("value", result)
    }
    
    @Test
    fun `getStringOrDefault should return default when key is missing`() {
        // Given
        val json = JsonParser.parseString("""{"otherKey": "value"}""").asJsonObject
        
        // When
        val result = JsonUtils.getStringOrDefault(json, "key", "default")
        
        // Then
        assertEquals("default", result)
    }
    
    @Test
    fun `getStringOrDefault should return default when value is not a string`() {
        // Given
        val json = JsonParser.parseString("""{"key": 123}""").asJsonObject
        
        // When
        val result = JsonUtils.getStringOrDefault(json, "key", "default")
        
        // Then
        assertEquals("default", result)
    }
    
    @Test
    fun `getIntOrDefault should return value when present`() {
        // Given
        val json = JsonParser.parseString("""{"key": 123}""").asJsonObject
        
        // When
        val result = JsonUtils.getIntOrDefault(json, "key", 456)
        
        // Then
        assertEquals(123, result)
    }
    
    @Test
    fun `getIntOrDefault should return default when key is missing`() {
        // Given
        val json = JsonParser.parseString("""{"otherKey": 123}""").asJsonObject
        
        // When
        val result = JsonUtils.getIntOrDefault(json, "key", 456)
        
        // Then
        assertEquals(456, result)
    }
    
    @Test
    fun `getIntOrDefault should return default when value is not a number`() {
        // Given
        val json = JsonParser.parseString("""{"key": "not a number"}""").asJsonObject
        
        // When
        val result = JsonUtils.getIntOrDefault(json, "key", 456)
        
        // Then
        assertEquals(456, result)
    }
    
    @Test
    fun `getBooleanOrDefault should return value when present`() {
        // Given
        val json = JsonParser.parseString("""{"key": true}""").asJsonObject
        
        // When
        val result = JsonUtils.getBooleanOrDefault(json, "key", false)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `getBooleanOrDefault should return default when key is missing`() {
        // Given
        val json = JsonParser.parseString("""{"otherKey": true}""").asJsonObject
        
        // When
        val result = JsonUtils.getBooleanOrDefault(json, "key", false)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getNestedObjectOrEmpty should return nested object when present`() {
        // Given
        val json = JsonParser.parseString("""{"nested": {"key": "value"}}""").asJsonObject
        
        // When
        val result = JsonUtils.getNestedObjectOrEmpty(json, "nested")
        
        // Then
        assertTrue(result.has("key"))
        assertEquals("value", result.get("key").asString)
    }
    
    @Test
    fun `getNestedObjectOrEmpty should return empty object when key is missing`() {
        // Given
        val json = JsonParser.parseString("""{"other": {"key": "value"}}""").asJsonObject
        
        // When
        val result = JsonUtils.getNestedObjectOrEmpty(json, "nested")
        
        // Then
        assertEquals(0, result.size())
    }
    
    @Test
    fun `toJsonString should convert object to JSON string`() {
        // Given
        val testObject = TestModel("test", 123)
        
        // When
        val result = JsonUtils.toJsonString(testObject)
        
        // Then
        val expected = """{"name":"test","value":123}"""
        assertEquals(expected, result)
    }
    
    @Test
    fun `fromJsonString should convert JSON string to object`() {
        // Given
        val jsonString = """{"name":"test","value":123}"""
        
        // When
        val result = JsonUtils.fromJsonString(jsonString, TestModel::class.java)
        
        // Then
        assertNotNull(result)
        assertEquals("test", result?.name)
        assertEquals(123, result?.value)
    }
    
    @Test
    fun `fromJsonString should return null for invalid JSON`() {
        // Given
        val invalidJson = """{"name":"test" "value":123}"""  // Missing comma
        
        // When
        val result = JsonUtils.fromJsonString(invalidJson, TestModel::class.java)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `fromJsonStringToList should convert JSON array to list of objects`() {
        // Given
        val jsonArray = """[{"name":"test1","value":123},{"name":"test2","value":456}]"""
        
        // When
        val result = JsonUtils.fromJsonStringToList(jsonArray, TestModel::class.java)
        
        // Then
        assertEquals(2, result.size)
        assertEquals("test1", result[0].name)
        assertEquals(123, result[0].value)
        assertEquals("test2", result[1].name)
        assertEquals(456, result[1].value)
    }
    
    @Test
    fun `fromJsonStringToList should return empty list for invalid JSON`() {
        // Given
        val invalidJson = """[{"name":"test1","value":123},{"name":"test2"]"""
        
        // When
        val result = JsonUtils.fromJsonStringToList(invalidJson, TestModel::class.java)
        
        // Then
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `isValidJson should return true for valid JSON`() {
        // Given
        val validJson = """{"key": "value"}"""
        
        // When
        val result = JsonUtils.isValidJson(validJson)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidJson should return false for invalid JSON`() {
        // Given
        val invalidJson = """{"key": "value"""  // Missing closing brace
        
        // When
        val result = JsonUtils.isValidJson(invalidJson)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isValidJson should return false for null or empty string`() {
        // Given
        val nullJson: String? = null
        val emptyJson = ""
        
        // When
        val nullResult = JsonUtils.isValidJson(nullJson)
        val emptyResult = JsonUtils.isValidJson(emptyJson)
        
        // Then
        assertFalse(nullResult)
        assertFalse(emptyResult)
    }
    
    @Test
    fun `createJsonObject should convert map to JsonObject`() {
        // Given
        val map = mapOf(
            "stringKey" to "stringValue",
            "numberKey" to 123,
            "booleanKey" to true,
            "nullKey" to null
        )
        
        // When
        val result = JsonUtils.createJsonObject(map)
        
        // Then
        assertEquals("stringValue", result.get("stringKey").asString)
        assertEquals(123, result.get("numberKey").asInt)
        assertTrue(result.get("booleanKey").asBoolean)
        assertTrue(result.has("nullKey") && result.get("nullKey").isJsonNull)
    }
    
    /**
     * Simple data class for testing JSON conversion.
     */
    private data class TestModel(
        val name: String,
        val value: Int
    )
} 