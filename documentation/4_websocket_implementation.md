# Pay Theory Android SDK - WebSocket Implementation

## Overview

The Pay Theory Android SDK uses WebSockets as the primary communication channel for payment processing. This approach offers several advantages:

1. **Real-time Communication**: Enables bidirectional communication between the SDK and Pay Theory servers
2. **Long-lived Connection**: Maintains a persistent connection during payment processing
3. **Efficient Protocol**: Reduces overhead compared to HTTP for multiple message exchanges
4. **Enhanced Security**: Implements end-to-end encryption for sensitive payment data

This document details the WebSocket architecture, implementation, and message flow within the SDK.

## Architecture

The WebSocket implementation follows a clean architecture with separation of concerns across multiple layers:

```
┌───────────────────────────────────┐
│ Payment / PaymentMethodToken      │ ◄─── Higher-level business logic
└───────────────┬───────────────────┘
                │ uses
                ▼
┌───────────────────────────────────┐
│ WebsocketInteractor                │ ◄─── Interface layer
└───────────────┬───────────────────┘
                │ delegates to
                ▼
┌───────────────────────────────────┐
│ WebsocketRepository               │ ◄─── Repository layer
└───────────────┬───────────────────┘
                │ delegates to
                ▼
┌───────────────────────────────────┐
│ WebServicesProvider               │ ◄─── Implementation layer
└───────────────┬───────────────────┘
                │ uses
                ▼
┌───────────────────────────────────┐
│ PTWebSocketListener               │ ◄─── Event handling layer
└───────────────────────────────────┘
```

### Key Components

#### 1. WebsocketInteractor

The top-level interface that the payment processing classes interact with:

```kotlin
class WebsocketInteractor(private val repository: WebsocketRepository) {
    @ExperimentalCoroutinesApi
    fun stopSocket() {
        repository.closeSocket()
    }

    @ExperimentalCoroutinesApi
    fun sendMessage(message: String) {
        repository.sendMessage(message)
    }

    @ExperimentalCoroutinesApi
    fun startSocket(ptToken: String, partner: String, stage: String): Channel<SocketUpdate> = 
        repository.startSocket(ptToken, partner, stage)
}
```

This class:
- Provides a clean API for socket operations
- Delegates actual implementation to the repository layer
- Returns a Kotlin Channel for event handling

#### 2. WebsocketRepository

Follows the repository pattern to abstract the data source:

```kotlin
class WebsocketRepository(private val webServicesProvider: WebServicesProvider) {
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken: String, partner: String, stage: String): Channel<SocketUpdate> =
        webServicesProvider.startSocket(ptToken, partner, stage)

    @ExperimentalCoroutinesApi
    fun sendMessage(message: String) {
        webServicesProvider.sendMessage(message)
    }

    @ExperimentalCoroutinesApi
    fun closeSocket() {
        webServicesProvider.stopSocket()
    }
}
```

This class:
- Acts as a wrapper around the service provider
- Maintains a clean separation between interface and implementation
- Follows dependency inversion principles

#### 3. WebServicesProvider

The concrete implementation that manages the WebSocket connection:

```kotlin
class WebServicesProvider {
    val normalClosureStatus = 1000
    private var webSocket: WebSocket? = null
    
    @ExperimentalCoroutinesApi
    private var webSocketListener: PTWebSocketListener? = null

    @ExperimentalCoroutinesApi
    fun startSocket(ptToken: String, partner: String, stage: String): Channel<SocketUpdate> =
        with(PTWebSocketListener()) {
            try {
                startSocket(this, ptToken, partner, stage)
                this@with.socketEventChannel
            } catch (ex: Exception) {
                throw SocketStartException(ex.message)
            }
        }

    @ExperimentalCoroutinesApi
    fun startSocket(webSocketListener: PTWebSocketListener, ptToken: String, partner: String, stage: String) {
        try {
            this.webSocketListener = webSocketListener
            val socketOkHttpClient = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(39, TimeUnit.SECONDS)
                .hostnameVerifier { _, _ -> true }
                .build()
            webSocket = socketOkHttpClient.newWebSocket(
                Request.Builder().url("wss://${partner}.secure.socket.${stage}.com/${partner}?pt_token=${ptToken}")
                    .build(),
                webSocketListener
            )
            socketOkHttpClient.dispatcher.executorService.shutdown()
        } catch (ex: Exception) {
            throw SocketStartException(ex.message)
        }
    }

    @ExperimentalCoroutinesApi
    fun sendMessage(message: String) {
        try {
            webSocket?.send(message)
        } catch (ex: Exception) {
            throw SocketMessageException(ex.message)
        }
    }

    @ExperimentalCoroutinesApi
    fun stopSocket() {
        try {
            webSocket?.close(normalClosureStatus, null)
            webSocket = null
            webSocketListener?.socketEventChannel?.close()
            webSocketListener = null
        } catch (ex: IllegalArgumentException) {
            webSocket = null
            webSocketListener = null
            throw SocketClosureException()
        }
    }
}
```

