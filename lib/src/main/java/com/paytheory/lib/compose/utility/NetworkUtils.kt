package com.paytheory.lib.compose.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utility class for network operations
 *
 * This class contains pure logic functions for checking network connectivity
 * following the testing strategy of isolating business logic from UI components.
 */
object NetworkUtils {
    
    /**
     * Constant for no network connection error message
     */
    const val NO_NETWORK_CONNECTION = "No valid network connection"
    
    /**
     * Checks if network connectivity is available on the device
     *
     * @param context The application or activity context
     * @return True if a network connection is available, false otherwise
     */
    fun isNetworkAvailable(context: Context): Boolean =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } == true
        }
} 