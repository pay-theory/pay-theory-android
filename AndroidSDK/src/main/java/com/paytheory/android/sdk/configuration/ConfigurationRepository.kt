package com.paytheory.android.sdk.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ConfigurationRepository(initialConfiguration: ConfigurationDetail) {
    protected val configurationDetail = MutableLiveData<ConfigurationDetail>()
    val configuration: LiveData<ConfigurationDetail> get() = configurationDetail
    init {
        configurationDetail.value = initialConfiguration
    }

    /**
     * Function to set ConfigurationDetails
     * @param configurationDetail details of configuration to be set
     */
    fun setConfiguration(updatedConfiguration:ConfigurationDetail) {
        configurationDetail.postValue(updatedConfiguration)
    }

}