package com.paytheory.android.test.fixtures

import org.json.JSONObject

/**
 * Test fixtures for common test data and responses
 */
object TestResponses {
    
    /**
     * Get a successful payment response JSON
     */
    fun successfulPaymentResponse(): String {
        val json = JSONObject().apply {
            put("transfer_complete", true)
            put("completed_transfer", true)
            put("receipt_number", "TEST-12345")
            put("last_four", "1111")
            put("transaction_id", "txn_test123456789")
            put("status", "COMPLETE")
            put("type", "PAYMENT")
        }
        return json.toString()
    }
    
    /**
     * Get a failed payment response JSON
     */
    fun failedPaymentResponse(errorMessage: String = "Payment declined"): String {
        val json = JSONObject().apply {
            put("transfer_complete", false)
            put("completed_transfer", false)
            put("error", true)
            put("error_message", errorMessage)
            put("status", "FAILED")
        }
        return json.toString()
    }
    
    /**
     * Get a successful tokenization response JSON
     */
    fun successfulTokenizationResponse(): String {
        val json = JSONObject().apply {
            put("tokenize_complete", true)
            put("payment_method_id", "pm_test123456789")
            put("last_four", "1111")
            put("status", "COMPLETE")
            put("type", "TOKEN")
        }
        return json.toString()
    }
    
    /**
     * Get a host token response JSON
     */
    fun hostTokenResponse(): String {
        val json = JSONObject().apply {
            put("result", "host_token_result")
            put("host_token", "test-host-token-123456789")
            put("socket_public_key", "test-socket-public-key")
        }
        return json.toString()
    }
    
    /**
     * Get a transfer part one response JSON
     */
    fun transferPartOneResponse(): String {
        val json = JSONObject().apply {
            put("transfer_part_one_result", true)
            put("transaction_id", "txn_test123456789")
        }
        return json.toString()
    }
    
    /**
     * Get a Google Pay wallet transaction response JSON
     */
    fun googlePayTransactionResponse(): String {
        val json = JSONObject().apply {
            put("wallet_complete", true)
            put("completed_transfer", true)
            put("receipt_number", "GPAY-12345")
            put("transaction_id", "txn_gpay123456789")
            put("status", "COMPLETE")
            put("type", "WALLET")
        }
        return json.toString()
    }
    
    /**
     * Get a generic error response JSON
     */
    fun errorResponse(message: String = "An error occurred"): String {
        val json = JSONObject().apply {
            put("error", true)
            put("message", message)
        }
        return json.toString()
    }
    
    /**
     * Get a connection message
     */
    fun connectionMessage(): String = "connected to socket"
    
    /**
     * Get a disconnection message
     */
    fun disconnectionMessage(): String = "disconnected from socket"
} 