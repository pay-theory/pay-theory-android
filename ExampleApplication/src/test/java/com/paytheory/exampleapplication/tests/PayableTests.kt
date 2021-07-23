package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.PaymentError
import com.paytheory.android.sdk.PaymentResult
import com.paytheory.android.sdk.PaymentResultFailure
import org.junit.Test

/**
 * Class that is used to test Payable interface
 */
class PayableTests {

    private val receiptNumber = "12345"
    private val lastFour = "1234"
    private val brand = "VISA"
    private val state = "OH"
    private val amount = 2000
    private val serviceFee = "1000"
    private val tags = mapOf("Custom tags" to "My tags")
    private val createdAt = "1234567"
    private val updateAt = "123456789"
    private val type = "CARD"
    private val reason = "Payment Declined"

    /**
     *
     */
    @Test
    fun paymentResultTests() {

        val paymentResult = PaymentResult(receiptNumber, lastFour,brand,state,amount,serviceFee,tags,createdAt,updateAt,type)

        assert(paymentResult.receipt_number == receiptNumber)
        assert(paymentResult.last_four == receiptNumber)
        assert(paymentResult.brand == brand)
        assert(paymentResult.state == state)
        assert(paymentResult.amount == amount)
        assert(paymentResult.service_fee == serviceFee)
        assert(paymentResult.tags == tags)
        assert(paymentResult.created_at == createdAt)
        assert(paymentResult.updated_at == updateAt)
        assert(paymentResult.type == type)

    }

    /**
     *
     */
    @Test
    fun paymentResultFailureTests() {
        val paymentResultFailure = PaymentResultFailure(receiptNumber,lastFour,brand,state,type)

        assert(paymentResultFailure.receipt_number == receiptNumber)
        assert(paymentResultFailure.last_four == receiptNumber)
        assert(paymentResultFailure.brand == brand)
        assert(paymentResultFailure.state == state)
        assert(paymentResultFailure.type == type)

    }

    /**
     *
     */
    @Test
    fun paymentErrorTests() {
        val paymentError = PaymentError(reason)

        assert(paymentError.reason == reason)

    }
}