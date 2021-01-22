package com.paytheory.paytheorylibrarysdk.classes

/**
 * Payment Class is created when OldTransaction has initiated
 * @property cardNumber card number value from user
 * @property cardExpMon card expiration month value from user
 * @property cardExpYear car expiration year value from user
 * @property cardCvv card cvv value from user
 * @property amount amount value from user
 * @property feeMode Fee mode value
 * @property tagsKey Tags Key value
 * @property tagsValue Tags value
 * @property firstName Card holders first name value from user
 * @property lastName Card holders last name value from user
 * @property addressOne Address One value from user
 * @property addressTwo Address Two value from user
 * @property city City value from user
 * @property state State value from user
 * @property zip Zip value from user
 * @property currency Currency type
 * @property convenienceFee Convenience fee added to payment amount
 */
class Payment(
    var cardNumber: Long?,
    var cardExpMon: Int?,
    var cardExpYear: Int?,
    var cardCvv: String?,
    var achAccountNumber: Long?,
    var achRoutingNumber: Int?,
    var achAccountType: String?,
    var amount: Int,
    var type: String,
    var feeMode: String = "",
    var tagsKey: String = "",
    var tagsValue: String = "",
    var firstName: String?= null,
    var lastName: String?= null,
    var addressOne: String? = null,
    var addressTwo: String?= null,
    var city: String?= null,
    var state: String?= null,
    var zip: String?= null
    ){

    var currency = ""
    var convenienceFee: String = ""
}