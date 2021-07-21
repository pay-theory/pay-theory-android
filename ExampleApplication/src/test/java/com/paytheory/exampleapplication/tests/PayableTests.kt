package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.PaymentError
import com.paytheory.android.sdk.PaymentResult
import com.paytheory.android.sdk.PaymentResultFailure
import org.junit.Test

/**
 * Class that is used to test Payable interface
 */
class PayableTests {

    val recieptNumber = "12345"
    val lastFour = "1234"
    val brand = "VISA"
    val state = "OH"
    val amount = 2000
    val serviceFee = "1000"
    val tags = mapOf("Custom tags" to "My tags")
    val createdAt = "1234567"
    val updateAt = "123456789"
    val type = "CARD"
    val reason = "Payment Declined"

    /**
     *
     */
    @Test
    fun paymentResultTests() {

        val paymentResult = PaymentResult(recieptNumber, lastFour,brand,state,amount,serviceFee,tags,createdAt,updateAt,type)

        assert(paymentResult.receipt_number == "12345")
        assert(paymentResult.last_four == "1234")
        assert(paymentResult.brand == "VISA")
        assert(paymentResult.state == "OH")
        assert(paymentResult.amount == 2000)
        assert(paymentResult.service_fee == "1000")
        assert(paymentResult.tags == mapOf("Custom tags" to "My tags"))
        assert(paymentResult.created_at == "123456")
        assert(paymentResult.updated_at == "1234567")
        assert(paymentResult.type == "CARD")

    }

    /**
     *
     */
    @Test
    fun paymentResultFailureTests() {
        val paymentResultFailure = PaymentResultFailure(recieptNumber,lastFour,brand,state,type)

        assert(paymentResultFailure.receipt_number == "12345")
        assert(paymentResultFailure.last_four == "1234")
        assert(paymentResultFailure.brand == "VISA")
        assert(paymentResultFailure.state == "OH")
        assert(paymentResultFailure.type == "CARD")

    }

    /**
     *
     */
    @Test
    fun paymentErrorTests() {
        val paymentError = PaymentError(reason)

        assert(paymentError.reason == "Payment Declined")

    }
}