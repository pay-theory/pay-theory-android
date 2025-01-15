package com.paytheory.android.sdk

import androidx.appcompat.app.AppCompatActivity
import com.paytheory.android.sdk.fragments.PayTheoryFragment

 open class PayTheoryMerchantActivity: AppCompatActivity() {
    var payTheoryFragment: PayTheoryFragment? = null
    fun initializePayTheoryActivity(fragment: PayTheoryFragment) {
        payTheoryFragment = fragment
    }
     fun clearFields() {
         runOnUiThread(Runnable() {
             payTheoryFragment?.clearFields()
       })
     }
}