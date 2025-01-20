# Pay Theory Android SDK

## Requirements

Pay Theory's Android SDK is available for Android SDK >= 26

## Register your application

Register your app in Pay Theory's merchant portal to use Pay Theory Android SDK

For each mobile app, you want to register

- enter your application package name

- enter your application description

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

- Add Pay Theory AndroidSDK to your app level `build.gradle` file

```kotlin
dependencies {
    ...
    implementation group: 'com.paytheory.android.sdk', name: 'AndroidSDK', version: 'LATEST_VERSION'
    ...
}
```

### Secure your API key

> **_NOTE:_**  While Pay Theory's API key is not considered a *secret key*
> it is still a good practice to avoid exposing it in your source code repository
> below is one approach you can take to adding your API key as a build resource

- Add API key to your project level `local.properties` file

```dotenv
API_KEY=YOUR-API-KEY
```

- Update app level build.gradle file
  - Read in and parse local properties
  - set your API key as a build resource


```kotlin
File secretPropsFile = project.rootProject.file('local.properties')
Properties p = new Properties()
new FileInputStream(secretPropsFile).withCloseable { is -> p.load(is) }
p.each { name, value -> ext[name] = value }

android {
    ...
    buildTypes {
        debug {
            ...
            resValue("string", "api_key", "${API_KEY}")
            ...
        }
        release {
            ...
            resValue("string", "api_key", "${API_KEY}")
            ...
        }
    }
    ...
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
        ...
        minSdk 26
        targetSdk 33
        ...
    }
```

## Usage

### Layout

Add PayTheoryFragment as a fragment in your activity's xml layout file

```xml
    <fragment
        android:id="@+id/payTheoryFragment"
        android:name="com.paytheory.android.sdk.fragments.PayTheoryFragment" />
```

### Code

- Implement the Payable interface from your activity activity

```Kotlin
class MainActivity : AppCompatActivity() , Payable {
    ...
}
```

- Set your api key.

```Kotlin
val apiKey = resources.getString(R.string.api_key)
```

- Set PayTheoryFragment inside your `onCreate` method.

```Kotlin
val payTheoryFragment = this.supportFragmentManager.findFragmentById(R.id.payTheoryFragment) as PayTheoryFragment
```

- Configure PayTheoryFragment with a PayTheoryConfiguration.

> **_NOTE:_**  PayTheoryConfiguration allows you to set a number of optional values
> the button used to trigger a payment / tokenization process and your apiKey are 
> required to construct a PayTheoryConfiguration.

```Kotlin
val configuration = PayTheoryConfiguration(submitButton, apiKey)
```

#### PayTheoryButton

> **_NOTE:_**  An instance of PayTheoryButton is required so it can be enabled or disabled 
> depending on the validity of the payment details being entered.
> PayTheoryButton can be extended and a custom version can be provided to handle responses
> to data entry in controlled fields.

```kotlin
    /**
     * Override disable function to react when Pay Theory disables payment
     */
    fun disable(){
        this.isEnabled = false
    }

    /**
     * Override enable function to react when Pay Theory enables payment
     */
    fun enable(){
        this.isEnabled = true
    }
```

#### Valid and Empty protocol

- PayTheoryFragment exposes the state of critical payment fields such as card number, security code, and expiration date
- These values can be accessed using the following syntax:

```kotlin
    payTheoryFragment.card.cardNumber.isValid()
    payTheoryFragment.card.cardNumber.isEmpty()
```

- exposed fields for card include card number, CVV, expiration date and postal code
- exposed fields for bank include account number and routing number
- exposed fields for cash include full name and contact

#### Additional Configurations

Before accepting a payment some additional configuration settings are required

- an amount and payment method type must be set for payments
- a payment method type must be set for tokenization, but amount must not be set
- if not set payment method type defaults to CARD
- CASH payment method type is not allowed for tokenization

```kotlin
        configuration.paymentMethodType = PaymentMethodType.BANK
        configuration.amount = 15000
```

- optionally additional data can be attached to your request

```kotlin
        configuration.metadata = metadata /** HashMap<Any,Any> **/
        configuration.payorInfo = payorInfo /** PayorInfo **/
        configuration.requireAccountName = true
        configuration.requireBillingAddress = true
        configuration.accountCode = "Test Account Code"
        configuration.reference = "Test Reference"
        configuration.sendReceipt = true
        configuration.receiptDescription = "Android Payment Receipt Test"
```

#### Payment Requests

Here is an example on how to use PayTheoryFragment class method transact():

```kotlin
try {
    payTheoryFragment.configurePayment(
        configuration
    )

    submitButton.setOnClickListener{
        payTheoryFragment.transact()
    }

} catch (e: Exception) {
    e.printStackTrace()
}
```

#### Tokenization Requests

Here is an example on how to use PayTheoryFragment class method tokenize():

```kotlin
try {
    payTheoryFragment.configureTokenize(
        configuration
    )
    
    submitButton.setOnClickListener{
        payTheoryFragment.tokenize()
    }
} catch (e: Exception) {
    e.printStackTrace()
}
```

#### Results

The transaction results will come back through your Payable interface as these classes:

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

- successful barcode request result

```Kotlin
data class BarcodeResult (
    @SerializedName("BarcodeId") val barcodeId: String,
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

## Support/Feedback

Feel free to get in touch:

- [GitHub Issues](https://github.com/pay-theory/pay-theory-android/issues) - For any issues or feedback
- [Support](https://paytheory.com/) / [support@paytheory.com](mailto:support@paytheory.com)

## License

The Pay Theory Android SDK is open source and available under the MIT license. See [LICENSE](https://github.com/pay-theory/pay-theory-android/blob/main/LICENSE)

MIT © [LICENSE](https://github.com/pay-theory/pay-theory-android/blob/main/LICENSE)


