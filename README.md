
# PayTheory Android SDK

## Setup

Create or open an Android project

Create or open an Activity file that will use Pay Theory Library.

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}
```

Create a button on your Activity layout.xml file that will initiate the payment page

```kotlin
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/payment_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Button"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Add internet permission to your android project's manifest.xml file. 

```kotlin
<uses-permission android:name="android.permission.INTERNET" />
```

Here is an example:

```kotlin
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.testingpaytheory">

    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TestingPayTheory">
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

Add library to your applications dependencies using jitpack.io 
+ Go to https://jitpack.io/
+ Enter https://github.com/pay-theory/pay-theory-android in the "Look up"
+ A list of versions should appear. You will use the most up-to-date version.

Instructions for Gradle:
+ In you project's root build.gradle file you will add "maven { url 'https://jitpack.io' }" 
(Make sure to add it under "allprojects" and "repositories". Also make sure it is under ALL existing repositories )

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    ext.kotlin_version = "1.4.20"
    repositories {
        google()
        jcenter()

    }
    dependencies {
        classpath "com.android.tools.build:gradle:4.1.1"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

+ On jitpack.io click on "Get it" next to the version you are adding
+ jitpack will give you a line of code to copy 

```kotlin
dependencies {
	        implementation 'com.github.pay-theory:pay-theory-android:0.0.1
	}
```

+ add the implementation to your applications build.gradle file
Here is an example of the build.grade file:

```kotlin
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    compileSdkVersion 30
    buildToolsVersion "30.0.2"

    defaultConfig {
        applicationId "com.example.testingpaytheory"
        minSdkVersion 16
        targetSdkVersion 30
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
}

dependencies {

    implementation 'com.github.pay-theory:pay-theory-android:0.0.1'

    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    implementation 'androidx.core:core-ktx:1.3.2'
    implementation 'androidx.appcompat:appcompat:1.2.0'
    implementation 'com.google.android.material:material:1.2.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
    testImplementation 'junit:junit:4.+'
    androidTestImplementation 'androidx.test.ext:junit:1.1.2'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
}
```

+ Sync and build project so Pay Theory Library can be imported into your application


## Usage

In your Activity's "onCreate" method:
+ create a variable to reference the button
+ add the setOnClickListener call
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button that will start PayTheoryActivity
        var paymentButton = findViewById<Button>(R.id.payment_button)

        setOnClickListener("ACH", paymentButton)

    }
}
```

Add these three methods inside the onCreate method

+ setOnClickListener()
This method initiates the Pay Theory payment page. 
paymentType - "ACH" or "Card" (Display input fields for the specific payment method)
button - Button that will start Pay Theory payment page

+ onActivityResult()
This method will return the result after a payment has been submitted.
returnString - result of payment request as a String in JSON format

+ showToast()
This method will display an alert of a string that is passed in.

Here is an example of how you will add to the onCreate method:
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Button that will start PayTheoryActivity
        var paymentButton = findViewById<Button>(R.id.payment_button)

        setOnClickListener("ACH", paymentButton)
    }

    //Payment Type should be ("Card" or "ACH")
    fun setOnClickListener(paymentType : String, button: Button) {
        button.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
            val intent = Intent(this, PayTheoryActivity::class.java)

            //Set Full-Account-Details ("True" or "False")
            intent.putExtra("Full-Account-Details", "True")

            //Payment Type is set ("Card" or "ACH")
            intent.putExtra("Payment-Type", paymentType)

            //Set Fee Mode ("surcharge" or "service-fee")
            intent.putExtra("Fee-Mode", "surcharge")

            //Set Payment Amount in cents ($50.25 = "5025")
            intent.putExtra("Payment-Amount", "5025")

            //Set Api-Key
            intent.putExtra("Api-Key", "MY API KEY")


            //Set Custom Tags for payments ( { Tags-Key : Tags-Value } )
            intent.putExtra("Tags-Key", "My Custom Tags")
            intent.putExtra("Tags-Value", "My Custom Tags Value")

            //Set Buyer Options ("True" or "False")
            intent.putExtra("Buyer-Options", "True")

            //Set Buyer Options data
            intent.putExtra("First-Name", "Buyer")
            intent.putExtra("Last-Name", "Options")
            intent.putExtra("Address-One", "123 Options Lane")
            intent.putExtra("Address-Two", "Apt 1")
            intent.putExtra("City", "Cincinnati")
            intent.putExtra("State", "OH")
            intent.putExtra("Country", "USA")
            intent.putExtra("Zip-Code", "45236")
            intent.putExtra("Phone-Number", "513-123-1234")
            intent.putExtra("Email-Address", "test@paytheory.com")

            //Start PayTheoryActivity
            startActivityForResult(intent, 1);
        }
    }


    // This method is called when the PayTheoryActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Get String data from PayTheoryActivity
                val returnString = data!!.getStringExtra("result")
                Log.d("Pay Theory", "Here is the result data string : $returnString")
                if (returnString != null) {
                    showToast(returnString)
                }
            } else {
                showToast("Error getting result data")
            }
        }
    }

    private fun showToast(message: String){
        Toast.makeText(
                this, message,
                Toast.LENGTH_LONG
        ).show()
    }
}
```

### Set configurations for Pay Theory Activity: 

#### Payment-Type (Required)

Allows you to dynamically change input fields for a card or bank account

```kotlin
//Payment Type is set ("Card" or "ACH")
intent.putExtra("Payment-Type", "Card")
```

#### Payment-Amount (Required)

Payment amount that will be submitted

```kotlin
//Set Payment Amount in cents ($50.25 = "5025")
intent.putExtra("Payment-Amount", "4000")
```

#### Api-Key (Required)

```kotlin
//Set Api-Key
intent.putExtra("Api-Key", "d9de91546564990737dd2f8049nhjy9dd6")
```

#### Full-Account-Details (Required)

```kotlin
//Set Full-Account-Details ("True" or "False")
intent.putExtra("Full-Account-Details", "True")
```

 **"True"**  
 
 True will display more fields for user to fill out for the payment transaction:
 
 + First name field  
 + Last name field  
 + Address one field  
 + Address two field  
 + City field  
 + State field  
 + Zip code field  
 + All required payment fields
 
**OR**  
  
**"False"**  

False will display fields only required for a payment transaction:

 + All required payment fields

#### Buyer-Options (Optional)

```kotlin
//Set Buyer Options ("True" or "False")
intent.putExtra("Buyer-Options", "True")

