package com.paytheory.lib.compose

import android.content.Context
import android.icu.util.TimeZone
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.paytheory.lib.PayTheoryConfiguration
import com.google.gson.Gson
import com.paytheory.lib.compose.utility.NetworkUtils
import com.paytheory.lib.compose.utility.PaymentFormUtils

/**
 * @deprecated Use NetworkUtils.NO_NETWORK_CONNECTION instead
 */
@Deprecated(
    message = "Use NetworkUtils.NO_NETWORK_CONNECTION instead",
    replaceWith = ReplaceWith("NetworkUtils.NO_NETWORK_CONNECTION", "com.paytheory.lib.compose.utility.NetworkUtils")
)
const val NO_NETWORK_CONNECTION = NetworkUtils.NO_NETWORK_CONNECTION

/**
 * @deprecated Use NetworkUtils.isNetworkAvailable instead
 */
@Deprecated(
    message = "Use NetworkUtils.isNetworkAvailable instead",
    replaceWith = ReplaceWith("NetworkUtils.isNetworkAvailable(context)", "com.paytheory.lib.compose.utility.NetworkUtils")
)
internal fun isNetworkAvailable(context: Context) = NetworkUtils.isNetworkAvailable(context)

/**
 * Creates payTheoryData object for transfer requests
 * @param configuration PayTheoryConfiguration object
 * @return HashMap<Any, Any> of pay theory data for the request
 */
@Deprecated(
    message = "Use PaymentFormUtils.createPayTheoryData instead",
    replaceWith = ReplaceWith("PaymentFormUtils.createPayTheoryData(configuration)", "com.paytheory.lib.compose.utility.PaymentFormUtils")
)
internal fun createPayTheoryData(configuration: PayTheoryConfiguration): HashMap<Any, Any> =
    PaymentFormUtils.createPayTheoryData(configuration)