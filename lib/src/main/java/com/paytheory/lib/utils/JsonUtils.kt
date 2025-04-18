package com.paytheory.lib.utils

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import timber.log.Timber

/**
 * Utility class for handling JSON operations.
 *
 * This class provides helper methods for parsing, extracting, and converting JSON data
 * safely with proper error handling. It uses Gson for JSON operations.
 */
object JsonUtils {
    
    private val gson: Gson = Gson()
    
    /**
     * Parse a JSON string safely, returning an empty JsonObject if parsing fails.
     *
     * @param jsonString The JSON string to parse
     * @return A JsonObject representation of the input, or an empty JsonObject if parsing fails
     */
    fun parseSafeJson(jsonString: String?): JsonObject {
        if (jsonString.isNullOrEmpty()) {
            return JsonObject()
        }
        
        return try {
            JsonParser.parseString(jsonString).asJsonObject
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Failed to parse JSON: %s", jsonString)
            JsonObject()
        }
    }
    
    /**
     * Get a string value from a JsonObject with a default fallback.
     *
     * @param json The JsonObject to extract from
     * @param key The key to look up
     * @param default The default value to return if the key is missing or not a string
     * @return The string value or the default
     */
    fun getStringOrDefault(json: JsonObject, key: String, default: String): String {
        if (!json.has(key)) {
            return default
        }
        
        val element = json.get(key)
        return if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            element.asString
        } else {
            default
        }
    }
    
    /**
     * Get an integer value from a JsonObject with a default fallback.
     *
     * @param json The JsonObject to extract from
     * @param key The key to look up
     * @param default The default value to return if the key is missing or not an integer
     * @return The integer value or the default
     */
    fun getIntOrDefault(json: JsonObject, key: String, default: Int): Int {
        if (!json.has(key)) {
            return default
        }
        
        val element = json.get(key)
        return try {
            if (element.isJsonPrimitive && element.asJsonPrimitive.isNumber) {
                element.asInt
            } else {
                default
            }
        } catch (e: NumberFormatException) {
            Timber.e(e, "Failed to parse integer for key: %s", key)
            default
        }
    }
    
    /**
     * Get a boolean value from a JsonObject with a default fallback.
     *
     * @param json The JsonObject to extract from
     * @param key The key to look up
     * @param default The default value to return if the key is missing or not a boolean
     * @return The boolean value or the default
     */
    fun getBooleanOrDefault(json: JsonObject, key: String, default: Boolean): Boolean {
        if (!json.has(key)) {
            return default
        }
        
        val element = json.get(key)
        return if (element.isJsonPrimitive && element.asJsonPrimitive.isBoolean) {
            element.asBoolean
        } else {
            default
        }
    }
    
    /**
     * Get a nested JsonObject with a default empty JsonObject fallback.
     *
     * @param json The JsonObject to extract from
     * @param key The key to look up
     * @return The nested JsonObject or an empty JsonObject if not found
     */
    fun getNestedObjectOrEmpty(json: JsonObject, key: String): JsonObject {
        if (!json.has(key)) {
            return JsonObject()
        }
        
        val element = json.get(key)
        return if (element.isJsonObject) {
            element.asJsonObject
        } else {
            JsonObject()
        }
    }
    
    /**
     * Convert an object to a JSON string.
     *
     * @param obj The object to convert
     * @return The JSON string representation
     */
    fun <T> toJsonString(obj: T): String {
        return gson.toJson(obj)
    }
    
    /**
     * Convert a JSON string to an object of the specified type.
     *
     * @param json The JSON string to convert
     * @param clazz The class of the target object
     * @return The object, or null if conversion fails
     */
    fun <T> fromJsonString(json: String, clazz: Class<T>): T? {
        return try {
            gson.fromJson(json, clazz)
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Failed to parse JSON to object: %s", json)
            null
        }
    }
    
    /**
     * Convert a JSON string to a list of objects of the specified type.
     *
     * @param json The JSON string to convert
     * @param clazz The class of the target object list elements
     * @return The list of objects, or an empty list if conversion fails
     */
    fun <T> fromJsonStringToList(json: String, clazz: Class<T>): List<T> {
        return try {
            val listType = TypeToken.getParameterized(List::class.java, clazz).type
            gson.fromJson(json, listType)
        } catch (e: JsonSyntaxException) {
            Timber.e(e, "Failed to parse JSON to list: %s", json)
            emptyList()
        }
    }
    
    /**
     * Check if a JSON string is valid.
     *
     * @param jsonString The JSON string to validate
     * @return True if the string is valid JSON, false otherwise
     */
    @SuppressLint("CheckResult")
    fun isValidJson(jsonString: String?): Boolean {
        if (jsonString.isNullOrEmpty()) {
            return false
        }
        
        return try {
            JsonParser.parseString(jsonString)
            true
        } catch (e: JsonSyntaxException) {
            false
        }
    }
    
    /**
     * Create a JsonObject from a map of key-value pairs.
     *
     * @param map The map of key-value pairs
     * @return A JsonObject containing the key-value pairs
     */
    fun createJsonObject(map: Map<String, Any?>): JsonObject {
        val jsonObject = JsonObject()
        
        map.forEach { (key, value) ->
            when (value) {
                null -> jsonObject.add(key, null)
                is String -> jsonObject.addProperty(key, value)
                is Number -> jsonObject.addProperty(key, value)
                is Boolean -> jsonObject.addProperty(key, value)
                is JsonElement -> jsonObject.add(key, value)
                else -> jsonObject.addProperty(key, value.toString())
            }
        }
        
        return jsonObject
    }
} 