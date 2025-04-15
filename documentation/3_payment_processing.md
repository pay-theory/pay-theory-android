# Pay Theory Android SDK - Payment Processing

## Overview

The Pay Theory Android SDK implements a secure, robust payment processing flow that handles various payment methods (card, ACH) while ensuring data security and compliance with payment industry standards. This document outlines the payment processing architecture and flow.

## Payment Processing Components

### Key Classes

1. **PaymentMethodProcessor**: Abstract base class that defines the core payment processing functionality
   - Manages the connection to Pay Theory's backend
   - Handles authentication and integrity verification
   - Provides abstract methods for specific payment implementations

2. **Payment**: Concrete implementation for processing payments
   - Handles card and ACH payment flows
   - Manages the transaction lifecycle
   - Processes payment responses

3. **PaymentMethodToken**: Concrete implementation for tokenizing payment methods
   - Stores payment credentials securely for future use
   - Does not process an actual payment

4. **PaymentViewModel**: Manages the payment state and user interface
   - Coordinates form validation
   - Manages the websocket connection
   - Handles payment submission and response

## Payment Processing Flow

### 1. Initialization

```
┌─────────────────┐     ┌───────────────────┐     ┌──────────────────┐
│    Host App     │────▶│ PayTheory         │────▶│PaymentMethod     │
│  (Activity)     │     │ Configuration     │     │Processor Init    │
└─────────────────┘     └───────────────────┘     └──────────────────┘
                                                           │
                                                           ▼
                                                 ┌──────────────────┐
                                                 │Google Play       │
                                                 │Integrity Warm-up │
                                                 └──────────────────┘
```

Key steps:
- Host app creates a `PayTheoryConfiguration` with API key and options
- Payment processor initializes with the configuration
- Google Play Integrity API is "warmed up" to prepare for verification

### 2. Form Input and Validation

```
┌─────────────────┐     ┌───────────────────┐     ┌──────────────────┐
│   User Input    │────▶│ PaymentViewModel  │────▶│  Field-by-field  │
│   (UI Fields)   │     │ Field Updates     │     │   Validation     │
└─────────────────┘     └───────────────────┘     └──────────────────┘
                                                           │
                                                           ▼
                                                 ┌──────────────────┐
                                                 │Validation State  │
                                                 │Updates via Payable│
                                                 └──────────────────┘
```

Key steps:
- User enters payment information in UI fields
- Each field update triggers validation
- Field state changes are reported through the Payable interface
- Form submission is enabled when all fields are valid

### 3. Payment Method Construction

When the user submits the form, the SDK constructs the appropriate payment detail object:

```kotlin
// For card payments
private fun constructCardPayment(): PaymentDetail {
    return PaymentDetail(
        number = cardNumber.value.secureValue.toString(),
        expiration_month = expMonth,
        expiration_year = expYear,
        security_code = cvc.value.secureValue.toString(),
        currency = "USD",
        amount = configuration.amount,
        type = "card",
        fee_mode = configuration.feeMode,
        address = constructAddress(),
        name = nameOnAccount.value.secureValue.toString(),
        payorInfo = configuration.payorInfo,
        // Additional configuration data
    )
}

// For ACH payments
private fun constructBankPayment(): PaymentDetail {
    return PaymentDetail(
        account_number = bankAccountNumber.value.secureValue.toString(),
        bank_code = bankRoutingNumber.value.secureValue.toString(),
        account_type = bankAccountType.value,
        currency = "USD",
        amount = configuration.amount,
        type = "ach",
        fee_mode = configuration.feeMode,
        address = constructAddress(),
        name = nameOnAccount.value.secureValue.toString(),
        payorInfo = configuration.payorInfo,
        // Additional configuration data
    )
}
```

### 4. Security and Authentication

Before submitting payment, the SDK performs security checks:

```
┌─────────────────┐     ┌───────────────────┐     ┌──────────────────┐
│ Payment Submit  │────▶│  PT Token API     │────▶│  Google Play     │
│                 │     │  Request          │     │  Integrity Check │
└─────────────────┘     └───────────────────┘     └──────────────────┘
                                                           │
                                                           ▼
                                                 ┌──────────────────┐
                                                 │ Secure WebSocket │
                                                 │ Connection       │
                                                 └──────────────────┘
```

Key steps:
1. SDK obtains a PT-Token from Pay Theory API
2. Google Play Integrity check verifies app authenticity
3. Secure WebSocket connection is established
4. Session and encryption keys are generated

### 5. Payment Data Encryption

The SDK uses NaCl encryption to secure payment data:

```kotlin
// Generate key pair for encryption
val keyPair = generateLocalKeyPair()
publicKey = Base64.getEncoder().encodeToString(keyPair.publicKey.asBytes)

// Encrypt payment request
val encryptedBody = encryptBox(
    Gson().toJson(paymentRequest),
    Key.fromBase64String(messageReactors!!.socketPublicKey)
)

// Create action request with encrypted body
return ActionRequest(
    requestAction,
    encryptedBody,
    publicKey,
    sessionKey
)
```

### 6. WebSocket Communication

Payment processing occurs over a secure WebSocket connection:

```
┌───────────────┐     ┌───────────────────┐     ┌──────────────────┐
│ Encrypted     │────▶│  WebSocket        │────▶│ Pay Theory       │
│ Payment Data  │     │  Transmission     │     │ Backend          │
└───────────────┘     └───────────────────┘     └──────────────────┘
                                                          │
                                                          ▼
                                                ┌──────────────────┐
                                                │ Response         │
                                                │ Processing       │
                                                └──────────────────┘
```