This class:
- Uses OkHttp's WebSocket implementation
- Configures the WebSocket client with appropriate timeouts
- Creates the WebSocket connection with authentication
- Provides methods for sending messages and closing the connection
- Defines custom exceptions for different failure scenarios

#### 4. PTWebSocketListener

Handles WebSocket lifecycle events:

```kotlin
@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
class PTWebSocketListener : WebSocketListener() {
    val socketEventChannel: Channel<SocketUpdate> = Channel(10)
    val normalClosureStatus = 1000
    
    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                println("Pay Theory Connected")
                socketEventChannel.send(SocketUpdate("connected to socket"))
            }
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                socketEventChannel.send(SocketUpdate(text))
            }
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        GlobalScope.launch {
            try {
                socketEventChannel.send(SocketUpdate(exception = SocketAbortedException()))
                println("Pay Theory Disconnected")
            } catch (e: ClosedSendChannelException) {
                val error = e.message.toString()
                println(error)
            }
        }
        webSocket.close(normalClosureStatus, null)
        socketEventChannel.close()
        println("Pay Theory Disconnected")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (!socketEventChannel.isClosedForSend) {
            GlobalScope.launch {
                socketEventChannel.send(SocketUpdate(exception = t))
            }
        }
    }
}

data class SocketUpdate(
    val text: String? = null,
    val byteString: ByteString? = null,
    val exception: Throwable? = null
)
```

This class:
- Extends OkHttp's WebSocketListener
- Creates a Coroutine Channel for asynchronous event handling
- Handles WebSocket events (open, message, closing, failure)
- Sends structured updates through the channel
- Manages proper cleanup of resources

#### 5. WebsocketMessageHandler

Interface for processing WebSocket messages:

```kotlin
interface WebsocketMessageHandler {
    fun receiveMessage(message: String)
    fun disconnect()
}
```

This interface:
- Defines a contract for handling WebSocket events
- Is implemented by the Payment and PaymentMethodToken classes
- Decouples message processing from WebSocket implementation

## WebSocket Connection Flow

### 1. Initialization

```
┌───────────────┐         ┌───────────────────┐         ┌─────────────────┐
│ PaymentMethod │         │                   │         │                 │
│ Processor     │────────▶│ ptTokenApiCall()  │────────▶│ Pay Theory API  │
│               │         │                   │         │                 │
└───────────────┘         └───────────────────┘         └─────────────────┘
                                                                  │
                                                                  ▼
                           ┌───────────────────┐         ┌─────────────────┐
                           │                   │         │ Google Play     │
                           │ establishViewModel│◀────────│ Integrity Check │
                           │                   │         │                 │
                           └─────────┬─────────┘         └─────────────────┘
                                     │
                                     ▼
                           ┌───────────────────┐
                           │ viewModel.        │
                           │ subscribeToSocket │
                           │ Events()          │
                           └─────────┬─────────┘
                                     │
                                     ▼
                           ┌───────────────────┐
                           │ webSocket         │
                           │ Interactor.       │
                           │ startSocket()     │
                           └───────────────────┘
```

1. The payment processor initiates the connection process
2. A PT-Token is obtained from the Pay Theory API
3. Google Play Integrity check is performed
4. The ViewModel subscribes to socket events
5. The WebSocket connection is established

### 2. Message Exchange

The SDK follows a message-based protocol for payment processing:

```
┌───────────────┐         ┌───────────────────┐         ┌─────────────────┐
│ Payment       │         │ WebSocket         │         │                 │
│ Processor     │────────▶│ Interactor.       │────────▶│ Pay Theory      │
│               │         │ sendMessage()     │         │ Server          │
└───────────────┘         └───────────────────┘         └─────────────────┘
       ▲                                                        │
       │                                                        │
       │                   ┌───────────────────┐                │
       │                   │ PTWebSocket       │                │
       └───────────────────│ Listener.         │◀───────────────┘
                           │ onMessage()       │
                           └───────────────────┘
```

1. The payment processor creates encrypted action requests
2. Messages are sent through the WebSocket connection
3. Responses are received through the WebSocketListener
4. Events are published through the Channel
5. The payment processor handles the messages based on type

