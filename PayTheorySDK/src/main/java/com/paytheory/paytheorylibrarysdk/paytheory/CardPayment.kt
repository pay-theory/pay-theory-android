package com.paytheory.paytheorylibrarysdk.paytheory

class CardPayment (
        var cardNumber : String,
        var cardExpMon : String,
        var cardExpYear : String,
        var cardCvv : String,
        var amount : Int,
        var convenienceFee: String = "",
        var currency: String = "",

){


}