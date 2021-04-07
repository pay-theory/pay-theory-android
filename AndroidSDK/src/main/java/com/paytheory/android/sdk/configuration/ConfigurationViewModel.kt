package com.paytheory.android.sdk.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Class that manages live configuration data
 * @param configurationRepository configuration data
 * @param uiContext application user interface context
 */
class ConfigurationViewModel(private val configurationRepository: ConfigurationRepository, private val uiContext: CoroutineContext) : ViewModel(),
    CoroutineScope {

    val configuration: LiveData<ConfigurationDetail> get() = configurationRepository.configuration

    override val coroutineContext: CoroutineContext
        get() = uiContext

    /**
     * Function that updates the repository data
     * @param updatedConfiguration configuration data to be updated
     */
    fun update(updatedConfiguration: ConfigurationDetail) {
        configurationRepository.setConfiguration(updatedConfiguration)
    }
}