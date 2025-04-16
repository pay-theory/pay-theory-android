package com.paytheory.lib.data.requests

import com.google.gson.annotations.SerializedName

/**
 * Data class to store host token request
 * for testing attestation outside of production set requireAttestation to true
 */
data class HostTokenRequest(
    @SerializedName("ptToken") val ptToken: String,
    @SerializedName("attestation") val attestation: String,
    @SerializedName("timing") val timing: Long,
    @SerializedName("origin") val origin: String = "android",
    @SerializedName("application_package_name") val applicationPackageName: String,
    @SerializedName("require_attestation") val requireAttestation: Boolean = true,
) 