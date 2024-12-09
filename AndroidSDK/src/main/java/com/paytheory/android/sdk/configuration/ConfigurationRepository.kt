package com.paytheory.android.sdk.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Class that creates initial configuration data
 * @param initialConfiguration initial configuration data
 */
class ConfigurationRepository(initialConfiguration: ConfigurationDetail) {
    private val configurationDetail = MutableLiveData<ConfigurationDetail>()
    val configuration: LiveData<ConfigurationDetail> get() = configurationDetail
    init {
        configurationDetail.value = initialConfiguration
    }

    /**
     * Function to set ConfigurationDetails
     * @param updatedConfiguration details of configuration to be set
     */
    fun setConfiguration(updatedConfiguration:ConfigurationDetail) {
        configurationDetail.postValue(updatedConfiguration)
    }

}