# Pay Theory Android SDK - UI Components

## Overview

The Pay Theory Android SDK provides a comprehensive set of UI components built with Jetpack Compose for payment collection. These components are designed to be flexible, customizable, and offer a seamless user experience while maintaining security and compliance standards.

## Core UI Components

### PaymentForm

The `PaymentForm` is the main composable that provides a complete payment form experience:

```kotlin
@Composable
fun PaymentForm(
    payable: Payable,
    configuration: PayTheoryConfiguration,
    fieldModifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier
)
```

This composable automatically:
- Adapts to the payment method type (card or ACH)
- Shows relevant fields based on configuration
- Handles validation with visual feedback
- Provides a payment button
- Manages the entire payment flow

### Payment Field Components

#### Card Payment Fields

1. **CardNumberField**: Input field for card number with real-time validation and card type detection
   - Formats card number (inserts spaces) as user types
   - Shows card brand icon based on detected card type
   - Provides visual validation feedback

2. **CardExpirationField**: Input field for card expiration date
   - Automatically formats to MM/YY format
   - Validates for valid date and expiration

3. **CVVField**: Input field for card verification value
   - Validates length based on card type
   - Provides masked input for security

4. **PostalCodeField**: Input field for billing postal/ZIP code
   - Validates format based on country standards
   - Required if `requireBillingAddress` is false

#### ACH Payment Fields

1. **AccountNumberField**: Input field for bank account number
   - Validates length and format
   - Provides masked input for security

2. **RoutingNumberField**: Input field for bank routing number
   - Validates checksum and format
   - Provides lookup of bank information

3. **AccountTypeSelector**: Dropdown for account type (checking/savings)
   - Styled to match other form elements
   - Required for ACH payments

#### Common Fields

1. **NameOnAccountField**: Input field for account holder name
   - Optional based on configuration
   - Validates format and length

2. **BillingAddressFields**: Group of address input fields
   - Address line 1 and 2
   - City
   - State/Region (dropdown for US)
   - Postal/ZIP code
   - Only shown if `requireBillingAddress` is true

### Action Components

1. **PaymentButton**: Primary button to submit payment
   - Adapts text based on payment action (Pay, Submit, Save)
   - Shows loading state during processing
   - Disables when form is invalid

2. **PaymentMethodSelector**: Toggle between payment methods
   - Only shown when multiple payment methods are supported
   - Uses segmented button design

### Feedback Components

1. **ValidationFeedback**: Shows validation errors under fields
   - Provides clear error messages
   - Uses appropriate color from theme

2. **ProcessingIndicator**: Shows payment processing state
   - Animation during submission
   - Success/failure indicators

3. **CardBrandIndicator**: Shows detected card brand icon
   - Updates as user types card number
   - Supports major card brands

## State Management

The UI components are connected to state management through:

1. **PaymentViewModel**: Manages overall payment state
   - Tracks form validity
   - Manages websocket connection
   - Coordinates payment submission

2. **PaymentFieldViewModel**: Manages individual field states
   - Provides real-time validation
   - Tracks user input and format
   - Emits state changes (Valid, Invalid, Empty)

### Field State Flow

Each field provides state updates through Kotlin Flow:

```kotlin
// Example state collection in a composable
val cardNumberState = fieldModel.cardNumberState.collectAsState()

// Responding to state changes
when (cardNumberState.value) {
    is FieldState.Valid -> { /* Handle valid state */ }
    is FieldState.Invalid -> { /* Show error */ }
    is FieldState.Empty -> { /* Handle empty state */ }
}
```

## Styling and Customization

### Material 3 Integration

The UI components are built with Material 3 and respect the application's theme:

- Color scheme (primary, surface, error)
- Typography
- Shapes
- Content alpha

### Customization Options

1. **Modifiers**: Each component accepts modifiers for layout customization
   - `fieldModifier`: Applied to input fields
   - `buttonModifier`: Applied to action buttons

2. **Outlined Option**: Toggle between filled and outlined text fields
   ```kotlin
   val ptConfig = PayTheoryConfiguration(
       outlined = true,
       // other configuration...
   )
   ```

3. **Theme Customization**: Adapt to your app's Material theme
   ```kotlin
   MaterialTheme(
       colorScheme = customColorScheme,
       typography = customTypography,
       shapes = customShapes
   ) {
       PaymentForm(/* ... */)
   }
   ```

## Accessibility

The UI components adhere to accessibility best practices:

- Sufficient touch target sizes
- Content descriptions for images and icons
- Color contrast compliance
- Support for screen readers
- Keyboard navigation support

## Security Features

The UI components include built-in security features:

1. **Secure Input Fields**: Secure handling of sensitive data
   - Uses `SecureString` wrapper to prevent memory inspection
   - Clears sensitive data after submission

2. **Visual Security**: Appropriate field masking
   - Password-style masking for CVV
   - Partial masking for card/account numbers

## Sample Layouts

### Card Payment Form

```
┌────────────────────────────────┐
│ Card Number                    │
│ •••• •••• •••• ••••       [VISA]│
├────────────────────────────────┤
│ Expiration     │ CVV           │
│ MM/YY          │ •••           │
├────────────────┴───────────────┤
│ Postal Code                    │
│ 12345                          │
├────────────────────────────────┤
│         [ Pay $10.00 ]         │
└────────────────────────────────┘
```

### ACH Payment Form with Billing Address

```
┌────────────────────────────────┐
│ Account Number                 │
│ ••••••••••                     │
├────────────────────────────────┤
│ Routing Number                 │
│ 123456789                      │
├────────────────────────────────┤
│ Account Type                   │
│ [Checking ▼]                   │
├────────────────────────────────┤
│ Name on Account                │
│ John Doe                       │
├────────────────────────────────┤
│ Address Line 1                 │
│ 123 Main St                    │
├────────────────────────────────┤
│ Address Line 2 (optional)      │
│ Apt 1                          │
├────────────────────────────────┤
│ City           │ State         │
│ Anytown        │ [OH ▼]        │
├────────────────┴───────────────┤
│ Postal Code                    │
│ 12345                          │
├────────────────────────────────┤
│    [ Submit Bank Payment ]     │
└────────────────────────────────┘
```

## Implementation Example

```kotlin
@Composable
fun PaymentScreen(
    payable: Payable,
    processModel: PaymentProcessViewModel,
    fieldModel: PaymentFieldViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Display payment form
        PaymentForm(
            payable = payable,
            configuration = PayTheoryConfiguration(
                apiKey = BuildConfig.PAY_THEORY_API_KEY,
                amount = 1000, // $10.00
                paymentMethodType = PaymentMethodType.CARD,
                requireBillingAddress = false
            ),
            fieldModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            buttonModifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        
        // Observe payment result state
        when (val state = processModel.paymentResultState.collectAsState().value) {
            is PaymentResultState.Success -> {
                Text(
                    text = "Payment successful! Receipt: ${state.result.receiptNumber}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            is PaymentResultState.Failure -> {
                Text(
                    text = "Payment failed: ${state.result.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            else -> { /* Handle other states */ }
        }
    }
} 