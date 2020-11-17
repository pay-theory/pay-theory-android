package com.paytheory.paytheorylibrarysdk.paytheory

class CardPayment(
    var cardNumber: Long,
    var cardExpMon: Int,
    var cardExpYear: Int,
    var cardCvv: Int,
    var amount: Int,
    var convenienceFee: String = "",
    var currency: String = "",

    ){


}