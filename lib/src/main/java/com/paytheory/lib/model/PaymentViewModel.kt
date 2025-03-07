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
import com.paytheory.lib.websocket.SocketUpdate
import com.paytheory.lib.websocket.WebServicesProvider
import com.paytheory.lib.websocket.WebsocketMessageHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PaymentViewModel @Inject constructor(packageName:String, configurationIn: PayTheoryConfiguration, payable: Payable) :ViewModel() {
    val webServicesProvider = WebServicesProvider()
    val webSocketRepository = WebsocketRepository(webServicesProvider)
    val interactor = WebsocketInteractor(webSocketRepository)

    val configuration = configurationIn
    val payTheoryData = createPayTheoryData(configuration)

    val payTheoryPayment = Payment(
        packageName,
        payable,
        payTheoryData,
        configurationIn,
        this
    )

    var validator = Validator()

    // region State Management
    sealed class PaymentState {
        object Idle : PaymentState()
        object ValidAndReady : PaymentState()
        object Processing : PaymentState()
        object Loading : PaymentState()
        data class Success(val paymentToken: String) : PaymentState()
        data class Error(val errorMessage: String) : PaymentState()
    }
    var clearCount = mutableIntStateOf(0)  // Add this line


    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState
    // endregion

    var isValidAndReady = false
    var errorMessage: String = ""

    // region Secure Data Holders
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
    // endregion

    var connected: Boolean = false

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

    val PaymentFieldState: HashMap<PaymentField, Boolean> = hashMapOf(
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



    /**
     * Function to disconnect WebSocket
     */
    @ExperimentalCoroutinesApi
    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactor.stopSocket()
            } catch (ex: Exception) {
                onSocketError(ex)
            }
            connected = false
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
                        onSocketError(it.exception)
                        connected = false
                    }
                }
            } catch (ex: Exception) {
                onSocketError(ex)
                connected = false
            }
        }
        connected = true
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
                onSocketError(ex)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun onSocketError(ex: Throwable) {
        val error = ex.message.toString()
        interactor.stopSocket()
        // catch errors "Read error: ssl=0x7340b644c8: I/O error during system call", "Software caused connection abort", "null", "Unable to resolve host"
        if (error.contains("Read error: ssl", ignoreCase = true) || error.contains("Software caused connection abort", ignoreCase = true) || error.contains("null", ignoreCase = true) || error.contains("Unable to resolve host", ignoreCase = true)){
//            if (payTheoryPayment != null){ // error for transaction request
//                if (payTheoryPayment is Payable){
                    println("Network Connection Error - Reconnecting...")
                    payTheoryPayment.resetSocket()
//                }
//            } else if (paymentMethodToken != null){
//                if (paymentMethodToken.context is Payable){
//                    println("Network Connection Error - Reconnecting...")
//                    paymentMethodToken.resetSocket()
//                }
//            }
        } else { //if error is not ssl error
            // error for transaction request
//            if (payment != null) {
                println("Error: $error")
                payTheoryPayment.context.handleError(PTError(ErrorCode.SocketError,error))

                // error for tokenization request
//            } else if (paymentMethodToken != null){
//                println("Error: $error")
//                paymentMethodToken.context.handleError(PTError(ErrorCode.TokenFailed,error))
//            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCleared() {
        disconnect()
        super.onCleared()
    }

    // region Public Setters
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
    // endregion

    // region Core Logic


    fun submitPayment() {
        var payment: PaymentDetail? = null
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
            payTheoryPayment.transact(payment!!)
            _paymentState.value = PaymentState.Processing
//        }
//        if (!isValidInput()) return

//        viewModelScope.launch {
//            try {
//                _paymentState.value = PaymentState.Loading
//
////                val encryptedBundle = createEncryptedBundle()
////                val response = apiService.tokenize(encryptedBundle)
////
////                handleResponse(response)
//            } catch (e: Exception) {
//                handleError(e)
//            } finally {
//                clearSensitiveData()
//            }
//        }
    }
    // endregion

    // region Security & Validation


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
        _paymentState.value = when {
            isValid -> PaymentState.ValidAndReady
            else -> PaymentState.Error("Invalid input")
        }

        errorMessage = if (isValid) {
            ""
        } else {
            "Invalid input"
        }

        isValidAndReady = isValid

    }

    private fun propagateState(field: PaymentField, isValid: Boolean): Boolean {
        if (PaymentFieldState.contains(field) && PaymentFieldState[field] != isValid) {
            PaymentFieldState[field] = isValid
            payTheoryPayment.context.handleStateChange(Pair(field, PaymentFieldState.get(field)!!))
            return PaymentFieldState[field]!!
        }
        else
            return isValid

    }

    private fun isValidCardNumber(): Boolean {
        return propagateState(PaymentField.CARD_NUMBER, validator.isValidCardNumber(cardNumber.value))
    }

    private fun isValidExpiration(): Boolean {
        return propagateState(PaymentField.CARD_EXPIRATION, validator.isValidExpiration(expiration.value))
    }
    private fun isValidCvc(): Boolean {
        return propagateState(PaymentField.CARD_CVC, validator.isValidCvc(cvc.value))
    }

    private fun isValidStreetAddress(): Boolean {
        return propagateState(PaymentField.ADDRESS_LINE1, validator.isNotEmpty(addressLine1.value))
    }

    private fun isValidCity(): Boolean {
        return propagateState(PaymentField.CITY, validator.isNotEmpty(city.value))
    }
    private fun isValidState(): Boolean {
        return propagateState(PaymentField.REGION, validator.isNotEmpty(region.value))
    }
    private fun isValidPostalCode(): Boolean {
        return propagateState(PaymentField.POSTAL_CODE, validator.isValidPostalCode(postalCode.value))
    }

    private fun isValidNameOnAccount(): Boolean {
        return propagateState(PaymentField.NAME_ON_ACCOUNT, validator.isNotEmpty(nameOnAccount.value))
    }

    private fun isValidBankAccountNumber(): Boolean {
        return propagateState(PaymentField.BANK_ACCOUNT_NUMBER, validator.isValidBankAccountNumber(bankAccountNumber.value))
    }

    private fun isValidBankRoutingNumber(): Boolean {
        return propagateState(PaymentField.BANK_ROUTING_NUMBER, validator.isValidBankRoutingNumber(bankRoutingNumber.value))
    }

    private fun isValidAccountType(): Boolean {
        return propagateState(PaymentField.BANK_ACCOUNT_TYPE, bankAccountType.value.isNotBlank())
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


    // endregion



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
//        expiration.value = TextFieldValue("")
        clearCount.intValue++
        _paymentState.value = PaymentState.Idle
    }
    // endregion


    // endregion

    // endregion
}

