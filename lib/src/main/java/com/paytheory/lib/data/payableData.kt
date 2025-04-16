@file:Suppress("PropertyName")

package com.paytheory.lib.data

import com.google.gson.annotations.SerializedName
import com.paytheory.lib.data.payable.BarcodeResult as PayableBarcodeResult
import com.paytheory.lib.data.payable.EncryptedMessage as PayableEncryptedMessage
import com.paytheory.lib.data.payable.EncryptedPaymentToken as PayableEncryptedPaymentToken
import com.paytheory.lib.data.payable.ErrorCode as PayableErrorCode
import com.paytheory.lib.data.payable.FailedTransactionResult as PayableFailedTransactionResult
import com.paytheory.lib.data.payable.PaymentMethodTokenResults as PayablePaymentMethodTokenResults
import com.paytheory.lib.data.payable.PTError as PayablePTError
import com.paytheory.lib.data.payable.SuccessfulTransactionResult as PayableSuccessfulTransactionResult
import com.paytheory.lib.data.payable.TransactionResult as PayableTransactionResult

@Deprecated("Use com.paytheory.lib.data.payable.ErrorCode instead", ReplaceWith("PayableErrorCode"))
typealias ErrorCode = PayableErrorCode

@Deprecated("Use com.paytheory.lib.data.payable.PTError instead", ReplaceWith("PayablePTError"))
typealias PTError = PayablePTError

@Deprecated("Use com.paytheory.lib.data.payable.BarcodeResult instead", ReplaceWith("PayableBarcodeResult"))
typealias BarcodeResult = PayableBarcodeResult

@Deprecated("Use com.paytheory.lib.data.payable.TransactionResult instead", ReplaceWith("PayableTransactionResult"))
typealias TransactionResult = PayableTransactionResult

@Deprecated("Use com.paytheory.lib.data.payable.SuccessfulTransactionResult instead", ReplaceWith("PayableSuccessfulTransactionResult"))
typealias SuccessfulTransactionResult = PayableSuccessfulTransactionResult

@Deprecated("Use com.paytheory.lib.data.payable.PaymentMethodTokenResults instead", ReplaceWith("PayablePaymentMethodTokenResults"))
typealias PaymentMethodTokenResults = PayablePaymentMethodTokenResults

@Deprecated("Use com.paytheory.lib.data.payable.FailedTransactionResult instead", ReplaceWith("PayableFailedTransactionResult"))
typealias FailedTransactionResult = PayableFailedTransactionResult

@Deprecated("Use com.paytheory.lib.data.payable.EncryptedMessage instead", ReplaceWith("PayableEncryptedMessage"))
typealias EncryptedMessage = PayableEncryptedMessage

@Deprecated("Use com.paytheory.lib.data.payable.EncryptedPaymentToken instead", ReplaceWith("PayableEncryptedPaymentToken"))
typealias EncryptedPaymentToken = PayableEncryptedPaymentToken