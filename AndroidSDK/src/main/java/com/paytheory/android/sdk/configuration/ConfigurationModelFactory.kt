package com.paytheory.android.sdk.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers

class ConfigurationModelFactory(
    private val repo: ConfigurationRepository
): ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return ConfigurationViewModel(repo, Dispatchers.Main) as T
    }

}