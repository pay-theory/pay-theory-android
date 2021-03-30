package com.paytheory.android.sdk.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class WebSocketViewModel(
    private val interactor: WebsocketInteractor,
    var payTheoryToken: String
):
    ViewModel() {

    /**
     * Function to start socket
     * @param handler WebSocket message handler
     */
    @ExperimentalCoroutinesApi
    fun subscribeToSocketEvents(handler: WebsocketMessageHandler) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("starting socket")
                interactor.startSocket(payTheoryToken).consumeEach {
                    if (it.exception == null) {
                        handler.receiveMessage(it.text!!)
                    } else {
                        onSocketError(it.exception!!)
                    }
                }
            } catch (ex: java.lang.Exception) {
                onSocketError(ex)
            }
        }
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
        println("Error occurred : ${ex.message}")
    }

    override fun onCleared() {
        interactor.stopSocket()
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
    fun startSocket(ptToken:String): Channel<SocketUpdate> = repository.startSocket(ptToken)

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
    fun startSocket(ptToken:String): Channel<SocketUpdate> =
        webServicesProvider.startSocket(ptToken)

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