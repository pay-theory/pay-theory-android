package com.paytheory.exampleapplication.tests

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.paytheory.android.sdk.configuration.*
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import kotlin.coroutines.CoroutineContext


class ConfigurationTests {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    val configData = ConfigurationDetail("test api key", 1234, false,
        false, PaymentType.CASH, FeeMode.SERVICE_FEE)

    @Test
    fun configurationDetailTest() {
        assert(configData.apiKey == "test api key" && configData.amount == 1234 && !configData.requireAccountName
                && !configData.requireAddress && configData.paymentType == PaymentType.CASH && configData.feeMode == "service_fee")
    }

    @Test
    fun configurationInjectorTest() {

        val configInjector = ConfigurationInjector(Mockito.mock(Application::class.java), configData)

        val configRepo = configInjector.provideConfigurationViewModelFactory()

        assert(configInjector is ConfigurationInjector && configRepo is ConfigurationModelFactory)
    }

    @Test
    fun configurationRepositoryTest() {
        val configRepo = ConfigurationRepository(configData)

        assert(configRepo is ConfigurationRepository)
        assert(configRepo.configuration.value == ConfigurationDetail("test api key", 1234, false,
            false, PaymentType.CASH, FeeMode.SERVICE_FEE))

        configRepo.setConfiguration(ConfigurationDetail("testing api key", 4321, true,
            true, PaymentType.CREDIT, FeeMode.SURCHARGE))

        assert(configRepo.configuration.value == ConfigurationDetail("testing api key", 4321, true,
            true, PaymentType.CREDIT, FeeMode.SURCHARGE))
    }

    @Test
    fun configurationModelFactoryTest() {
        val configFactor = ConfigurationModelFactory(ConfigurationRepository(configData))

        assert(configFactor is ConfigurationModelFactory)
    }

    @Test
    fun configurationViewModelTest() {
        val configViewModel = ConfigurationViewModel(ConfigurationRepository(configData), Dispatchers.Main)

        val context = configViewModel.coroutineContext

        assert(configViewModel is ConfigurationViewModel)
        assert(context is CoroutineContext)
        assert(!configViewModel.configuration.value!!.requireAddress)
        assert(!configViewModel.configuration.value!!.requireAccountName)

        configViewModel.update(ConfigurationDetail("testing api key", 4321, true,
            true, PaymentType.CREDIT, FeeMode.SURCHARGE))

        assert(configViewModel.configuration.value!!.requireAddress)
        assert(configViewModel.configuration.value!!.requireAccountName)

    }
}


