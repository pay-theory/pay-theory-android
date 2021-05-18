package com.paytheory.exampleapplication.tests

import com.paytheory.android.sdk.PaymentError
import com.paytheory.android.sdk.PaymentResult
import com.paytheory.android.sdk.PaymentResultFailure
import org.junit.Test

/**
 * Class that is used to test Payable interface
 */
class PayableTests {
    /**
     *
     */
    @Test
    fun paymentResultTests() {
        val map = mapOf("Custom tags" to "My tags")
        val paymentResult = PaymentResult("12345", "1234", "VISA", "OH",
            2000, "1000", map, "123456", "1234567", "CARD")


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
        val paymentResultFailure = PaymentResultFailure("12345", "1234",
            "VISA", "OH", "CARD")


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
        val paymentError = PaymentError("Payment Declined")

        assert(paymentError.reason == "Payment Declined")

    }
}