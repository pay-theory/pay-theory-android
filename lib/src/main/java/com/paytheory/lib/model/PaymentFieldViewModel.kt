package com.paytheory.lib.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paytheory.lib.model.PaymentViewModel.FieldState
import com.paytheory.lib.model.PaymentViewModel.PaymentField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentFieldViewModel : ViewModel() {
    private val _nameState = MutableStateFlow<FieldState>(FieldState.INIT)
    val nameState = _nameState.asStateFlow()
    fun updateNameState(newState: FieldState) {
        viewModelScope.launch {
            _nameState.emit(newState)
        }
    }

    private val _bankAccountState = MutableStateFlow<FieldState>(FieldState.INIT)
    val bankAccountState = _bankAccountState.asStateFlow()
    fun updateBankAccountState(newState: FieldState) {
        viewModelScope.launch {
            _bankAccountState.emit(newState)
        }
    }


    private val _bankRoutingState = MutableStateFlow<FieldState>(FieldState.INIT)
    val bankRoutingState = _bankRoutingState.asStateFlow()
    fun updateBankRoutingState(newState: FieldState) {
        viewModelScope.launch {
            _bankRoutingState.emit(newState)
        }
    }

    private val _bankAccountTypeState = MutableStateFlow<FieldState>(FieldState.INIT)
    val bankAccountTypeState = _bankAccountTypeState.asStateFlow()
    fun updateBankAccountTypeState(newState: FieldState) {
        viewModelScope.launch {
            _bankAccountTypeState.emit(newState)
        }
    }

    private val _cardNumberState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cardNumberState = _cardNumberState.asStateFlow()
    fun updateCardNumberState(newState: FieldState) {
        viewModelScope.launch {
            _cardNumberState.emit(newState)
        }
    }

    private val _cardCvcState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cardCvcState = _cardCvcState.asStateFlow()
    fun updateCardCvcState(newState: FieldState) {
        viewModelScope.launch {
            _cardCvcState.emit(newState)
        }
    }

    private val _cardExpirationState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cardExpirationState = _cardExpirationState.asStateFlow()
    fun updateCardExpirationState(newState: FieldState) {
        viewModelScope.launch {
            _cardExpirationState.emit(newState)
        }
    }

    private val _addressState = MutableStateFlow<FieldState>(FieldState.INIT)
    val addressState = _addressState.asStateFlow()
    fun updateAddressState(newState: FieldState) {
        viewModelScope.launch {
            _addressState.emit(newState)
        }
    }

    private val _cityState = MutableStateFlow<FieldState>(FieldState.INIT)
    val cityState = _cityState.asStateFlow()
    fun updateCityState(newState: FieldState) {
        viewModelScope.launch {
            _cityState.emit(newState)
        }
    }

    private val _regionState = MutableStateFlow<FieldState>(FieldState.INIT)
    val regionState = _regionState.asStateFlow()
    fun updateRegionState(newState: FieldState) {
        viewModelScope.launch {
            _regionState.emit(newState)
        }
    }

    private val _postalCodeState = MutableStateFlow<FieldState>(FieldState.INIT)
    val postalCodeState = _postalCodeState.asStateFlow()
    fun updatePostalCodeState(newState: FieldState) {
        viewModelScope.launch {
            _postalCodeState.emit(newState)
        }
    }


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