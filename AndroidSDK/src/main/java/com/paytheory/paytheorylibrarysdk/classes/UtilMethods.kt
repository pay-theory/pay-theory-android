
package com.paytheory.paytheorylibrarysdk.classes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast


object UtilMethods {


    /**
     * @param context
     * @action show progress loader
     */
    fun showLoading(context: Context){

    }

    /**
     * @action hide progress loader
     */
    fun hideLoading(){


    }


    /**
     * @param context
     * @action show Long toast message
     */
    fun showLongToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


    /**
     * @param context
     * @return true or false mentioning the device is connected or not
     * @brief checking the internet connection on run time
     */
    fun isConnectedToInternet(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }

}
