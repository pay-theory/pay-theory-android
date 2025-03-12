package com.paytheory.lib.model

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paytheory.lib.ErrorCode
import com.paytheory.lib.PTError
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.Payment
import com.paytheory.lib.SuccessfulTransactionResult
import com.paytheory.lib.api.PTTokenResponse
import com.paytheory.lib.compose.createPayTheoryData
import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper
import com.paytheory.lib.configuration.PaymentMethodType
import com.paytheory.lib.data.Address
import com.paytheory.lib.data.PaymentDetail
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
    enum class FieldState {
        EMPTY,
        READY,
        INVALID,
        INIT
    }
    enum class PaymentField {
        NAME_ON_ACCOUNT,
        BANK_ACCOUNT_NUMBER,
        BANK_ROUTING_NUMBER,
        BANK_ACCOUNT_TYPE,
        CARD_NUMBER,
        CARD_EXPIRATION,
        CARD_CVC,
        ADDRESS_LINE1,
        ADDRESS_LINE2,
        CITY,
        REGION,
        POSTAL_CODE
    }

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Loading)
    val paymentFieldValid: HashMap<PaymentField, Boolean> = hashMapOf(
        Pair<PaymentField, Boolean>(PaymentField.NAME_ON_ACCOUNT, true),
        Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_NUMBER, true),
        Pair<PaymentField, Boolean>(PaymentField.BANK_ROUTING_NUMBER, true),
        Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_TYPE, true),
        Pair<PaymentField, Boolean>(PaymentField.CARD_NUMBER, true),
        Pair<PaymentField, Boolean>(PaymentField.CARD_EXPIRATION, true),
        Pair<PaymentField, Boolean>(PaymentField.CARD_CVC, true),
        Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE1, true),
        Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE2, true),
        Pair<PaymentField, Boolean>(PaymentField.CITY, true),
        Pair<PaymentField, Boolean>(PaymentField.REGION, true),
        Pair<PaymentField, Boolean>(PaymentField.POSTAL_CODE, true)
    )
    val paymentFieldEmpty: HashMap<PaymentField, Boolean> = hashMapOf(
        Pair<PaymentField, Boolean>(PaymentField.NAME_ON_ACCOUNT, false),
        Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_NUMBER, false),
        Pair<PaymentField, Boolean>(PaymentField.BANK_ROUTING_NUMBER, false),
        Pair<PaymentField, Boolean>(PaymentField.BANK_ACCOUNT_TYPE, false),
        Pair<PaymentField, Boolean>(PaymentField.CARD_NUMBER, false),
        Pair<PaymentField, Boolean>(PaymentField.CARD_EXPIRATION, false),
        Pair<PaymentField, Boolean>(PaymentField.CARD_CVC, false),
        Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE1, false),
        Pair<PaymentField, Boolean>(PaymentField.ADDRESS_LINE2, false),
        Pair<PaymentField, Boolean>(PaymentField.CITY, false),
        Pair<PaymentField, Boolean>(PaymentField.REGION, false),
        Pair<PaymentField, Boolean>(PaymentField.POSTAL_CODE, false)
    )
    val paymentFieldState: HashMap<PaymentField, FieldState> = hashMapOf(
        Pair<PaymentField, FieldState>(PaymentField.NAME_ON_ACCOUNT, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.BANK_ACCOUNT_NUMBER, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.BANK_ROUTING_NUMBER, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.BANK_ACCOUNT_TYPE, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.CARD_NUMBER, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.CARD_EXPIRATION, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.CARD_CVC, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.ADDRESS_LINE1, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.ADDRESS_LINE2, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.CITY, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.REGION, FieldState.INIT),
        Pair<PaymentField, FieldState>(PaymentField.POSTAL_CODE, FieldState.INIT)
    )
    private val webServicesProvider = WebServicesProvider()
    private val webSocketRepository = WebsocketRepository(webServicesProvider)
    internal val interactor = WebsocketInteractor(webSocketRepository)
    val configuration = configurationIn
    val payTheoryData = createPayTheoryData(configuration)
    val payTheoryPayment = Payment(
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


    var bankAccountNumber = mutableStateOf(SecureString(""))
    var bankRoutingNumber = mutableStateOf(SecureString(""))
    var bankAccountType = mutableStateOf("")
    var cardNumber = mutableStateOf(SecureString(""))
    var expiration = mutableStateOf(SecureStringWrapper(SecureString(""),null))
    var cvc = mutableStateOf(SecureString(""))
    var nameOnAccount = mutableStateOf(SecureString(""))
    var addressLine1 = mutableStateOf(SecureString(""))
    var addressLine2 = mutableStateOf(SecureString(""))
    var city = mutableStateOf(SecureString(""))
    var region = mutableStateOf(SecureString(""))
    var postalCode = mutableStateOf(SecureString(""))

    /**
     * Function to disconnect WebSocket
     */
    @ExperimentalCoroutinesApi
    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.stopSocket()
            } catch (ex: Exception) {
                onSocketError(ex,"Socket disconnect failed")
            }
            connected = false
            _paymentState.value = PaymentState.Loading
        }
    }

    /**
     * Function to start socket
     * @param handler WebSocket message handler
     */
    @ExperimentalCoroutinesApi
    fun subscribeToSocketEvents(handler: WebsocketMessageHandler, ptTokenResponse:PTTokenResponse, attestationResult:String?) {

        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.startSocket(ptTokenResponse.ptToken, configuration.partnerName, configuration.stageName).consumeEach {
                    if (it.exception == null) {
                        handler.receiveMessage(it.text!!)

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
     * Function to send messages to server
     * @param message message to send to server
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onSocketError(ex: Throwable, reason: String) {
        val error = ex.message.toString()
        interactor.stopSocket()
        // catch errors "Read error: ssl=0x7340b644c8: I/O error during system call", "Software caused connection abort", "null", "Unable to resolve host"
        if (error.contains("Read error: ssl", ignoreCase = true) || error.contains("Software caused connection abort", ignoreCase = true) || error.contains("null", ignoreCase = true) || error.contains("Unable to resolve host", ignoreCase = true)) {

            println("Network Connection Error - Reconnecting...")
            payTheoryPayment.resetSocket()
        } else if (error == "executor rejected") {
        } else { //if error is not ssl error
            payTheoryPayment.context.handleError(PTError(ErrorCode.SocketError,error))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        disconnect()
        super.onCleared()
    }

    fun updateNameOnAccount(value: SecureString) {
        nameOnAccount.value = value
        validateInputs()
    }
    fun updateBankAccountNumber(value: SecureString) {
        bankAccountNumber.value = value
        validateInputs()
    }
    fun updateBankRoutingNumber(value: SecureString) {
        bankRoutingNumber.value = value
        validateInputs()
    }
    fun updateBankAccountType(value: String) {
        bankAccountType.value = value
        validateInputs()
    }
    fun updateCardNumber(value: SecureString) {
        cardNumber.value = value
        validateInputs()
    }
    fun updateExpiration(value: SecureStringWrapper) {

        expiration.value = value
        validateInputs()
    }
    fun updateCvc(value: SecureString) {
        cvc.value = value
        validateInputs()
    }
    fun updateAddressLine1(value: SecureString) {
        addressLine1.value = value
        validateInputs()
    }
    fun updateAddressLine2(value: SecureString) {
        addressLine2.value = value
    }
    fun updateCity(value: SecureString) {
        city.value = value
        validateInputs()
    }
    fun updateRegion(value: SecureString) {
        region.value = value
        validateInputs()
    }
    fun updatePostalCode(value: SecureString) {
        postalCode.value = value
        validateInputs()
    }

    fun submitPayment() {
        var payment: PaymentDetail? = null
        if (_paymentState.value == PaymentState.Processing) {
            payTheoryPayment.context.handleError(PTError(ErrorCode.ActionInProgress,"Payment already in progress"))
            return
        } else if (_paymentState.value is PaymentState.Success) {
            payTheoryPayment.context.handleError(PTError(ErrorCode.ActionComplete,"Payment already completed"))
            return
        } else if (_paymentState.value is PaymentState.Loading) {
            payTheoryPayment.context.handleError(PTError(ErrorCode.InProgress,"No connection available"))
            return
        }
        if (configuration.paymentMethodType == PaymentMethodType.ACH) {
            payment = PaymentDetail(
                timing = System.currentTimeMillis(),
                amount = configuration.amount,
                type = configuration.paymentMethodType.toString(),
                account_type = bankAccountType.value,
                name = nameOnAccount.value.revealForUi(),
                account_number = bankAccountNumber.value.revealForUi(),
                bank_code = bankRoutingNumber.value.revealForUi(),
                fee_mode = configuration.feeMode,
                address = Address(
                    line1 = addressLine1.value.revealForUi(),
                    line2 = addressLine2.value.revealForUi(),
                    city = city.value.revealForUi(),
                    region = region.value.revealForUi(),
                    postal_code = postalCode.value.revealForUi()
                ),
                payorInfo = configuration.payorInfo
            )
        } else if (configuration.paymentMethodType == PaymentMethodType.CARD) {
            payment = PaymentDetail(
                timing = System.currentTimeMillis(),
                amount = configuration.amount,
                type = configuration.paymentMethodType.toString(),
                name = nameOnAccount.value.revealForUi(),
                number = cardNumber.value.revealForUi(),
                security_code = cvc.value.revealForUi(),
                expiration_month = if (expiration.value.secureValue.revealForUi()
                        .isNotBlank()
                ) expiration.value.secureValue.revealForUi().split("/")[0] else null,
                expiration_year = if (expiration.value.secureValue.revealForUi()
                        .isNotBlank()
                ) expiration.value.secureValue.revealForUi().split("/")[1] else null,
                fee_mode = configuration.feeMode,
                address = Address(
                    line1 = addressLine1.value.revealForUi(),
                    line2 = addressLine2.value.revealForUi(),
                    city = city.value.revealForUi(),
                    region = region.value.revealForUi(),
                    postal_code = postalCode.value.revealForUi()
                ),
                payorInfo = configuration.payorInfo
            )
        }
        try {
            payTheoryPayment.transact(payment!!)
        } catch (ex: Exception) {
            onSocketError(ex,"Payment submission failed")
            _paymentState.value = PaymentState.Error(ex.message!!)
            return
        }

        _paymentState.value = PaymentState.Processing

    }

    /**
     * Validates the payment form based on the selected payment method type and configuration.
     *
     * This function checks the validity of various input fields, including:
     * - For ACH payments: Name on account, bank account number, bank routing number, account type, and optionally billing address details (if required by configuration).
     * - For non-ACH payments: Card number, expiration date, CVC, and optionally either billing address or postal code (depending on configuration).
     *
     * The validation results are then used to update the `_paymentState` and `errorMessage`.
     *
     * - `_paymentState`: Set to `PaymentState.ValidAndReady` if all inputs are valid, otherwise `PaymentState.Error("Invalid input")`.
     * - `errorMessage`: Set to an empty string if inputs are valid, otherwise "Invalid input".
     *
     * If the `errorMessage` is not empty, it also calls `payTheoryPayment.context.handleError()` to report the error.
     *
     * Finally, it updates the `isValidAndReady` flag based on the validation result.
     *
     * @throws PTError if the inputs are not valid and it attempts to call `payTheoryPayment.context.handleError()`.
     */
    private fun validateInputs() {
        var isValid = false
        if (configuration.paymentMethodType == PaymentMethodType.ACH) {
            var relevant = mutableListOf(
                isValidNameOnAccount(),
                isValidBankAccountNumber(),
                isValidBankRoutingNumber(),
                isValidAccountType()
            )

            if (configuration.requireBillingAddress) {
                relevant.add(addressLine1.value.revealForUi().isNotBlank())
                relevant.add(city.value.revealForUi().isNotBlank())
                relevant.add(region.value.revealForUi().isNotBlank())
                relevant.add(postalCode.value.revealForUi().isNotBlank())
            }

            isValid = relevant.all { it }

        } else{

            var relevant = mutableListOf(
                isValidCardNumber(),
                isValidExpiration(),
                isValidCvc()
            )

            if (configuration.requireBillingAddress) {
                relevant.add(isValidAccountAddress())
            } else {
                relevant.add(isValidPostalCode())
            }
            isValid = relevant.all { it }


        }


        val derivedPaymentState = when {
            isValid -> PaymentState.ValidAndReady
            else  -> PaymentState.Error("Invalid input")
        }

        errorMessage = if (isValid) {
            ""
        } else {
            "Invalid input"
        }

        if (errorMessage.isNotEmpty()) {

            if (!(derivedPaymentState is PaymentState.Error && _paymentState.value is PaymentState.Error)) {
                payTheoryPayment.context.handleError(PTError(ErrorCode.NotValid, errorMessage))
                _paymentState.value = derivedPaymentState
            }

        } else {
            _paymentState.value = derivedPaymentState
        }

        isValidAndReady = isValid

    }

    /**
     * Propagates the state of a payment field based on its validity and emptiness.
     *
     * This function updates the internal state of a payment field (`paymentFieldState`) and related
     * properties (`paymentFieldValid`, `paymentFieldEmpty`) based on whether the field is valid,
     * empty, or neither. It also notifies the payment context about the state change.
     *
     * @param field The payment field whose state is being propagated.
     * @param isValid `true` if the field's current input is considered valid, `false` otherwise.
     * @param isEmpty `true` if the field is currently empty, `false` otherwise.
     * @return `true` if the field is valid, `false` otherwise (same as the `isValid` input).
     *
     * The function operates based on the following logic:
     * 1. **Empty State:** If `isEmpty` is `true` and the field's current state is not `EMPTY`,
     *    it sets the state to `EMPTY`, marks the field as empty, and notifies the context.
     * 2. **Ready State:** If `isValid` is `true` and the field's current state is not `READY`,
     *    it sets the state to `READY`, marks the field as valid, potentially updates the emptiness status,
     *    and notifies the context.
     * 3. **Invalid State:** If `isEmpty` is `false`, `isValid` is `false`, and the field's current
     *    state is not `INVALID`, it sets the state to `INVALID`, marks the field as invalid,
     *    marks the field as not empty, and notifies the context.
     *
     * The context is notified through the `payTheoryPayment.context.handleStateChange` method,
     * which receives a `Pair` containing the field and its new state.
     *
     * Example Scenarios
     *  - The user clears a field: isEmpty will be true, isValid will be false, state will change to EMPTY.
     *  - The user enters a valid input: isEmpty will be false, isValid will be true, state will change to READY.
     *  - The user enters an invalid input: isEmpty will be false, isValid will be false, state will change to INVALID.
     */
    internal fun propagateState(field: PaymentField, isValid: Boolean, isEmpty: Boolean): Boolean {
        var fieldState: FieldState? = paymentFieldState[field]

        if (isEmpty && fieldState != FieldState.EMPTY) {
            paymentFieldState[field] = FieldState.EMPTY
            paymentFieldEmpty[field] = true
            payTheoryPayment.context.handleStateChange(Pair(field, paymentFieldState[field]!!))
        } else if (fieldState != FieldState.READY && isValid) {
            paymentFieldState[field] = FieldState.READY
            paymentFieldValid[field] = true
            paymentFieldEmpty[field] = isEmpty
            payTheoryPayment.context.handleStateChange(Pair(field, paymentFieldState[field]!!))
        } else if (isEmpty == false && fieldState != FieldState.INVALID && isValid == false) {
            paymentFieldState[field] = FieldState.INVALID
            paymentFieldValid[field] = false
            paymentFieldEmpty[field] = false
            payTheoryPayment.context.handleStateChange(Pair(field, paymentFieldState[field]!!))
        }
        return isValid

    }
    private fun isValidCardNumber(): Boolean {
        return propagateState(PaymentField.CARD_NUMBER, validator.isValidCardNumber(cardNumber.value,), cardNumber.value.stringLength() == 0)
    }
    private fun isValidExpiration(): Boolean {
        return propagateState(PaymentField.CARD_EXPIRATION, validator.isValidExpiration(expiration.value), expiration.value.secureValue.stringLength() == 0)
    }
    private fun isValidCvc(): Boolean {
        return propagateState(PaymentField.CARD_CVC, validator.isValidCvc(cvc.value), cvc.value.stringLength() == 0)
    }
    private fun isValidStreetAddress(): Boolean {
        return propagateState(PaymentField.ADDRESS_LINE1, validator.isNotEmpty(addressLine1.value), addressLine1.value.stringLength() == 0)
    }
    private fun isValidCity(): Boolean {
        return propagateState(PaymentField.CITY, validator.isNotEmpty(city.value), city.value.stringLength() == 0)
    }
    private fun isValidState(): Boolean {
        return propagateState(PaymentField.REGION, validator.isNotEmpty(region.value), region.value.stringLength() == 0)
    }
    private fun isValidPostalCode(): Boolean {
        return propagateState(PaymentField.POSTAL_CODE, validator.isValidPostalCode(postalCode.value), postalCode.value.stringLength() == 0)
    }
    private fun isValidNameOnAccount(): Boolean {
        return propagateState(PaymentField.NAME_ON_ACCOUNT, validator.isNotEmpty(nameOnAccount.value), nameOnAccount.value.stringLength() == 0)
    }
    private fun isValidBankAccountNumber(): Boolean {
        return propagateState(PaymentField.BANK_ACCOUNT_NUMBER, validator.isValidBankAccountNumber(bankAccountNumber.value), bankAccountNumber.value.stringLength() == 0)
    }
    private fun isValidBankRoutingNumber(): Boolean {
        return propagateState(PaymentField.BANK_ROUTING_NUMBER, validator.isValidBankRoutingNumber(bankRoutingNumber.value), bankRoutingNumber.value.stringLength() == 0)
    }
    private fun isValidAccountType(): Boolean {
        return propagateState(PaymentField.BANK_ACCOUNT_TYPE, bankAccountType.value.isNotBlank(), bankAccountType.value.isBlank())
    }
    private fun isValidAccountAddress(): Boolean {
        var relevant = mutableListOf(
            isValidStreetAddress(),
            isValidCity(),
            isValidState(),
            isValidPostalCode()
        )
        return relevant.all { it }
    }


    fun paymentSuccess(result: SuccessfulTransactionResult) {
        _paymentState.value = PaymentState.Success(result.receiptNumber)
    }


    internal fun clearSensitiveData() {
        listOf(
            nameOnAccount, addressLine1, addressLine2,
            city, region, cardNumber, cvc,
            bankAccountNumber, bankRoutingNumber,
             postalCode
        ).forEach {
            it.value.setValue("")
        }
        bankAccountType.value = ""
        expiration.value = SecureStringWrapper(SecureString(""),null)
        for (field in PaymentField.entries) {
            paymentFieldState[field] = FieldState.INIT
            paymentFieldEmpty[field] = false
            paymentFieldValid[field] = true
        }

        clearCount.intValue++
        _paymentState.value = PaymentState.Idle
    }

}