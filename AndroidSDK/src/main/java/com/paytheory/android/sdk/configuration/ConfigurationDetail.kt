package com.paytheory.android.sdk.configuration
enum class PaymentType {
    CREDIT, BANK, CASH
}
data class ConfigurationDetail(val apiKey:String = "",
                          val amount:Int = 0,
                          val requireAccountName: Boolean = true,
                          val requireAddress: Boolean = true,
                          val paymentType: PaymentType = PaymentType.CREDIT)