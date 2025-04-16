package com.paytheory.lib.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import com.paytheory.lib.configuration.PaymentMethodAction
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.valid.Validator
import com.paytheory.lib.websocket.WebsocketMessageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.spy
import org.mockito.kotlin.check
import java.util.HashMap

@ExperimentalCoroutinesApi
class PaymentViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var payable: Payable

    private lateinit var testConfig: PayTheoryConfiguration
    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Create test configuration with isTestMode = true to avoid NaCl and Activity dependencies
        testConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey") // This will set isTestMode to true automatically
            .setPaymentMethodType(PaymentMethodType.CARD)
            .setPaymentMethodAction(PaymentMethodAction.PAYMENT)
            .build()

        // Create the ViewModel with test dependencies
        viewModel = PaymentViewModel(
            packageName = "com.paytheory.test",
            configurationIn = testConfig,
            payable = payable
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial payment state should be Loading`() = runTest {
        assertEquals(PaymentViewModel.PaymentState.Loading, viewModel.paymentState.value)
    }

    @Test
    fun `updateToReadyState changes state to Idle`() = runTest {
        // When
        viewModel.updateToReadyState()
        
        // Then
        assertEquals(PaymentViewModel.PaymentState.Idle, viewModel.paymentState.value)
    }

    @Test
    fun `updateToProcessingState changes state to Processing`() = runTest {
        // When
        viewModel.updateToProcessingState()
        
        // Then
        assertEquals(PaymentViewModel.PaymentState.Processing, viewModel.paymentState.value)
    }

    @Test
    fun `paymentSuccess updates state to Success with receipt number`() = runTest {
        // Given
        val successResult = SuccessfulTransactionResult(
            state = "completed",
            amount = "1000",
            brand = "visa",
            lastFour = "1234",
            serviceFee = "0",
            currency = "USD",
            metadata = HashMap<Any, Any>(),
            receiptNumber = "REC-12345",
            createdAt = "1234567890",
            paymentMethodId = "",
            payorId = "cus_123"
        )
        
        // When
        viewModel.paymentSuccess(successResult)
        
        // Then
        val state = viewModel.paymentState.value
        assertTrue(state is PaymentViewModel.PaymentState.Success)
        assertEquals("REC-12345", (state as PaymentViewModel.PaymentState.Success).paymentToken)
    }

    @Test
    fun `tokenSuccess updates state to Success with payment method ID`() = runTest {
        // Given
        val tokenResult = PaymentMethodTokenResults(
            paymentMethodId = "pm_123456789",
            brand = "visa",
            state = "active",
            metadata = HashMap<Any, Any>(), 
            payor_id = "cus_123",
            lastFour = "4242",
            firstSix = "424242",
            expiration = "1225",
            paymentType = "card"
        )
        
        // When
        viewModel.tokenSuccess(tokenResult)
        
        // Then
        val state = viewModel.paymentState.value
        assertTrue(state is PaymentViewModel.PaymentState.Success)
        assertEquals("pm_123456789", (state as PaymentViewModel.PaymentState.Success).paymentToken)
    }

    @Test
    fun `clearSensitiveData clears all form fields`() = runTest {
        // Given - setup some form data
        viewModel.cardNumber.value = SecureStringWrapper(SecureString("4242424242424242"), null)
        viewModel.expiration.value = SecureStringWrapper(SecureString("1225"), null)
        viewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        viewModel.postalCode.value = SecureStringWrapper(SecureString("12345"), null)
        
        // When
        viewModel.clearSensitiveData()
        
        // Then
        assertEquals(0, viewModel.cardNumber.value.secureValue.stringLength())
        assertEquals(0, viewModel.expiration.value.secureValue.stringLength())
        assertEquals(0, viewModel.cvc.value.secureValue.stringLength())
        assertEquals(0, viewModel.postalCode.value.secureValue.stringLength())
        assertTrue(viewModel.clearCount.intValue > 0)
    }

    @Test
    fun `validateInputs with valid card data sets isValidAndReady to true`() = runTest {
        // Given - setup valid card data
        viewModel.cardNumber.value = SecureStringWrapper(SecureString("4242424242424242"), null)
        viewModel.expiration.value = SecureStringWrapper(SecureString("1225"), null)
        viewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        viewModel.postalCode.value = SecureStringWrapper(SecureString("12345"), null)
        viewModel.connected = true
        
        // Mock validator to return true for validation methods
        val mockValidator = mock(Validator::class.java)
        viewModel.validator = mockValidator
        `when`(mockValidator.isValidCardNumber(viewModel.cardNumber.value)).thenReturn(true)
        `when`(mockValidator.isValidExpiration(viewModel.expiration.value)).thenReturn(true)
        `when`(mockValidator.isValidCvc(viewModel.cvc.value)).thenReturn(true)
        `when`(mockValidator.isValidPostalCode(viewModel.postalCode.value)).thenReturn(true)
        
        // When
        viewModel.validateInputs()
        
        // Then
        assertTrue(viewModel.isValidAndReady)
        assertEquals(PaymentViewModel.PaymentState.ValidAndReady, viewModel.paymentState.value)
    }

    @Test
    fun `validateInputs with valid ACH data sets isValidAndReady to true`() = runTest {
        // Create test config with ACH payment method
        val achConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setPaymentMethodType(PaymentMethodType.ACH)
            .build()
            
        val achViewModel = PaymentViewModel(
            packageName = "com.paytheory.test",
            configurationIn = achConfig,
            payable = payable
        )
        
        // Given - setup valid ACH data
        achViewModel.nameOnAccount.value = SecureStringWrapper(SecureString("John Doe"), null)
        achViewModel.bankAccountNumber.value = SecureStringWrapper(SecureString("123456789"), null)
        achViewModel.bankRoutingNumber.value = SecureStringWrapper(SecureString("123456789"), null)
        achViewModel.bankAccountType.value = "checking"
        achViewModel.connected = true
        
        // Mock validator to return true for validation methods
        val mockValidator = mock(Validator::class.java)
        achViewModel.validator = mockValidator
        `when`(mockValidator.isNotEmpty(achViewModel.nameOnAccount.value)).thenReturn(true)
        `when`(mockValidator.isValidBankAccountNumber(achViewModel.bankAccountNumber.value)).thenReturn(true)
        `when`(mockValidator.isValidBankRoutingNumber(achViewModel.bankRoutingNumber.value)).thenReturn(true)
        
        // When
        achViewModel.validateInputs()
        
        // Then
        assertTrue(achViewModel.isValidAndReady)
        assertEquals(PaymentViewModel.PaymentState.ValidAndReady, achViewModel.paymentState.value)
    }

    @Test
    fun `validateInputs when not connected sets isValidAndReady to false`() = runTest {
        // Given - setup valid card data but not connected
        viewModel.cardNumber.value = SecureStringWrapper(SecureString("4242424242424242"), null)
        viewModel.expiration.value = SecureStringWrapper(SecureString("1225"), null)
        viewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        viewModel.postalCode.value = SecureStringWrapper(SecureString("12345"), null)
        viewModel.connected = false
        
        // Mock validator to return true for validation methods
        val mockValidator = mock(Validator::class.java)
        viewModel.validator = mockValidator
        `when`(mockValidator.isValidCardNumber(viewModel.cardNumber.value)).thenReturn(true)
        `when`(mockValidator.isValidExpiration(viewModel.expiration.value)).thenReturn(true)
        `when`(mockValidator.isValidCvc(viewModel.cvc.value)).thenReturn(true)
        `when`(mockValidator.isValidPostalCode(viewModel.postalCode.value)).thenReturn(true)
        
        // When
        viewModel.validateInputs()
        
        // Then
        assertFalse(viewModel.isValidAndReady)
    }

    @Test
    fun `submitPayment with already processing state reports error`() = runTest {
        // Given
        viewModel._paymentState.value = PaymentViewModel.PaymentState.Processing
        
        // When
        viewModel.submitPayment()
        
        // Then
        verify(payable).handleError(
            check { error ->
                assertEquals(ErrorCode.ActionInProgress, error.code)
                assertEquals("Payment already in progress", error.error)
            }
        )
    }

    @Test
    fun `submitPayment with success state reports error`() = runTest {
        // Given
        viewModel._paymentState.value = PaymentViewModel.PaymentState.Success("REC-12345")
        
        // When
        viewModel.submitPayment()
        
        // Then
        verify(payable).handleError(
            check { error ->
                assertEquals(ErrorCode.ActionComplete, error.code)
                assertEquals("Payment already completed", error.error)
            }
        )
    }

    @Test
    fun `submitPayment with loading state reports error`() = runTest {
        // Given
        viewModel._paymentState.value = PaymentViewModel.PaymentState.Loading
        
        // When
        viewModel.submitPayment()
        
        // Then
        verify(payable).handleError(
            check { error ->
                assertEquals(ErrorCode.InProgress, error.code)
                assertEquals("No connection available", error.error)
            }
        )
    }

    @Test
    fun `disconnect sets connected to false and updates state to Loading`() = runTest {
        // Given
        viewModel.connected = true
        viewModel._paymentState.value = PaymentViewModel.PaymentState.Idle
        
        // When
        viewModel.disconnect()
        
        // Then
        assertFalse(viewModel.connected)
        assertEquals(PaymentViewModel.PaymentState.Loading, viewModel.paymentState.value)
    }
    
    @Test
    fun `validateInputs with invalid card number sets isValidAndReady to false`() = runTest {
        // Given - setup with invalid card number
        viewModel.cardNumber.value = SecureStringWrapper(SecureString("4242111122223333"), null)
        viewModel.expiration.value = SecureStringWrapper(SecureString("1225"), null)
        viewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        viewModel.postalCode.value = SecureStringWrapper(SecureString("12345"), null)
        viewModel.connected = true
        
        // Use real validator instead of mock
        viewModel.validator = Validator()
        
        // When
        viewModel.validateInputs()
        
        // Then
        assertFalse(viewModel.isValidAndReady)
    }
    
    @Test
    fun `validateInputs with invalid expiration sets isValidAndReady to false`() = runTest {
        // Given - setup with expired date
        viewModel.cardNumber.value = SecureStringWrapper(SecureString("4242424242424242"), null)
        viewModel.expiration.value = SecureStringWrapper(SecureString("1220"), null) // Expired date
        viewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        viewModel.postalCode.value = SecureStringWrapper(SecureString("12345"), null)
        viewModel.connected = true
        
        // Use real validator
        viewModel.validator = Validator()
        
        // When
        viewModel.validateInputs()
        
        // Then
        assertFalse(viewModel.isValidAndReady)
    }
    
    @Test
    fun `validateInputs with invalid postal code sets isValidAndReady to false`() = runTest {
        // Given - setup with invalid postal code
        viewModel.cardNumber.value = SecureStringWrapper(SecureString("4242424242424242"), null)
        viewModel.expiration.value = SecureStringWrapper(SecureString("1225"), null)
        viewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        viewModel.postalCode.value = SecureStringWrapper(SecureString("ABC"), null) // Invalid format
        viewModel.connected = true
        
        // Use real validator
        viewModel.validator = Validator()
        
        // When
        viewModel.validateInputs()
        
        // Then
        assertFalse(viewModel.isValidAndReady)
    }
    
    @Test
    fun `validateInputs with valid card data and billing address requirements sets isValidAndReady to true`() = runTest {
        // Create config that requires billing address
        val addressConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setPaymentMethodType(PaymentMethodType.CARD)
            .setRequireBillingAddress(true)
            .build()
            
        val addressViewModel = PaymentViewModel(
            packageName = "com.paytheory.test",
            configurationIn = addressConfig,
            payable = payable
        )
        
        // Given - setup all required fields
        addressViewModel.cardNumber.value = SecureStringWrapper(SecureString("4242424242424242"), null)
        addressViewModel.expiration.value = SecureStringWrapper(SecureString("1225"), null)
        addressViewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        addressViewModel.addressLine1.value = SecureStringWrapper(SecureString("123 Main St"), null)
        addressViewModel.city.value = SecureStringWrapper(SecureString("Anytown"), null)
        addressViewModel.region.value = SecureStringWrapper(SecureString("CA"), null)
        addressViewModel.postalCode.value = SecureStringWrapper(SecureString("12345"), null)
        addressViewModel.connected = true
        
        // Mock validator to return true for all methods
        val mockValidator = mock(Validator::class.java)
        addressViewModel.validator = mockValidator
        `when`(mockValidator.isValidCardNumber(addressViewModel.cardNumber.value)).thenReturn(true)
        `when`(mockValidator.isValidExpiration(addressViewModel.expiration.value)).thenReturn(true)
        `when`(mockValidator.isValidCvc(addressViewModel.cvc.value)).thenReturn(true)
        `when`(mockValidator.isNotEmpty(addressViewModel.addressLine1.value)).thenReturn(true)
        `when`(mockValidator.isNotEmpty(addressViewModel.city.value)).thenReturn(true)
        `when`(mockValidator.isNotEmpty(addressViewModel.region.value)).thenReturn(true)
        `when`(mockValidator.isValidPostalCode(addressViewModel.postalCode.value)).thenReturn(true)
        
        // When
        addressViewModel.validateInputs()
        
        // Then
        assertTrue(addressViewModel.isValidAndReady)
        assertEquals(PaymentViewModel.PaymentState.ValidAndReady, addressViewModel.paymentState.value)
    }
    
    @Test
    fun `validateInputs with missing required billing field sets isValidAndReady to false`() = runTest {
        // Create config that requires billing address
        val addressConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setPaymentMethodType(PaymentMethodType.CARD)
            .setRequireBillingAddress(true)
            .build()
            
        val addressViewModel = PaymentViewModel(
            packageName = "com.paytheory.test",
            configurationIn = addressConfig,
            payable = payable
        )
        
        // Given - missing city field
        addressViewModel.cardNumber.value = SecureStringWrapper(SecureString("4242424242424242"), null)
        addressViewModel.expiration.value = SecureStringWrapper(SecureString("1225"), null)
        addressViewModel.cvc.value = SecureStringWrapper(SecureString("123"), null)
        addressViewModel.addressLine1.value = SecureStringWrapper(SecureString("123 Main St"), null)
        addressViewModel.city.value = SecureStringWrapper(SecureString(""), null) // Missing city
        addressViewModel.region.value = SecureStringWrapper(SecureString("CA"), null)
        addressViewModel.postalCode.value = SecureStringWrapper(SecureString("12345"), null)
        addressViewModel.connected = true
        
        // Mock validator
        val mockValidator = mock(Validator::class.java)
        addressViewModel.validator = mockValidator
        `when`(mockValidator.isValidCardNumber(addressViewModel.cardNumber.value)).thenReturn(true)
        `when`(mockValidator.isValidExpiration(addressViewModel.expiration.value)).thenReturn(true)
        `when`(mockValidator.isValidCvc(addressViewModel.cvc.value)).thenReturn(true)
        `when`(mockValidator.isNotEmpty(addressViewModel.addressLine1.value)).thenReturn(true)
        `when`(mockValidator.isNotEmpty(addressViewModel.city.value)).thenReturn(false) // City validation fails
        `when`(mockValidator.isNotEmpty(addressViewModel.region.value)).thenReturn(true)
        `when`(mockValidator.isValidPostalCode(addressViewModel.postalCode.value)).thenReturn(true)
        
        // When
        addressViewModel.validateInputs()
        
        // Then
        assertFalse(addressViewModel.isValidAndReady)
    }
    
    @Test
    fun `submitPayment with token action and amount reports error`() = runTest {
        // Create config with TOKEN action and amount
        val tokenConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setPaymentMethodType(PaymentMethodType.CARD)
            .setPaymentMethodAction(PaymentMethodAction.TOKEN)
            .setAmount(1000) // Invalid - can't have amount with token action
            .build()
            
        val tokenViewModel = PaymentViewModel(
            packageName = "com.paytheory.test",
            configurationIn = tokenConfig,
            payable = payable
        )
        
        // Given
        tokenViewModel._paymentState.value = PaymentViewModel.PaymentState.ValidAndReady
        
        // When
        tokenViewModel.submitPayment()
        
        // Then
        verify(payable).handleError(
            check { error ->
                assertEquals(ErrorCode.NotValid, error.code)
                assertEquals("Cannot have amount with token action", error.error)
            }
        )
    }
    
    @Test
    fun `submitPayment with payment action and insufficient amount reports error`() = runTest {
        // Create config with PAYMENT action and insufficient amount
        val paymentConfig = PayTheoryConfiguration.Builder()
            .setApiKey("test-paytheory-apikey")
            .setPaymentMethodType(PaymentMethodType.CARD)
            .setPaymentMethodAction(PaymentMethodAction.PAYMENT)
            .setAmount(5) // Invalid - must be at least 10
            .build()
            
        val paymentViewModel = PaymentViewModel(
            packageName = "com.paytheory.test",
            configurationIn = paymentConfig,
            payable = payable
        )
        
        // Given
        paymentViewModel._paymentState.value = PaymentViewModel.PaymentState.ValidAndReady
        
        // When
        paymentViewModel.submitPayment()
        
        // Then
        verify(payable).handleError(
            check { error ->
                assertEquals(ErrorCode.NotValid, error.code)
                assertEquals("Must provide amount greater than 10", error.error)
            }
        )
    }

    @Test
    fun `update field methods correctly update state values`() = runTest {
        // Create a test wrapper
        val testValue = SecureStringWrapper(SecureString("test value"), null)
        
        // Test each update method
        viewModel.updateNameOnAccount(testValue)
        assertEquals(testValue, viewModel.nameOnAccount.value)
        
        viewModel.updateCardNumber(testValue)
        assertEquals(testValue, viewModel.cardNumber.value)
        
        viewModel.updateExpiration(testValue)
        assertEquals(testValue, viewModel.expiration.value)
        
        viewModel.updateCvc(testValue)
        assertEquals(testValue, viewModel.cvc.value)
        
        viewModel.updateAddressLine1(testValue)
        assertEquals(testValue, viewModel.addressLine1.value)
        
        viewModel.updateAddressLine2(testValue)
        assertEquals(testValue, viewModel.addressLine2.value)
        
        viewModel.updateCity(testValue)
        assertEquals(testValue, viewModel.city.value)
        
        viewModel.updateRegion(testValue)
        assertEquals(testValue, viewModel.region.value)
        
        viewModel.updatePostalCode(testValue)
        assertEquals(testValue, viewModel.postalCode.value)
        
        viewModel.updateBankAccountNumber(testValue)
        assertEquals(testValue, viewModel.bankAccountNumber.value)
        
        viewModel.updateBankRoutingNumber(testValue)
        assertEquals(testValue, viewModel.bankRoutingNumber.value)
        
        // Test the string update
        val accountType = "savings"
        viewModel.updateBankAccountType(accountType)
        assertEquals(accountType, viewModel.bankAccountType.value)
    }
} 