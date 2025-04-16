package com.paytheory.lib.data.payable

enum class ErrorCode {
    ActionComplete, //The payment action has already been completed.
    ActionInProgress, //A payment action is already in progress.
    AttestationFailed, //The device attestation process failed.
    InProgress, //Initialization is still in progress.
    InvalidAPIKey, //The provided API key is invalid or not recognized.
    NotValid, //The provided data is not valid.
    SocketError, //An error occurred with the WebSocket connection.
    TokenFailed,	//Token generation or validation failed.
    GooglePayUnavailable, //Google Pay is not available on this device
    GooglePayError, //An error occurred during Google Pay payment processing
    GooglePayCancelled //User cancelled the Google Pay payment
} 