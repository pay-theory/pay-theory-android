# Pay Theory Android Test Utilities

This package contains utilities and helpers for testing the Pay Theory Android SDK. These tools are designed to simplify writing unit and integration tests for the SDK.

## Package Structure

```
com.paytheory.android.test/
├── factories/      # Test data factories
├── fixtures/       # Test fixtures and mock responses
├── helpers/        # Test helper functions
└── mocks/          # Mock implementations
```

## Factories

Test data factories generate test objects with sensible defaults for tests:

- `BaseFactory<T>`: Generic interface for all test data factories
- `PaymentDetailFactory`: Creates test payment details with cards, ACH, and Google Pay data
- `PayTheoryConfigurationFactory`: Creates SDK configuration objects for testing

### Example Usage:

```kotlin
// Create a factory
val factory = PaymentDetailFactory()

// Create a payment detail with default values
val paymentDetail = factory.create()

// Create with a specific amount
val customPaymentDetail = factory.createWithAmount(BigDecimal("15.99"))

// Create with ACH details
val achPaymentDetail = factory.createWithAch()

// Create with Google Pay details
val googlePayDetail = factory.createWithGooglePay()
```

## Mocks

Mock implementations of key interfaces for testing:

- `TestPayable`: Implementation of the `Payable` interface that records callbacks
- `FakeWebSocketRepository`: Simulates the WebSocket connection
- `FakeIntegrityTokenProvider`: Provides a fake integrity token for testing

### Example Usage:

```kotlin
// Create mocks
val payable = TestPayable()
val webSocketRepository = FakeWebSocketRepository()

// Configure mocks
webSocketRepository.shouldFailConnection = true
webSocketRepository.responseDelay = 100L

// Check interaction results
assertEquals(1, payable.successCallCount)
assertEquals(0, payable.errorCallCount)
assertNotNull(payable.lastResult)
```

## Helpers

Helper functions for common testing tasks:

- `FlowTestUtils`: Utilities for testing Kotlin Flows
- `LiveDataTestUtils`: Utilities for testing LiveData
- `ComposeTestUtils`: Utilities for testing Jetpack Compose UI components

### Flow Testing Example:

```kotlin
// Collect values from a flow with timeout
val values = FlowTestUtils.collectValues(flow, 5, 1000L)

// Collect a single value
val value = FlowTestUtils.collectSingleValue(flow, 1000L)

// Use extension function
val results = flow.test(timeout = 1000L).awaitValues(5)
```

### LiveData Testing Example:

```kotlin
// Get value with timeout
val value = LiveDataTestUtils.getValueOrAwait(liveData, 1000L)

// Observe for multiple values
val values = LiveDataTestUtils.observeForValues(liveData, 3, 1000L)

// Use test observer
val testObserver = liveData.test()
// ... trigger LiveData changes ...
assertEquals(3, testObserver.values.size)
```

### Compose Testing Example:

```kotlin
// Test a composable
ComposeTestUtils.testComposable { testScope ->
    // Set content
    testScope.setContent {
        MyComposable()
    }
    
    // Assert on the composable
    testScope.waitForIdle()
    // ... assertions ...
}

// Capture state during testing
val stateCaptor = ComposeTestUtils.captureState(initialValue = "")
MyComposable(onValueChange = { stateCaptor.value = it })
assertEquals("new value", stateCaptor.value)
```

## Fixtures

Test fixtures provide mock data for testing:

- `TestResponses`: Contains JSON response fixtures for various scenarios

### Example Usage:

```kotlin
// Get a successful payment response
val successResponse = TestResponses.successfulPaymentResponse()

// Get a failed payment response with custom error
val failedResponse = TestResponses.failedPaymentResponse("Invalid card")

// Get a connection message
val connectionMessage = TestResponses.connectionMessage()
``` 