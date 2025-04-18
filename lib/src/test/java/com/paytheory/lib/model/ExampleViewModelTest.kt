package com.paytheory.lib.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.IOException

/**
 * Example ViewModel for demonstration purposes
 */
class ExampleViewModel {
    // Define states
    sealed class ViewState {
        object Idle : ViewState()
        object Loading : ViewState()
        data class Success(val data: String) : ViewState()
        data class Error(val message: String) : ViewState()
    }
    
    // Internal mutable state flow
    private val _viewState = MutableStateFlow<ViewState>(ViewState.Idle)
    
    // Public immutable state flow
    val viewState: StateFlow<ViewState> = _viewState
    
    // Example dependencies
    var repository: DataRepository? = null
    
    // State update method
    fun updateState(newState: ViewState) {
        _viewState.value = newState
    }
    
    // Example action methods
    fun loadData() {
        try {
            _viewState.value = ViewState.Loading
            val data = repository?.fetchData() ?: throw IOException("Repository not initialized")
            _viewState.value = ViewState.Success(data)
        } catch (e: Exception) {
            _viewState.value = ViewState.Error(e.message ?: "Unknown error")
        }
    }
    
    fun resetState() {
        _viewState.value = ViewState.Idle
    }
}

/**
 * Example repository for the ViewModel to interact with
 */
interface DataRepository {
    fun fetchData(): String
}

/**
 * Simple implementation for testing
 */
class TestDataRepository : DataRepository {
    var shouldThrow = false
    var testData = "Test data"
    
    override fun fetchData(): String {
        if (shouldThrow) {
            throw IOException("Network error")
        }
        return testData
    }
}

/**
 * Example test class for ExampleViewModel
 */
@ExperimentalCoroutinesApi
class ExampleViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ExampleViewModel
    
    // Use a concrete implementation instead of a mock for simpler testing
    private lateinit var testRepository: TestDataRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Initialize the repository and ViewModel
        testRepository = TestDataRepository()
        viewModel = ExampleViewModel()
        viewModel.repository = testRepository
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state is Idle`() = runTest {
        // Then
        assertEquals(ExampleViewModel.ViewState.Idle, viewModel.viewState.value)
    }
    
    @Test
    fun `updateState updates the current state`() = runTest {
        // Given
        val newState = ExampleViewModel.ViewState.Loading
        
        // When
        viewModel.updateState(newState)
        
        // Then
        assertEquals(newState, viewModel.viewState.value)
    }
    
    @Test
    fun `loadData transitions from Idle to Loading to Success`() = runTest {
        // Given
        testRepository.testData = "Test data"
        testRepository.shouldThrow = false
        
        // Initial state
        assertEquals(ExampleViewModel.ViewState.Idle, viewModel.viewState.value)
        
        // When
        viewModel.loadData()
        
        // Then - state should be Success
        val currentState = viewModel.viewState.value
        assertTrue(currentState is ExampleViewModel.ViewState.Success)
        assertEquals("Test data", (currentState as ExampleViewModel.ViewState.Success).data)
    }
    
    @Test
    fun `loadData handles error and updates state`() = runTest {
        // Given
        testRepository.shouldThrow = true
        
        // Initial state
        assertEquals(ExampleViewModel.ViewState.Idle, viewModel.viewState.value)
        
        // When
        viewModel.loadData()
        
        // Then - state should be Error
        val currentState = viewModel.viewState.value
        assertTrue(currentState is ExampleViewModel.ViewState.Error)
        assertEquals("Network error", (currentState as ExampleViewModel.ViewState.Error).message)
    }
    
    @Test
    fun `resetState changes state back to Idle`() = runTest {
        // Given - set state to something other than Idle
        viewModel.updateState(ExampleViewModel.ViewState.Loading)
        assertEquals(ExampleViewModel.ViewState.Loading, viewModel.viewState.value)
        
        // When
        viewModel.resetState()
        
        // Then
        assertEquals(ExampleViewModel.ViewState.Idle, viewModel.viewState.value)
    }
    
    @Test
    fun `test complete flow with state transitions`() = runTest {
        // Given
        testRepository.testData = "Success data"
        testRepository.shouldThrow = false
        
        // Initial state
        assertEquals(ExampleViewModel.ViewState.Idle, viewModel.viewState.value)
        
        // When - load data
        viewModel.loadData()
        
        // Then - success state
        val successState = viewModel.viewState.value
        assertTrue(successState is ExampleViewModel.ViewState.Success)
        
        // When - reset
        viewModel.resetState()
        
        // Then - back to idle
        assertEquals(ExampleViewModel.ViewState.Idle, viewModel.viewState.value)
        
        // When - make repository throw exception
        testRepository.shouldThrow = true
        viewModel.loadData()
        
        // Then - error state
        val errorState = viewModel.viewState.value
        assertTrue(errorState is ExampleViewModel.ViewState.Error)
    }
} 