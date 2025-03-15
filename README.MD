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
    implementation 'com.paytheory.lib:ComposePayments:VERSION'
}
```

## Basic Implementation

1. Implement the `Payable` interface in your Activity or ViewModel:

```kotlin
class MainActivity : ComponentActivity(), Payable {
    private val paymentProcessModel: PaymentProcessViewModel by viewModels()
    private val paymentFieldModel: PaymentFieldViewModel by viewModels()

    // Implement Payable interface methods
    override fun handleSuccess(
        successfulTransactionResult: SuccessfulTransactionResult) {
        paymentProcessModel.updateState(
            PaymentResultState.Success(successfulTransactionResult)
        )
    }

    override fun handleFailure(
        failedTransactionResult: FailedTransactionResult) {
        paymentProcessModel.updateState(
            PaymentResultState.Failure(failedTransactionResult)
        )
    }

    override fun handleStateChange(field: PaymentField, fieldState: FieldState) {
        // Update the field state in the ViewModel
        paymentFieldModel.updateState(field, fieldState)
        
        // Example of responding to specific field states
        when (field) {
            PaymentField.CARD_NUMBER -> {
                when (fieldState) {
                    is FieldState.Valid -> {
                        // Handle valid card number
                        val cardType = fieldState.cardType
                        Log.d("PaymentForm", "Valid card: $cardType")
                    }
                    is FieldState.Invalid -> {
                        // Handle invalid card number
                        val error = fieldState.error
                        Log.d("PaymentForm", "Invalid card: $error")
                    }
                    is FieldState.Empty -> {
                        // Handle empty card number field
                        Log.d("PaymentForm", "Card number empty")
                    }
                }
            }
            PaymentField.BANK_ACCOUNT_NUMBER -> {
                when (fieldState) {
                    is FieldState.Valid -> {
                        // Handle valid bank account number
                        Log.d("PaymentForm", "Valid bank account")
                    }
                    is FieldState.Invalid -> {
                        // Handle invalid bank account number
                        val error = fieldState.error
                        Log.d("PaymentForm", "Invalid bank account: $error")
                    }
                }
            }
            PaymentField.POSTAL_CODE -> {
                when (fieldState) {
                    is FieldState.Valid -> {
                        // Handle valid postal code
                        Log.d("PaymentForm", "Valid postal code")
                    }
                    is FieldState.Invalid -> {
                        // Handle invalid postal code
                        val error = fieldState.error
                        Log.d("PaymentForm", "Invalid postal code: $error")
                    }
                }
            }
            // Handle other fields as needed
        }
    }

    // ... implement other Payable interface methods
}
```

2. Configure the payment form:

```kotlin
val ptConfig = PayTheoryConfiguration(
    apiKey = BuildConfig.PAY_THEORY_API_KEY,
    amount = 1000, 
    // Amount in cents - not required for token
    paymentMethodType = PaymentMethodType.CARD, 
    // or PaymentMethodType.ACH
    requireBillingAddress = false,
    requireAccountName = false,
    paymentMethodAction = PaymentMethodAction.PAYMENT 
    // or PaymentMethodAction.TOKEN
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

## Styling

The Pay Theory SDK uses Material 3 theming with Jetpack Compose, allowing you to customize the appearance of the payment form to match your app's design.

### Material Theme Customization

```kotlin
@Composable
fun PayTheoryTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary = Color(0xFF2196F3),
        secondary = Color(0xFF03DAC6),
        surface = Color(0xFFFFFFFF),
        error = Color(0xFFB00020)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            // Custom typography settings
            bodyLarge = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            )
        ),
        shapes = Shapes(
            // Custom shape settings
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp)
        )
    ) {
        content()
    }
}
```

### Modifiers and Spacing

The PaymentForm accepts modifiers for both fields and buttons:

```kotlin
@Composable
fun StyledPaymentForm(
    payable: Payable,
    configuration: PayTheoryConfiguration
) {
    PaymentForm(
        payable = payable,
        configuration = configuration,
        fieldModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        buttonModifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
    )
}
```

### Resources and Documentation

For more detailed information about styling with Jetpack Compose:

- [Material 3 Design](https://m3.material.io/develop/android/jetpack-compose) - Official Material 3 guidelines for Compose
- [Compose Theming](https://developer.android.com/jetpack/compose/themes) - Learn about theming in Jetpack Compose
- [Material Components](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary) - Material 3 component documentation
- [Compose Modifiers](https://developer.android.com/jetpack/compose/modifiers) - Guide to using modifiers in Compose
- [Typography](https://developer.android.com/jetpack/compose/text) - Text styling in Compose
- [Color](https://developer.android.com/jetpack/compose/themes/material#color) - Color system in Material 3

### Best Practices

1. **Theme Consistency**: Use your app's existing Material Theme to ensure the payment form matches your app's design language.
2. **Accessibility**: Maintain sufficient color contrast and touch target sizes for better accessibility.
3. **Responsive Design**: Use flexible modifiers like `fillMaxWidth()` and relative padding for different screen sizes.
4. **Dark Theme Support**: Consider both light and dark theme variants when customizing colors.
5. **Error States**: Maintain clear visual feedback for error states while keeping brand consistency.

## Security

The SDK handles sensitive payment data securely and is PCI compliant. All communication with Pay Theory servers is encrypted.

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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please contact support@paytheory.com or visit [Pay Theory's documentation](https://docs.paytheory.com). 