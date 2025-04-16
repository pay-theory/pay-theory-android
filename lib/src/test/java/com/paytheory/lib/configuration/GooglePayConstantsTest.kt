package com.paytheory.lib.configuration

import com.google.android.gms.wallet.WalletConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class GooglePayConstantsTest {

    @Test
    fun `environment constants should match WalletConstants values`() {
        assertEquals(WalletConstants.ENVIRONMENT_TEST, GooglePayConstants.ENVIRONMENT_TEST)
        assertEquals(WalletConstants.ENVIRONMENT_PRODUCTION, GooglePayConstants.ENVIRONMENT_PRODUCTION)
    }
    
    @Test
    fun `default supported networks should include major card networks`() {
        val networks = GooglePayConstants.DEFAULT_SUPPORTED_NETWORKS
        
        // Verify major card networks are included
        assertTrue(networks.contains("AMEX"))
        assertTrue(networks.contains("DISCOVER"))
        assertTrue(networks.contains("JCB"))
        assertTrue(networks.contains("MASTERCARD"))
        assertTrue(networks.contains("VISA"))
        
        // Verify we have exactly the expected number
        assertEquals(5, networks.size)
    }
    
    @Test
    fun `default supported methods should include CRYPTOGRAM_3DS`() {
        val methods = GooglePayConstants.DEFAULT_SUPPORTED_METHODS
        
        // CRYPTOGRAM_3DS is required by the PayTheory backend
        assertTrue(methods.contains("CRYPTOGRAM_3DS"))
        
        // Verify we have exactly the expected number (only CRYPTOGRAM_3DS initially)
        assertEquals(1, methods.size)
    }
    
    @Test
    fun `gateway information should be correctly defined`() {
        assertEquals("paytheory", GooglePayConstants.GATEWAY_NAME)
        assertEquals("BCR2DN4TZ342R7QF", GooglePayConstants.GATEWAY_MERCHANT_ID)
    }
    
    @Test
    fun `country and currency codes should be set to US and USD`() {
        assertEquals("US", GooglePayConstants.COUNTRY_CODE)
        assertEquals("USD", GooglePayConstants.CURRENCY_CODE)
    }
} 