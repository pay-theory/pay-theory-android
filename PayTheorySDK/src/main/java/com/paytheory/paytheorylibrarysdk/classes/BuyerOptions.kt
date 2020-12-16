package com.paytheory.paytheorylibrarysdk.classes



/**
 * BuyerOptions Class is created when "Buyer-Options" is set to "True"
 * @property firstName first name value from user
 * @property lastName last name value from user
 * @property addressOne address one value from user
 * @property addressTwo address two value from user
 * @property city city value from user
 * @property state state value from user
 * @property country country value from user
 * @property zipCode zip value from user
 * @property phoneNumber phone number value from user
 * @property email email value from user
 */
class BuyerOptions(
        val firstName : String,
        val lastName : String,
        val addressOne : String,
        val addressTwo : String,
        val city : String,
        val state : String,
        val country : String,
        val zipCode : String,
        val phoneNumber: String,
        val email : String,

)