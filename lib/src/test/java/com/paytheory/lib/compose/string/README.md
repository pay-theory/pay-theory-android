# SecureString Component Tests

This directory contains comprehensive tests for the `SecureString` component and related classes, which are part of the Pay Theory secure text handling system.

## Test Files Overview

### Core SecureString Tests

1. **SecureStringTest.kt**
   - Tests basic functionality of SecureString class
   - Verifies construction, manipulation, and data access
   - Validates comparison and hash code behavior

2. **SecureStringLengthTest.kt**
   - Specialized tests for string length handling
   - Tests with various string types (empty, ASCII, Unicode, etc.)
   - Tests with different character encodings

3. **SecureStringEncodingTest.kt**
   - Focuses on secure encoding/decoding methods in SecureString
   - Tests byte structure patterns
   - Tests handling of various character types (ASCII, Unicode, emojis)
   - Tests round-trip encoding/decoding

4. **SecureStringUtilsTest.kt**
   - Tests utility functions and special cases
   - Tests memory clearing functionality
   - Tests thread safety with concurrent access
   - Tests comparison operations and hash code consistency

### Wrapper and Integration Tests

5. **SecureStringWrapperTest.kt**
   - Tests SecureStringWrapper functionality
   - Verifies correct state representation
   - Tests value updating and visibility

6. **SecureStringWrapperEdgeCasesTest.kt**
   - Tests edge cases for SecureStringWrapper
   - Tests with empty strings
   - Tests selection bounds handling
   - Tests with Unicode characters
   - Tests state transitions and original object integrity

## Testing Patterns

This test suite demonstrates several important testing patterns:

1. **Comprehensive Coverage**: Each component is tested for both standard functionality and edge cases.

2. **Behavioral Testing**: Tests focus on observable behavior rather than implementation details.

3. **Isolation**: Each test file focuses on a specific aspect of functionality.

4. **Thread Safety**: Includes tests for concurrent access to verify thread safety.

5. **Memory Safety**: Tests verify proper memory clearing of sensitive data.

6. **Equality Testing**: Tests proper implementation of equals() and hashCode().

## Running the Tests

Run the tests with:

```bash
./gradlew :lib:testDebugUnitTest --tests "com.paytheory.lib.compose.string.*"
```

## Notes on Test Implementation

- When testing equality of SecureString objects, use `contentEquals()` on the internal byte arrays rather than relying on `equals()` due to reference comparison in ByteArray.
- When testing string length, use `revealForUi().length` rather than the internal `stringLength()` method.
- For testing with high Unicode characters (like emojis), be aware they may be represented as surrogate pairs in Java. 