package com.paytheory.paytheorylibrarysdk.paytheory

class CardPayment(
    var cardNumber: Long,
    var cardExpMon: Int,
    var cardExpYear: Int,
    var cardCvv: Int,
    var amount: Int,
    var feeMode: String = "",
    var tags: String? = "",
    var cardFirstName: String?= null,
    var cardLastName: String?= null,
    var cardAddressOne: String? = null,
    var cardAddressTwo: String?= null,
    var cardCity: String?= null,
    var cardState: String?= null,
    var cardZip: String?= null,
    ){

    var currency = ""
    var convenienceFee: String = ""
}