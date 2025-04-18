# Pay Theory Android SDK - Google Pay Integration

## Overview

Google Pay integration in the Pay Theory Android SDK provides a modern, secure payment option for Android applications. This guide outlines how to implement Google Pay in your application using our SDK, best practices, configuration options, and sample implementations.

## Key Features

- Seamless Google Pay payment processing
- Secure tokenization of payment information
- Customizable Google Pay button
- Comprehensive error handling
- Easy integration with existing Pay Theory payment flows

## Prerequisites

Before implementing Google Pay with the Pay Theory SDK:

1. **Google Pay Developer Account**: Ensure you have access to Google Pay API
2. **Pay Theory API Key**: Valid API key with Google Pay permissions
3. **Android Studio**: Latest version with Kotlin support
4. **Android Device/Emulator**: With Google Play Services installed (API 23+)
5. **Pay Theory SDK**: Latest version of the Pay Theory Android SDK

## Implementation

### 1. Configuration

Enable Google Pay in your `PayTheoryConfiguration`:

```kotlin
val configuration = PayTheoryConfiguration.Builder()
    .apiKey("your-api-key")
    .amount(1000) // $10.00
    .enableGooglePay(
        merchantName = "Your Business Name",
        allowPrepaidCards = true,
        allowCreditCards = true
    )
    .setGooglePayButtonType(GooglePayButtonType.Pay)
    .setGooglePayButtonColor(GooglePayButtonColor.Black)
    .setGooglePayBillingAddressRequired(true)
    .setGooglePayBillingAddressFormat(GooglePayBillingAddressFormat.Min)
    .setGooglePayEnvironment(GooglePayEnvironment.TEST) // Use PRODUCTION for production
    .build()
```

#### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `merchantName` | Your business name shown on Google Pay sheet | Required |
| `allowPrepaidCards` | Whether to allow prepaid cards | `true` |
| `allowCreditCards` | Whether to allow credit cards | `true` |
| `googlePayButtonType` | Button type (Pay, Buy, Checkout, etc.) | `Pay` |
| `googlePayButtonColor` | Button color (Black, White) | `Black` |
| `googlePayBillingAddressRequired` | Whether to collect billing address | `false` |
| `googlePayBillingAddressFormat` | Address format (Min, Full) | `Min` |
| `googlePayEnvironment` | Environment (TEST, PRODUCTION) | `TEST` |
| `googlePayShippingAddressRequired` | Whether to collect shipping address | `false` |
| `googlePayPhoneNumberRequired` | Whether to collect phone number | `false` |

### 2. UI Implementation

The SDK provides two primary components for Google Pay integration:

#### GooglePayForm

A complete form with availability checking and Google Pay button:

```kotlin
@Composable
fun PaymentScreen(payable: Payable) {
    GooglePayForm(
        payable = payable,
        configuration = configuration,
        buttonModifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}
```

#### StandaloneGooglePayButton

For more customized implementations, you can use the standalone button:

```kotlin
@Composable
fun CustomPaymentScreen(payable: Payable) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Your custom UI elements
        
        StandaloneGooglePayButton(
            payable = payable,
            configuration = configuration,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            hideWhenUnavailable = true,
            onUnavailable = {
                // Handle unavailability (e.g., show alternative payment methods)
            }
        )
    }
}
```

### 3. Handling Google Pay Results

Implement the `Payable` interface to receive Google Pay payment results:

```kotlin
class PaymentActivity : ComponentActivity(), Payable {
    // Other implementation details...
    
    override fun handleSuccess(result: PTResult) {
        // Handle successful Google Pay payment
        val receiptNumber = result.receiptNumber
        val lastFour = result.last_four
        val paymentType = result.paymentType // Will be "GOOGLE_PAY"
        
        // Update UI or navigate to success screen
    }
    
    override fun handleFailure(result: PTResult) {
        // Handle failed Google Pay payment
        val errorMessage = result.message
        
        // Show error to user
    }
    
    override fun handleError(error: PTError) {
        // Handle Google Pay errors
        val errorCode = error.code // Check for GooglePayError code
        val errorMessage = error.message
        
        // Show appropriate error message
    }
    
    override fun handlePaymentStart(paymentType: String) {
        // Called when Google Pay sheet is about to be shown
        // paymentType will be "GOOGLE_PAY"
        
        // Update UI to indicate processing
    }
}
```

## Google Pay Flow

The Google Pay integration follows this flow:

1. **Availability Check**: SDK checks if Google Pay is available on the device
2. **Button Display**: Google Pay button is shown if available
3. **User Interaction**: User taps Google Pay button
4. **Payment Sheet**: Google Pay payment sheet appears
5. **User Confirmation**: User selects payment method and confirms
6. **Token Processing**: Pay Theory processes the Google Pay token
7. **Result Callback**: Success, failure, or error is reported via Payable interface

## Error Handling

Common Google Pay errors and how to handle them:

