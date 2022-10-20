package com.paytheory.android.sdk.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers

/**
 * Class that creates a view model provider
 * @param repo configuration repository used to create view model provider
 */
class ConfigurationModelFactory(
    private val repo: ConfigurationRepository
): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ConfigurationViewModel(repo, Dispatchers.Main) as T
    }

}