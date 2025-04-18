package com.paytheory.lib.compose

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.compose.utility.GooglePayFormUtils
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.model.PaymentViewModel

/**
 * A standalone Google Pay button that handles the complete payment flow.
 *
 * This composable provides a Google Pay button that checks for Google Pay availability,
 * handles the payment flow, and processes the transaction. It's designed to be used
 * independently outside of PaymentForm when only Google Pay payments are desired.
 *
 * The component provides a simple way to integrate Google Pay into any UI without
 * needing to manage the state or processing logic directly.
 *
 * @param payable The Payable interface implementation for payment callbacks
 * @param configuration Pay Theory configuration with Google Pay settings
 * @param modifier Modifier to apply to the Google Pay button
 * @param hideWhenUnavailable Whether to hide the button when Google Pay is unavailable
 * @param onUnavailable Optional callback when Google Pay is unavailable
 * @param buttonType The type of Google Pay button to display
 * @param buttonColor The color scheme of the Google Pay button
 */
@Composable
fun StandaloneGooglePayButton(
    payable: Payable,
    configuration: PayTheoryConfiguration,
    modifier: Modifier = Modifier,
    hideWhenUnavailable: Boolean = false,
    onUnavailable: (() -> Unit)? = null,
    buttonType: GooglePayButtonType = configuration.googlePayButtonType,
    buttonColor: GooglePayButtonColor = configuration.googlePayButtonColor
) {
    // Validate configuration using utility
    try {
        GooglePayFormUtils.validateGooglePayConfiguration(configuration)
    } catch (e: IllegalStateException) {
        throw e
    }

    // Get local context and cast to Activity for Google Pay processing
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Validate context using utility
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
    
    // State for Google Pay availability
    var isGooglePayAvailable by remember { mutableStateOf(false) }
    var isCheckingAvailability by remember { mutableStateOf(true) }
    
    // Check Google Pay availability using utility
    LaunchedEffect(Unit) {
        GooglePayFormUtils.checkGooglePayAvailability(googlePayProcessor) { available ->
            isGooglePayAvailable = available
            isCheckingAvailability = false
            
            // If Google Pay is unavailable and an onUnavailable callback was provided, invoke it
            if (!available) {
                onUnavailable?.invoke()
            }
        }
    }
    
    // Only show the button if Google Pay is available or if we're not hiding when unavailable
    if (!isCheckingAvailability && (isGooglePayAvailable || !hideWhenUnavailable)) {
        GooglePayButton(
            onClick = { GooglePayFormUtils.initiateGooglePayPayment(googlePayProcessor) },
            enabled = isGooglePayAvailable,
            modifier = modifier.padding(vertical = 4.dp),
            buttonType = buttonType,
            buttonColor = buttonColor
        )
    }
} 