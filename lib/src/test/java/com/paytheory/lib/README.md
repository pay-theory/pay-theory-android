# Pay Theory Android SDK Test Plan

## Overview
This directory contains tests for the core `com.paytheory.lib` package. Current coverage is at 24%, and the goal is to significantly improve this coverage using a structured approach.

## Classes to Cover
1. **PayTheoryConfiguration** - Configuration class for Pay Theory SDK
2. **ContextProvider & Payable** - Interface implementations
3. **PaymentMethodProcessor** - Base class for payment processing
4. **Payment** - Payment implementation
5. **PaymentMethodToken** - Tokenization implementation

## Testing Strategy

### 1. PayTheoryConfiguration Testing (Priority: High)
- Test builder pattern thoroughly
- Test parameter validation (invalid API keys, amounts, etc.)
- Test environment detection and setup
- Verify Google Pay configuration validation
- Test default values and overrides

### 2. ContextProvider and Payable Interface Tests (Priority: Medium)
- Create mock implementations
- Test default method implementations
- Test interface contract enforcement

### 3. PaymentMethodProcessor Tests (Priority: High)
- Mock dependencies (websocket, API services)
- Test credential management
- Test socket connection/disconnection
- Test message handling
- Test integrity check processing
- Test error handling scenarios

### 4. Payment and PaymentMethodToken Classes (Priority: High)
- Test message routing
- Test payment processing flow
- Test tokenization flow
- Test request queue handling
- Test socket event handling

## Implementation Approach
1. **Separation of Concerns**:
   - Create smaller, focused test classes for each component
   - Use mocking extensively (similar to GooglePayProcessorTest)

2. **Environment Setup**:
   - Use Robolectric for Android dependencies
   - Set up proper test mode configurations

3. **Advanced Techniques**:
   - Use reflection for accessing private fields when necessary
   - Create test providers for mocking network responses
   - Create fake socket implementations

## Implementation Order
1. PayTheoryConfiguration (easiest to start with)
2. ContextProvider/Payable implementations
3. PaymentMethodProcessor
4. Payment and PaymentMethodToken classes

## Related Tests
The approach is inspired by our successful patterns in `GooglePayProcessorTest.kt`. 