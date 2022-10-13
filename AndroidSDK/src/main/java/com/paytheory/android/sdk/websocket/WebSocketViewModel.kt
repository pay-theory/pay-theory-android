package com.paytheory.android.sdk.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paytheory.android.sdk.Error
import com.paytheory.android.sdk.Payable
import com.paytheory.android.sdk.PaymentMethodToken
import com.paytheory.android.sdk.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

/**
 * Creates a view model of WebSocket
 * @param interactor WebSocket interactor
 * @param payTheoryToken token used for security
 */
@ExperimentalCoroutinesApi
class WebSocketViewModel(
    private val interactor: WebsocketInteractor,
    var payTheoryToken: String,
    private val partner: String,
    private val stage: String,
    private val transaction: Transaction?,
    private val paymentMethodToken: PaymentMethodToken?
):
    ViewModel() {


    var connected: Boolean = false

    /**
     * Function to disconnect WebSocket
     */
    @ExperimentalCoroutinesApi
    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.stopSocket()
            } catch (ex: java.lang.Exception) {
                onSocketError(ex)
            }
            connected = false
        }
    }

    /**
     * Function to start socket
     * @param handler WebSocket message handler
     */
    @ExperimentalCoroutinesApi
    fun subscribeToSocketEvents(handler: WebsocketMessageHandler) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.startSocket(payTheoryToken, partner, stage).consumeEach {
                    if (it.exception == null) {
                        handler.receiveMessage(it.text!!)

                    } else {
                        onSocketError(it.exception)
                        connected = false
                    }
                }
            } catch (ex: java.lang.Exception) {
                onSocketError(ex)
                connected = false
            }
        }
        connected = true
    }

    /**
     * Function to send messages to server
     * @param message message to send to server
     */
    @ExperimentalCoroutinesApi
    fun sendSocketMessage(message:String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.sendMessage(message)
            } catch (ex: java.lang.Exception) {
                onSocketError(ex)
            }
        }
    }

    private fun onSocketError(ex: Throwable) {
        val error = ex.message.toString()
        println("Error occurred : $error")

        interactor.stopSocket()

        // catch error "Read error: ssl=0x7340b644c8: I/O error during system call, Software caused connection abort"
        if (error.contains("Read error: ssl", ignoreCase = true) || error.contains("Software caused connection abort", ignoreCase = true)){
            if (transaction != null){ // error for transaction request
                if (transaction.context is Payable){
                    println("transaction reset socket")
                    transaction.resetSocket()
                }
            } else if (paymentMethodToken != null){
                if (paymentMethodToken.context is Payable){
                    println("paymentMethodToken reset socket")
                    paymentMethodToken.resetSocket()
                }
            }
        } else { //if error is not ssl error
            // error for transaction request
            if (transaction != null) {
                if (transaction.context is Payable){
                    println("transaction standard error")
                    transaction.context.handleError(Error(error))
                }
            // error for tokenization request
            } else if (paymentMethodToken != null){
                if (paymentMethodToken.context is Payable){
                    println("paymentMethodToken standard error")
                    paymentMethodToken.context.handleError(Error(error))
                }
            }
        }
    }

    override fun onCleared() {
        disconnect()
        super.onCleared()
    }

}

/**
 * Creates WebSocket interactor to start, stop and send messages
 * @param repository WebSocket repository
 */
class WebsocketInteractor constructor(private val repository: WebsocketRepository) {

    /**
     * Function to close WebSocket
     */
    @ExperimentalCoroutinesApi
    fun stopSocket() {
        repository.closeSocket()
    }

    /**
     * Function to send messages though a WebSocket
     */
    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        repository.sendMessage(message)
    }

    /**
     * Function to start WebSocket
     * @param ptToken token used for security
     */
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String, partner: String, stage: String): Channel<SocketUpdate> = repository.startSocket(ptToken, partner, stage)

}

/**
 * Creates WebSocket repository to start, stop and send messages
 * @param webServicesProvider WebSocket web services provider
 */
class WebsocketRepository constructor(private val webServicesProvider: WebServicesProvider) {

    /**
     * Function to start WebSocket
     * @param ptToken token used for security
     */
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String, partner: String, stage: String): Channel<SocketUpdate> =
        webServicesProvider.startSocket(ptToken, partner, stage)

    /**
     * Function to send messages though a WebSocket
     */
    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        webServicesProvider.sendMessage(message)
    }

    /**
     * Function to close WebSocket
     */
    @ExperimentalCoroutinesApi
    fun closeSocket() {
        webServicesProvider.stopSocket()
    }
}