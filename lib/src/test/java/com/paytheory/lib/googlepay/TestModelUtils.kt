package com.paytheory.lib.googlepay

import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.data.payable.ErrorCode
import io.mockk.mockk
import org.json.JSONObject
import java.util.Date

/**
 * Utility class for creating test models
 * Provides implementations of model classes that might be missing during testing
 */
object TestModelUtils {
    
    /**
     * Creates a PaymentDetail for Google Pay testing
     *
     * @param token The Google Pay token
     * @param amount The payment amount in cents
     * @param feeMode The fee mode (default: "merchant_fee")
     * @return A PaymentDetail object for testing
     */
    fun createGooglePayPaymentDetail(
        token: String = "test-google-pay-token",
        amount: Int = 1099,
        feeMode: String = "MERCHANT_FEE"
    ): PaymentDetail {
        return PaymentDetail(
            type = "wallet",
            timing = Date().time,
            amount = amount,
            currency = "USD",
            walletType = "GOOGLE_PAY",
            digitalWalletPayload = token,
            fee_mode = feeMode,
            service_fee = "0",
            name = "Test User"
        )
    }
    
    /**
     * Creates a PTError for testing
     *
     * @param code The error code
     * @param message The error message
     * @return A PTError object for testing
     */
    fun createPTError(
        code: ErrorCode = ErrorCode.GooglePayError,
        message: String = "Google Pay error"
    ): PTError {
        return PTError(code, message)
    }
    
    /**
     * Creates a simple JSON representation of a Google Pay token
     *
     * @param token The token string
     * @return A JSON string representing a Google Pay token response
     */
    fun createGooglePayTokenJson(token: String = "test-google-pay-token"): String {
        val json = JSONObject()
        val paymentMethodData = JSONObject()
        val tokenizationData = JSONObject()
        
        tokenizationData.put("token", token)
        paymentMethodData.put("tokenizationData", tokenizationData)
        json.put("paymentMethodData", paymentMethodData)
        
        return json.toString()
    }
    
    /**
     * Creates a mock PaymentDetail with common attributes
     *
     * @return A mock PaymentDetail configured with default attributes
     */
    fun createMockPaymentDetail(): PaymentDetail {
        val paymentDetail = mockk<PaymentDetail>(relaxed = true)
        
        // Configure common attributes
        io.mockk.every { paymentDetail.type } returns "wallet"
        io.mockk.every { paymentDetail.amount } returns 1099
        io.mockk.every { paymentDetail.currency } returns "USD"
        io.mockk.every { paymentDetail.walletType } returns "GOOGLE_PAY"
        io.mockk.every { paymentDetail.digitalWalletPayload } returns "test-google-pay-token"
        io.mockk.every { paymentDetail.fee_mode } returns "MERCHANT_FEE"
        
        return paymentDetail
    }
    
    /**
     * Creates a simple websocket response message
     *
     * @param type The message type
     * @param successful Whether the response indicates success
     * @param errorMessage Optional error message
     * @return A JSON string representing a websocket response
     */
    fun createWebsocketResponseJson(
        type: String = "response",
        successful: Boolean = true,
        errorMessage: String? = null
    ): String {
        val json = JSONObject()
        json.put("type", type)
        json.put("success", successful)
        
        if (!successful && errorMessage != null) {
            json.put("error", JSONObject().put("message", errorMessage))
        }
        
        return json.toString()
    }
} 