| Error Code | Meaning | Resolution |
|------------|---------|------------|
| `GooglePayUnavailable` | Google Pay not available on device | Show alternative payment methods |
| `GooglePayCancelled` | User cancelled the Google Pay flow | No action needed, user decided not to proceed |
| `GooglePayError` | Generic Google Pay processing error | Check detailed message and log for troubleshooting |
| `GooglePayNetworkError` | Network error during Google Pay processing | Prompt user to check connection and retry |
| `GooglePayConfigurationError` | Invalid configuration for Google Pay | Check your configuration parameters |

## Testing Google Pay

### Test Environment

1. Set `googlePayEnvironment` to `GooglePayEnvironment.TEST`
2. Use Google's test cards:
   - `4111111111111111` (Visa)
   - `5555555555554444` (Mastercard)
   - See [Google Pay API testing documentation](https://developers.google.com/pay/api/android/guides/resources/test-card-suite) for full list

### Testing Steps

1. **Basic Availability**: Verify Google Pay button appears on devices with Google Pay
2. **Button Styling**: Test different button types and colors
3. **Payment Flow**: Complete end-to-end payment with test cards
4. **Error Handling**: Test cancellation and network error scenarios
5. **Configuration Options**: Test with different configuration options

## Production Considerations

Before going to production:

1. Switch to `GooglePayEnvironment.PRODUCTION`
2. Ensure your Google Pay production account is approved
3. Thoroughly test with your Pay Theory production API key
4. Implement comprehensive error handling
5. Consider adding fallback payment methods

## Best Practices

1. **Button Placement**: Follow Google's guidelines for button placement
2. **Loading States**: Show clear loading indicators during processing
3. **Error Handling**: Provide clear error messages for all scenarios
4. **Fallback Methods**: Always offer alternative payment methods
5. **Testing**: Test on multiple device types and API levels

## Sample Implementation

Complete example of a Google Pay payment screen:

```kotlin
class GooglePayDemoActivity : ComponentActivity(), Payable {
    private lateinit var configuration: PayTheoryConfiguration
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize configuration
        configuration = PayTheoryConfiguration.Builder()
            .apiKey(BuildConfig.PAY_THEORY_API_KEY)
            .amount(1999) // $19.99
            .enableGooglePay(
                merchantName = "Demo Store",
                allowPrepaidCards = true,
                allowCreditCards = true
            )
            .setGooglePayButtonType(GooglePayButtonType.Buy)
            .setGooglePayButtonColor(GooglePayButtonColor.Black)
            .setGooglePayEnvironment(GooglePayEnvironment.TEST)
            .build()
        
        setContent {
            PayTheoryTheme {
                GooglePayPaymentScreen(this, configuration)
            }
        }
    }
    
    // Payable interface implementation
    override fun handleSuccess(result: PTResult) {
        // Handle successful payment
        Toast.makeText(
            this,
            "Payment successful! Receipt: ${result.receiptNumber}",
            Toast.LENGTH_LONG
        ).show()
    }
    
    override fun handleFailure(result: PTResult) {
        // Handle payment failure
        Toast.makeText(
            this,
            "Payment failed: ${result.message}",
            Toast.LENGTH_LONG
        ).show()
    }
    
    override fun handleError(error: PTError) {
        // Handle errors
        Toast.makeText(
            this,
            "Error: ${error.message}",
            Toast.LENGTH_LONG
        ).show()
    }
    
    override fun handlePaymentStart(paymentType: String) {
        // Payment started
        Log.d("GooglePayDemo", "Starting $paymentType payment")
    }
}

@Composable
fun GooglePayPaymentScreen(payable: Payable, configuration: PayTheoryConfiguration) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Complete Your Purchase",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Total: $${configuration.amount / 100}.00",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        GooglePayForm(
            payable = payable,
            configuration = configuration,
            buttonModifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        
        Text(
            text = "Or pay with another method",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Button(
            onClick = { /* Navigate to traditional payment form */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Card or Bank Payment")
        }
    }
}
```

## Troubleshooting

Common issues and solutions:

1. **Google Pay button doesn't appear**
   - Check that Google Play Services is installed and updated
   - Verify that the device supports Google Pay
   - Ensure `enableGooglePay()` is called in configuration

2. **Google Pay transactions fail**
   - Verify API key has Google Pay permissions
   - Check merchant name and configuration
   - Ensure test cards are used in TEST environment

3. **Incorrect button styling**
   - Review Google Pay branding guidelines
   - Ensure button type and color are properly set in configuration

4. **Payment successful but no callback**
   - Check that Payable interface is properly implemented
   - Verify that the Activity implementing Payable is not destroyed

## Resources

- [Google Pay API Documentation](https://developers.google.com/pay/api)
- [Google Pay Brand Guidelines](https://developers.google.com/pay/api/android/guides/brand-guidelines)
- [Google Pay Test Cards](https://developers.google.com/pay/api/android/guides/resources/test-card-suite)
- [Pay Theory Support](https://paytheory.com/support) 