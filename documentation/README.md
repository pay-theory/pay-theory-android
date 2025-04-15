# Pay Theory Android SDK Documentation

## Introduction

This directory contains comprehensive documentation on the Pay Theory Android SDK. The SDK provides a secure, flexible way to integrate payment processing into Android applications using Jetpack Compose.

## Documentation Files

1. [**Architecture Overview**](1_architecture_overview.md) - Core components and high-level architecture
2. [**UI Components**](2_ui_components.md) - Detailed documentation of all UI elements and composables
3. [**Payment Processing**](3_payment_processing.md) - In-depth explanation of the payment processing flow
4. [**WebSocket Implementation**](4_websocket_implementation.md) - Details of the WebSocket communication layer
5. [**Security Features**](5_security_features.md) - Security mechanisms and PCI compliance measures

## SDK Features

- Credit/Debit card payments
- ACH (bank transfer) payments
- Payment method tokenization
- Customizable UI components
- Real-time field validation
- Secure payment processing
- Compose-based implementation

## Key Components Overview

### Configuration

`PayTheoryConfiguration` - Central configuration class for all payment settings.

### User Interface

Jetpack Compose UI components with Material 3 design:
- Card payment form
- ACH payment form 
- Tokenization form
- Validation feedback
- Payment state indicators

### Payment Processing

Secure, PCI-compliant payment processing flow:
- Real-time validation
- End-to-end encryption
- Device attestation
- Secure memory handling
- WebSocket communication

### Integration

Clean, simple integration pattern:
- Payable interface
- ViewModel state management
- Kotlin Flow for reactivity
- Comprehensive error handling

## Implementation Example

Basic implementation requires:

1. Implementing the `Payable` interface
2. Creating a `PayTheoryConfiguration` object
3. Adding the `PaymentForm` composable to your UI

```kotlin
class MainActivity : ComponentActivity(), Payable {
    private val paymentProcessModel: PaymentProcessViewModel by viewModels()
    
    // Implement Payable interface methods...
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PayTheoryTheme {
                PaymentForm(
                    payable = this,
                    configuration = PayTheoryConfiguration(
                        apiKey = BuildConfig.PAY_THEORY_API_KEY,
                        amount = 1000, // $10.00
                        paymentMethodType = PaymentMethodType.CARD
                    )
                )
            }
        }
    }
}
```

## Architecture Diagram

```
┌─────────────────┐     ┌───────────────────┐     ┌──────────────────┐
│  Host App       │────▶│ Payable Interface │◀───▶│ Payment Form     │
└─────────────────┘     └───────────────────┘     └──────────────────┘
                                │                          │
                                ▼                          ▼
                        ┌───────────────┐         ┌─────────────────┐
                        │PaymentViewModel│◀───────▶│Field Validation │
                        └───────────────┘         └─────────────────┘
                                │
                                ▼
                        ┌───────────────┐
                        │   Payment     │
                        │   Processor   │
                        └───────────────┘
                                │
                                ▼
                        ┌───────────────┐
                        │   WebSocket   │
                        │   Stack       │
                        └───────────────┘
                                │
                                ▼
                        ┌───────────────┐
                        │  Pay Theory   │
                        │  Backend      │
                        └───────────────┘
``` 