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

    /**
     * Function that creates a configuration view model
     * @param modelClass class that view model is being created for
     * @return returns configuration view model
     * @Suppress warns of unchecked cast
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ConfigurationViewModel(repo, Dispatchers.Main) as T
    }

}