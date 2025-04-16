package com.paytheory.android.test.factories

import com.paytheory.android.sdk.model.FeeMode
import com.paytheory.android.sdk.model.PayTheoryConfiguration
import com.paytheory.android.sdk.model.PaymentMethodType
import com.paytheory.android.sdk.model.PaymentType
import java.math.BigDecimal

/**
 * Factory for creating PayTheoryConfiguration test objects
 */
class PayTheoryConfigurationFactory : BaseFactory<PayTheoryConfiguration> {
    
    /**
     * Creates a default configuration with card payment
     */
    override fun create(): PayTheoryConfiguration = PayTheoryConfiguration(
        apiKey = "test-paytheory-apikey",
        amount = BigDecimal("10.00"),
        paymentType = PaymentType.PAYMENT,
        paymentMethodType = PaymentMethodType.CARD,
        feeMode = FeeMode.MERCHANT_FEE,
        outlined = false,
        requireBillingAddress = false,
        requireName = true
    )
    
    /**
     * Creates a configuration for ACH payments
     */
    fun createForAch(): PayTheoryConfiguration = PayTheoryConfiguration(
        apiKey = "test-paytheory-apikey",
        amount = BigDecimal("10.00"),
        paymentType = PaymentType.PAYMENT,
        paymentMethodType = PaymentMethodType.ACH,
        feeMode = FeeMode.MERCHANT_FEE,
        outlined = false,
        requireBillingAddress = false,
        requireName = true
    )
    
    /**
     * Creates a configuration for tokenization
     */
    fun createForTokenization(): PayTheoryConfiguration = PayTheoryConfiguration(
        apiKey = "test-paytheory-apikey",
        amount = BigDecimal.ZERO,
        paymentType = PaymentType.TOKEN,
        paymentMethodType = PaymentMethodType.CARD,
        feeMode = FeeMode.MERCHANT_FEE,
        outlined = false,
        requireBillingAddress = false,
        requireName = true
    )
    
    /**
     * Creates a configuration with Google Pay enabled
     */
    fun createWithGooglePay(): PayTheoryConfiguration {
        val config = create()
        return config.copy(
            googlePayEnabled = true,
            googlePayMerchantName = "Test Merchant",
            googlePayAllowPrepaidCards = true,
            googlePayAllowCreditCards = true
        )
    }
    
    /**
     * Creates a configuration with specific amount
     */
    fun createWithAmount(amount: BigDecimal): PayTheoryConfiguration {
        val config = create()
        return config.copy(amount = amount)
    }
    
    /**
     * Creates a configuration with full billing address required
     */
    fun createWithBillingAddress(): PayTheoryConfiguration {
        val config = create()
        return config.copy(requireBillingAddress = true)
    }
    
    /**
     * Creates a configuration with buyer fee mode
     */
    fun createWithBuyerFee(): PayTheoryConfiguration {
        val config = create()
        return config.copy(feeMode = FeeMode.BUYER_FEE)
    }
} 