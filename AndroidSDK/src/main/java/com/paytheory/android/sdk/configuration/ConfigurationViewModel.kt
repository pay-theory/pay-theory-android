package com.paytheory.android.sdk.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * ViewModel class that manages live configuration data for the PayTheory SDK.
 *
 * This class provides access to the current configuration details and allows
 * updating the configuration data.
 *
 * @param configurationRepository The repository responsible for managing configuration data.
 * @param uiContext The coroutine context for UI operations.
 */
class ConfigurationViewModel(private val configurationRepository: ConfigurationRepository, private val uiContext: CoroutineContext) : ViewModel(),
    CoroutineScope {

    /**
     * LiveData object providing access to the current configuration details.
     */
    val configuration: LiveData<ConfigurationDetail> get() = configurationRepository.configuration

    /**
     * The coroutine context used for UI operations.
     */
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