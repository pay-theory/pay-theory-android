package com.paytheory.android.test.factories

import com.paytheory.android.sdk.model.FeeMode
import com.paytheory.android.sdk.model.PaymentDetail
import com.paytheory.android.sdk.model.PayorInfo
import com.paytheory.android.sdk.model.SecureString
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

/**
 * Factory for creating PaymentDetail instances for testing
 */
class PaymentDetailFactory : BaseFactory<PaymentDetail> {
    override fun create(): PaymentDetail {
        return PaymentDetail(
            paymentId = "pt-payment-123456789",
            amount = 1000,
            last4 = "4242",
            brand = "visa",
            accountType = "credit",
            merchantReference = "order-123",
            paymentType = "card"
        )
    }
    
    override fun createRandom(): PaymentDetail {
        val brands = listOf("visa", "mastercard", "amex", "discover")
        val accountTypes = listOf("credit", "debit")
        val paymentTypes = listOf("card", "ach")
        
        return PaymentDetail(
            paymentId = "pt-payment-" + UUID.randomUUID().toString().substring(0, 8),
            amount = Random.nextInt(100, 10000),
            last4 = Random.nextInt(1000, 9999).toString(),
            brand = brands.random(),
            accountType = accountTypes.random(),
            merchantReference = "order-" + Random.nextInt(1000, 9999),
            paymentType = paymentTypes.random()
        )
    }
    
    /**
     * Creates a PaymentDetail with a specific amount
     *
     * @param amount The payment amount in cents
     * @return A PaymentDetail with the specified amount
     */
    fun createWithAmount(amount: Int): PaymentDetail {
        return create().copy(amount = amount)
    }
    
    /**
     * Creates a PaymentDetail with a specific payment type
     *
     * @param paymentType The type of payment (e.g., "card", "ach")
     * @return A PaymentDetail with the specified payment type
     */
    fun createWithType(paymentType: String): PaymentDetail {
        return create().copy(paymentType = paymentType)
    }
    
    /**
     * Creates a PaymentDetail for ACH transaction
     */
    fun createWithAch(): PaymentDetail = PaymentDetail(
        account_number = SecureString("12345678").toString(),
        bank_code = "123456789",
        account_type = "checking",
        currency = "USD",
        amount = BigDecimal("10.00"),
        type = "ach",
        fee_mode = FeeMode.MERCHANT_FEE,
        name = "Test User",
        payorInfo = createDefaultPayorInfo()
    )
    
    /**
     * Creates a PaymentDetail for a Google Pay transaction
     */
    fun createWithGooglePay(googlePayToken: String = "test_google_pay_token"): PaymentDetail = PaymentDetail(
        googlePayToken = googlePayToken,
        currency = "USD",
        amount = BigDecimal("10.00"),
        type = "google_pay",
        fee_mode = FeeMode.MERCHANT_FEE,
        payorInfo = createDefaultPayorInfo()
    )
    
    /**
     * Creates a PaymentDetail with different fee mode
     */
    fun createWithFeeMode(feeMode: FeeMode): PaymentDetail {
        val payment = create()
        return payment.copy(fee_mode = feeMode)
    }
    
    /**
     * Creates a PaymentDetail with invalid card number for testing error cases
     */
    fun createWithInvalidCardNumber(): PaymentDetail {
        val payment = create()
        return payment.copy(number = SecureString("1234567890123456").toString())
    }
    
    /**
     * Creates a default PayorInfo object for testing
     */
    private fun createDefaultPayorInfo(): PayorInfo = PayorInfo(
        name = "Test User",
        email = "test@example.com",
        phone = "5555555555"
    )
} 