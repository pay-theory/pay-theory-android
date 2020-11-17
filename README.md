
# PayTheory

## How to use PayTheory

1. Add library to project

    implementation ''

2. Import Activity in Activity that will will request to pay

	import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity

3. Add on click listener to button that will request to start Pay Theory Activity

```
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    var toPaymentButton = findViewById<Button>(R.id.toPayment)
    toPaymentButton.setOnClickListener {
        val intent = Intent(this, PayTheoryActivity::class.java)
        startActivityForResult(intent, 1);
	 }
}
```

4. Add method to retrieve result data once Pay Theory Activity has completed

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

## How to change Pay Theory Activity Theme
1. Go to [PayTheoryActivity.kt ](https://github.com/pay-theory/pay-theory-android/blob/main/PayTheorySDK/src/main/java/com/paytheory/paytheorylibrarysdk/paytheory/PayTheoryActivity.kt)

2. In PayTheoryActivity.kt change the theme (**setTheme(R.style.DarkTheme)**)

```
override fun onCreate(savedInstanceState: Bundle?) {
    //Allows the style of an activity if setTheme is added
    setTheme(R.style.DarkTheme)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_pay_theory)
```

## Author

PayTheory

## License

PayTheory is available under the MIT license. See the LICENSE file for more info.