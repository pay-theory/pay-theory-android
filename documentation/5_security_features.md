# Pay Theory Android SDK - Security Features

## Overview

The Pay Theory Android SDK implements multiple layers of security to protect sensitive payment information and ensure compliance with industry standards. This document outlines the key security features of the SDK and how they work together to create a secure payment processing environment.

## Key Security Components

### 1. Google Play Integrity API

The SDK uses Google Play Integrity API for device attestation and app verification:

```kotlin
private fun initiateGooglePlayIntegrityCheck(ptTokenResponse: PTTokenResponse) {
    // Skip for test API key
    if (configuration.apiKey == "test-paytheory-apikey") {
        establishViewModel(ptTokenResponse)
    } else {
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
                payable.handleError(
                    PTError(
                        ErrorCode.AttestationFailed,
                        exception.message!!
                    )
                )
            }
    }
}
```

**Benefits:**
- Verifies that the app hasn't been tampered with
- Confirms the device is a legitimate Android device
- Protects against emulators and rooted devices
- Creates a challenge-response verification

**Implementation Details:**
- Initializes during SDK startup to reduce latency
- Uses SHA-256 hashing for request verification
- Includes attestation token with payment requests
- Requires Google Play Services on the device

### 2. End-to-End Encryption

The SDK uses NaCl (libsodium) cryptography for payment data encryption:

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

**Benefits:**
- Prevents exposure of sensitive data, even during transmission
- Secures data with public-key cryptography
- Ensures data can only be decrypted by Pay Theory servers
- Protects against man-in-the-middle attacks

**Implementation Details:**
- Uses X25519 for key exchange
- Uses XSalsa20 for symmetric encryption
- Uses Poly1305 for message authentication
- Generates unique key pairs for each session

### 3. Secure Communication Channel

All network communication uses secure channels:

1. **HTTPS for API Calls**:
   - TLS encryption for all API requests
   - Certificate pinning to prevent MITM attacks
   - Secure header handling

2. **WSS for WebSocket Communication**:
   - Secure WebSocket protocol (wss://)
   - TLS for transport layer security
   - Authentication tokens in connection URL

### 4. Secure Data Handling

#### SecureString Implementation

The SDK uses a custom `SecureString` implementation to protect sensitive data in memory:

```kotlin
class SecureString(value: String = "") {
    private val stringValue: CharArray = value.toCharArray()
    
    fun stringLength(): Int {
        return stringValue.size
    }

    override fun toString(): String {
        return String(stringValue)
    }

    // ... other methods
}

class SecureStringWrapper(val secureValue: SecureString, val cardBrand: String? = null)
```

**Benefits:**
- Prevents accidental logging of sensitive data
- Avoids string pooling and memory dumps
- Limits exposure of card data in memory
- Provides clear access control to sensitive values

#### Data Clearing

Sensitive data is promptly cleared after use:

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
    
    bankAccountType.value = ""
    
    // Clear field states
    for (field in PaymentField.entries) {
        paymentFieldState[field] = FieldState.INIT
        paymentFieldEmpty[field] = false
        paymentFieldValid[field] = true
        payTheoryProcessor.payable.handleStateChange(Pair(field, paymentFieldState[field]!!))
    }

    clearCount.intValue++
    isValidAndReady = false
}
```

**Benefits:**
- Minimizes duration sensitive data remains in memory
- Reduces risk of memory-based attacks
- Ensures PCI DSS compliance
- Creates clean state after transaction

### 5. Tokenization Support

The SDK supports payment method tokenization, which enhances security by:

- Replacing sensitive payment details with non-sensitive tokens
- Allowing repeat payments without storing card/bank details
- Isolating sensitive data to secure Pay Theory systems
- Reducing PCI compliance scope for integrating applications

### 6. Field-level Validation

The SDK performs extensive validation on payment fields:

```kotlin
private fun isValidCardNumber(): Boolean {
    return propagateState(
        payTheoryProcessor, 
        PaymentField.CARD_NUMBER, 
        validator.isValidCardNumber(cardNumber.value), 
        cardNumber.value.secureValue.stringLength() == 0
    )
}

