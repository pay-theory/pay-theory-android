package com.paytheory.android.sdk.configuration

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class ConfigurationViewModel(private val configurationRepository: ConfigurationRepository, private val uiContext: CoroutineContext) : ViewModel(),
    CoroutineScope {

    val configuration: LiveData<ConfigurationDetail> get() = configurationRepository.configuration

    override val coroutineContext: CoroutineContext
        get() = uiContext

    fun update(updatedConfiguration: ConfigurationDetail) {
        configurationRepository.setConfiguration(updatedConfiguration)
    }
}