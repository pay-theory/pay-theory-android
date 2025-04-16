@file:Suppress("PropertyName")

package com.paytheory.lib.data

import com.paytheory.lib.data.requests.ActionRequest as RequestsActionRequest
import com.paytheory.lib.data.requests.CashRequest as RequestsCashRequest
import com.paytheory.lib.data.requests.HostTokenRequest as RequestsHostTokenRequest
import com.paytheory.lib.data.requests.PaymentDetail as RequestsPaymentDetail
import com.paytheory.lib.data.requests.PaymentMethodTokenData as RequestsPaymentMethodTokenData
import com.paytheory.lib.data.requests.TokenizeRequest as RequestsTokenizeRequest
import com.paytheory.lib.data.requests.TransferPartOneRequest as RequestsTransferPartOneRequest

/**
 * Data class to store host token request
 * for testing attestation outside of production set requireAttestation to true
 */
@Deprecated("Use com.paytheory.lib.data.requests.HostTokenRequest instead", ReplaceWith("RequestsHostTokenRequest"))
typealias HostTokenRequest = RequestsHostTokenRequest

/**
 * Data class to store cash request
 * @param hostToken token with transaction details
 * @param paymentDetail object that contains payment details
 * @param timing calculated timing
 * @param payorInfo optional buyer options data
 */
@Deprecated("Use com.paytheory.lib.data.requests.CashRequest instead", ReplaceWith("RequestsCashRequest"))
typealias CashRequest = RequestsCashRequest

/**
 * Data class for tokenize request
 */
@Deprecated("Use com.paytheory.lib.data.requests.TokenizeRequest instead", ReplaceWith("RequestsTokenizeRequest"))
typealias TokenizeRequest = RequestsTokenizeRequest

/**
 * Data class for transfer part one request
 * @param hostToken token with transaction details
 */
@Deprecated("Use com.paytheory.lib.data.requests.TransferPartOneRequest instead", ReplaceWith("RequestsTransferPartOneRequest"))
typealias TransferPartOneRequest = RequestsTransferPartOneRequest

/**
 * Data class to store action message data
 * @param action type of action that will occur
 * @param encoded encoded message with transaction details
 * @param publicKey encryption key
 */
@Deprecated("Use com.paytheory.lib.data.requests.ActionRequest instead", ReplaceWith("RequestsActionRequest"))
typealias ActionRequest = RequestsActionRequest

/**
 * Data class to store payment method token details
 * @param type type of payment
 * @param timing calculated timing
 * @param name account holder name
 * @param accountNumber account number
 * @param account_type type of account
 * @param bank_code routing number
 * @param number card number
 * @param security_code card security code
 * @param expiration_year card expiration year
 * @param expiration_month card expiration month
 * @param address billing address
 */
@Deprecated("Use com.paytheory.lib.data.requests.PaymentMethodTokenData instead", ReplaceWith("RequestsPaymentMethodTokenData"))
typealias PaymentMethodTokenData = RequestsPaymentMethodTokenData

/**
 * Data class to store payment details
 * @param type type of payment
 * @param timing calculated timing
 * @param amount amount of payment
 * @param currency currency type of payment
 * @param name account holder name
 * @param account_number account number
 * @param account_type type of account
 * @param bank_code routing number
 * @param number card number
 * @param security_code card security code
 * @param expiration_year card expiration year
 * @param expiration_month card expiration month
 * @param address billing address
 * @param walletType type of digital wallet (e.g., GOOGLE_PAY)
 * @param digitalWalletPayload encrypted digital wallet token
 */
@Deprecated("Use com.paytheory.lib.data.requests.PaymentDetail instead", ReplaceWith("RequestsPaymentDetail"))
typealias PaymentDetail = RequestsPaymentDetail
