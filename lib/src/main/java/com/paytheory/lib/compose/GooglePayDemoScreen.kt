package com.paytheory.lib.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.Payable
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.data.PTError
import com.paytheory.lib.data.PTResult

/**
 * A demonstration screen showing the Google Pay integration options.
 * 
 * This composable showcases different ways to integrate Google Pay:
 * 1. Integrated within the standard PaymentForm
 * 2. As a standalone component
 * 3. Different button styles
 * 
 * This demo is meant to serve as a reference for developers integrating Google Pay.
 * 
 * @param payable The Payable interface implementation for payment callbacks
 * @param apiKey Pay Theory API key for testing
 */
@Composable
fun GooglePayDemoScreen(
    payable: Payable,
    apiKey: String
) {
    // Create basic configuration for Google Pay
    val basicConfig = PayTheoryConfiguration.Builder()
        .setApiKey(apiKey)
        .setAmount("10.99")
        .enableGooglePay("Pay Theory")
        .setGooglePayEnvironment(GooglePayEnvironment.TEST)
        .build()
    
    // Create configuration with additional options
    val advancedConfig = PayTheoryConfiguration.Builder()
        .setApiKey(apiKey)
        .setAmount("25.50")
        .enableGooglePay("Pay Theory Demo Store")
        .setGooglePayButtonType(GooglePayButtonType.CHECKOUT)
        .setGooglePayButtonColor(GooglePayButtonColor.WHITE)
        .setGooglePayBillingAddressRequired(true)
        .setGooglePayEnvironment(GooglePayEnvironment.TEST)
        .build()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Google Pay Integration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section 1: Google Pay with PaymentForm
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Standard Integration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Google Pay integrated with PaymentForm",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PaymentForm(
                    payable = payable,
                    configuration = basicConfig
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section 2: Standalone Google Pay
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Standalone Integration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Google Pay as a standalone payment option",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                StandaloneGooglePayButton(
                    payable = payable,
                    configuration = basicConfig,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section 3: Different button styles
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Button Styles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Black "Pay" button (default)
                GooglePayButton(
                    onClick = {},
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                    buttonType = GooglePayButtonType.PAY,
                    buttonColor = GooglePayButtonColor.BLACK
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // White "Checkout" button
                GooglePayButton(
                    onClick = {},
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                    buttonType = GooglePayButtonType.CHECKOUT,
                    buttonColor = GooglePayButtonColor.WHITE
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Black "Subscribe" button
                GooglePayButton(
                    onClick = {},
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(),
                    buttonType = GooglePayButtonType.SUBSCRIBE,
                    buttonColor = GooglePayButtonColor.BLACK
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Disabled button
                GooglePayButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    buttonType = GooglePayButtonType.BUY,
                    buttonColor = GooglePayButtonColor.BLACK
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Note: Using official Google Pay button library",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Section 4: Advanced configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Advanced Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Text(
                    text = "Customized Google Pay with address required",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                StandaloneGooglePayButton(
                    payable = payable,
                    configuration = advancedConfig,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Simple implementation of Payable for the demo screen
 */
class DemoPayable : Payable {
    override fun handleSuccess(result: PTResult) {
        // Handle successful payment
    }
    
    override fun handleError(error: PTError) {
        // Handle payment error
    }
} 