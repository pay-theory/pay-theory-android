package com.paytheory.paytheorylibrarysdk.classes

/**
 * Payment Class is created when Transaction has initiated
 * @property cardNumber card number value from user
 * @property cardExpMon card expiration month value from user
 * @property cardExpYear car expiration year value from user
 * @property cardCvv card cvv value from user
 * @property amount amount value from user
 * @property feeMode Fee mode value
 * @property tagsKey Tags Key value
 * @property tagsValue Tags value
 * @property cardFirstName Card holders first name value from user
 * @property cardLastName Card holders last name value from user
 * @property cardAddressOne Address One value from user
 * @property cardAddressTwo Address Two value from user
 * @property cardCity City value from user
 * @property cardState State value from user
 * @property cardZip Zip value from user
 * @property currency Currency type
 * @property convenienceFee Convenience fee added to payment amount
 */
class Payment(
    var cardNumber: Long,
    var cardExpMon: Int,
    var cardExpYear: Int,
    var cardCvv: Int,
    var amount: Int,
    var feeMode: String = "",
    var tagsKey: String? = "",
    var tagsValue: String? = "",
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