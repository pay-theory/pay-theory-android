package com.paytheory.android.sdk

/**
 * Object that contains constant variables
 */
class Constants(partner: String, stage: String) {
    val API_BASE_PATH: String
    val NO_INTERNET_ERROR: String

    init {
         API_BASE_PATH = "https://$partner.token.service.$stage.com/$partner/"
         NO_INTERNET_ERROR = "No internet connection"
    }
}