### 3. Connection Termination

```
┌───────────────┐         ┌───────────────────┐         ┌─────────────────┐
│ Payment       │         │ WebSocket         │         │                 │
│ Processor     │────────▶│ Interactor.       │────────▶│ Pay Theory      │
│               │         │ stopSocket()      │         │ Server          │
└───────────────┘         └───────────────────┘         └─────────────────┘
```

1. The WebSocket is closed with normal status code
2. Resources are cleaned up (channels closed, references nullified)
3. Disconnect event is published through the channel

## Message Types and Protocol

The WebSocket communication protocol uses several message types:

### Request Messages

1. **Connection Setup**: Initial authentication message with integrity token
2. **Payment Requests**: Encrypted payment details
   - `host:transfer_part1`: Card and ACH payment initiation
   - `host:barcode`: Cash payment barcode generation
   - `host:tokenize`: Payment method tokenization

### Response Messages

1. **General Responses**:
   - `connected to socket`: Connection established
   - `disconnected from socket`: Connection closed
   - `Internal server error`: Server-side error

2. **Specialized Responses**:
   - `host_token`: Host token for session
   - `transfer_confirmation`: Confirmation of payment initiation
   - `transfer_complete`: Payment completion
   - `barcode_complete`: Barcode generation complete
   - `tokenize_complete`: Tokenization complete

### Message Detection

```kotlin
private fun getWebSocketMessageType(message: String): String {
    return when {
        message.indexOf(COMPLETED_TRANSFER) > -1 -> COMPLETED_TRANSFER
        message.indexOf(BARCODE_RESULT) > -1 -> BARCODE_RESULT
        message.indexOf(TRANSFER_PART_ONE_RESULT) > -1 -> TRANSFER_PART_ONE_RESULT
        message.indexOf(HOST_TOKEN_RESULT) > -1 -> HOST_TOKEN_RESULT
        else -> UNKNOWN
    }
}
```

## Security Measures

### 1. Authentication

- PT-Token required for connection setup
- Google Play Integrity token for device attestation
- Session key for message authentication

### 2. Encryption

- NaCl library for public-key encryption
- Public/private key pairs generated for each session
- All payment data encrypted before transmission

### 3. Connection Security

- WSS (WebSocket Secure) protocol
- TLS for transport security
- Custom hostname verification

## Error Handling and Reconnection

### 1. Connection Errors

The SDK implements a resilient connection mechanism:

```kotlin
fun resetSocket() {
    if (resetCounter < 50) {
        resetCounter++
        ptTokenApiCall(payable)
    } else {
        messageReactors?.onError("NETWORK_ERROR: Please check device connection", this)
    }
}
```

This provides:
- Automatic reconnection attempts
- Circuit breaker pattern to prevent infinite retries
- Clear error reporting after maximum attempts

### 2. Message Errors

Specialized error handling for different error types:

```kotlin
private fun onSocketError(ex: Throwable, reason: String) {
    if (ex.message!!.contains("executor rejected")) {
        return
    }
    val error = "$reason ${ex.message}"
    interactor.stopSocket()
    // catch errors "Read error: ssl=0x7340b644c8: I/O error during system call", "Software caused connection abort", "null", "Unable to resolve host"
    if (error.contains("Read error: ssl", ignoreCase = true)
        || error.contains("Software caused connection abort", ignoreCase = true)
        || error.contains("null", ignoreCase = true)
        || error.contains("Unable to resolve host", ignoreCase = true)) {
        println("Network Connection Error - Reconnecting...")
        payTheoryProcessor.resetSocket()
    } else if (error == "executor rejected") {
        println("Socket connection removed.")
    } else { //if error is not ssl error
        payTheoryProcessor.payable.handleError(PTError(ErrorCode.SocketError,error))
    }
    connected = false
    _paymentState.value = PaymentState.Loading
}
```

This provides:
- Pattern matching for common network errors
- Appropriate recovery strategies for different error types
- User-friendly error reporting through the Payable interface

## Conclusion

The Pay Theory Android SDK's WebSocket implementation provides:

1. **Robust Architecture**: Clean separation of concerns with layered design
2. **Secure Communication**: End-to-end encryption for sensitive payment data
3. **Resilient Connection**: Automatic reconnection with circuit breaker pattern
4. **Efficient Protocol**: Real-time bidirectional communication
5. **Comprehensive Error Handling**: Specific strategies for different error scenarios

This implementation ensures reliable payment processing while maintaining security and performance requirements. 