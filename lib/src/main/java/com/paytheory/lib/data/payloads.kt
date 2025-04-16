@file:Suppress("PropertyName")

package com.paytheory.lib.data

import com.paytheory.lib.data.payloads.Address as PayloadsAddress
import com.paytheory.lib.data.payloads.PaymentData as PayloadsPaymentData
import com.paytheory.lib.data.payloads.PaymentMethodData as PayloadsPaymentMethodData
import com.paytheory.lib.data.payloads.PayorInfo as PayloadsPayorInfo

@Deprecated("Use com.paytheory.lib.data.payloads.PayorInfo instead", ReplaceWith("PayloadsPayorInfo"))
typealias PayorInfo = PayloadsPayorInfo

@Deprecated("Use com.paytheory.lib.data.payloads.PaymentData instead", ReplaceWith("PayloadsPaymentData"))
typealias PaymentData = PayloadsPaymentData

@Deprecated("Use com.paytheory.lib.data.payloads.PaymentMethodData instead", ReplaceWith("PayloadsPaymentMethodData"))
typealias PaymentMethodData = PayloadsPaymentMethodData

@Deprecated("Use com.paytheory.lib.data.payloads.Address instead", ReplaceWith("PayloadsAddress"))
typealias Address = PayloadsAddress