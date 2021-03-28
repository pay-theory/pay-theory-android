package com.paytheory.android.sdk.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

class WebSocketViewModel(
    private val interactor: WebsocketInteractor,
    var payTheoryToken: String
):
    ViewModel() {
    @ExperimentalCoroutinesApi
    fun subscribeToSocketEvents(handler: WebsocketMessageHandler) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

class WebsocketInteractor constructor(private val repository: WebsocketRepository) {

    @ExperimentalCoroutinesApi
    fun stopSocket() {
        repository.closeSocket()
    }

    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        repository.sendMessage(message)
    }

    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String): Channel<SocketUpdate> = repository.startSocket(ptToken)

}

class WebsocketRepository constructor(private val webServicesProvider: WebServicesProvider) {

    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String): Channel<SocketUpdate> =
        webServicesProvider.startSocket(ptToken)

    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        webServicesProvider.sendMessage(message)
    }

    @ExperimentalCoroutinesApi
    fun closeSocket() {
        webServicesProvider.stopSocket()
    }
}