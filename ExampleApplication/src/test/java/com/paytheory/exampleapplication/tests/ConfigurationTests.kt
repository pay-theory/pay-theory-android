package com.paytheory.exampleapplication.tests

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.paytheory.android.sdk.configuration.*
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import kotlin.coroutines.CoroutineContext

/**
 * Class to test configuration class
 */
class ConfigurationTests {

    val apiKey = "test api key"
    val amountOne = 1234
    val amountTwo = 4321
    val requireAccountName = false
    val requireAddress = false
    val feeMode = "service_fee"

    @get:Rule
    val rule = InstantTaskExecutorRule()

    val configData = ConfigurationDetail(apiKey, amountOne, requireAccountName,
        requireAddress, PaymentType.CASH, FeeMode.SERVICE_FEE)

    /**
     * Function to test configuration detail
     */
    @Test
    fun configurationDetailTest() {
        assert(configData.apiKey == apiKey && configData.amount == amountOne && !configData.requireAccountName
                && !configData.requireAddress && configData.paymentType == PaymentType.CASH && configData.feeMode == feeMode)
    }

    /**
     * Function to test configuration injector
     */
    @Test
    fun configurationInjectorTest() {

        val configInjector = ConfigurationInjector(Mockito.mock(Application::class.java), configData)

        val configRepo = configInjector.provideConfigurationViewModelFactory()

        assert(configInjector is ConfigurationInjector && configRepo is ConfigurationModelFactory)
    }

    /**
     * Function to test configuration repository
     */
    @Test
    fun configurationRepositoryTest() {
        val configRepo = ConfigurationRepository(configData)

        assert(configRepo is ConfigurationRepository)
        assert(configRepo.configuration.value == ConfigurationDetail(apiKey, amountOne, requireAccountName,
            requireAddress, PaymentType.CASH, FeeMode.SERVICE_FEE))

        configRepo.setConfiguration(ConfigurationDetail(apiKey, amountTwo, requireAccountName,
            requireAddress, PaymentType.CREDIT, FeeMode.SURCHARGE))

        assert(configRepo.configuration.value == ConfigurationDetail(apiKey, amountTwo, requireAccountName,
            requireAddress, PaymentType.CREDIT, FeeMode.SURCHARGE))
    }

    /**
     * Function to test configuration factory
     */
    @Test
    fun configurationModelFactoryTest() {
        val configFactor = ConfigurationModelFactory(ConfigurationRepository(configData))

        assert(configFactor is ConfigurationModelFactory)
    }

    /**
     * Function to test configuration view model
     */
    @Test
    fun configurationViewModelTest() {
        val configViewModel = ConfigurationViewModel(ConfigurationRepository(configData), Dispatchers.Main)

        val context = configViewModel.coroutineContext

        assert(configViewModel is ConfigurationViewModel)
        assert(context is CoroutineContext)
        assert(!configViewModel.configuration.value!!.requireAddress)
        assert(!configViewModel.configuration.value!!.requireAccountName)

        configViewModel.update(ConfigurationDetail(apiKey, amountTwo, requireAccountName,
            requireAddress, PaymentType.CREDIT, FeeMode.SURCHARGE))

        assert(configViewModel.configuration.value!!.requireAddress)
        assert(configViewModel.configuration.value!!.requireAccountName)

    }
}


