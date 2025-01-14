package com.paytheory.android.sdk.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Class that creates initial configuration data
 * @param initialConfiguration initial configuration data
 */
class ConfigurationRepository(initialConfiguration: ConfigurationDetail) {
    /**
     * Mutable live data for configuration details
     */
    private val configurationDetail = MutableLiveData<ConfigurationDetail>()
    /**
     * Live data for configuration details
     */
    val configuration: LiveData<ConfigurationDetail> get() = configurationDetail

    /**
     * Initializer for ConfigurationRepository
     */
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