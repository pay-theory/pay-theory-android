package com.paytheory.lib.configuration

import com.paytheory.lib.PayTheoryConfiguration
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility class for building Google Pay API requests
 */
object GooglePayRequestBuilder {
    /**
     * Creates a Google Pay API request to check whether the user can pay with Google Pay
     */
    fun buildIsReadyToPayRequest(config: PayTheoryConfiguration): JSONObject {
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().apply {
                put(baseCardPaymentMethod(config))
            })
        }
    }
    
    /**
     * Creates a Google Pay API request for payment data
     */
    fun buildPaymentDataRequest(config: PayTheoryConfiguration, price: String): JSONObject {
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().apply {
                put(cardPaymentMethod(config).apply {
                    put("tokenizationSpecification", buildTokenizationSpecification())
                })
            })
            put("transactionInfo", JSONObject().apply {
                put("totalPrice", price)
                put("totalPriceStatus", "FINAL")
                put("currencyCode", GooglePayConstants.CURRENCY_CODE)
                put("countryCode", GooglePayConstants.COUNTRY_CODE)
            })
            put("merchantInfo", JSONObject().apply {
                put("merchantName", config.googlePayMerchantName)
            })
            
            // Add shipping address requirement if needed
            if (config.googlePayShippingAddressRequired) {
                put("shippingAddressRequired", true)
                put("shippingAddressParameters", JSONObject().apply {
                    put("phoneNumberRequired", config.googlePayPhoneNumberRequired)
                })
            }
        }
    }
    
    /**
     * Build tokenization specification for the gateway
     */
    fun buildTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject().apply {
                put("gateway", GooglePayConstants.GATEWAY_NAME)
                put("gatewayMerchantId", GooglePayConstants.GATEWAY_MERCHANT_ID)
            })
        }
    }
    
    /**
     * Build base card payment method
     */
    private fun baseCardPaymentMethod(config: PayTheoryConfiguration): JSONObject {
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", JSONArray().apply {
                    // Include supported authentication methods
                    for (method in config.googlePaySupportedMethods) {
                        put(method)
                    }
                })
                put("allowedCardNetworks", JSONArray().apply {
                    for (network in config.googlePayAllowedCardNetworks) {
                        put(network)
                    }
                })
                put("allowPrepaidCards", config.googlePayAllowPrepaidCards)
                put("allowCreditCards", config.googlePayAllowCreditCards)
            })
        }
    }
    
    /**
     * Build card payment method with billing address if required
     */
    private fun cardPaymentMethod(config: PayTheoryConfiguration): JSONObject {
        val cardPaymentMethod = baseCardPaymentMethod(config)
        val parameters = cardPaymentMethod.getJSONObject("parameters")
        
        if (config.googlePayBillingAddressRequired) {
            parameters.put("billingAddressRequired", true)
            parameters.put("billingAddressParameters", JSONObject().apply {
                put("format", when (config.googlePayBillingAddressFormat) {
                    GooglePayBillingAddressFormat.MINIMAL -> "MIN"
                    GooglePayBillingAddressFormat.FULL -> "FULL"
                })
            })
        }
        
        return cardPaymentMethod
    }
} 