# Pay Theory Android SDK Documentation

This directory contains comprehensive documentation for the Pay Theory Android SDK. The documentation is structured as follows:

## Core Documentation

1. [**Architecture Overview**](1_architecture_overview.md) - High-level architectural overview of the SDK, including core components and data flow

2. [**UI Components**](2_ui_components.md) - Details of the Jetpack Compose UI components, including forms, fields, and buttons

3. [**Payment Processing**](3_payment_processing.md) - Payment flow, processing mechanisms, and security measures

4. [**WebSocket Implementation**](4_websocket_implementation.md) - Communication channel details and message handling

5. [**Security Features**](5_security_features.md) - Security aspects of the SDK, including encryption and integrity checks

6. [**Google Pay Integration**](6_google_pay_integration.md) - Implementation guide and features for Google Pay integration

## Supporting Documents

- [**Story Points**](Story-Points.md) - Information about story point estimation for development tasks

- [**Wallet Transactions**](wallet_transaction.md) - Details on wallet-based payment processing

- [**Google Pay Release Checklist**](google_pay_release_checklist.md) - Release preparation checklist for Google Pay integration

## Getting Started

For new users, we recommend starting with the [Architecture Overview](1_architecture_overview.md) to understand the SDK's general structure, followed by the [UI Components](2_ui_components.md) and [Payment Processing](3_payment_processing.md) documentation to learn about implementation details.

For Google Pay integration specifically, refer to the [Google Pay Integration](6_google_pay_integration.md) guide.

## Maintenance

These documentation files are updated regularly as features are added or modified. If you find any inconsistencies or have suggestions for improvements, please submit an issue or pull request to the repository.

---

Copyright © Pay Theory, Inc. All rights reserved.

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