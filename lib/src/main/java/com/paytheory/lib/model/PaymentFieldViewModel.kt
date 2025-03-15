package com.paytheory.lib.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the state of individual payment form fields.
 *
 * This ViewModel maintains separate state flows for each payment field (card details, bank details,
 * and address information). Each field's state is managed independently and can be updated
 * asynchronously as user input is validated.
 *
 * The states are exposed as immutable StateFlows to ensure thread-safe state management and
 * proper reactive updates to the UI.
 */
class PaymentFieldViewModel : ViewModel() {
    /** State flow for the name on account field */
    private val _nameState = MutableStateFlow<FieldState>(FieldState.INIT)
    val nameState = _nameState.asStateFlow()
    
    /**
     * Updates the state of the name field.
     * @param newState The new state to set for the name field
     */
    fun updateNameState(newState: FieldState) {
        viewModelScope.launch {
            _nameState.emit(newState)
        }
    }

    /** State flow for the bank account number field */
    private val _bankAccountState = MutableStateFlow<FieldState>(FieldState.INIT)
    val bankAccountState = _bankAccountState.asStateFlow()
    
    /**
     * Updates the state of the bank account number field.
     * @param newState The new state to set for the bank account number field
     */
    fun updateBankAccountState(newState: FieldState) {
        viewModelScope.launch {
            _bankAccountState.emit(newState)
        }
    }

    /** State flow for the bank routing number field */
    private val _bankRoutingState = MutableStateFlow<FieldState>(FieldState.INIT)
    val bankRoutingState = _bankRoutingState.asStateFlow()
    
    /**
     * Updates the state of the bank routing number field.
     * @param newState The new state to set for the bank routing number field
     */
    fun updateBankRoutingState(newState: FieldState) {
        viewModelScope.launch {
            _bankRoutingState.emit(newState)
        }
    }

    /** State flow for the bank account type field */
    private val _bankAccountTypeState = MutableStateFlow<FieldState>(FieldState.INIT)
    val bankAccountTypeState = _bankAccountTypeState.asStateFlow()
    
    /**
     * Updates the state of the bank account type field.
     * @param newState The new state to set for the bank account type field
     */
    fun updateBankAccountTypeState(newState: FieldState) {
        viewModelScope.launch {
            _bankAccountTypeState.emit(newState)
        }
    }

    /** State flow for the card number field */
    private val _cardNumberState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cardNumberState = _cardNumberState.asStateFlow()
    
    /**
     * Updates the state of the card number field.
     * @param newState The new state to set for the card number field
     */
    fun updateCardNumberState(newState: FieldState) {
        viewModelScope.launch {
            _cardNumberState.emit(newState)
        }
    }

    /** State flow for the card CVC field */
    private val _cardCvcState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cardCvcState = _cardCvcState.asStateFlow()
    
    /**
     * Updates the state of the card CVC field.
     * @param newState The new state to set for the card CVC field
     */
    fun updateCardCvcState(newState: FieldState) {
        viewModelScope.launch {
            _cardCvcState.emit(newState)
        }
    }

    /** State flow for the card expiration field */
    private val _cardExpirationState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cardExpirationState = _cardExpirationState.asStateFlow()
    
    /**
     * Updates the state of the card expiration field.
     * @param newState The new state to set for the card expiration field
     */
    fun updateCardExpirationState(newState: FieldState) {
        viewModelScope.launch {
            _cardExpirationState.emit(newState)
        }
    }

    /** State flow for the address line 1 field */
    private val _addressState = MutableStateFlow<FieldState>(FieldState.INIT)
    val addressState = _addressState.asStateFlow()
    
    /**
     * Updates the state of the address field.
     * @param newState The new state to set for the address field
     */
    fun updateAddressState(newState: FieldState) {
        viewModelScope.launch {
            _addressState.emit(newState)
        }
    }

    /** State flow for the city field */
    private val _cityState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cityState = _cityState.asStateFlow()
    
    /**
     * Updates the state of the city field.
     * @param newState The new state to set for the city field
     */
    fun updateCityState(newState: FieldState) {
        viewModelScope.launch {
            _cityState.emit(newState)
        }
    }

    /** State flow for the region/state field */
    private val _regionState = MutableStateFlow<FieldState>(FieldState.INIT)
    val regionState = _regionState.asStateFlow()
    
    /**
     * Updates the state of the region field.
     * @param newState The new state to set for the region field
     */
    fun updateRegionState(newState: FieldState) {
        viewModelScope.launch {
            _regionState.emit(newState)
        }
    }

    /** State flow for the postal code field */
    private val _postalCodeState = MutableStateFlow<FieldState>(FieldState.INIT)
    val postalCodeState = _postalCodeState.asStateFlow()
    
    /**
     * Updates the state of the postal code field.
     * @param newState The new state to set for the postal code field
     */
    fun updatePostalCodeState(newState: FieldState) {
        viewModelScope.launch {
            _postalCodeState.emit(newState)
        }
    }

    /**
     * Updates the state of a specific payment field.
     *
     * This function serves as a central point for updating the state of any payment field.
     * It routes the update to the appropriate field-specific update function based on the
     * [PaymentField] parameter.
     *
     * @param field The payment field to update
     * @param fieldState The new state to set for the specified field
     */
    fun updateState(field: PaymentField, fieldState: FieldState) {
        when (field) {
            PaymentField.NAME_ON_ACCOUNT -> updateNameState(fieldState)
            PaymentField.BANK_ACCOUNT_NUMBER -> updateBankAccountState(fieldState)
            PaymentField.BANK_ROUTING_NUMBER -> updateBankRoutingState(fieldState)
            PaymentField.CARD_NUMBER -> updateCardNumberState(fieldState)
            PaymentField.CARD_EXPIRATION -> updateCardExpirationState(fieldState)
            PaymentField.CARD_CVC -> updateCardCvcState(fieldState)
            PaymentField.ADDRESS_LINE1 -> updateAddressState(fieldState)
            PaymentField.CITY ->  updateCityState(fieldState)
            PaymentField.REGION ->  updateRegionState(fieldState)
            PaymentField.POSTAL_CODE -> updatePostalCodeState(fieldState)
            PaymentField.BANK_ACCOUNT_TYPE -> updateBankAccountTypeState(fieldState)
            else -> {}
        }
    }
}