package com.paytheory.android.test.factories

import com.paytheory.android.sdk.model.Transaction
import com.paytheory.android.sdk.model.TransactionStatus
import java.util.Date
import java.util.UUID
import kotlin.random.Random

/**
 * Factory for creating Transaction instances for testing
 */
class TransactionFactory : BaseFactory<Transaction> {
    override fun create(): Transaction {
        return Transaction(
            id = "pt-transaction-123456789",
            amount = 1000,
            status = TransactionStatus.COMPLETED,
            createdAt = Date(),
            reference = "ref-123",
            paymentMethod = "card"
        )
    }
    
    override fun createRandom(): Transaction {
        val statuses = listOf(
            TransactionStatus.COMPLETED, 
            TransactionStatus.PENDING, 
            TransactionStatus.FAILED
        )
        val paymentMethods = listOf("card", "ach", "cash")
        
        return Transaction(
            id = "pt-transaction-" + UUID.randomUUID().toString().substring(0, 8),
            amount = Random.nextInt(100, 10000),
            status = statuses.random(),
            createdAt = Date(System.currentTimeMillis() - Random.nextLong(0, 30L * 24 * 60 * 60 * 1000)),
            reference = "ref-" + Random.nextInt(1000, 9999),
            paymentMethod = paymentMethods.random()
        )
    }
    
    /**
     * Creates a Transaction with a specific status
     *
     * @param status The transaction status
     * @return A Transaction with the specified status
     */
    fun createWithStatus(status: TransactionStatus): Transaction {
        return create().copy(status = status)
    }
    
    /**
     * Creates a Transaction with a specific amount
     *
     * @param amount The transaction amount in cents
     * @return A Transaction with the specified amount
     */
    fun createWithAmount(amount: Int): Transaction {
        return create().copy(amount = amount)
    }
} 