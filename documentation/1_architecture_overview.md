# Pay Theory Android SDK - Architecture Overview

## Introduction

The Pay Theory Android SDK is a Jetpack Compose-based payment processing solution that provides a secure, flexible way to integrate payment processing capabilities into Android applications. The SDK supports credit/debit card payments, ACH (bank transfer) payments, Google Pay, and payment method tokenization.

## Core Components

The SDK architecture follows a clean separation of concerns with clear interfaces between components:

### Configuration

- **PayTheoryConfiguration**: Central configuration class that manages all payment settings including:
  - API key and environment (lab, study, production)
  - Payment amount
  - Payment method type (CARD, ACH, GOOGLE_PAY)
  - Payment action (PAYMENT, TOKEN)
  - Fee mode (MERCHANT_FEE, BUYER_FEE)
  - Optional settings (billing address, receipt options, Google Pay options, etc.)

### Payment Processing

- **PaymentMethodProcessor**: Abstract base class that handles communication with Pay Theory's platform
  - Manages websocket connections
  - Handles Google Play Integrity checks for security
  - Provides base functionality for payment processing
  
- **Payment**: Implementation of PaymentMethodProcessor for processing payments
  - Handles transaction flow for card and ACH payments
  - Manages message encryption and secure communication

- **PaymentMethodToken**: Implementation for tokenizing payment methods
  - Stores payment details securely for future use
  - Returns a token instead of processing a payment

- **GooglePayProcessor**: Implementation for Google Pay payments
  - Handles Google Pay API interactions
  - Processes Google Pay tokens
  - Integrates with WebSocket stack for payment processing

### State Management

- **PaymentViewModel**: ViewModel responsible for managing payment state
  - Maintains form field values and validation state
  - Manages websocket connection state
  - Processes payment field updates
  - Coordinates payment submission

- **PaymentFieldViewModel**: Manages individual payment field states
  - Provides real-time validation feedback
  - Emits state changes for UI reactivity

### WebSocket Communication

The SDK uses a multi-layered approach to websocket communication:

- **WebsocketInteractor**: Top-level interface for websocket operations
- **WebsocketRepository**: Middle layer that delegates to service provider
- **WebServicesProvider**: Core implementation with OkHttpClient
- **PTWebSocketListener**: Handles websocket lifecycle events
- **WebsocketMessageHandler**: Interface for processing messages

### Google Pay Integration

- **GooglePayClient**: Handles interactions with Google's PaymentsClient API
  - Creates payment requests based on configuration
  - Checks device readiness for Google Pay
  - Manages the Google Pay payment flow

- **GooglePayUtil**: Utility class for Google Pay operations
  - Provides helper methods for payment processing
  - Handles token extraction and formatting

- **GooglePayFormUtils**: Helper utilities for Compose UI integration
  - Manages Google Pay availability checking
  - Provides utilities for UI state management

### Security

- **Google Play Integrity API**: Verifies the app's authenticity
- **NaCl Encryption**: Provides end-to-end encryption for payment data
- **Secure Storage**: Handles sensitive payment information

### Interface

- **Payable**: Core interface implemented by hosting applications
  - Defines callbacks for payment events
  - Provides hooks for UI state updates
  - Handles success, failure, and error states

## Data Flow

1. **Configuration**: App initializes SDK with PayTheoryConfiguration
2. **Form Input**: User enters payment information via UI components or selects Google Pay
3. **Validation**: Fields are validated in real-time with feedback
4. **Submission**: 
   - SDK obtains Play Integrity token for security attestation
   - SDK establishes secure websocket connection
   - Payment data is encrypted and sent to Pay Theory servers
5. **Processing**: Transaction is processed by Pay Theory
6. **Callback**: Results are returned to the application via Payable interface

## Google Pay Flow

For Google Pay payments, the flow differs slightly:

1. **Availability Check**: SDK checks if Google Pay is available on the device
2. **Button Display**: Google Pay button is shown if available
3. **User Interaction**: User taps Google Pay button
4. **Payment Sheet**: Google Pay payment sheet appears
5. **User Confirmation**: User selects payment method and confirms
6. **Token Processing**: Pay Theory processes the Google Pay token
7. **Callback**: Results are returned to the application via Payable interface

## Architecture Diagram

```
┌─────────────┐     ┌───────────────────┐     ┌──────────────────┐
│  Host App   │────▶│ Payable Interface │◀───▶│ Payment Components│
└─────────────┘     └───────────────────┘     └──────────────────┘
                            │                          │
                            ▼                          ▼
                    ┌───────────────┐         ┌─────────────────┐
                    │PaymentViewModel│◀───────▶│Field Validation │
                    └───────────────┘         └─────────────────┘
                            │
                            ▼
┌───────────────┐   ┌───────────────┐         ┌─────────────────┐
│  Google Pay   │◀──▶│   Payment     │◀───────▶│ Google Play     │
│  API          │   │   Processor   │         │ Integrity API   │
└───────────────┘   └───────────────┘         └─────────────────┘
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

## Environment Support

- **Production**: Works on devices with Google Play Services
- **Development**: Works on emulators and devices with Play Services
- **Unsupported**: Devices without Google Play Services

## Integration Points

The SDK provides several integration points for host applications:

1. **Payable Interface**: Main callback interface for payment events
2. **PayTheoryConfiguration**: Configuration builder for payment settings
3. **UI Components**: Compose-based UI elements for payment forms
   - Standard card and ACH forms
   - Google Pay button components
4. **State Observation**: Flow-based state monitoring for custom UI integration 