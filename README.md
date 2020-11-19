
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

3. Add on click listener to button that will request to start Pay Theory Activity

```
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    var toPaymentButton = findViewById<Button>(R.id.toPayment)

    toPaymentButton.setOnClickListener {
    ***NEXT STEPS***
	 }
}
```

4. Set Api-Key as string and set Payment-Amount as string in pennies

```
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

        //On Click Listener to start PayTheoryActivity
        toPaymentButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            //Set payment amount value in pennies
            intent.putExtra("Payment-Amount", "***Payment-Amount-Here***")
            //Set api-key value
            intent.putExtra("Api-Key", "***Api-Key-Here***")
            //Start PayTheoryActivity
            startActivityForResult(intent, 1);
        }
    }
```

Should look like this:

```
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

        //On Click Listener to start PayTheoryActivity
        toPaymentButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            //Set payment amount value in pennies
            intent.putExtra("Payment-Amount", "5050")
            //Set api-key value
            intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
            //Start PayTheoryActivity
            startActivityForResult(intent, 1);
        }
    }
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

Final code should look similar to this

```
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Button that will start PayTheoryActivity
        var toPaymentButton = findViewById<Button>(R.id.toPayment)

        //On Click Listener to start PayTheoryActivity
        toPaymentButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            //Set payment amount value in pennies
            intent.putExtra("Payment-Amount", "5050")
            //Set api-key value
            intent.putExtra("Api-Key", "pt-sandbox-dev-d9de9154964990737db2f80499029dd6")
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
                val returnString = data!!.getStringExtra("keyName")
                Log.e("Main Activity", "Here is the result data string : $returnString")
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