//Set Buyer Options data
intent.putExtra("First-Name", "Henry")
intent.putExtra("Last-Name", "Smith")
intent.putExtra("Address-One", "123 Greenwood Drive")
intent.putExtra("Address-Two", "Apt 2")
intent.putExtra("City", "Cincinnati")
intent.putExtra("State", "OH")
intent.putExtra("Country", "USA")
intent.putExtra("Zip-Code", "45236")
intent.putExtra("Phone-Number", "513-111-1111")
intent.putExtra("Email-Address", "Hsmith@gmail.com")
```

#### Fee-Mode (Optional)

The Fee-Mode is defaulted to "surcharge"

```kotlin
//Set Fee Mode ("surcharge" or "service-fee")
intent.putExtra("Fee-Mode", "service_fee")
```

#### Custom-Tags (Optional)

Add custom tags as key-value pair to transactions (Customer ID, Tracking #, etc.)
Example Tags:
 ```kotlin
 { Tags-Key : Tags-Value }
```

```kotlin
//Set Custom Tags for payments 
intent.putExtra("Tags-Key", "tagKey")
intent.putExtra("Tags-Value", "tagValue")
```


## Handle Response

This method is used to retrieve result data once Pay Theory Activity has completed.
You can use the "returnString" variable to get completion response.

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 1) {
        if (resultCode == Activity.RESULT_OK) {
            // Get String data from PayTheoryActivity
            val returnString = data!!.getStringExtra("keyName")
            Log.d("Main Activity","Here is the result data string : $returnString")
	     }
	 }
}
```

### Completion response

Upon completion of authorization and capture, details similar to the following are returned:

*note that the service fee is included in amount*

```json
{
    "receipt_number":"pt-env-XXXXXX",
    "last_four": "XXXX",
    "brand": "XXXXXXXXX",
    "created_at":"YYYY-MM-DDTHH:MM:SS.ssZ",
    "amount": 999,
    "service_fee": 195,
    "OH":"SUCCEEDED",
    "tags":{ "pay-theory-environment":"env","pt-number":"pt-env-XXXXXX", "YOUR_TAG_KEY": "YOUR_TAG_VALUE" }
}
```

If a failure or decline occurs during the transaction, the response will be similar to the following:

```json
{
    "receipt_number":"pt-test-XXXXXX",
    "last_four":"XXXX",
    "brand":"VISA",
    "OH":"FAILURE",
    "type":"some descriptive reason for the failure / decline"
}
```

## Style

Go to your applications [AndroidManifest.xml](https://github.com/pay-theory/pay-theory-android/blob/main/Example%20Application/src/main/AndroidManifest.xml) file
Change theme for application to ensure PayTheoryActivity has same theme as application

```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.PayTheoryLibrary">
<!--android:theme="@style/Theme.AppCompat.DayNight">-->

</application>
```

## License

PayTheory is available under the MIT license. See the LICENSE file for more info.