package com.paytheory.lib.compose

import android.app.Activity
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
import timber.log.Timber

/**
 * A composable function that displays a Google Pay button and manages the Google Pay payment flow.
 *
 * This composable function is responsible for:
 * 1.  **Context Validation:** Ensuring the provided context is a valid Activity, which is required
 *     for launching Google Pay UI elements. If not, it reports an error via the [payable] interface
 *     and terminates early.
 * 2.  **ViewModel Initialization:** Obtaining or creating a [PaymentViewModel] instance associated
 *     with the current composition scope, providing necessary configuration and callbacks.
 * 3.  **Activity Result Handling:** Registering an ActivityResultLauncher to handle the result
 *     returned by the Google Pay payment sheet activity. It processes success (extracting the token),
 *     cancellation, and error scenarios, reporting outcomes via the [payable] interface.
 * 4.  **Google Pay Processor Initialization:** Creating an instance of [GooglePayProcessor] using
 *     [GooglePayFormUtils.createGooglePayProcessor], which encapsulates the logic for interacting
 *     with the Google Pay API. The processor is remembered across recompositions.
 * 5.  **Google Pay Availability Check:** Asynchronously checking if Google Pay is available and
 *     configured correctly on the user's device using [GooglePayFormUtils.checkGooglePayAvailability].
 *     It manages the UI state (`isCheckingAvailability`, `isGooglePayAvailable`) to display appropriate
 *     content (loading indicator, button, or unavailability message).
 * 6.  **Launcher Configuration:** Setting the previously registered ActivityResultLauncher on the
 *     [GooglePayProcessor] instance using a [LaunchedEffect] to ensure it's done after composition.
 * 7.  **UI Rendering:** Displaying UI elements based on the availability check state:
 *     - A loading message while checking.
 *     - The [GooglePayButton] if available, configured with the provided parameters and triggering
 *       the payment flow via [GooglePayFormUtils.initiateGooglePayPayment] on click.
 *     - An informative message if Google Pay is unavailable.
 *
 * This composable integrates tightly with [GooglePayUtil], [GooglePayProcessor], [GooglePayFormUtils],
 * and the [Payable] interface to provide a complete Google Pay experience within a Compose UI.
 *
 * @param payable The [Payable] interface implementation used for reporting payment outcomes (success, failure, errors)
 *                and handling payment start notifications.
 * @param configuration The [PayTheoryConfiguration] object containing essential settings for Pay Theory and
 *                      Google Pay, such as API key, amount, merchant details, button style, and allowed payment methods.
 * @param buttonModifier An optional [Modifier] to customize the layout and appearance of the underlying [GooglePayButton].
 *                       Defaults to an empty [Modifier].
 * @param buttonType The visual style of the Google Pay button (e.g., `PLAIN`, `BUY`). Defaults to the value specified
 *                   in the [configuration].
 * @param buttonColor The color theme of the Google Pay button (e.g., `DARK`, `LIGHT`). Defaults to the value specified
 *                    in the [configuration].
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
                Timber.d("Payment data received successfully")
                result.data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let { paymentData ->
                        Timber.d(paymentData.toString())
                        val token = googlePayUtil.extractPaymentToken(paymentData)
                        Timber.d(token)
                        // TODO: Send token to your backend
                        // TODO: Call payable.handleSuccess(...) or payable.handleError(...)
                    } ?: run {
                        Timber.e("PaymentData.getFromIntent returned null")
                        payable.handleError(PTError(ErrorCode.GooglePayError, "Failed to retrieve payment data"))
                    }
                } ?: run {
                    Timber.e("Result data intent is null")
                    payable.handleError(PTError(ErrorCode.GooglePayError, "Failed to retrieve payment data"))
                }
            }
            Activity.RESULT_CANCELED -> {
                Timber.d("User cancelled the Google Pay flow.")
                // payable.handleError(...) // Optional: Handle cancellation
            }
            AutoResolveHelper.RESULT_ERROR -> {
                val status = AutoResolveHelper.getStatusFromIntent(result.data)
                Timber.e("Google Pay returned an error. Status: ${status?.statusMessage} Code: ${status?.statusCode}")
                payable.handleError(PTError(ErrorCode.GooglePayError, "Google Pay returned an error: ${status?.statusMessage}"))
            }
            else -> {
                Timber.w("Unhandled result code: ${result.resultCode}")
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