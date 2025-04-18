package com.paytheory.lib.compose

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.compose.utility.GooglePayFormUtils
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.data.payable.ErrorCode
import com.paytheory.lib.data.payable.PTError
import com.paytheory.lib.googlepay.GooglePayUtil
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
    val googlePayUtil = GooglePayUtil.getInstance()

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

    // 1. Register the launcher (earlier in the composable)
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // 2. Implement the callback logic here
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d("GooglePayResult", "Payment data received successfully")
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let { paymentData ->
                        Log.d("PaymentData", paymentData.toString())
                        val token = googlePayUtil.extractPaymentToken(paymentData)
                        Log.d("PaymentToken", token)
                        // TODO: Send token to your backend
                        // TODO: Call payable.handleSuccess(...) or payable.handleError(...)
                    } ?: run {
                        Log.e("GooglePayResult", "PaymentData.getFromIntent returned null")
                        payable.handleError(PTError(ErrorCode.GooglePayError, "Failed to retrieve payment data"))
                    }
                } ?: run {
                    Log.e("GooglePayResult", "Result data intent is null")
                    payable.handleError(PTError(ErrorCode.GooglePayError, "Failed to retrieve payment data"))
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.d("GooglePayResult", "User cancelled the Google Pay flow.")
                // payable.handleError(...) // Optional: Handle cancellation
            }
            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(result.data)
                Log.e("GooglePayResult", "Google Pay returned an error. Status: ${status?.statusMessage} Code: ${status?.statusCode}")
                payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay returned an error: ${status?.statusMessage}"))
            }
            else -> {
                Log.w("GooglePayResult", "Unhandled result code: ${result.resultCode}")
                payable.handleError(PTError(ErrorCode.GooglePayError, "Unknown Google Pay result"))
            }
        }
    }

    // Create Google Pay processor using utility
    val googlePayProcessor = remember {
        GooglePayFormUtils.createGooglePayProcessor(payable, activity!!, configuration, viewModel)
    }

    // We need to observe Google Pay availability with a state
    var isGooglePayAvailable by remember { mutableStateOf(false) }
    var isCheckingAvailability by remember { mutableStateOf(true) }

    // Update the state when availability check completes
    GooglePayFormUtils.checkGooglePayAvailability(googlePayProcessor) { available ->
        isGooglePayAvailable = available
        isCheckingAvailability = false
    }

    LaunchedEffect(googlePayProcessor, activityLauncher) {
        googlePayProcessor.setActivityLauncher(activityLauncher)
    }    

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            isCheckingAvailability -> {
                // Checking availability state
                Text("Checking Google Pay availability...")
            }

            isGooglePayAvailable -> {
                // Google Pay is available, show button
                GooglePayButton(
                    onClick = { GooglePayFormUtils.initiateGooglePayPayment(googlePayProcessor) },
                    enabled = true,
                    modifier = buttonModifier,
                    buttonType = buttonType,
                    buttonColor = buttonColor
                )
            }

            else -> {
                // Google Pay is not available
                Text("Google Pay is not available on this device")
            }
        }
    }
}