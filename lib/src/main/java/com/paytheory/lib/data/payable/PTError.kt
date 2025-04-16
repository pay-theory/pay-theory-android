package com.paytheory.lib.data.payable

import com.google.gson.annotations.SerializedName

/**
 * Data class that represents the error received if a transaction fails
 * @param code the PTErrorCode
 * @param error a description of the error
 */
data class PTError (
    @SerializedName("code") val code: ErrorCode,
    @SerializedName("error") val error: String
) 