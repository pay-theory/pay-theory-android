package com.paytheory.lib.compose

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.compose.utility.GooglePayFormUtils
import com.paytheory.lib.model.PaymentViewModel

/**
 * A Google Pay payment form that provides a Google Pay button and handles payment processing.
 *
 * This composable checks for Google Pay availability on the device and displays a Google Pay
 * button if available. It manages the entire Google Pay payment flow, from availability checking
 * to payment processing.
 *
 * @param payable The Payable interface implementation for payment callbacks
 * @param configuration Pay Theory configuration with Google Pay settings
 * @param buttonModifier Modifier to apply to the Google Pay button
 * @param buttonType The type of Google Pay button to display
 * @param buttonColor The color scheme of the Google Pay button
 */
@Composable
fun GooglePayForm(
    payable: Payable,
    configuration: PayTheoryConfiguration,
    buttonModifier: Modifier = Modifier,
    buttonType: GooglePayButtonType = configuration.googlePayButtonType,
    buttonColor: GooglePayButtonColor = configuration.googlePayButtonColor
) {
    // Get local context and cast to Activity for Google Pay processing
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Use utilities to validate context and check Google Pay availability
    if (!GooglePayFormUtils.validateGooglePayContext(activity, payable)) {
        return
    }
    
    // Get or create payment view model
    val packageName = context.applicationContext.packageName
    val viewModel: PaymentViewModel = viewModel {
        PaymentViewModel(
            packageName,
            configuration,
            payable
        )
    }
    
    // Create Google Pay processor using utility
    val googlePayProcessor = remember {
        GooglePayFormUtils.createGooglePayProcessor(payable, activity!!, configuration, viewModel)
    }

// This is not necessary when using GooglePayButton
//    // We need to observe Google Pay availability with a state
//    var isGooglePayAvailable by remember { mutableStateOf(false) }
//    var isCheckingAvailability by remember { mutableStateOf(true) }
//
//    // Update the state when availability check completes
//    GooglePayFormUtils.checkGooglePayAvailability(googlePayProcessor) { available ->
//        isGooglePayAvailable = available
//        isCheckingAvailability = false
//    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GooglePayButton(
            onClick = { GooglePayFormUtils.initiateGooglePayPayment(googlePayProcessor) },
            enabled = true,
            modifier = buttonModifier,
            buttonType = buttonType,
            buttonColor = buttonColor
        )
//        when {
//            isCheckingAvailability -> {
//                // Checking availability state
//                Text("Checking Google Pay availability...")
//            }
//            isGooglePayAvailable -> {
//                // Google Pay is available, show button
//                GooglePayButton(
//                    onClick = { GooglePayFormUtils.initiateGooglePayPayment(googlePayProcessor) },
//                    enabled = true,
//                    modifier = buttonModifier,
//                    buttonType = buttonType,
//                    buttonColor = buttonColor
//                )
//            }
//            else -> {
//                // Google Pay is not available
//                Text("Google Pay is not available on this device")
//            }
//        }
    }
} 