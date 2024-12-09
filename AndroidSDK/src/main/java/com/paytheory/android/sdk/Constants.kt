package com.paytheory.android.sdk

/**
 * Object that contains constant variables
 */
class Constants(partner: String, stage: String) {
    val API_BASE_PATH: String = "https://$partner.$stage.com/"

}
