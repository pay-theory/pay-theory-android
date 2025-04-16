package com.paytheory.lib.googlepay

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Utility class for accessing private methods and properties using reflection
 * This is used for testing components with private implementation details
 */
object TestReflectionUtils {
    
    /**
     * Get the value of a private property from an object
     *
     * @param obj The object instance
     * @param fieldName The name of the private field
     * @return The value of the private field
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getPrivateProperty(obj: Any, fieldName: String): T {
        val field = findField(obj.javaClass, fieldName)
        field.isAccessible = true
        return field.get(obj) as T
    }
    
    /**
     * Set the value of a private property in an object
     *
     * @param obj The object instance
     * @param fieldName The name of the private field
     * @param value The value to set
     */
    fun setPrivateProperty(obj: Any, fieldName: String, value: Any?) {
        val field = findField(obj.javaClass, fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
    
    /**
     * Invoke a private method on an object
     *
     * @param obj The object instance
     * @param methodName The name of the private method
     * @param args The arguments to pass to the method
     * @return The result of the method invocation
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> invokePrivateMethod(
        obj: Any,
        methodName: String,
        vararg args: Any?
    ): T {
        val parameterTypes = args.map { 
            it?.javaClass ?: Any::class.java 
        }.toTypedArray<Class<*>>()
        
        val method = findMethod(obj.javaClass, methodName, parameterTypes)
        method.isAccessible = true
        return method.invoke(obj, *args) as T
    }
    
    /**
     * Get a private static property value
     *
     * @param clazz The class containing the static field
     * @param fieldName The name of the private static field
     * @return The value of the private static field
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getPrivateStaticProperty(clazz: Class<*>, fieldName: String): T {
        val field = findField(clazz, fieldName)
        field.isAccessible = true
        return field.get(null) as T
    }
    
    /**
     * Set a private static property value
     *
     * @param clazz The class containing the static field
     * @param fieldName The name of the private static field
     * @param value The value to set
     */
    fun setPrivateStaticProperty(clazz: Class<*>, fieldName: String, value: Any?) {
        val field = findField(clazz, fieldName)
        field.isAccessible = true
        field.set(null, value)
    }
    
    /**
     * Invoke a private static method
     *
     * @param clazz The class containing the static method
     * @param methodName The name of the private static method
     * @param args The arguments to pass to the method
     * @return The result of the method invocation
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> invokePrivateStaticMethod(
        clazz: Class<*>,
        methodName: String,
        vararg args: Any?
    ): T {
        val parameterTypes = args.map { 
            it?.javaClass ?: Any::class.java 
        }.toTypedArray<Class<*>>()
        
        val method = findMethod(clazz, methodName, parameterTypes)
        method.isAccessible = true
        return method.invoke(null, *args) as T
    }
    
    /**
     * Find a field in a class or its superclasses
     *
     * @param clazz The class to search in
     * @param fieldName The name of the field
     * @return The Field object
     */
    private fun findField(clazz: Class<*>, fieldName: String): Field {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName)
            } catch (e: NoSuchFieldException) {
                currentClass = currentClass.superclass
            }
        }
        throw NoSuchFieldException("Field $fieldName not found in ${clazz.name} or any superclass")
    }
    
    /**
     * Find a method in a class or its superclasses
     *
     * @param clazz The class to search in
     * @param methodName The name of the method
     * @param parameterTypes The parameter types of the method
     * @return The Method object
     */
    private fun findMethod(
        clazz: Class<*>,
        methodName: String,
        parameterTypes: Array<Class<*>>
    ): Method {
        var currentClass: Class<*>? = clazz
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredMethod(methodName, *parameterTypes)
            } catch (e: NoSuchMethodException) {
                currentClass = currentClass.superclass
            }
        }
        throw NoSuchMethodException(
            "Method $methodName with parameters ${parameterTypes.joinToString(", ")} not found in ${clazz.name} or any superclass"
        )
    }
} 