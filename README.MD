# Pay Theory Android SDK

The Pay Theory Android SDK provides a simple way to integrate payment processing into your Android application using Jetpack Compose. The SDK supports both card and ACH (bank transfer) payments, as well as payment method tokenization.

## Features

- Credit/Debit card payments
- ACH (bank transfer) payments
- Payment method tokenization
- Customizable UI components
- Real-time field validation
- Secure payment processing
- Compose-based implementation

## Requirements

- Android API level 28 or higher
- Jetpack Compose
- Kotlin 2.10.0 or higher
- Pay Theory API key

## Installation

Add the Pay Theory Android SDK to your app's `build.gradle` file:

```gradle
dependencies {
    implementation 'com.paytheory.android.sdk:AndroidSDK:2.8.4'
}
```

## Securing Your API Key

To keep your Pay Theory API key secure and out of version control, store it in your `local.properties` file:

1. Add your API key to `local.properties`:
```properties
pay.theory.api.key=your_api_key_here
```

2. In your app's `build.gradle` file, add code to read the API key:
```gradle
android {
    defaultConfig {
        // Other config...
        
        def localProperties = new Properties()
        localProperties.load(new FileInputStream(rootProject.file("local.properties")))
        
        buildConfigField "String", "PAY_THEORY_API_KEY", "\"${localProperties['pay.theory.api.key']}\""
    }
}
```

3. Access your API key in code:
```kotlin
val ptConfig = PayTheoryConfiguration(
    apiKey = BuildConfig.PAY_THEORY_API_KEY,
    // other configuration...
)
```

Make sure to add `local.properties` to your `.gitignore` file to prevent it from being committed to version control:
```gitignore
local.properties
```

## ProGuard Configuration

If you're using ProGuard for code obfuscation, add the following rules to your `proguard-rules.pro` file:

```proguard
# Keep source file names and line numbers for better debugging
-keepattributes SourceFile,LineNumberTable

# Repackage classes into the top-level
-repackageclasses

# Keep Compose-related classes
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt { *; }

# Keep SSL-related classes
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
```

Enable ProGuard in your app's `build.gradle` file:

```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## Basic Implementation

1. Implement the `Payable` interface in your Activity or ViewModel:

```kotlin
class MainActivity : ComponentActivity(), Payable {
    private val paymentProcessModel: PaymentProcessViewModel by viewModels()
    private val paymentFieldModel: PaymentFieldViewModel by viewModels()

    // Implement Payable interface methods
    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {
        paymentProcessModel.updateState(PaymentResultState.Success(successfulTransactionResult))
    }

    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        paymentProcessModel.updateState(PaymentResultState.Failure(failedTransactionResult))
    }

    // ... implement other Payable interface methods
}
```

2. Configure the payment form:

```kotlin
val ptConfig = PayTheoryConfiguration(
    apiKey = BuildConfig.PAY_THEORY_API_KEY,
    amount = 1000, // Amount in cents
    paymentMethodType = PaymentMethodType.CARD, // or PaymentMethodType.ACH
    requireBillingAddress = false,
    requireAccountName = false,
    paymentMethodAction = PaymentMethodAction.PAYMENT // or PaymentMethodAction.TOKEN
)
```

3. Add the PaymentForm composable to your UI:

```kotlin
@Composable
fun PaymentScreen(
    payable: Payable,
    processModel: PaymentProcessViewModel,
    fieldModel: PaymentFieldViewModel
) {
    PaymentForm(
        payable = payable,
        configuration = ptConfig,
        fieldModifier = Modifier.padding(8.dp),
        buttonModifier = Modifier.fillMaxWidth()
    )
}
```

## Form Field State Management

The SDK provides granular control over form field states through the `PaymentFieldViewModel`. This allows you to track user interactions, validate input in real-time, and respond to field state changes.

### Observing Field States

```kotlin
@Composable
fun PaymentScreen(
    payable: Payable,
    processModel: PaymentProcessViewModel,
    fieldModel: PaymentFieldViewModel
) {
    // Collect field states
    val cardNumberState = fieldModel.cardNumberState.collectAsState()
    val expirationState = fieldModel.expirationState.collectAsState()
    val cvvState = fieldModel.cvvState.collectAsState()
    val postalCodeState = fieldModel.postalCodeState.collectAsState()

    // Example of responding to field state changes
    LaunchedEffect(cardNumberState.value) {
        when (cardNumberState.value) {
            is FieldState.Valid -> {
                // Handle valid card number
                val cardType = (cardNumberState.value as FieldState.Valid).cardType
                // Update UI based on card type
            }
            is FieldState.Invalid -> {
                // Handle invalid input
                val error = (cardNumberState.value as FieldState.Invalid).error
                // Show error message
            }
            is FieldState.Empty -> {
                // Handle empty field
            }
        }
    }
}
```

### Available Field States

Each form field provides the following states:

```kotlin
sealed class FieldState {
    object Empty : FieldState()
    data class Valid(
        val value: String,
        val cardType: String? = null // Only for card number field
    ) : FieldState()
    data class Invalid(
        val value: String,
        val error: String
    ) : FieldState()
}
```

### Accessible Field States

The `PaymentFieldViewModel` provides access to these states:

- `cardNumberState`: Card number validation and card type detection
- `expirationState`: Card expiration date validation
- `cvvState`: CVV code validation
- `postalCodeState`: Postal/ZIP code validation
- `accountNumberState`: Bank account number validation (ACH)
- `routingNumberState`: Bank routing number validation (ACH)
- `accountNameState`: Account holder name validation
- `addressState`: Billing address validation

### Custom Validation Example

```kotlin
@Composable
fun CustomPaymentForm(fieldModel: PaymentFieldViewModel) {
    val cardState = fieldModel.cardNumberState.collectAsState()
    
    Column {
        PaymentForm(/* ... */)
        
        // Custom validation feedback
        cardState.value.let { state ->
            when (state) {
                is FieldState.Valid -> {
                    Text(
                        text = "Using ${state.cardType} card",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is FieldState.Invalid -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> { /* Handle other states */ }
            }
        }
    }
}
```

### Pre-submission Validation

```kotlin
fun validateBeforeSubmit(fieldModel: PaymentFieldViewModel): Boolean {
    return fieldModel.run {
        cardNumberState.value is FieldState.Valid &&
        expirationState.value is FieldState.Valid &&
        cvvState.value is FieldState.Valid &&
        (if (requiresPostalCode) postalCodeState.value is FieldState.Valid else true)
    }
}
```

## Payment States

The SDK provides various payment states through `PaymentResultState`:

- `Loading`: Initial loading state
- `Idle`: Ready for input
- `Processing`: Payment is being processed
- `Success`: Payment completed successfully
- `Failure`: Payment failed
- `TokenSuccess`: Payment method tokenized successfully
- `Error`: Error occurred during processing

## Example Usage

Check the `qualitydemo` module in this repository for a complete implementation example, including:

- Card payments
- ACH payments
- Payment method tokenization
- Field state management
- Error handling
- UI customization

## Configuration Options

The `PayTheoryConfiguration` class supports various options:

- `apiKey`: Your Pay Theory API key
- `amount`: Payment amount in cents
- `paymentMethodType`: CARD or ACH
- `requireBillingAddress`: Whether to require billing address
- `requireAccountName`: Whether to require account name
- `paymentMethodAction`: PAYMENT or TOKEN
- `outlined`: Use outlined text fields

## Security

The SDK handles sensitive payment data securely and is PCI compliant. All communication with Pay Theory servers is encrypted.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please contact support@paytheory.com or visit [Pay Theory's documentation](https://docs.paytheory.com). 