# PayTheory

## How to use PayTheory

1. Add library to project

    implementation ''

2. import Activity in Activity that will will request to pay

import com.paytheory.paytheorylibrarysdk.paytheory.PayTheoryActivity

3. Add on click listener to button that will request to start Pay Theory Activity

    //Button that will start PayTheoryActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var toPaymentButton = findViewById<Button>(R.id.toPayment)
        toPaymentButton.setOnClickListener {
            val intent = Intent(this, PayTheoryActivity::class.java)
            startActivityForResult(intent, 1);
        }
    }

4. Add method to retrieve result data once Pay Theory Activity has completed

    // This method is called when the PayTheoryActivity finishes
    'override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Get String data from PayTheoryActivity
                val returnString = data!!.getStringExtra("keyName")
                Log.e("Main Activity","Here is the result data string : $returnString")
            }
        }
    }'




## Author

PayTheory

## License

PayTheory is available under the MIT license. See the LICENSE file for more info.