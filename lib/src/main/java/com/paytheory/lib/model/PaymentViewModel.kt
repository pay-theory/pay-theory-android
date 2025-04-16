package com.paytheory.lib.model

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.Payment
import com.paytheory.lib.PaymentMethodToken
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import com.paytheory.lib.compose.utility.PaymentFormUtils
import com.paytheory.lib.configuration.PaymentMethodAction
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.data.requests.PaymentDetail
import com.paytheory.lib.valid.Validator
import com.paytheory.lib.websocket.WebServicesProvider
import com.paytheory.lib.websocket.WebsocketInteractor
import com.paytheory.lib.websocket.WebsocketMessageHandler
import com.paytheory.lib.websocket.WebsocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing payment form state and processing payments through Pay Theory.
 *
 * This class handles:
 * - Payment form state management
 * - Input validation
 * - WebSocket communication
 * - Payment processing
 *
 * @property configuration The Pay Theory configuration for this payment instance
 * @property payTheoryData Additional data required for payment processing
 * @property payTheoryProcessor The processor handling payment or tokenization
 * @property paymentState Current state of the payment process
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(packageName:String, configurationIn: PayTheoryConfiguration, payable: Payable) :ViewModel() {
    sealed class PaymentState {
        object Idle : PaymentState()
        object ValidAndReady : PaymentState()
        object Processing : PaymentState()
        object Loading : PaymentState()
        data class Success(val paymentToken: String) : PaymentState()
        data class Error(val errorMessage: String) : PaymentState()
    }



    val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Loading)

    private val webServicesProvider = WebServicesProvider()
    private val webSocketRepository = WebsocketRepository(webServicesProvider)
    internal val interactor = WebsocketInteractor(webSocketRepository)
    val configuration = configurationIn
    val payTheoryData = PaymentFormUtils.createPayTheoryData(configuration)
    val payTheoryProcessor = if (configuration.paymentMethodAction==PaymentMethodAction.TOKEN)
        PaymentMethodToken(
            packageName,
            payable,
            payTheoryData,
            configurationIn,
            this
        ) else Payment(
            packageName,
            payable,
            payTheoryData,
            configurationIn,
            this
        )

    val paymentState: StateFlow<PaymentState> = _paymentState


    var validator = Validator()
    var clearCount = mutableIntStateOf(0)  // Add this line
    var isValidAndReady = false
    var errorMessage: String = ""
    var connected: Boolean = false


    var bankAccountNumber = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var bankRoutingNumber = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var bankAccountType = mutableStateOf("")
    var cardNumber = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var expiration = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var cvc = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var nameOnAccount = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var addressLine1 = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var addressLine2 = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var city = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var region = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var postalCode = mutableStateOf(SecureStringWrapper(SecureString(""),null))

    /**
     * Disconnects the payment session.
     * Sets connected state to false and updates the payment state to Loading.
     */
    fun disconnect() {
        connected = false
        // First update isValidAndReady flag to maintain consistency
        isValidAndReady = false
        // Then update payment state
        _paymentState.value = PaymentState.Loading
    }

    /**
     * Subscribes to WebSocket events and handles incoming messages.
     *
     * @param handler The handler for processing WebSocket messages
     * @param ptTokenResponse The token response containing authentication information
     */
    @ExperimentalCoroutinesApi
    fun subscribeToSocketEvents(handler: WebsocketMessageHandler, ptTokenResponse:PTTokenResponse) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.startSocket(ptTokenResponse.ptToken, configuration.partnerName, configuration.stageName).consumeEach {
                    if (it.exception == null) {
                        handler.receiveMessage(it.text!!)
                        connected = true
                    } else if (it.exception.message == "executor rejected") {
                        connected = false
                        _paymentState.value = PaymentState.Loading
                    } else {
                        onSocketError(it.exception,"Failed to receive message")
                        connected = false
                        _paymentState.value = PaymentState.Loading
                    }
                }
            } catch (ex: Exception) {
                onSocketError(ex,"Failed to start socket")
                connected = false
                _paymentState.value = PaymentState.Loading
            }
        }
        connected = true
        _paymentState.value = PaymentState.Idle
    }

    /**
     * Sends a message through the WebSocket connection.
     *
     * @param message The message to send to the server
     */
    @ExperimentalCoroutinesApi
    fun sendSocketMessage(message:String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.sendMessage(message)
            } catch (ex: Exception) {
                onSocketError(ex,"Failed to send message")
            }
        }
    }

    /**
     * Handles WebSocket errors and manages reconnection attempts.
     *
     * @param ex The exception that occurred
     * @param reason A description of what operation failed
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onSocketError(ex: Throwable, reason: String) {
        if (ex.message!!.contains("executor rejected")) {
                return
        }
        val error = "$reason ${ex.message}"
        interactor.stopSocket()
        // catch errors "Read error: ssl=0x7340b644c8: I/O error during system call", "Software caused connection abort", "null", "Unable to resolve host"
        if (error.contains("Read error: ssl", ignoreCase = true)
            || error.contains("Software caused connection abort", ignoreCase = true)
            || error.contains("null", ignoreCase = true)
            || error.contains("Unable to resolve host", ignoreCase = true)) {
            println("Network Connection Error - Reconnecting...")
            payTheoryProcessor.resetSocket()
        } else if (error == "executor rejected") {
            println("Socket connection removed.")
        } else { //if error is not ssl error
            payTheoryProcessor.payable.handleError(PTError(ErrorCode.SocketError,error))
        }
        connected = false
        _paymentState.value = PaymentState.Loading
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        disconnect()
        super.onCleared()
    }

    /**
     * Updates the name on account field and triggers validation.
     *
     * @param value The new name value as a SecureString
     */
    fun updateNameOnAccount(value: SecureStringWrapper) {
        nameOnAccount.value = value
        validateInputs()
    }
    fun updateBankAccountNumber(value: SecureStringWrapper) {
        bankAccountNumber.value = value
        validateInputs()
    }
    fun updateBankRoutingNumber(value: SecureStringWrapper) {
        bankRoutingNumber.value = value
        validateInputs()
    }
    fun updateBankAccountType(value: String) {
        bankAccountType.value = value
        validateInputs()
    }
    fun updateCardNumber(value: SecureStringWrapper) {
        cardNumber.value = value
        validateInputs()
    }
    fun updateExpiration(value: SecureStringWrapper) {

        expiration.value = value
        validateInputs()
    }
    fun updateCvc(value: SecureStringWrapper) {
        cvc.value = value
        validateInputs()
    }
    fun updateAddressLine1(value: SecureStringWrapper) {
        addressLine1.value = value
        validateInputs()
    }
    fun updateAddressLine2(value: SecureStringWrapper) {
        addressLine2.value = value
    }
    fun updateCity(value: SecureStringWrapper) {
        city.value = value
        validateInputs()
    }
    fun updateRegion(value: SecureStringWrapper) {
        region.value = value
        validateInputs()
    }
    fun updatePostalCode(value: SecureStringWrapper) {
        postalCode.value = value
        validateInputs()
    }

    /**
     * Validates all input fields based on the payment method type and configuration.
     *
     * For ACH payments, validates:
     * - Name on account
     * - Bank account number
     * - Bank routing number
     * - Account type
     * - Billing address (if required)
     *
     * For card payments, validates:
     * - Card number
     * - Expiration date
     * - CVC
     * - Billing address or postal code (based on configuration)
     *
     * Updates the payment state and validity flags based on the validation results.
     */
    fun validateInputs() {
        var isReady = false
        if (configuration.paymentMethodType == PaymentMethodType.ACH) {
            val relevant = mutableListOf<Boolean>()
            for (field in BankFields.entries) {
                when (field) {
                    BankFields.NAME_ON_ACCOUNT -> relevant.add(isValidNameOnAccount())
                    BankFields.BANK_ACCOUNT_NUMBER -> relevant.add(isValidBankAccountNumber())
                    BankFields.BANK_ROUTING_NUMBER -> relevant.add(isValidBankRoutingNumber())
                    BankFields.BANK_ACCOUNT_TYPE -> relevant.add(isValidAccountType())
                }
            }

            if (configuration.requireBillingAddress) {
                relevant.add(isValidAccountAddress())
            }

            isReady = relevant.all { it }

        } else {

            val relevant = mutableListOf<Boolean>()
            for (field in CreditCardFields.entries) {
                when (field) {
                    CreditCardFields.CARD_NUMBER -> relevant.add(isValidCardNumber())
                    CreditCardFields.CARD_EXPIRATION -> relevant.add(isValidExpiration())
                    CreditCardFields.CARD_CVC -> relevant.add(isValidCvc())
                }
            }

            if (configuration.requireBillingAddress) {
                relevant.add(isValidAccountAddress())
            } else {
                relevant.add(isValidPostalCode())
            }
            isReady = relevant.all { it }

        }




        if (!connected) {
            isValidAndReady = false
        }
        else {
            val derivedPaymentState =  when {
                isReady -> PaymentState.ValidAndReady
                else -> _paymentState.value
            }
            _paymentState.value = derivedPaymentState
            isValidAndReady = isReady
        }


    }

    private fun isValidCardNumber(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.CARD_NUMBER, validator.isValidCardNumber(cardNumber.value), cardNumber.value.secureValue.stringLength() == 0)
    }
    private fun isValidExpiration(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.CARD_EXPIRATION, validator.isValidExpiration(expiration.value), expiration.value.secureValue.stringLength() == 0)
    }
    private fun isValidCvc(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.CARD_CVC, validator.isValidCvc(cvc.value), cvc.value.secureValue.stringLength() == 0)
    }
    private fun isValidStreetAddress(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.ADDRESS_LINE1, validator.isNotEmpty(addressLine1.value), addressLine1.value.secureValue.stringLength() == 0)
    }
    private fun isValidCity(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.CITY, validator.isNotEmpty(city.value), city.value.secureValue.stringLength() == 0)
    }
    private fun isValidState(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.REGION, validator.isNotEmpty(region.value), region.value.secureValue.stringLength() == 0)
    }
    private fun isValidPostalCode(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.POSTAL_CODE, validator.isValidPostalCode(postalCode.value), postalCode.value.secureValue.stringLength() == 0)
    }
    private fun isValidNameOnAccount(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.NAME_ON_ACCOUNT, validator.isNotEmpty(nameOnAccount.value), nameOnAccount.value.secureValue.stringLength() == 0)
    }
    private fun isValidBankAccountNumber(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.BANK_ACCOUNT_NUMBER, validator.isValidBankAccountNumber(bankAccountNumber.value), bankAccountNumber.value.secureValue.stringLength() == 0)
    }
    private fun isValidBankRoutingNumber(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.BANK_ROUTING_NUMBER, validator.isValidBankRoutingNumber(bankRoutingNumber.value), bankRoutingNumber.value.secureValue.stringLength() == 0)
    }
    private fun isValidAccountType(): Boolean {
        return propagateState(payTheoryProcessor, PaymentField.BANK_ACCOUNT_TYPE, bankAccountType.value.isNotBlank(), bankAccountType.value.isBlank())
    }

    /**
     * Validates the complete billing address.
     *
     * Checks all required address fields:
     * - Street address (line 1)
     * - City
     * - Region (State)
     * - Postal code
     *
     * @return true if all required address fields are valid, false otherwise
     */
    fun isValidAccountAddress(): Boolean {
        val relevant = mutableListOf<Boolean>()
        for (field in AddressFields.entries) {
            when (field) {
                AddressFields.ADDRESS_LINE1 -> relevant.add(isValidStreetAddress())
                AddressFields.CITY -> relevant.add(isValidCity())
                AddressFields.REGION -> relevant.add(isValidState())
                AddressFields.POSTAL_CODE -> relevant.add(isValidPostalCode())
            }
        }
        return relevant.all { it }
    }


    /**
     * Handles successful payment completion.
     *
     * Updates the payment state to Success with the receipt number and
     * clears sensitive data from the form.
     *
     * @param result The successful transaction result containing the receipt number
     */
    fun paymentSuccess(result: SuccessfulTransactionResult) {
        _paymentState.value = PaymentState.Success(result.receiptNumber)
        clearSensitiveData()
    }
    fun tokenSuccess(result: PaymentMethodTokenResults) {
        _paymentState.value = PaymentState.Success(result.paymentMethodId)
        clearSensitiveData()
    }


    /**
     * Clears all sensitive data from the form and resets field states.
     *
     * This includes:
     * - All form field values
     * - Field states (valid/invalid/empty)
     * - Validation flags
     *
     * Increments the clear count to notify observers of the clear operation.
     */
    internal fun clearSensitiveData() {
        listOf(
            nameOnAccount, addressLine1, addressLine2,
            city, region, cardNumber, cvc,
            bankAccountNumber, bankRoutingNumber,
             postalCode, expiration
        ).forEach {
            it.value = SecureStringWrapper(SecureString(""),null)
        }
        bankAccountType.value = ""
        for (field in PaymentField.entries) {
            paymentFieldState[field] = FieldState.INIT
            paymentFieldEmpty[field] = false
            paymentFieldValid[field] = true
            payTheoryProcessor.payable.handleStateChange(Pair(field, paymentFieldState[field]!!))
        }

        clearCount.intValue++
        isValidAndReady = false
    }

    /**
     * Updates the payment state to the processing state.
     * Called when a payment is being processed.
     */
    fun updateToProcessingState() {
        _paymentState.value = PaymentState.Processing
    }

    /**
     * Updates the payment state to the ready state.
     * Called when a payment form is ready for user input.
     */
    fun updateToReadyState() {
        _paymentState.value = PaymentState.Idle
        validateInputs()
    }

    /**
     * Submits the payment for processing.
     * 
     * This function performs pre-submission validation and handles various error cases:
     * - Payment already in progress
     * - Payment already completed
     * - No connection available
     * - Invalid amount with token action
     *
     * If validation passes, it constructs the appropriate payment detail object
     * and submits it for processing.
     */
    fun submitPayment() {
        var payment: PaymentDetail? = null
        if (_paymentState.value == PaymentState.Processing) {
            payTheoryProcessor.payable.handleError(PTError(ErrorCode.ActionInProgress,"Payment already in progress"))
            return
        } else if (_paymentState.value is PaymentState.Success) {
            payTheoryProcessor.payable.handleError(PTError(ErrorCode.ActionComplete,"Payment already completed"))
            return
        } else if (_paymentState.value == PaymentState.Loading) {
            payTheoryProcessor.payable.handleError(PTError(ErrorCode.InProgress,"No connection available"))
            return
        }

        if (configuration.amount > 0 && configuration.paymentMethodAction == PaymentMethodAction.TOKEN) {
            payTheoryProcessor.payable.handleError(PTError(ErrorCode.NotValid,"Cannot have amount with token action"))
            return
        }
        if (configuration.amount < 10 && configuration.paymentMethodAction == PaymentMethodAction.PAYMENT) {
            payTheoryProcessor.payable.handleError(PTError(ErrorCode.NotValid,"Must provide amount greater than 10"))
            return
        }
        
        if (_paymentState.value != PaymentState.ValidAndReady) {
            payTheoryProcessor.payable.handleError(PTError(ErrorCode.NotValid,"Payment not ready to be submitted"))
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            _paymentState.value = PaymentState.Processing
        }
        
        try {
            val paymentConfigured: PaymentDetail = if (configuration.paymentMethodType == PaymentMethodType.ACH) {
                constructBankPayment()
            } else {
                constructCardPayment()
            }
            payTheoryProcessor.process(paymentConfigured)
        } catch (ex: Exception) {
            onSocketError(ex,"Payment submission failed")
            _paymentState.value = PaymentState.Error(ex.message ?: "Unknown error")
            return
        }
    }

}

