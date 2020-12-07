
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
            intent.putExtra("Api-Key", d9de91546564990737dd2f8049nhjy9dd6")

            //Set Display type ("Card-Only" or "Card-Account")
            intent.putExtra("Display", "Card-Only")

            //Set Buyer Options ("True" or "False")
            intent.putExtra("Buyer-Options", "True")

            //Set Buyer Options data
            intent.putExtra("First-Name", "firstName")
            intent.putExtra("Last-Name", "lastName")
            intent.putExtra("Address-One", "addressOne")
            intent.putExtra("Address-Two", "addressTwo")
            intent.putExtra("City", "city")
            intent.putExtra("State", "state")
            intent.putExtra("Country", "country")
            intent.putExtra("Zip-Code", "zipcode")
            intent.putExtra("Phone-Number", "phoneNumber")
            intent.putExtra("Email-Address", "emailAddress")

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

#### Required

Payment-Amount (Required)

```kotlin
//Set Payment Amount in cents ($50.25 = "5025")
intent.putExtra("Payment-Amount", "4000")
```

Api-Key (Required)

```kotlin
//Set Api-Key
intent.putExtra("Api-Key", d9de91546564990737dd2f8049nhjy9dd6")
```

Display type (Required)

```kotlin
//Set Display type ("Card-Only" or "Card-Account")
intent.putExtra("Display", "Card-Only")
```

#### Optional

Buyer-Options (Optional)

```kotlin
//Set Buyer Options ("True" or "False")
intent.putExtra("Buyer-Options", "True")

//Set Buyer Options data
intent.putExtra("First-Name", "firstName")
intent.putExtra("Last-Name", "lastName")
intent.putExtra("Address-One", "addressOne")
intent.putExtra("Address-Two", "addressTwo")
intent.putExtra("City", "city")
intent.putExtra("State", "state")
intent.putExtra("Country", "country")
intent.putExtra("Zip-Code", "zipcode")
intent.putExtra("Phone-Number", "phoneNumber")
intent.putExtra("Email-Address", "emailAddress")
```

Fee-Mode (Optional)

```kotlin
//Set Fee Mode ("surcharge" or "service-fee")
intent.putExtra("Fee-Mode", "service_fee")
```

Custom-Tags (Optional)

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
    "state":"SUCCEEDED",
    "tags":{ "pay-theory-environment":"env","pt-number":"pt-env-XXXXXX", "YOUR_TAG_KEY": "YOUR_TAG_VALUE" }
}
```

If a failure or decline occurs during the transaction, the response will be similar to the following:

```json
{
    "receipt_number":"pt-test-XXXXXX",
    "last_four":"XXXX",
    "brand":"VISA",
    "state":"FAILURE",
    "type":"some descriptive reason for the failure / decline"
}
```

## Style

Go to your applications [AndroidManifest.xml](https://github.com/pay-theory/pay-theory-android/blob/main/Example%20Application/src/main/AndroidManifest.xml) file
Change theme for application to ensure PayTheoryActivity has same theme as application

```kotlin
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
    //Button that will start PayTheoryActivity
    var toPaymentButton = findViewById<Button>(R.id.toPayment)

    //Buyer-Options = True , Display = "Card-Only", Tags = "My Custom Tags"
    toPaymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
        val intent = Intent(this, PayTheoryActivity::class.java)

        //Set Payment Amount in cents ($50.25 = "5025")
        intent.putExtra("Payment-Amount", "4000")

        //Set Api-Key
        intent.putExtra("Api-Key", d9de91546564990737dd2f8049nhjy9dd6")

        //Set Display type ("Card-Only" or "Card-Account")
        intent.putExtra("Display", "Card-Only")

        //Set Buyer Options ("True" or "False")
        intent.putExtra("Buyer-Options", "True")

        //Set Buyer Options data
        intent.putExtra("First-Name", "firstName")
        intent.putExtra("Last-Name", "lastName")
        intent.putExtra("Address-One", "addressOne")
        intent.putExtra("Address-Two", "addressTwo")
        intent.putExtra("City", "city")
        intent.putExtra("State", "state")
        intent.putExtra("Country", "country")
        intent.putExtra("Zip-Code", "zipcode")
        intent.putExtra("Phone-Number", "phoneNumber")
        intent.putExtra("Email-Address", "emailAddress")

        //Set Fee Mode ("surcharge" or "service-fee")
        intent.putExtra("Fee-Mode", "service_fee")

        //Set Custom Tags for payments
        intent.putExtra("Tags-Key", "tagKey")
        intent.putExtra("Tags-Value", "tagValue")

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

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    //Button that will start PayTheoryActivity
    var toPaymentButton = findViewById<Button>(R.id.toPayment)

    //Buyer-Options = True , Display = "Card-Only", Tags = "My Custom Tags"
    toPaymentButton.setOnClickListener { //On Click Listener to start PayTheoryActivity with Buyer Options Fields
        val intent = Intent(this, PayTheoryActivity::class.java)

        //Set Payment Amount in cents ($50.25 = "5025")
        intent.putExtra("Payment-Amount", "6523")

        //Set Api-Key
        intent.putExtra("Api-Key", d9de91546564990737dd2f8049nhjy9dd6")

        //Set Display type ("Card-Only" or "Card-Account")
        intent.putExtra("Display", "Card-Account")

        //Set Buyer Options ("True" or "False")
        intent.putExtra("Buyer-Options", "False")

        //Set Fee Mode ("surcharge" or "service-fee")
        intent.putExtra("Fee-Mode", "surcharge")

        //Set Custom Tags for payments
        intent.putExtra("Tags-Key", "Customer ID")
        intent.putExtra("Tags-Value", "customerId")

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