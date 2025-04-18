package com.paytheory.qualitydemo


import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssuredWorkload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.GeneratingTokens
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paytheory.lib.PayableActivity
import com.paytheory.lib.Payable
import com.paytheory.lib.data.payable.BarcodeResult
import com.paytheory.lib.data.payable.FailedTransactionResult
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.data.payable.PaymentMethodTokenResults
import com.paytheory.lib.data.payable.SuccessfulTransactionResult
import com.paytheory.lib.model.FieldState
import com.paytheory.lib.model.PaymentField
import com.paytheory.lib.model.PaymentFieldViewModel
import com.paytheory.lib.model.PaymentProcessViewModel
import com.paytheory.lib.model.PaymentResultState
import com.paytheory.qualitydemo.compose.PaymentBank
import com.paytheory.qualitydemo.compose.PaymentCard
import com.paytheory.qualitydemo.compose.PaymentCardRotate
import com.paytheory.qualitydemo.compose.TokenizeBank
import com.paytheory.qualitydemo.compose.TokenizeCard
import com.paytheory.qualitydemo.ui.theme.JetsnacksampleTheme


/**
 * Main Activity for the Simple Compose app.
 * This activity demonstrates how to integrate the PayTheory payment library into a Compose application.
 * It implements the [Payable] interface to handle payment events and uses ViewModels to manage payment state.
 */
class MainActivity : PayableActivity() {
    /**
     * ViewModel for managing the payment process state.
     */
    private val paymentProcessModel: PaymentProcessViewModel by viewModels()
    /**
     * ViewModel for managing the state of individual payment fields.
     */
    private val paymentFieldModel: PaymentFieldViewModel by viewModels()

    sealed class Screens(val route : String) {
        object TokenizeCard : Screens("tokenize_card_route")
        object TokenizeBank : Screens("tokenize_bank_route")
        object PaymentCard : Screens("payment_card_route")
        object PaymentBank : Screens("payment_bank_route")
        object PaymentCardRotate : Screens("payment_rotate_route")
    }

