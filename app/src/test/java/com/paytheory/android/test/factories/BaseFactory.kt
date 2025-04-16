package com.paytheory.android.test.factories

/**
 * Base interface for test data factories
 * 
 * Provides common methods for creating test data objects
 * 
 * @param T the type of object this factory creates
 */
interface BaseFactory<T> {
    /**
     * Creates a new instance with default values
     * 
     * @return A new instance of the object
     */
    fun create(): T
    
    /**
     * Creates a new instance with random values
     *
     * @return A new instance of the object with randomized values
     */
    fun createRandom(): T
    
    /**
     * Creates a list of instances with default values
     *
     * @param count The number of instances to create
     * @return A list of new instances
     */
    fun createList(count: Int): List<T> {
        return (1..count).map { create() }
    }
    
    /**
     * Creates a list of instances with random values
     *
     * @param count The number of instances to create
     * @return A list of new instances with randomized values
     */
    fun createRandomList(count: Int): List<T> {
        return (1..count).map { createRandom() }
    }
} 