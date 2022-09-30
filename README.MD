# Pay Theory Android SDK

## Requirements

Written in Kotlin and requires Android 8.0 (API level 26)

## Register your application

Before you can use Pay Theory Android SDK you must register your app in Pay Theory's merchant portal

![App Registration](https://assets.paytheory.com/android/android-registration.png)

For each mobile app, you want to register 
*   enter SHA-256 digest of release signing key
*   enter SHA-256 digest of debug signing key
*   enter your application package name

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

The Pay Theory Android SDK is now available at [Maven Repository](https://repo1.maven.org/maven2/com/paytheory/). The latest version is available via `mavenCentral()`:

- Add mavenCentral() to your project level build.gradle file

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
  implementation 'com.paytheory:pay-theory-android:2.7.0'
}
```

### Update minimum sdk version to 26 or higher in application level build.gradle file

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

### Fragment

Add PayTheoryFragment as a fragment in your activity's xml layout file

[Click here for an example](https://github.com/pay-theory/pay-theory-android/blob/premain/ExampleApplication/src/main/res/layout/activity_main.xml)

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

...

    fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult){
        
    }

    fun handleFailure(failedTransactionResult: FailedTransactionResult){
        
    }

    fun handleBarcodeSuccess(barcodeResult: BarcodeResult){
        
    }

    fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults){
        
    }

    fun confirmation(confirmationMessage: ConfirmationMessage, transaction: Transaction){
        
    }

    fun handleError(error: Error){
        
    }
...

}
```

Set your api key

```Kotlin
private val apiKey = "API_KEY"
```

Set PayTheoryFragment inside your onCreate method

```Kotlin
val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment
```

Configure PayTheoryFragment inside your onCreate method

```Kotlin
payTheoryFragment.configure(apiKey = apiKey, amount = 1000)
```


[Click here to see how our demo handles transaction responses](https://github.com/pay-theory/pay-theory-android/blob/main/ExampleApplication/src/main/java/com/paytheory/android/example/MainActivity.kt)

## Additional PayTheoryFragment Configurations

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
        transactionType = TransactionType.CARD
        metadata = metadata,
        payorInfo = payorInfo
    )

} catch (e: Exception) {
    e.printStackTrace()
}
```

### Transaction Type
Optionally set the transaction type
* by default the SDK provides a simple card input (additional configurations available)
```Kotlin
payTheoryFragment.configure(apiKey, amount)
```
```Kotlin
payTheoryFragment.configure(apiKey, amount, TransactionType.CARD)
```
```Kotlin
payTheoryFragment.configure(apiKey, amount, TransactionType.CASH)
```
```Kotlin
payTheoryFragment.configure(apiKey, amount, TransactionType.BANK)
```

### Fee Mode
Optionally set the fee mode. By default FeeMode.INTERCHANGE mode is used FeeMode.SERVICE_FEE mode is available only when enabled by Pay Theory

```Kotlin
payTheoryFragment.configure(apiKey, amount, TransactionType.CARD, false, false, false, FeeMode.INTERCHANGE)
```

```Kotlin
payTheoryFragment.configure(apiKey, amount, TransactionType.CARD, false, false, false, FeeMode.SERVICE_FEE)
```

### Buyer Options
Optionally pass additional customer data to Pay Theory. Use PayorInfo and Address data model from Pay Theory SDK
```Kotlin
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

payTheoryFragment.configure(apiKey, amount, TransactionType.CARD, false, false, false, FeeMode.INTERCHANGE, payorInfo)
```
### Custom Tags
To track payments with custom metadata simply add the following when initializing the SDK:
- **pay-theory-account-code**: Code that will be used to track the payment and can be filtered by.
- **pay-theory-reference**: Custom description assigned to a payment that can later be filtered by.
- **payment-parameters-name**: A specific payment parameter that has been created in Pay Theory Portals.

```Kotlin
val metadata = hashMapOf("pay-theory-account-code" to accountCode, "pay-theory-reference" to payTheoryReference, "payment-parameters-name" to paymentParameter)
payTheoryFragment.configure(apiKey,2501, TransactionType.CARD, false, false,  false, FeeMode.SERVICE_FEE, payorInfo, metadata)
```

### Example Configurations
```Kotlin

