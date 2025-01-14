package com.paytheory.android.sdk.configuration

import android.app.Application
import androidx.lifecycle.AndroidViewModel

/**
 * Class that provides access to configuration view model factory
 * @param application host application
 * @param configurationDetail configuration data
 */
class ConfigurationInjector (application: Application, private val configurationDetail: ConfigurationDetail): AndroidViewModel(application) {

    /**
     * Function that creates Configuration Repository
     */
    private fun getConfigurationRepository(): ConfigurationRepository {
        return ConfigurationRepository(configurationDetail)
    }

    /**
     * Create and return configuration model factory
     */
    fun provideConfigurationViewModelFactory(): ConfigurationModelFactory =
        ConfigurationModelFactory(
            getConfigurationRepository()
        )

}