/**
 * Creates WebSocket interactor to start, stop and send messages
 * @param repository WebSocket repository
 */
class WebsocketInteractor(private val repository: WebsocketRepository) {

    /**
     * Function to close WebSocket
     */
    @ExperimentalCoroutinesApi
    fun stopSocket() {
        repository.closeSocket()
    }

    /**
     * Function to send messages though a WebSocket
     */
    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        repository.sendMessage(message)
    }

    /**
     * Function to start WebSocket
     * @param ptToken token used for security
     */
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String, partner: String, stage: String): Channel<SocketUpdate> = repository.startSocket(ptToken, partner, stage)

}

/**
 * Creates WebSocket repository to start, stop and send messages
 * @param webServicesProvider WebSocket web services provider
 */
class WebsocketRepository(private val webServicesProvider: WebServicesProvider) {

    /**
     * Function to start WebSocket
     * @param ptToken token used for security
     */
    @ExperimentalCoroutinesApi
    fun startSocket(ptToken:String, partner: String, stage: String): Channel<SocketUpdate> =
        webServicesProvider.startSocket(ptToken, partner, stage)

    /**
     * Function to send messages though a WebSocket
     */
    @ExperimentalCoroutinesApi
    fun sendMessage(message:String) {
        webServicesProvider.sendMessage(message)
    }

    /**
     * Function to close WebSocket
     */
    @ExperimentalCoroutinesApi
    fun closeSocket() {
        webServicesProvider.stopSocket()
    }
}