// use ACH bank account
payTheoryFragment.configure(apiKey, amount, TransactionType.BANK)

// use ACH bank account and require account name
payTheoryFragment.configure(apiKey, amount, TransactionType.BANK, requireAccountName = true)

// use ACH bank account and require account name and billing address
payTheoryFragment.configure(apiKey, amount, TransactionType.BANK, requireAccountName = true, requireBillingAddress = true)

// use credit card account
payTheoryFragment.configure(apiKey, amount, TransactionType.CARD)

// use credit card and require account name
payTheoryFragment.configure(apiKey, amount, TransactionType.CARD, requireAccountName = true)

// use credit card and require account name and billing address
payTheoryFragment.configure(apiKey, amount, TransactionType.CARD, requireAccountName = true, requireBillingAddress = true)

// use cash
payTheoryFragment.configure(apiKey, amount, TransactionType.CASH)

```

## Tokenization Requests
            //PayTheoryFragment configuration for card payments
//            payTheoryFragment.tokenizePaymentMethod(
//                apiKey = apiKey,
//                tokenizationType = TokenizationType.CARD,
//                requireAccountName = true,
//                requireBillingAddress = false,
//                payorInfo = payorInfo,
//                payorId = "PAYOR_ID",
//                metadata = metadata
//            )

## Results

The transaction results will come back through as these classes:

*   complete ach/card payment result
 
```Kotlin
data class PaymentResult (
    @SerializedName("receipt_number") val receipt_number: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("service_fee") val service_fee: String?,
    @SerializedName("metadata") val metadata: Map<String,String>?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    @SerializedName("type") val type: String?
)
```

*   complete cash barcode result
```Kotlin
data class BarcodeResult (
    @SerializedName("BarcodeUid") val barcodeUid: String,
    @SerializedName("barcodeUrl") val barcodeUrl: String,
    @SerializedName("barcode") val barcode: String,
    @SerializedName("barcodeFee") val barcodeFee: String,
    @SerializedName("Merchant") val merchant: String,
    @SerializedName("MapUrl") val mapUrl : String
)
```

*   payment confirmation data
```Kotlin
data class PaymentConfirmation (
    @SerializedName("bin") val bin: Bin,
    @SerializedName("idempotency") val idempotency: String,
    @SerializedName("payment") val payment: Payment,
    @SerializedName("payment-token") val paymentToken: String,
    @SerializedName("payment_intent_id") val paymentIntentId: String,
)
```

*   failed ach/card payment result

```Kotlin
data class PaymentResultFailure (
    @SerializedName("receipt_number") val receipt_number: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("state") val state: String,
    @SerializedName("type") val type: String?
)
```

*   error processing card, ach, or barcode results

```Kotlin
data class TransactionError (
    @SerializedName("reason") val reason: String
)
```

## Testing
A Java version between 8 and 16 is required to execute Gradle commands.

### Unit testing

Run this line in the command prompt:

Windows
```powershell
gradlew unitTesting
```

Linux
```shell
./gradlew unitTesting
```

### Unit and UI testing (Requires an android device or emulator)

Run this line in the command prompt:

Windows
```powershell
gradlew completeTesting
```

Linux
```shell
./gradlew completeTesting
```

## Support/Feedback

Feel free to get in touch:

* [GitHub Issues](https://github.com/pay-theory/pay-theory-android/issues) - For any issues or feedback
* [Support](https://paytheory.com/) / [support@paytheory.com](mailto:support@paytheory.com) -

## License

The Pay Theory Android SDK is open source and available under the MIT license. See [LICENSE](https://github.com/pay-theory/pay-theory-android/blob/main/LICENSE)

MIT © [LICENSE](https://github.com/pay-theory/pay-theory-android/blob/main/LICENSE)
