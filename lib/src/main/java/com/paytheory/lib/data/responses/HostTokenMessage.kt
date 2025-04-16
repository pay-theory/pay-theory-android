package com.paytheory.lib.data.responses

import com.google.gson.annotations.SerializedName

/**
 * Data class to store host token message details
 * @param type
 * @param body
 */
data class HostTokenMessage (
    @SerializedName("type") val type: String,
    @SerializedName("body") val body: HostToken
) 