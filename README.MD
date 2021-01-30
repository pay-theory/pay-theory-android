# Pay Theory Android SDK

## Register your application

Before you can use Pay Theory Android SDK you must register your app in Pay Theory's merchant portal

![App Registration](http://books-ui-assets.s3-website-us-east-1.amazonaws.com/android/android-registration.png)

For each mobile app you want to register 
 * enter SHA-256 digest of release signing key
 * enter SHA-256 digest of debug signing key
 * enter your application package name

### Find sigining key digest

You can find you signing key digests using Android Studio

![Open Gradle](http://books-ui-assets.s3-website-us-east-1.amazonaws.com/android/open-gradle)

In upper right corner of Android Studio click on Gradle

![Signing Report](http://books-ui-assets.s3-website-us-east-1.amazonaws.com/android/signing-report)

Select Signing Report

![Run Report](http://books-ui-assets.s3-website-us-east-1.amazonaws.com/android/run-report)

Right click and choose run

![SHA-256](http://books-ui-assets.s3-website-us-east-1.amazonaws.com/android/sha-256)

Copy your SHA-256 digest

## Install SDK to gradle

### Install jitpack to top level gradle

```gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```

### Install SDK to application level gradle

```gradle
dependencies {

    ...
    implementation 'com.github.pay-theory:pay-theory-android:1.1.1-alpha'
    ...
}
}
```

## Usage

### Layout

add Pay Theory Fragment to your layout

```xml
<SomeLayout>

    <fragment
        android:id="@+id/payTheoryFragment"
        android:name="com.paytheory.android.sdk.fragments.PayTheoryFragment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        />

</SomeLayout>
```

### Activity

retrieve the fragment from your activity's onCreate and initialize

Kotlin
```Kotlin
        val payTheoryFragment = this.supportFragmentManager
                .findFragmentById(R.id.payTheoryFragment)

        val payTheoryArgs = Bundle()
        payTheoryArgs.putString(PayTheoryFragment.API_KEY, "My-API-Key")
        payTheoryArgs.putInt(PayTheoryFragment.AMOUNT,5000)

        payTheoryFragment!!.arguments = payTheoryArgs
```

Java
```Java
       Fragment payTheoryFragment = this.getSupportFragmentManager()
                .findFragmentById(R.id.payTheoryFragment);

       Bundle payTheoryArgs = new Bundle();
       payTheoryArgs.putString(PayTheoryFragment.Companion.getAPI_KEY(), "My-API-Key");
       payTheoryArgs.putInt(PayTheoryFragment.Companion.getAMOUNT(),5000);

       payTheoryFragment.setArguments(payTheoryArgs);
```

 * by default the SDK provides a simple credit card input
 * applying optional settings allows customiztion

```Kotlin
        // display an account name field
        payTheoryArgs.putBoolean(PayTheoryFragment.ACCOUNT_NAME_ENABLED,true)

        // display billing address fields
        payTheoryArgs.putBoolean(PayTheoryFragment.BILLING_ADDRESS_ENABLED,true)

        // collect ACH bank details instead of credi card
        payTheoryArgs.putBoolean(PayTheoryFragment.USE_ACH,true)
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

 * complete result
 
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
 * failed result

```Kotlin
data class PaymentResult (
    @SerializedName("receipt_number") val receipt_number: String,
    @SerializedName("last_four") val last_four: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("state") val state: String,
    @SerializedName("type") val type: String?,)
```

 * errors will arrive as

```Kotlin
data class PaymentError (
    @SerializedName("reason") val reason: String,)
```

## License

MIT © [pay theory](https://github.com/pay-theory)
