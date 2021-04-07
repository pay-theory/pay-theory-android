package com.paytheory.android.sdk.configuration

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class ConfigurationInjector (application: Application, private val configurationDetail: ConfigurationDetail): AndroidViewModel(application) {

    private fun getConfigurationRepository(): ConfigurationRepository {
        return ConfigurationRepository(configurationDetail)
    }

    fun provideConfigurationViewModelFactory(): ConfigurationModelFactory =
        ConfigurationModelFactory(
            getConfigurationRepository()
        )

}
