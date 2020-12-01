
# PayTheory

## How to use PayTheory

1. Add library to project

```
implementation ''
```

2. Import PayTheoryActivity in Activity that will request to submit payment

```
import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity
```

3. Copy into activity that will call Pay Theory by setOnClickListener

```
class ExampleAppMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

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
```

4. Set configurations: Payment-Amount (Required), Api-Key (Required), Display type (Required), Buyer-Options (Optional), Fee-Mode (Optional), Custom-Tags (Optional)

```
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
```

5. Add method to retrieve result data once Pay Theory Activity has completed

```
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
```

Here are complete examples

```
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

```
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

## How to change Pay Theory Activity Theme
1. Go to your applications [AndroidManifest.xml](https://github.com/pay-theory/pay-theory-android/blob/main/Example%20Application/src/main/AndroidManifest.xml) file

2. Change theme for application to ensure PayTheoryActivity has same theme as application

```
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.PayTheoryLibrary">
<!--        android:theme="@style/Theme.AppCompat.DayNight">-->

```

## Author

PayTheory

## License

PayTheory is available under the MIT license. See the LICENSE file for more info.