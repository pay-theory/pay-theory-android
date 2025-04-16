# Data Package Organization

This directory contains data models used throughout the Pay Theory Android SDK.

## Structure

- `models/` - Core data models
  - `common.kt` - Shared models like Address, PayorInfo
  - `payment.kt` - Payment method models (Card, Bank details)
  - `wallet.kt` - Digital wallet specific models

- `requests/` - API request structures  
  - `payment_requests.kt` - Payment transaction requests
  - `tokenization_requests.kt` - Tokenization-related requests

- `responses/` - API response structures
  - `payment_responses.kt` - Payment transaction responses
  - `tokenization_responses.kt` - Tokenization-related responses

## Naming Conventions

- Models should use clear, descriptive names reflecting their purpose
- Request classes should end with "Request"
- Response classes should end with "Response" or "Result" 