# Reactors Package

## Overview
The reactors package contains classes responsible for handling WebSocket connections and message processing in the Pay Theory Android SDK.

### Key Components
- **ConnectionReactors**: Manages WebSocket connection events (connection establishment and termination)
- **MessageReactors**: Processes various types of messages received from the WebSocket connection

## Testing Strategy

### Testing Approach
Based on the successful patterns established in `GooglePayProcessorTest`, we follow these principles:

1. **Use Relaxed Mocks**: Create relaxed mocks for all dependencies to reduce setup complexity
   ```kotlin
   mockPayable = mockk(relaxed = true)
   mockViewModel = mockk(relaxed = true)
   ```

2. **Verify Behavior**: Test interaction with dependencies rather than implementation details
   ```kotlin
   verify { 
       paymentViewModel.sendSocketMessage(match { message ->
           // Verify message contents match expectations
           true
       })
   }
   ```

3. **Access Private Methods/Fields When Necessary**: Use reflection to test non-public members
   ```kotlin
   private fun getPrivateFieldValue(target: Any, fieldName: String): Any? {
       val field = target.javaClass.getDeclaredField(fieldName)
       field.isAccessible = true
       return field.get(target)
   }
   ```

4. **Use Spies for Internal Logic**: When testing protected methods, create spies
   ```kotlin
   val spyReactors = spyk(messageReactors)
   ```

### ConnectionReactors Testing

Key test cases:
- `onConnected_whenConnectionEstablished_sendsHostTokenRequest`: Verify correct request is sent on connection
- `onDisconnected_whenConnectionDrops_callsDisconnectOnViewModel`: Verify disconnection handling
- `onConnected_whenExceptionOccurs_handlesError`: Verify error handling during connection

### MessageReactors Testing

Organized by functionality:

1. **Credential Management**
   - Testing `onHostToken`, `onTokenizeHostToken`, `establishConnection`
   - Verifying correct storing and passing of connection credentials

2. **Transaction Processing**
   - Mock the encryption/decryption using relaxed mocks
   - Test success and failure scenarios
   - Capture and verify payment details

3. **Error Handling**
   - Test exception paths
   - Verify proper error propagation

### Mocking Encryption/Decryption

Since decryption involves native code:
- Mock the `decryptBox` function using MockK's `mockkStatic`
- Return prepared test data for different scenarios

## Implementation Phases

### Phase 1: Basic Setup (5 points)
- Create relaxed mocks for all dependencies
- Set up utility methods for reflection if needed

### Phase 2: ConnectionReactors Tests (10 points)
- Test `onConnected` verifying correct socket message is sent
- Test `onDisconnected` verifying disconnect is called
- Mock exceptions and verify error handling

### Phase 3: MessageReactors Tests (15 points)
- Test credential management methods
- Test transaction handling with mocked encryption
- Test error handling paths

## Related Documentation

For more details on testing patterns and approaches:
- See `.notes/Google-Pay-Testing/testing-patterns.md`
- See `.notes/testing-roadmap.md`
- See `.notes/testing-implementation-plan.md` 