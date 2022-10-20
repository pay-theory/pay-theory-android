# Pay Theory Android SDK

## Requirements

Pay Theory's Android SDK is available for Android SDK >= 26

## Register your application

Register your app in Pay Theory's merchant portal to use Pay Theory Android SDK

![App Registration](https://assets.paytheory.com/android/android-registration.png)

For each mobile app, you want to register

- enter SHA-256 digest of release signing key
- enter SHA-256 digest of debug signing key
- enter your application package name

### Find signing key digest

You can find you signing key digests using Android Studio

![Open Gradle](https://assets.paytheory.com/android/open-gradle)

In upper right corner of Android Studio click on Gradle

![Signing Report](https://assets.paytheory.com/android/signing-report)

Select Signing Report

![Run Report](https://assets.paytheory.com/android/run-report)

Right-click and choose run

![SHA-256](https://assets.paytheory.com/android/sha-256)

Copy your SHA-256 digest

## Installation

The Pay Theory Android SDK is now available at [Maven Repository](https://repo1.maven.org/maven2/com/paytheory/)

The latest version is available via `mavenCentral()`:

- Add mavenCentral() to your project level `build.gradle` file

```kotlin
buildscript {
    repositories {
        ...
        mavenCentral()
    }
}
```

```kotlin
dependencies {
  implementation 'com.paytheory:pay-theory-android:2.7.1'
}
```

### Update your app level `build.gradle`

- change minSdk version to 26 or higher

- change targetSdk and compileSdk to 33 or higher

```Kotlin
android {
    ...
    compileSdk 33
    ...
    defaultConfig {
        applicationId "com.example.pay_theory_android_sdk_kotlin_demo"
        minSdk 26
        targetSdk 33
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }
```

## Usage

### Layout

Add PayTheoryFragment as a fragment in your activity's xml layout file

```xml
    <fragment
    android:id="@+id/payTheoryFragment"
    android:name="com.paytheory.android.sdk.fragments.PayTheoryFragment"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### Code

Add Payable interface along with its functions to your activity

```Kotlin
class MainActivity : AppCompatActivity() , Payable {

    fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult){
        println(successfulTransactionResult)
    }

    fun handleFailure(failedTransactionResult: FailedTransactionResult){
        println(failedTransactionResult)
    }

    fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction){
        println(confirmationMessage)
    }

    fun handleError(error: Error){
        println(error)
    }

    fun handleBarcodeSuccess(barcodeResult: BarcodeResult){
        println(barcodeResult)
    }

    fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults){
        println(paymentMethodToken)
    }
}
```

Set your api key.

```Kotlin
private val apiKey = "API_KEY"
```

Set PayTheoryFragment inside your `onCreate` method.

```Kotlin
val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment
```

Configure PayTheoryFragment inside your `onCreate` method.

```Kotlin
payTheoryFragment.configure(apiKey = apiKey, amount = 1000)
```

## Additional Configurations

Configurations are modified inside `payTheoryFragment.configure()` or `payTheoryFragment.tokenize()` methods.

*Note if any configurations are incorrectly set PayTheoryFragment will throw IllegalArgumentException*
*Note if the device's network connection is not available PayTheoryFragment will throw NetworkErrorException*

### Transaction Type

Optionally set the transaction type

- by default the SDK provides a simple card input

```Kotlin
// debit/credit card fields
transactionType = TransactionType.CARD 
```

```Kotlin
// bank account fields
transactionType = TransactionType.BANK 
```

```Kotlin
// cash/barcode fields
transactionType = TransactionType.CASH 
```

### Account Name

Optionally set the transaction to require the account name input field

- by default the SDK sets requireAccountName to false

```Kotlin
// will include account name field
requireAccountName = true 
```

```Kotlin
// will not include account name field
requireAccountName = false 
```

### Billing Address

Optionally set the transaction to require input fields for a full billing address

- by default the SDK sets requireBillingAddress to false

```Kotlin
// will include billing address fields
requireBillingAddress = true 
```

```Kotlin
// will not include billing address fields
requireBillingAddress = false 
```

### Confirmation

Optionally set the transaction to require a confirmation step

- by default the SDK sets confirmation to false

```Kotlin
// will include a confirmation step 
confirmation = true 
```

```Kotlin
// will not include a confirmation step 
confirmation = false 
```

Complete Pay Theory transaction after user confirmation

```Kotlin
override fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction) {
    // YOUR USER CONFIRMATION STEP...
        
    if (userAgrees == true) {
        transaction.completeTransfer()  //This will confirm and continue processing the transaction 
    }
}
```

### Fee Mode

Optionally set the fee mode

- By default FeeMode.INTERCHANGE mode is used, FeeMode.SERVICE_FEE mode is available only when enabled by Pay Theory

```Kotlin
//set fee mode as interchange
feeMode = FeeMode.INTERCHANGE
```

```Kotlin
//set fee mode as service fee (enabled by Pay Theory)
feeMode = FeeMode.SERVICE_FEE
```

### Metadata

Optionally track payments with custom metadata, simply add metadata with a HashMap:

```Kotlin
//Set optional metadata configuration
val metadata: HashMap<Any,Any> = hashMapOf(
    "studentId" to "student_1859034",
    "courseId" to "course_1859034"
)

payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    metadata = metadata
)
```

### Payor Info

Optionally pass additional customer data to Pay Theory.

Use data models **PayorInfo** and **Address** from Pay Theory SDK

```Kotlin
//Set optional payorInfo configuration
val payorInfo = PayorInfo(
    "John",
    "Smith",
    "john@gmail.com",
    "5135555555",
    Address(
        "10549 Reading Rd",
        "Apt 1",
        "Cincinnati",
        "OH",
        "45241",
        "USA")
)

payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    payorInfo = payorInfo
)
```

### Payor Id

Optionally set the payor id

- By default a new payorId is created with payorInfo

```Kotlin
//set payor id
payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    payorId = "PAYOR_ID"
)
```

### Account Code

Optionally set the account code

- accountCode is tracked for each transaction in Pay Theory portals

```Kotlin
//set account code
payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    accountCode = "ACCOUNT_CODE"
)
```

### Reference

Optionally set the reference

- reference is tracked for each transaction in Pay Theory portals

```Kotlin
//set reference
payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    reference = "REFERENCE"
)
```

### Payment Parameters

Optionally set payment parameters

- By default your Pay Theory defaulted payment parameter is used for all transactions

```Kotlin
//set payment parameter
payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    paymentParameters = "PAYMENT_PARAMETERS"
)
```

### Invoice Id

Optionally set an invoice id

- Set an invoice id to submit the transaction for a Pay Theory invoice

```Kotlin
//set invoice id
payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    invoiceId = "INVOICE_ID"
)
```

### Receipts

Optionally enable receipts

- By default sendReceipt is set to false
- The default receipt message will send if receiptDescription is not included

```Kotlin
//set invoice id
payTheoryFragment.configure(
    apiKey = "API_KEY",
    amount = 1000,
    sendReceipt = true,
    receiptDescription = "School Fees"
)
```

### Example Configuration

Here is an example on how to use PayTheoryFragment class method configure():

```Kotlin

