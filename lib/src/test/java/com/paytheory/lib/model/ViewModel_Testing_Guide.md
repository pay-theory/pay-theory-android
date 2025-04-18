# ViewModel Testing Guide for Pay Theory Android SDK

This guide outlines best practices for testing ViewModels in the Pay Theory Android SDK, focusing on state management, Flow testing, and comprehensive coverage.

## Test Structure

Each ViewModel test class should follow this general structure:

1. **Setup and teardown** for test dispatcher and dependencies
2. **State transition tests** for each possible state
3. **Error handling tests** for edge cases and unexpected inputs
4. **Helper methods** for common test data and setup

## Required Dependencies

```kotlin
// Test dependencies for build.gradle.kts
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:4.8.1")
testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
testImplementation("androidx.arch.core:core-testing:2.2.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
```

## Basic Test Class Template

```kotlin
@ExperimentalCoroutinesApi
class YourViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: YourViewModel
    
    @Mock
    private lateinit var dependency1: Dependency1
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Initialize test data
        // ...
        
        // Create ViewModel with mocked dependencies
        viewModel = YourViewModel(dependency1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // Tests go here...
}
```

## Testing Flow State

To test Flow state in a ViewModel, use the flow's `first()` method to get its current value:

```kotlin
@Test
fun `initial state should be Loading`() = runTest {
    // Check initial state
    assertEquals(ExpectedState.Loading, viewModel.stateFlow.value)
    
    // Perform an action that changes state
    viewModel.loadData()
    
    // Check new state
    assertEquals(ExpectedState.Success(expectedData), viewModel.stateFlow.value)
}
```

For testing a sequence of state changes:

```kotlin
@Test
fun `test state transitions`() = runTest {
    // Initial state
    assertEquals(ExpectedState.Idle, viewModel.stateFlow.value)
    
    // First transition
    viewModel.startLoading()
    assertEquals(ExpectedState.Loading, viewModel.stateFlow.value)
    
    // Second transition
    viewModel.finishLoading(result)
    assertEquals(ExpectedState.Success(result), viewModel.stateFlow.value)
}
```

## Testing State Transitions

Test each possible state transition individually:

```kotlin
@Test
fun `transition from Idle to Loading`() = runTest {
    // Setup initial state if needed
    viewModel.updateState(State.Idle)
    
    // Verify initial state
    assertEquals(State.Idle, viewModel.stateFlow.value)
    
    // Trigger transition to Loading
    viewModel.startLoading()
    
    // Assert new state
    assertEquals(State.Loading, viewModel.stateFlow.value)
}
```

## Testing Complex Scenarios

Test complete flows from start to finish, including error handling:

```kotlin
@Test
fun `test complete payment flow with error and retry`() = runTest {
    // Initial state
    assertEquals(PaymentState.Idle, viewModel.paymentState.value)
    
    // Start payment
    viewModel.startPayment()
    assertEquals(PaymentState.Processing, viewModel.paymentState.value)
    
    // Simulate error
    viewModel.simulateError("Error message")
    assertEquals(PaymentState.Error("Error message"), viewModel.paymentState.value)
    
    // Retry
    viewModel.retry()
    assertEquals(PaymentState.Processing, viewModel.paymentState.value)
    
    // Simulate success
    viewModel.simulateSuccess("receipt123")
    assertEquals(PaymentState.Success("receipt123"), viewModel.paymentState.value)
}
```

## Testing Error Handling

Always test how the ViewModel handles errors:

```kotlin
@Test
fun `test network error handling`() = runTest {
    // Mock dependency to throw exception
    `when`(dependency1.fetchData()).thenThrow(IOException("Network error"))
    
    // Trigger action that will cause error
    viewModel.loadData()
    
    // Verify error state
    val errorState = viewModel.stateFlow.value
    assertTrue(errorState is State.Error)
    assertEquals("Network error", (errorState as State.Error).message)
}
```

## Testing with Reflection

For cases where you need to modify private fields during testing:

```kotlin
@Test
fun `test modifying private fields`() = runTest {
    // Get field reference using reflection
    val validatorField = YourViewModel::class.java.getDeclaredField("validator")
    validatorField.isAccessible = true
    
    // Get original value
    val originalValidator = validatorField.get(viewModel) 
    
    try {
        // Replace with mock
        val mockValidator = mock(Validator::class.java)
        validatorField.set(viewModel, mockValidator)
        
        // Test with mocked validator
        // ...
    } finally {
        // Restore original value
        validatorField.set(viewModel, originalValidator)
    }
}
```

## Best Practices

1. **Test all state transitions**: Ensure every possible state transition is tested.
2. **Test error handling**: Verify the ViewModel properly handles and emits errors.
3. **Use helper methods**: Create helper methods for test data and common setup.
4. **Test edge cases**: Test unusual scenarios like rapid state changes or duplicate states.
5. **Keep tests readable**: Use descriptive test names and clear structure.
6. **Test initialization**: Verify the initial state is correct.
7. **Test user interactions**: Test how the ViewModel responds to user actions.
8. **Use reflection carefully**: When modifying private fields, ensure proper cleanup.

## Examples

For concrete examples, refer to:
- `PaymentViewModelTest.kt`
- `PaymentFieldViewModelTest.kt`
- `PaymentProcessViewModelTest.kt`

These provide comprehensive templates for testing different types of ViewModels with various state management patterns. 