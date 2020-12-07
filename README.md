
# PayTheory Android SDK

## Setup

Add library to your applications dependencies using jcenter's repository

```kotlin

compile 'com.paytheory.android:pay-theory-android:0.1'

```

Import PayTheoryActivity in your Activity that will have payment on click listener

```kotlin
import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity
```

Copy this code into your Activity

```kotlin
class ExampleAppMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
        toPaymentButton.setOnClickListener { 
            val intent = Intent(this, PayTheoryActivity::class.java)

            //Set Payment Amount in cents ($50.25 = "5025")
            intent.putExtra("Payment-Amount", "4000")

            //Set Api-Key
            intent.putExtra("Api-Key", "d9de91546564990737dd2f8049nhjy9dd6")

            //Set Display type ("Card-Only" or "Card-Account")
            intent.putExtra("Display", "Card-Only")

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

            //Set Fee Mode ("surcharge" or "service-fee")
            intent.putExtra("Fee-Mode", "service_fee")

            //Set Custom Tags for payments
            intent.putExtra("Tags-Key", "tagKey")
            intent.putExtra("Tags-Value", "tagValue")

            //Start PayTheoryActivity
            startActivityForResult(intent, 1);
        }
    }
}
```


## Usage

### Set configurations for Pay Theory Activity: 

#### Payment-Amount (Required)

```kotlin
//Set Payment Amount in cents ($50.25 = "5025")
intent.putExtra("Payment-Amount", "4000")
```

#### Api-Key (Required)

```kotlin
//Set Api-Key
intent.putExtra("Api-Key", "d9de91546564990737dd2f8049nhjy9dd6")
```

#### Display type (Required)

Select which type of display you prefer for you application:

1. **Card-Only**
-Card number field
-Card expiration month field
-Card expiration year field
-Card cvv field


**OR**


2. **Card-Account**
-First name field
-Last name field
-Address one field
-Address two field
-City field
-State field
-Zip code field
-Card number field
-Card expiration month field
-Card expiration year field
-Card cvv field

```kotlin
//Set Display type ("Card-Only" or "Card-Account")
intent.putExtra("Display", "Card-Only")
```

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

Add custom tags to transactions (Customer ID, Tracking #, etc.)

```kotlin
//Set Custom Tags for payments
intent.putExtra("Tags-Key", "tagKey")
intent.putExtra("Tags-Value", "tagValue")
```


## Handle Response

Copy this code into your Activity. This method is used to retrieve result data once Pay Theory Activity has completed

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

## Complete Code Samples

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    
    var toPaymentButton = findViewById<Button>(R.id.toPayment)

    toPaymentButton.setOnClickListener { 
        val intent = Intent(this, PayTheoryActivity::class.java)

        intent.putExtra("Payment-Amount", "4632")

        intent.putExtra("Api-Key", "d9de91546564990737dd2f8049nhjy9dd6")

        intent.putExtra("Display", "Card-Only")

        intent.putExtra("Buyer-Options", "True")

        intent.putExtra("First-Name", "Henry")
        intent.putExtra("Last-Name", "Smith")
        intent.putExtra("Address-One", "123 Greenwood Drive")
        intent.putExtra("Address-Two", "Apt 2")
        intent.putExtra("City", "Cincinnati")
        intent.putExtra("State", "OH")
        intent.putExtra("Country", "USA")
        intent.putExtra("Zip-Code", "45236")
        intent.putExtra("Phone-Number", "513-111-1111")
        intent.putExtra("Email-Address", "H_smith@gmail.com")

        intent.putExtra("Fee-Mode", "service_fee")

        intent.putExtra("Tags-Key", "customerID")
        intent.putExtra("Tags-Value", "ID-12548")

        startActivityForResult(intent, 1);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                val returnString = data!!.getStringExtra("keyName")

                Log.e("Main Activity","Here is the result data string : $returnString")
            }
        }
    }
}
```

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    //Button that will start PayTheoryActivity
    var toPaymentButton = findViewById<Button>(R.id.toPayment)

        //Buyer-Options = True , Display = "Card-Only", Tags = "My Custom Tags"
        //On Click Listener to start PayTheoryActivity with Buyer Options Fields
        toPaymentButton.setOnClickListener { 
        val intent = Intent(this, PayTheoryActivity::class.java)

        //Set Payment Amount in cents ($50.25 = "5025")
        intent.putExtra("Payment-Amount", "6523")

        //Set Api-Key
        intent.putExtra("Api-Key", "d9de91546564990737dd2f8049nhjy9dd6")

        //Set Display type ("Card-Only" or "Card-Account")
        intent.putExtra("Display", "Card-Account")

        //Set Buyer Options ("True" or "False")
        intent.putExtra("Buyer-Options", "False")

        //Fee Mode (defaults to "surcharge" if not added)

        //Set Custom Tags for payments
        intent.putExtra("Tags-Key", "customerID")
        intent.putExtra("Tags-Value", "ID-12648")

        //Start PayTheoryActivity
        startActivityForResult(intent, 1);
    }

    // This method is called when the PayTheoryActivity finishes
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Get String data from PayTheoryActivity
                val returnString = data!!.getStringExtra("keyName")
                Log.e("Main Activity","Here is the result data string : $returnString")
            }
        }
    }
}
```


## License

PayTheory is available under the MIT license. See the LICENSE file for more info.