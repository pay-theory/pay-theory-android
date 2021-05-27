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

### Install jitpack to top level Gradle

```gradle
allprojects {
    repositories {
        google()
        maven { url 'https://jitpack.io' }
        jcenter()
    }
}
```

### Install SDK to application level Gradle

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

## Usage

### Layout

add a fragment container to your layout to hold the Pay Theory Fragment

```xml
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/payTheoryContainer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
/>
```

### Code

create a PayTheoryFragment and set your API key

Kotlin
```Kotlin
    private val payTheoryFragment = PayTheoryFragment()
    private val apiKey = "YOUR-API-KEY"
```

Java
```Java
   private final PayTheoryFragment payTheoryFragment = new PayTheoryFragment();
   private final String apiKey = "pt-sandbox-finix-3f77175085e9834c6f514a77eddfdb87";
```

from onCreateView add PayTheoryFragment to the view

Kotlin
```Kotlin
override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        this.childFragmentManager.beginTransaction()
            .add(R.id.payTheoryContainer, payTheoryFragment)
            .commit()

        return root
    }
```

Java
```Java
public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      

      this.getChildFragmentManager().beginTransaction().add(R.id.payTheoryContainer, (Fragment)this.payTheoryFragment).commit();
      
      return root;
   }
```

once you have an amount established configure Pay Theory

Kotlin
```Kotlin
    
    val amount = 5000
    payTheoryFragment.configure(apiKey,amount)

```

Java
```Java
    
    int amount = 5000;
    this.payTheoryFragment.configure(this.apiKey, amount)

```

*   by default the SDK provides a simple credit card input
*   applying optional settings allows customization

```Kotlin

    // use ACH bank account
    payTheoryFragment.configure(apiKey, amount, PaymentType.BANK)

    // use ACH bank account and require billing address
    payTheoryFragment.configure(apiKey, amount, PaymentType.CREDIT, requireAccountName:true, requireBillingAddress:true)

    // use credit card account
    payTheoryFragment.configure(apiKey, amount, PaymentType.CREDIT)

    // use credit card and require account name
    payTheoryFragment.configure(apiKey, amount, PaymentType.CREDIT, requireAccountName:true)

    // use credit card and require billing address
    payTheoryFragment.configure(apiKey, amount, PaymentType.CREDIT, requireAccountName:true, requireBillingAddress:true)

    // coming soon
    // use cash
    // payTheoryFragment.configure(apiKey, amount, PaymentType.CASH)

```

React to events by implementing the Payable interface

Kotlin
```Kotlin
class MainActivity : AppCompatActivity() , Payable {

...
    override fun paymentComplete(paymentResult: PaymentResult) {
    }

    override fun paymentFailed(paymentFailure: PaymentResult) {
    }

    override fun paymentError(paymentError: PaymentError) {
    }
...
}
```

Java
```Java
public class MainActivity extends AppCompatActivity implements Payable {

...
    @Override
    public void paymentComplete(@NotNull PaymentResult paymentResult) {
    }

    @Override
    public void paymentError(@NotNull PaymentError paymentError) {
    }

    @Override
    public void paymentFailed(@NotNull PaymentResult paymentResult) {
    }
...
}
```

## PaymentResult

The payment result will come back complete or failed

*   complete result
 
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
    @SerializedName("updated_at") val updated_at: String?)
```
*   failed result

```Kotlin
data class PaymentResult (
    @SerializedName("receipt_number") val receipt_number: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("state") val state: String,
    @SerializedName("type") val type: String?,)
```

*   errors will arrive as

```Kotlin
data class PaymentError (
    @SerializedName("reason") val reason: String)
```

## Testing
There are two different ways you are able to run testing:

Unit testing
The following commands can be ran in the command prompt:

Windows
```
gradlew unitTesting
```

Linux
```
./gradlew unitTesting
```

Unit and UI testing (Requires an android device or emulator)
The following commands can be ran in the command prompt:

Windows
```
gradlew completeTesting
```

Linux
```
./gradlew completeTesting
```

## License

MIT © [pay theory](https://github.com/pay-theory)
