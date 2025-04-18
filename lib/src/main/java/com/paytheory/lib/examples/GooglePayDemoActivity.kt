package com.paytheory.lib.examples

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paytheory.lib.Payable
import com.paytheory.lib.PayTheoryConfiguration
import com.paytheory.lib.compose.GooglePayForm
import com.paytheory.lib.compose.StandaloneGooglePayButton
import com.paytheory.lib.configuration.GooglePayBillingAddressFormat
import com.paytheory.lib.configuration.GooglePayButtonColor
import com.paytheory.lib.configuration.GooglePayButtonType
import com.paytheory.lib.configuration.GooglePayEnvironment
import com.paytheory.lib.theme.PayTheoryTheme
import com.paytheory.lib.ui.PTError
import com.paytheory.lib.ui.PTResult

/**
 * Demo activity showing how to implement Google Pay with the Pay Theory SDK.
 * 
 * This activity demonstrates:
 * 1. Google Pay configuration
 * 2. Using the GooglePayForm component
 * 3. Using the StandaloneGooglePayButton component
 * 4. Handling payment results
 */
class GooglePayDemoActivity : ComponentActivity(), Payable {
    private lateinit var configuration: PayTheoryConfiguration
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize configuration
        // In a real app, use your actual API key from a secure source
        configuration = PayTheoryConfiguration.Builder()
            .apiKey("YOUR_PAY_THEORY_API_KEY")
            .amount(1999) // $19.99
            .enableGooglePay(
                merchantName = "Demo Store",
                allowPrepaidCards = true,
                allowCreditCards = true
            )
            .setGooglePayButtonType(GooglePayButtonType.Buy)
            .setGooglePayButtonColor(GooglePayButtonColor.Black)
            .setGooglePayEnvironment(GooglePayEnvironment.TEST)
            .build()
        
        setContent {
            PayTheoryTheme {
                GooglePayDemoScreen(this, configuration)
            }
        }
    }
    
    // Payable interface implementation
    override fun handleSuccess(result: PTResult) {
        // Handle successful payment
        Toast.makeText(
            this,
            "Payment successful! Receipt: ${result.receiptNumber}",
            Toast.LENGTH_LONG
        ).show()
        
        Log.d("GooglePayDemo", "Payment succeeded - Receipt: ${result.receiptNumber}")
    }
    
    override fun handleFailure(result: PTResult) {
        // Handle payment failure
        Toast.makeText(
            this,
            "Payment failed: ${result.message}",
            Toast.LENGTH_LONG
        ).show()
        
        Log.e("GooglePayDemo", "Payment failed: ${result.message}")
    }
    
    override fun handleError(error: PTError) {
        // Handle errors
        Toast.makeText(
            this,
            "Error: ${error.message}",
            Toast.LENGTH_LONG
        ).show()
        
        Log.e("GooglePayDemo", "Error: ${error.code} - ${error.message}")
    }
    
    override fun handlePaymentStart(paymentType: String) {
        // Payment started
        Toast.makeText(
            this,
            "Starting $paymentType payment...",
            Toast.LENGTH_SHORT
        ).show()
        
        Log.d("GooglePayDemo", "Starting $paymentType payment")
    }
}

/**
 * Main screen for the Google Pay demo.
 * 
 * This composable shows a simple product purchase UI with Google Pay options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePayDemoScreen(payable: Payable, configuration: PayTheoryConfiguration) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google Pay Demo") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product information
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Product: Premium Subscription",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Text(
                        text = "Total: $${configuration.amount / 100}.00",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Text(
                        text = "This is a demonstration of Google Pay integration with Pay Theory",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Section title
            Text(
                text = "Pay with Google Pay",
                style = MaterialTheme.typography.titleMedium
            )
            
            // GooglePayForm component demonstration
            GooglePayForm(
                payable = payable,
                configuration = configuration,
                buttonModifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Alternative payment methods section
            Text(
                text = "Other Google Pay Integration Options",
                style = MaterialTheme.typography.titleMedium
            )
            
            // StandaloneGooglePayButton demonstration
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Standalone Google Pay Button:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // StandaloneGooglePayButton with custom handling
                    StandaloneGooglePayButton(
                        payable = payable,
                        configuration = configuration,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        hideWhenUnavailable = false,
                        onUnavailable = {
                            // This would be called if Google Pay is not available
                            Log.d("GooglePayDemo", "Google Pay is not available")
                        },
                        buttonType = GooglePayButtonType.Checkout,
                        buttonColor = GooglePayButtonColor.White
                    )
                    
                    Text(
                        text = "This shows how to use the standalone button component with custom styling and behavior.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Additional payment options
            Button(
                onClick = { /* Navigate to traditional payment form */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pay with Card or Bank Account")
            }
        }
    }
} 