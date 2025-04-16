package com.paytheory.lib.data

import com.paytheory.lib.data.responses.BarcodeMessage as ResponsesBarcodeMessage
import com.paytheory.lib.data.responses.HostToken as ResponsesHostToken
import com.paytheory.lib.data.responses.HostTokenMessage as ResponsesHostTokenMessage

/**
 * Data class to store resulting barcode data
 * @param barcodeId
 * @param barcodeUrl
 * @param barcode
 * @param barcodeFee
 * @param merchant
 */
@Deprecated("Use com.paytheory.lib.data.responses.BarcodeMessage instead", ReplaceWith("ResponsesBarcodeMessage"))
typealias BarcodeMessage = ResponsesBarcodeMessage

/**
 * Data class to store host token message details
 * @param type
 * @param body
 */
@Deprecated("Use com.paytheory.lib.data.responses.HostTokenMessage instead", ReplaceWith("ResponsesHostTokenMessage"))
typealias HostTokenMessage = ResponsesHostTokenMessage

/**
 * Data class to store host token message details
 * @param hostToken token with transaction details
 * @param publicKey encryption key to encode/decode messages
 * @param sessionKey encryption key to encode/decode messages
 */
@Deprecated("Use com.paytheory.lib.data.responses.HostToken instead", ReplaceWith("ResponsesHostToken"))
typealias HostToken = ResponsesHostToken