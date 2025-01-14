package com.paytheory.android.sdk

/**
 * Object that contains constant variables
 */
class Constants(partner: String, stage: String) {
    val apiBasePath: String = "https://$partner.$stage.com/"

}
