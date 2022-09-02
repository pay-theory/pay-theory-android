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

pay-theory-android is available through jitpack for android projects

### Project level build.gradle file
- Add maven jitpack url
- Add jcenter() or mavenCentral()

```gradle
allprojects {
    repositories {
        google()
        maven { url 'https://jitpack.io' }
        mavenCentral()
        jcenter()
    }
}
```

### Application level build.gradle file

- Add implementation for pay-theory-android
- Add implementation for java jna

```gradle
dependencies {
    ...
    implementation ('com.github.pay-theory:pay-theory-android:PAY-THEORY-VERSION') {
        exclude group: 'net.java.dev.jna', module:'jna'
    }
    implementation 'net.java.dev.jna:jna:JNA-VERSION@aar'
    ...
}
```

[![](https://jitpack.io/v/pay-theory/pay-theory-android.svg)](https://jitpack.io/#pay-theory/pay-theory-android)

_replace PAY-THEORY-VERSION with the release version shown above_

[![](https://jitpack.io/v/java-native-access/jna.svg)](https://jitpack.io/#java-native-access/jna)

_replace JNA-VERSION with the release version shown above_

### Update minimum sdk version to 21 or higher in application level build.gradle file

```Kotlin
defaultConfig {
    applicationId "com.example.testapp"
    minSdkVersion 21
    targetSdkVersion 30
    versionCode 1
    versionName "1.0"
    testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
}
```

## Usage

### Fragment

add PayTheoryFragment to your activity's xml layout file

[Click here to see example](https://github.com/pay-theory/pay-theory-android/blob/premain/ExampleApplication/src/main/res/layout/activity_main.xml)
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <fragment
        android:id="@+id/payTheoryFragment"
        android:name="com.paytheory.android.sdk.fragments.PayTheoryFragment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        />

</LinearLayout>
```

### Code

Add this line to for your API key

```Kotlin
private val apiKey = "YOUR-API-KEY"
```

Add this line inside your onCreate method set PayTheoryFragment

```Kotlin
val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment
```

Inside your onCreate method configure Pay Theory
 * by default the SDK provides a simple credit card input (additional configurations available)

```Kotlin
payTheoryFragment.configure(apiKey,8500)
```


Add Payable interface and methods for transaction responses

```Kotlin
class MainActivity : AppCompatActivity() , Payable {

...
    override fun paymentComplete(paymentResult: PaymentResult) {
    }

    override fun barcodeComplete(barcodeResult: BarcodeResult) {
    }

    override fun paymentFailed(paymentFailure: PaymentResult) {
    }

    override fun paymentError(paymentError: PaymentError) {
    }

    override fun paymentConfirmation(paymentConfirmation: PaymentConfirmation, transaction: Transaction) {
    }
...

}
```
[Click here to see how our demo handles transaction responses](https://github.com/pay-theory/pay-theory-android/blob/main/ExampleApplication/src/main/java/com/paytheory/android/example/MainActivity.kt)

## Additional PayTheoryFragment Configurations

Here is an example on how to use PayTheoryFragment class method configure():
```Kotlin

val payorInfo = PayorInfo(firstName, lastName, email, phoneNumber, Address(addressLineOne, addressLineTwo, addressCity, addressState, addressZip, addressCountry))

val metadata = hashMapOf("pay-theory-account-code" to "test-acccount-code", "pay-theory-reference" to "android-test")

payTheoryFragment.configure(
    apiKey,                         // String : Pay Theory api key
    amount,                         // Int : amount of the transaction in total cents 
    TransactionType.CARD,           // String : requested transaction type (TransactionType.CARD, TransactionType.BANK, TransactionType.CASH) : defaults to TransactionType.CREDIT
    false,                          // Boolean : set account name field to be displayed : defaults to true
    false,                          // Boolean : set address fields to be displayed : defaults to true
    false,                          // Boolean : require payment confirmation before completing payment : defaults to false
    FeeMode.SERVICE_FEE,            // String : Pay Theory fee mode (FeeMode.INTERCHANGE, FeeMode.SERVICE_FEE) : defaults to FeeMode.INTERCHANGE
    payorInfo,                   // PayorInfo : buyer data sent to Pay Theory : defaults to empty object
    metadata,                           // HashMap<String, String> : set optional metadata for Pay Theory : defaults to empty hash map
)
```

### Transaction Type
Optionally set the transaction type
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
val payorInfo = PayorInfo("Jim", "Smith", "jim.smith@gmail.com", "513-123-4567",
    Address("123 Testing Lane", "Apt 2", "Cincinnati", "OH", "45236", "USA"))

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

## License

MIT © [pay theory](https://github.com/pay-theory)
