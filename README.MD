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

## Install SDK to Gradle

### Install jitpack to project level build.gradle file

```gradle
allprojects {
    repositories {
        google()
        maven { url 'https://jitpack.io' }
        jcenter()
    }
}
```

### Install SDK to application level build.gradle file

```gradle
dependencies {
    ...
    implementation ('com.github.pay-theory:pay-theory-android:PAY-THEORY-RELEASE') {
        exclude group: 'net.java.dev.jna', module:'jna'
    }
    implementation 'net.java.dev.jna:jna:JNA-RELEASE@aar'
    ...
}
```

[![](https://jitpack.io/v/pay-theory/pay-theory-android.svg)](https://jitpack.io/#pay-theory/pay-theory-android)

_replace PAY-THEORY-RELEASE with the release version shown above_

[![](https://jitpack.io/v/java-native-access/jna.svg)](https://jitpack.io/#java-native-access/jna)

_replace JNA-RELEASE with the release version shown above_

### Update minimum sdk version to 21 in application level build.gradle file

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

### Layout

add the Pay Theory Fragment container to your layout xml file

```xml
<fragment
    android:id="@+id/payTheoryFragment"
    android:name="com.paytheory.android.sdk.fragments.PayTheoryFragment"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### Code

*   set your API key

```Kotlin
private val apiKey = "YOUR-API-KEY"
```

*   inside your onCreate method set PayTheoryFragment

```Kotlin
val payTheoryFragment = this.supportFragmentManager
    .findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment
```

*   inside your onCreate method configure Pay Theory

```Kotlin
val amountOfPayment = 5236
payTheoryFragment.configure(apiKey,amountOfPayment, PaymentType.CREDIT, false, false, FeeMode.SURCHARGE)
```

*   by default the SDK provides a simple credit card input
*   applying optional settings allows customization

```Kotlin

// use ACH bank account
payTheoryFragment.configure(apiKey, amount, PaymentType.BANK)

// use ACH bank account and require account name
payTheoryFragment.configure(apiKey, amount, PaymentType.BANK, requireAccountName = true)

// use ACH bank account and require account name and billing address
payTheoryFragment.configure(apiKey, amount, PaymentType.BANK, requireAccountName = true, requireBillingAddress = true)

// use credit card account
payTheoryFragment.configure(apiKey, amount, PaymentType.CREDIT)

// use credit card and require account name
payTheoryFragment.configure(apiKey, amount, PaymentType.CREDIT, requireAccountName = true)

// use credit card and require account name and billing address
payTheoryFragment.configure(apiKey, amount, PaymentType.CREDIT, requireAccountName = true, requireBillingAddress = true)

// use cash
payTheoryFragment.configure(apiKey, amount, PaymentType.CASH)

```

React to events by implementing the Payable interface

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
...

}
```

## Results

The payment or barcode results will come back complete or failed

*   complete ach/card payment result
 
```Kotlin
data class PaymentResult (
    @SerializedName("receipt_number") val receipt_number: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("state") val state: String,
    @SerializedName("amount") val amount: Int?,
    @SerializedName("service_fee") val service_fee: String?,
    @SerializedName("tags") val tags: Map<String,String>?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?
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

*   other errors will arrive as

```Kotlin
data class PaymentError (
    @SerializedName("reason") val reason: String)
```

## Testing
A Java version between 8 and 16 is required to execute Gradle commands.

Unit testing

The following commands can be ran in the command prompt:

Windows
```powershell
gradlew unitTesting
```

Linux
```shell
./gradlew unitTesting
```

Unit and UI testing (Requires an android device or emulator)

The following commands can be ran in the command prompt:

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
