# Pay Theory Compose Components Testing Strategy

## Overview

This document outlines our testing strategy for the Pay Theory Compose UI components. Our approach focuses on effective unit testing of business logic while acknowledging the unique challenges of testing Jetpack Compose UI components.

## Testing Challenges with Compose

Testing Compose UI components directly presents several challenges:

1. **Composable Functions** - `@Composable` functions require a Compose runtime environment
2. **UI Testing** - Visual verification requires instrumented tests
3. **State Management** - UI state can be complex and difficult to test
4. **Dependency on Android Context** - Many components need an Android context

## Our Testing Approach

We've adopted a layered approach to testing that focuses on the following strategies:

### 1. Logic Extraction

We extract pure business logic from Composable functions into standalone utility classes:

```kotlin
// Instead of embedding logic in Composables:
@Composable 
fun Component(...) {
    // Logic embedded in the Composable
}

// We extract logic to utility classes:
object ComponentUtils {
    // Pure logic functions that are easily testable
}

@Composable 
fun Component(...) {
    // Uses the utility class for logic
    ComponentUtils.someFunction(...)
}
```

### 2. Testing Pyramid

We follow a testing pyramid with three types of tests:

1. **Unit Tests (Primary Focus)** - Test utility classes and pure logic
2. **Integration Tests** - Test how components work together
3. **UI Tests** - Use ComposeTestRule for UI verification when required

### 3. Testable Components

Our utility classes follow these principles:

- **Pure Functions** - No side effects or dependencies on Android framework
- **Clear Inputs/Outputs** - Well-defined parameters and return values
- **Single Responsibility** - Each function does one thing well
- **Separation of Concerns** - Logic separate from UI

## Implementation Pattern

### Step 1: Create Utility Classes

```kotlin
// Example: GooglePayButtonUtils.kt
object GooglePayButtonUtils {
    fun mapButtonType(buttonType: GooglePayButtonType): ButtonType {
        // Logic for mapping button types
    }
}
```

### Step 2: Write Comprehensive Unit Tests

```kotlin
// Example: GooglePayButtonUtilsTest.kt
class GooglePayButtonUtilsTest {
    @Test
    fun `mapButtonType maps PAY correctly`() {
        assertEquals(ButtonType.Pay, GooglePayButtonUtils.mapButtonType(GooglePayButtonType.PAY))
    }
}
```

### Step 3: Refactor Components to Use Utilities

```kotlin
// Example: Refactored GooglePayButton.kt
@Composable
fun GooglePayButton(...) {
    PayButton(
        // Use utility class instead of embedded logic
        type = GooglePayButtonUtils.mapButtonType(buttonType),
        // ...
    )
}
```

## Test Coverage Goals

- **Unit Tests**: 80% code coverage for all utility functions and business logic
- **Focus Areas**: Input validation, data formatting, state management, mapping logic
- **Edge Cases**: Handle all edge cases and error scenarios in unit tests

## Currently Tested Components

- **GooglePayButtonUtils** - Button type and theme mapping
- **TextFieldUtils** - Field validation, card formatting, and error state management
- **TransformationUtils** - Credit card formatting, text masking, and offset mapping
- **ExpirationFieldUtils** - Expiration date formatting and validation
- **CardFieldUtils** - Credit card number formatting, validation and card network detection
- **CvcFieldUtils** - CVC validation with card network-specific rules
- **BankFieldUtils** - Bank account and routing number formatting and validation

## Next Steps

1. Continue refactoring remaining components with utility extraction
2. Add specialized tests for secure input components combining multiple utilities
3. Consider integration tests for complex component interactions
4. Improve test coverage reporting 