    data class BottomNavigationItem(
        val label : String = "",
        val icon : ImageVector = Icons.Filled.GeneratingTokens,
        val route : String = ""
    ) {

        //function to get the list of bottomNavigationItems
        fun bottomNavigationItems() : List<BottomNavigationItem> {
            return listOf(
                BottomNavigationItem(
                    label = "Tokenize Card",
                    icon = Icons.Filled.CreditScore,
                    route = Screens.TokenizeCard.route,

                ),
                BottomNavigationItem(
                    label = "Tokenize Bank",
                    icon = Icons.Filled.AssuredWorkload,
                    route = Screens.TokenizeBank.route
                ),
                BottomNavigationItem(
                    label = "Card Payment",
                    icon = Icons.Filled.CreditCard,
                    route = Screens.PaymentCard.route
                ),
                BottomNavigationItem(
                    label = "Bank Payment",
                    icon = Icons.Filled.AccountBalance,
                    route = Screens.PaymentBank.route
                ),
                BottomNavigationItem(
                    label = "Card Payment Rotate",
                    icon = Icons.Filled.ScreenRotation,
                    route = Screens.PaymentCardRotate.route
                ),
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JetsnacksampleTheme {
                var navigationSelectedItem by remember {
                    mutableIntStateOf(0)
                }
                var processState = paymentProcessModel.paymentState.collectAsState()
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            //getting the list of bottom navigation items for our data class
                            BottomNavigationItem().bottomNavigationItems().forEachIndexed {index,navigationItem ->

                                //iterating all items with their respective indexes
                                NavigationBarItem(
                                    selected = index == navigationSelectedItem,
                                    alwaysShowLabel = false,
                                    label = {
                                        Text(navigationItem.label)
                                    },
                                    icon = {
                                        Icon(
                                            navigationItem.icon,
                                            contentDescription = navigationItem.label
                                        )
                                    },
                                    onClick = {
                                        navigationSelectedItem = index
                                        navController.navigate(navigationItem.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    enabled = processState.value !in listOf(PaymentResultState.Processing)
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    Column {
                        NavHost(
                            navController = navController,
                            startDestination = Screens.TokenizeCard.route,
                            modifier = Modifier.padding(paddingValues = paddingValues)) {
                            composable(Screens.TokenizeCard.route) {
                                TokenizeCard(
                                    payable = this@MainActivity,
                                    processModel = paymentProcessModel, // Pass ViewModel
                                    fieldModel = paymentFieldModel, // Pass ViewModel
                                )
                            }
                            composable(Screens.TokenizeBank.route) {
                                TokenizeBank(
                                    payable = this@MainActivity,
                                    processModel = paymentProcessModel, // Pass ViewModel
                                    fieldModel = paymentFieldModel, // Pass ViewModel
                                )
                            }
                            composable(Screens.PaymentCard.route) {
                                PaymentCard(
                                    payable = this@MainActivity,
                                    processModel = paymentProcessModel, // Pass ViewModel
                                    fieldModel = paymentFieldModel, // Pass ViewModel
                                )
                            }
                            composable(Screens.PaymentBank.route) {
                                PaymentBank(
                                    payable = this@MainActivity,
                                    processModel = paymentProcessModel, // Pass ViewModel
                                    fieldModel = paymentFieldModel, // Pass ViewModel
                                )
                            }
                            composable(Screens.PaymentCardRotate.route) {
                                PaymentCardRotate(
                                    payable = this@MainActivity,
                                    processModel = paymentProcessModel, // Pass ViewModel
                                    fieldModel = paymentFieldModel, // Pass ViewModel
                                )
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * Called when a payment transaction is successful.
     * Updates the payment process ViewModel with the success state.
     * @param successfulTransactionResult The result of the successful transaction.
     */
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        paymentProcessModel.updateState(PaymentResultState.Success(successfulTransactionResult))
        Log.d("MainActivity", "handleSuccess: $successfulTransactionResult")
    }

    /**
     * Called when a payment transaction fails.
     * Updates the payment process ViewModel with the failure state.
     * @param failedTransactionResult The result of the failed transaction.
     */
    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        paymentProcessModel.updateState(PaymentResultState.Failure(failedTransactionResult))
        Log.d("MainActivity", "handleFailure: $failedTransactionResult")
    }

    /**
     * Called when a tokenization is successful.
     * Updates the payment process ViewModel with the token success state.
     * @param paymentMethodToken The tokenized payment method result.
     */
    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        paymentProcessModel.updateState(PaymentResultState.TokenSuccess(paymentMethodToken))
        Log.d("MainActivity", "handleTokenizeSuccess: $paymentMethodToken")
    }

    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        paymentProcessModel.updateState(PaymentResultState.BarcodeSuccess(barcodeResult))
        Log.d("MainActivity", "handleBarcodeSuccess: $barcodeResult")
    }

    /**
     * Called when an error occurs during the payment process.
     * Updates the payment process ViewModel with the error state.
     * @param error The PTError that occurred.
     */
    override fun handleError(error: PTError) {
        paymentProcessModel.updateState(PaymentResultState.Error(error))
        Log.d("MainActivity", "handleError: $error")
    }

    /**
     * Called when the payment form is ready for input.
     * Updates the payment process ViewModel to the idle state.
     * @param isReady A boolean indicating whether the form is ready.
     */
    override fun handleReady(isReady: Boolean) {
        paymentProcessModel.updateState(PaymentResultState.Idle)
        Log.d("MainActivity", "handleReady: $isReady")
    }

    /**
     * Called when a payment transaction starts.
     * Updates the payment process ViewModel to the loading state.
     * @param paymentType The type of payment being processed.
     */
    override fun handlePaymentStart(paymentType: String) {

        paymentProcessModel.updateState(PaymentResultState.Processing)

        Log.d("MainActivity", "handlePaymentStart: $paymentType")
    }

    /**
     * Called when a tokenization process starts.
     * Logs the start of the tokenization process.
     * @param paymentType The type of payment being tokenized.
     */
    override fun handleTokenStart(paymentType: String) {
        paymentProcessModel.updateState(PaymentResultState.Processing)
        Log.d("MainActivity", "handleTokenStart: $paymentType")
    }

    /**
     * Called when the state of a payment field changes.
     * Updates the payment field ViewModel with the new state.
     * @param fieldState A pair of the payment field and its new state.
     */

    override fun handleStateChange(fieldState: Pair<PaymentField, FieldState>) {
        paymentFieldModel.updateState(fieldState.first, fieldState.second)
    }
}