//PayorInfo configuration
val payorInfo = PayorInfo(
    "John",
    "Doe",
    "johndoe@paytheory.com",
    "5135555555",
    Address(
        "10549 Reading Rd",
        "Apt 1",
        "Cincinnati",
        "OH",
        "45241",
        "USA")
)

//metadata configuration
val metadata: HashMap<Any,Any> = hashMapOf(
    "studentId" to "student_1859034",
    "courseId" to "course_1859034"
)

try {
    //PayTheoryFragment configuration for card payments
    payTheoryFragment.configure(
        apiKey = apiKey,
        amount = 1000,
        transactionType = TransactionType.CARD,
        metadata = metadata,
        payorInfo = payorInfo
    )

} catch (e: Exception) {
    e.printStackTrace()
}
```

## Tokenization Requests

Here is an example on how to use PayTheoryFragment class method tokenize():

```kotlin
try {
    //PayTheoryFragment configuration for creating a payment method token
    payTheoryFragment.tokenize(
        apiKey = apiKey,
        tokenizationType = TokenizationType.CARD,
        requireAccountName = true,
        requireBillingAddress = true,
        payorInfo = payorInfo,
        metadata = metadata
    )

} catch (e: Exception) {
    e.printStackTrace()
}
```

## Results

The transaction results will come back through as these classes:

- successful card/bank payment result

```Kotlin
data class SuccessfulTransactionResult (
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("last_four") val lastFour: String,
    @SerializedName("service_fee") val serviceFee: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("receipt_number") val receiptNumber: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("payor_id") val payorId: String,
)
```

- confirmation step result

```Kotlin
data class ConfirmationMessage (
    @SerializedName("payment_token") val paymentToken: String,
    @SerializedName("payer_id") val payerId: String?,
    @SerializedName("processor_payment_method_id") val processorPaymentMethodId: String?,
    @SerializedName("merchant_uid") val merchantUid: String?,
    @SerializedName("last_four") val lastFour: String?,
    @SerializedName("first_six") val firstSix: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("session_key") val sessionKey: String,
    @SerializedName("processor") val processor: String,
    @SerializedName("expiration") var expiration: String?,
    @SerializedName("idempotency") val idempotency: String,
    @SerializedName("billing_name") val billingName: String?,
    @SerializedName("billing_address") val billingAddress: Address?,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("fee_mode") val fee_mode: String,
    @SerializedName("fee") var fee: String,
    @SerializedName("processor_merchant_id") val processor_merchant_id: String?,
    @SerializedName("payment_method") val payment_method: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("pay_theory_data") val pay_theory_data: HashMap<Any, Any>?,
    @SerializedName("payor_info") val payorInfo: PayorInfo?,
    @SerializedName("payor_id") var payor_id: String?,
    @SerializedName("invoice_id") val invoice_id: String?,
    @SerializedName("payment_intent_id") val paymentIntentId: String?,
)
```

- successful barcode request result

```Kotlin
data class BarcodeResult (
    @SerializedName("BarcodeUid") val barcodeUid: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
    @SerializedName("mapUrl") val mapUrl : String
)
```

- successful payment method token result

```Kotlin
data class PaymentMethodTokenResults (
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("metadata") val metadata: HashMap<Any, Any>?,
    @SerializedName("payor_id") var payor_id: String?,
    @SerializedName("last_four") val lastFour: String?,
    @SerializedName("first_six") val firstSix: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("expiration") val expiration: String?,
    @SerializedName("payment_type") val paymentType: String
)
```

- failed card, bank, barcode, and token request results

```Kotlin
data class FailedTransactionResult (
    @SerializedName("state") val state: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("last_four") val lastFour: String,
    @SerializedName("receipt_number") val receiptNumber: String,
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("payor_id") val payorId: String,
)

```

- any processing error results

```Kotlin
data class Error (
    @SerializedName("reason") val reason: String
)
```

## Development

If you are using a virtual device for development, it must have google play store installed.

## Testing

A Java version between 8 and 16 is required to execute Gradle commands.

## Support/Feedback

Feel free to get in touch:

- [GitHub Issues](https://github.com/pay-theory/pay-theory-android/issues) - For any issues or feedback
- [Support](https://paytheory.com/) / [support@paytheory.com](mailto:support@paytheory.com)

## License

The Pay Theory Android SDK is open source and available under the MIT license. See [LICENSE](https://github.com/pay-theory/pay-theory-android/blob/main/LICENSE)

MIT © [LICENSE](https://github.com/pay-theory/pay-theory-android/blob/main/LICENSE)
