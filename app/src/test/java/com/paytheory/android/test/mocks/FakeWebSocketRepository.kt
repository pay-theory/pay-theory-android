package com.paytheory.android.test.mocks

import com.paytheory.android.sdk.api.SocketUpdate
import com.paytheory.android.sdk.api.WebsocketRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * Fake implementation of WebsocketRepository for testing
 * 
 * Simulates WebSocket communication without actual network calls
 */
@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class FakeWebSocketRepository : WebsocketRepository {
    
    // Capture sent messages for test validation
    val sentMessages = mutableListOf<String>()
    
    // Control the behavior of the fake repository
    var shouldFailConnection = false
    var shouldDelayResponses = false
    var responseDelayMs: Long = 200
    var connectionActive = false
    
    // Channel for socket updates
    private val socketChannel = Channel<SocketUpdate>(10)
    
    // Preset payment data for simulated responses
    var receiptNumber = "TEST-${UUID.randomUUID().toString().substring(0, 8)}"
    var transactionId = "txn_${UUID.randomUUID().toString().substring(0, 16)}"
    
    override fun startSocket(ptToken: String, partner: String, stage: String): Channel<SocketUpdate> {
        connectionActive = !shouldFailConnection
        
        if (connectionActive) {
            // Simulate connection established
            GlobalScope.launch {
                socketChannel.send(SocketUpdate("connected to socket"))
                
                // Simulate host token response
                val hostTokenResponse = JSONObject().apply {
                    put("result", "host_token_result")
                    put("host_token", "test-host-token-${UUID.randomUUID()}")
                    put("socket_public_key", "test-socket-public-key")
                }
                socketChannel.send(SocketUpdate(hostTokenResponse.toString()))
            }
        } else {
            // Simulate connection failure
            GlobalScope.launch {
                socketChannel.send(SocketUpdate(exception = Exception("Connection failed")))
            }
        }
        
        return socketChannel
    }
    
    override fun sendMessage(message: String) {
        sentMessages.add(message)
        
        if (!connectionActive) {
            return
        }
        
        // Parse the message to determine appropriate response
        GlobalScope.launch {
            if (shouldDelayResponses) {
                kotlinx.coroutines.delay(responseDelayMs)
            }
            
            when {
                message.contains("host:transfer_part1") -> {
                    sendTransferPart1Response()
                    // After a delay, send the completion message
                    kotlinx.coroutines.delay(100)
                    sendTransferCompleteResponse()
                }
                message.contains("host:tokenize") -> {
                    sendTokenizeResponse()
                }
                message.contains("host:wallet_transaction") -> {
                    sendWalletTransactionResponse()
                }
                else -> {
                    // Unknown message type
                    socketChannel.send(SocketUpdate("Unknown message type"))
                }
            }
        }
    }
    
    override fun closeSocket() {
        connectionActive = false
        GlobalScope.launch {
            socketChannel.send(SocketUpdate("disconnected from socket"))
            socketChannel.close()
        }
    }
    
    /**
     * Simulate transfer_part1 response
     */
    private suspend fun sendTransferPart1Response() {
        val response = JSONObject().apply {
            put("transfer_part_one_result", true)
            put("transaction_id", transactionId)
        }
        socketChannel.send(SocketUpdate(response.toString()))
    }
    
    /**
     * Simulate transfer_complete response
     */
    private suspend fun sendTransferCompleteResponse() {
        val response = JSONObject().apply {
            put("transfer_complete", true)
            put("completed_transfer", true)
            put("receipt_number", receiptNumber)
            put("last_four", "1111")
            put("transaction_id", transactionId)
            put("status", "COMPLETE")
            put("type", "PAYMENT")
        }
        socketChannel.send(SocketUpdate(response.toString()))
    }
    
    /**
     * Simulate tokenize response
     */
    private suspend fun sendTokenizeResponse() {
        val response = JSONObject().apply {
            put("tokenize_complete", true)
            put("payment_method_id", "pm_${UUID.randomUUID().toString().substring(0, 16)}")
            put("last_four", "1111")
            put("status", "COMPLETE")
            put("type", "TOKEN")
        }
        socketChannel.send(SocketUpdate(response.toString()))
    }
    
    /**
     * Simulate wallet transaction response
     */
    private suspend fun sendWalletTransactionResponse() {
        val response = JSONObject().apply {
            put("wallet_complete", true)
            put("completed_transfer", true)
            put("receipt_number", receiptNumber)
            put("transaction_id", transactionId)
            put("status", "COMPLETE")
            put("type", "WALLET")
        }
        socketChannel.send(SocketUpdate(response.toString()))
    }
    
    /**
     * Simulate an error response
     */
    fun sendErrorResponse(errorMessage: String) {
        GlobalScope.launch {
            val response = JSONObject().apply {
                put("error", true)
                put("message", errorMessage)
            }
            socketChannel.send(SocketUpdate(response.toString()))
        }
    }
} 