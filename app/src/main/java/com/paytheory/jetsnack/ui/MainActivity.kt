/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paytheory.jetsnack.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.paytheory.lib.BarcodeResult
import com.paytheory.lib.FailedTransactionResult
import com.paytheory.lib.PTError
import com.paytheory.lib.Payable
import com.paytheory.lib.PaymentMethodTokenResults
import com.paytheory.lib.SuccessfulTransactionResult

class MainActivity : ComponentActivity(), Payable {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { JetsnackApp() }
    }

    override fun handleReady(isReady: Boolean) {
        Log.d("MainActivity", "handleReady: $isReady")
    }

    override fun handlePaymentStart(paymentType: String) {
        Log.d("MainActivity", "handlePaymentStart: $paymentType")
    }

    override fun handleTokenStart(paymentType: String) {
        Log.d("MainActivity", "handleTokenStart: $paymentType")
    }

    override fun handleSuccess(successfulTransactionResult: SuccessfulTransactionResult) {

        setContent { JetsnackApp() }
        Log.d("MainActivity", "handleSuccess: $successfulTransactionResult")
    }

    override fun handleFailure(failedTransactionResult: FailedTransactionResult) {
        Log.d("MainActivity", "handleFailure: $failedTransactionResult")
    }

    override fun handleBarcodeSuccess(barcodeResult: BarcodeResult) {
        Log.d("MainActivity", "handleBarcodeSuccess: $barcodeResult")
    }

    override fun handleTokenizeSuccess(paymentMethodToken: PaymentMethodTokenResults) {
        Log.d("MainActivity", "handleTokenizeSuccess: $paymentMethodToken")
    }

    override fun handleError(error: PTError) {
        Log.d("MainActivity", "handleError: $error")
    }
}
