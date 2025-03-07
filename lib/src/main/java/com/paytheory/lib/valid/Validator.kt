package com.paytheory.lib.valid

import com.paytheory.lib.compose.string.SecureString
import com.paytheory.lib.compose.string.SecureStringWrapper


class Validator {



    private fun luhnCheck(secureString: SecureString): Boolean {
        val sanitized = secureString.revealForUi().toString().replace("\\D".toRegex(), "")
        val sum = sanitized
            .map { it.toString().toInt() }
            .reversed()
            .mapIndexed { index, digit ->
                if (index % 2 == 1) {
                    val doubled = digit * 2
                    if (doubled > 9) doubled - 9 else doubled
                } else {
                    digit
                }
            }.sum()

        return sum % 10 == 0
    }

    private fun routingCheck(secureString: SecureString): Boolean {
        val sanitized = secureString.revealForUi().toString().replace("\\D".toRegex(), "")
        val sum = sanitized
            .map { it.toString().toInt() }
            .reversed()
            .mapIndexed { index, digit ->
                if ((sanitized.length-index) % 3 == 0) {
                    digit
                } else if ((sanitized.length-index) % 3 == 2) {
                    digit * 7
                } else {
                    digit * 3
                }
            }.sum()
        return sum % 10 == 0
    }

    /**
     * 3(d1+d4+d7) + 7(d2+d5+d8) + (d3+d6+d9) mod 10 = 0
     * 3(2+0+3) + 7(4+7+2) + (4+7+3) mod 10 = 0
     * 15 + 30 + 13 mod 10 = 0
     * **/


    fun isValidBankAccountNumber(secureString: SecureString): Boolean {
        return secureString.revealForUi().filter { it.isDigit() }.length in 7..17
    }

    fun isValidBankRoutingNumber(secureString: SecureString): Boolean {
        return secureString.revealForUi().filter { it.isDigit() }.let { raw ->
            raw.toString().length == 9 && routingCheck(secureString)
        }
    }
    fun isValidCardNumber(secureString: SecureString): Boolean {
        return secureString.revealForUi().filter { it.isDigit() }.let { raw ->
            raw.toString().length in 15..16 && luhnCheck(secureString)
        }
    }


    fun isValidExpiration(expiration: SecureStringWrapper): Boolean {
        return expiration.secureValue.revealForUi()
            .matches(Regex("^(0[1-9]|1[0-2])/(\\d{2})$"))
    }

    fun isValidCvc(secureString: SecureString): Boolean {
        return secureString.revealForUi().length >= 3
    }

    fun isValidPostalCode(secureString: SecureString): Boolean = secureString.revealForUi().toString().length >= 5
            && secureString.revealForUi().toString().length <= 6

    fun isNotEmpty(secureString: SecureString): Boolean = secureString.revealForUi().isNotEmpty()
}