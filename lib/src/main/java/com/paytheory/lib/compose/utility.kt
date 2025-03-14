package com.paytheory.lib.compose

import android.content.Context
import android.icu.util.TimeZone
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.paytheory.lib.PayTheoryConfiguration

const val NO_NETWORK_CONNECTION = "No valid network connection"




internal fun isNetworkAvailable(context: Context) =
    (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
        getNetworkCapabilities(activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } == true
    }


/**
 * Creates payTheoryData object for transfer requests
 * @param configuration PayTheoryConfiguration object
 * @return HashMap<Any, Any> of pay theory data for the request
 */
internal fun createPayTheoryData(configuration: PayTheoryConfiguration): HashMap<Any, Any> {
    //create pay_theory_data object for host:transfer_part1 action request
    val payTheoryData = hashMapOf<Any, Any>()

    payTheoryData["skip_validation"] = configuration.skipTokenizeValidation

    payTheoryData["send_receipt"] = configuration.sendReceipt
    if (configuration.sendReceipt == true) {

        if (configuration.receiptDescription.isNotBlank()){
            payTheoryData["receipt_description"] = configuration.receiptDescription
        }
    }
    // if paymentParameters is given add to pay_theory_data
    if (configuration.paymentParameters.isNotBlank()) {
        payTheoryData["payment_parameters"] = configuration.paymentParameters
    }
    // if payorId is given add to pay_theory_data
    if (configuration.payorId.isNotBlank()) {
        payTheoryData["payorId"] = configuration.payorId
    }
    // if invoiceId is given add to pay_theory_data
    if (configuration.invoiceId.isNotBlank()) {
        payTheoryData["invoice_id"] = configuration.invoiceId
    }
    // if account_code is given add to pay_theory_data
    if (configuration.accountCode.isNotBlank()) {
        payTheoryData["account_code"] = configuration.accountCode
    }
    // if reference is given add to pay_theory_data
    if (configuration.reference.isNotBlank()) {
        payTheoryData["reference"] = configuration.reference
    }

    payTheoryData["fee"] = configuration.serviceFee as Any

    if (configuration.apiKey == "test-paytheory-apikey") {
        payTheoryData["timezone"] = "test"
    } else {
        payTheoryData["timezone"] = TimeZone.getDefault().id
    }


    return payTheoryData
}