private fun isValidBankRoutingNumber(): Boolean {
    return propagateState(
        payTheoryProcessor, 
        PaymentField.BANK_ROUTING_NUMBER, 
        validator.isValidBankRoutingNumber(bankRoutingNumber.value), 
        bankRoutingNumber.value.secureValue.stringLength() == 0
    )
}
```

**Benefits:**
- Prevents submission of invalid data
- Reduces fraud attempts
- Provides immediate feedback to users
- Implements industry-standard validation algorithms

### 7. Error Handling and Logging

The SDK implements secure error handling:

```kotlin
fun handleError(error: PTError) {
    // Log error code without sensitive details
    Log.e("PayTheory", "Error: ${error.code}")
    
    // Notify application through Payable interface
    payable.handleError(error)
    
    // Update UI state
    viewModel.updateState(PaymentResultState.Error(error))
}
```

**Benefits:**
- Avoids logging sensitive information
- Provides structured error reporting
- Uses specific error codes instead of detailed messages
- Sanitizes error output

## Security Architecture

The SDK follows a defense-in-depth approach with multiple security layers:

```
┌─────────────────────────────────────────────┐
│                                             │
│  User Interface Layer                       │
│  - Masked input fields                      │
│  - Validation feedback                      │
│  - Secure field handling                    │
│                                             │
├─────────────────────────────────────────────┤
│                                             │
│  Business Logic Layer                       │
│  - Field validation                         │
│  - SecureString usage                       │
│  - State management                         │
│                                             │
├─────────────────────────────────────────────┤
│                                             │
│  Encryption Layer                           │
│  - NaCl encryption                          │
│  - Public/private key cryptography          │
│  - Session key management                   │
│                                             │
├─────────────────────────────────────────────┤
│                                             │
│  Transport Layer                            │
│  - TLS/WSS protocol                         │
│  - Secure WebSocket                         │
│  - Session tokens                           │
│                                             │
├─────────────────────────────────────────────┤
│                                             │
│  Authentication Layer                       │
│  - API key validation                       │
│  - Google Play Integrity                    │
│  - Device attestation                       │
│                                             │
└─────────────────────────────────────────────┘
```

## PCI DSS Compliance

The Pay Theory Android SDK is designed with PCI DSS compliance in mind:

1. **Requirement 2**: Does not use vendor-supplied defaults
   - Generates unique keys for each session
   - No hardcoded credentials

2. **Requirement 3**: Protects stored cardholder data
   - Does not persistently store card data
   - Uses secure memory handling

3. **Requirement 4**: Encrypts transmission of cardholder data
   - End-to-end encryption
   - Secure communication channels

4. **Requirement 6**: Develops and maintains secure systems
   - Regular security updates
   - Secure coding practices

5. **Requirement 8**: Identifies and authenticates access
   - API key authentication
   - Device attestation

6. **Requirement 10**: Tracks and monitors access
   - Structured error logging
   - Activity tracking without sensitive data

7. **Requirement 11**: Regularly tests security systems
   - Regular security audits
   - Vulnerability testing

## Environment Security

### Production Environment

- Enforces Google Play Integrity checks
- Requires Google Play Services
- Rejects emulator environments
- Validates device integrity

### Development/Testing Environment

- Maintains similar security model with test credentials
- Allows emulator usage with development API keys
- Provides clear error messages for security requirements
- Maintains segmented test environment

## Security Best Practices for Integrators

1. **API Key Protection**:
   ```kotlin
   // In build.gradle
   buildConfigField "String", "PAY_THEORY_API_KEY", "\"${localProperties['pay.theory.api.key']}\""
   
   // In application code
   val ptConfig = PayTheoryConfiguration(
       apiKey = BuildConfig.PAY_THEORY_API_KEY,
       // other configuration...
   )
   ```

2. **ProGuard Configuration**:
   ```proguard
   # Keep source file names and line numbers for better debugging
   -keepattributes SourceFile,LineNumberTable
   
   # Repackage classes into the top-level
   -repackageclasses
   
   # Keep Compose-related classes
   -keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt { *; }
   ```

3. **Handling Unsupported Environments**:
   ```kotlin
   try {
       // Initialize payment form
   } catch (e: Exception) {
       if (e is PTError && e.code == ErrorCode.AttestationFailed) {
           // Show user-friendly message about unsupported device
       }
   }
   ```

## Conclusion

The Pay Theory Android SDK implements a comprehensive security model that protects sensitive payment data through:

1. **Multiple Security Layers**: Defense-in-depth approach
2. **End-to-End Encryption**: Protects data during transmission
3. **Device Attestation**: Verifies legitimate devices and apps
4. **Secure Memory Handling**: Protects data in memory
5. **PCI Compliance**: Follows payment card industry standards

These security features work together to create a secure environment for payment processing while maintaining a seamless user experience. 