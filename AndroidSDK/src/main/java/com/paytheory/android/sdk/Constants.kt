package com.paytheory.android.sdk

/**
 * Object that contains constant variables
 */
class Constants(env: String) {
    val API_BASE_PATH: String
    val NO_INTERNET_ERROR: String

    init {
         API_BASE_PATH = "https://$env.tags.api.paytheorystudy.com/"
         NO_INTERNET_ERROR = "No internet connection"
    }
}