The WebSocket communication architecture follows a layered approach:
1. **WebsocketInteractor**: High-level interface used by `Payment` class
2. **WebsocketRepository**: Repository pattern implementation
3. **WebServicesProvider**: Core WebSocket implementation using OkHttp
4. **PTWebSocketListener**: Handles WebSocket lifecycle events

### 7. Message Handling and Responses

The SDK handles various message types from the WebSocket:

```kotlin
// Message type detection
private fun getWebSocketMessageType(message: String): String {
    return when {
        message.indexOf(COMPLETED_TRANSFER) > -1 -> COMPLETED_TRANSFER
        message.indexOf(BARCODE_RESULT) > -1 -> BARCODE_RESULT
        message.indexOf(TRANSFER_PART_ONE_RESULT) > -1 -> TRANSFER_PART_ONE_RESULT
        message.indexOf(HOST_TOKEN_RESULT) > -1 -> HOST_TOKEN_RESULT
        else -> UNKNOWN
    }
}

// Message handling
override fun receiveMessage(message: String) {
    when (message) {
        CONNECTED -> connectionReactors!!.onConnected()
        DISCONNECTED -> connectionReactors!!.onDisconnected()
        INTERNAL_SERVER_ERROR -> messageReactors!!.onError(message, this)
        else -> {
            when (getWebSocketMessageType(message)) {
                HOST_TOKEN_RESULT -> messageReactors!!.onHostToken(message, this)
                BARCODE_RESULT -> messageReactors!!.onBarcode(message, viewModel, this)
                COMPLETED_TRANSFER -> messageReactors!!.completeTransaction(
                    message, viewModel, this
                )
                else -> messageReactors!!.onError(message, this)
            }
        }
    }
}
```

### 8. Transaction Completion

When a transaction is completed:

```
┌───────────────┐     ┌───────────────────┐     ┌──────────────────┐
│ Transaction   │────▶│  Transaction      │────▶│ Callback to      │
│ Complete      │     │  Result Creation  │     │ Payable Interface│
└───────────────┘     └───────────────────┘     └──────────────────┘
                                                          │
                                                          ▼
                                                ┌──────────────────┐
                                                │ UI State Update  │
                                                │                  │
                                                └──────────────────┘
```

- Success or failure result objects are created
- Appropriate callbacks are made to the Payable interface
- The ViewModel updates its state
- Sensitive data is cleared from memory

## Security Features

### Google Play Integrity API

The SDK uses Google Play Integrity API to verify the authenticity of the app and device:

```kotlin
private fun initiateGooglePlayIntegrityCheck(ptTokenResponse: PTTokenResponse) {
    // Generate request hash using server challenge
    val digest = MessageDigest.getInstance("SHA-256")
    val requestHash = digest.digest(
        ptTokenResponse.challengeOptions.challenge.toByteArray(Charsets.UTF_8)
    )

    // Request integrity token
    val integrityTokenResponse: Task<StandardIntegrityToken> =
        integrityTokenProvider!!.request(
            StandardIntegrityTokenRequest.builder()
                .setRequestHash(Base64.getEncoder().encodeToString(requestHash))
                .build()
        )
    
    // Process response
    integrityTokenResponse
        .addOnSuccessListener { response ->
            establishViewModel(ptTokenResponse, response.token())
        }
        .addOnFailureListener { exception ->
            // Handle failure
        }
}
```

### Secure Data Handling

1. **SecureString**: Custom class that protects sensitive data in memory
   - Prevents logging and string pooling
   - Ensures data is properly cleared from memory

2. **Data Clearing**: After transaction completion, sensitive data is cleared:
   ```kotlin
   internal fun clearSensitiveData() {
       listOf(
           nameOnAccount, addressLine1, addressLine2,
           city, region, cardNumber, cvc,
           bankAccountNumber, bankRoutingNumber,
           postalCode, expiration
       ).forEach {
           it.value = SecureStringWrapper(SecureString(""),null)
       }
       // Clear additional fields and states
   }
   ```

3. **End-to-End Encryption**: All sensitive data is encrypted before transmission
   - Uses NaCl cryptographic library
   - Implements public-key encryption

## Payment Types

### Card Payments

Card payment processing involves:
1. Card data collection and validation
2. Card type detection
3. Creation of encrypted card payment request
4. Processing through `transfer_part1` action
5. Handling of transaction completion

### ACH Payments

ACH payment processing involves:
1. Bank account data collection and validation
2. Account type selection (checking/savings)
3. Creation of encrypted ACH payment request
4. Processing through `transfer_part1` action
5. Handling of transaction completion

### Payment Tokenization

Instead of processing a payment, tokenization:
1. Collects and validates payment credentials
2. Creates an encrypted tokenization request
3. Stores the credentials securely on Pay Theory servers
4. Returns a payment method token for future use

## Error Handling

The SDK implements comprehensive error handling:

1. **Network Errors**: Automatic retry mechanism with circuit breaker
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

2. **Validation Errors**: Real-time feedback for input validation
   - Field-by-field validation with specific error messages
   - Preventing submission of invalid data

3. **Processing Errors**: Structured error reporting
   - Specific error codes via PTError class
   - Detailed error messages through Payable interface

## Conclusion

The Pay Theory Android SDK implements a robust, secure payment processing flow that:
- Ensures data security through encryption and secure handling
- Validates device integrity through Google Play Integrity API
- Provides real-time validation and feedback
- Offers multiple payment options (card, ACH)
- Maintains PCI compliance
- Delivers a seamless user experience with detailed error handling 