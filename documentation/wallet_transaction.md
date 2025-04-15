# Wallet Transaction Websocket Message Format

This document outlines the message format required to process wallet transactions through the Pay Theory websocket.

## Message Overview

When sending a wallet transaction through the websocket, the message must follow this specific format:

```json
{
    "action": "host:wallet_transaction",
    "sessionKey": "MmNmMWQ5YmUtMmY1Yi00YmVmLTgxYmMtMTRlYWQxZTMzYTFi",
    "encoded": "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",
    "publicKey": "i5sUHjAzJn+DYRnceOluCRUOyIIp2zZ9Muf0CIPAciQ="
}
```

## Message Components

The message consists of the following components:

1. **action**: The action type identifier - must be set to `"host:wallet_transaction"`
2. **sessionKey**: A unique session identifier
3. **encoded**: The encrypted payload containing the transaction details
4. **publicKey**: The public key used for encryption/decryption

## Payload Structure

The encrypted `encoded` field contains a JSON payload with the following structure:

```json
{
    "digital_wallet_payload": "<wallet_payload_data>",
    "merchant_uid": "<merchant_identifier>",
    "amount": <transaction_amount>,
    "payor": {
        "address_line1": "<address_line1>",
        "address_line2": "<address_line2>",
        "city": "<city>",
        "country": "<country>",
        "email": "<email>",
        "full_name": "<full_name>",
        "phone": "<phone>",
        "postal_code": "<postal_code>",
        "region": "<region>"
    },
    "reference": "<reference_identifier>",
    "account_code": "<account_code>",
    "metadata": "<json_stringified_metadata>",
    "additional_purchase_data": {
        "level3_data_line_item": [
            {
                "item_code": "<item_code>",
                "item_description": "<item_description>",
                "qty": "<quantity>",
                "unit_cost": "<unit_cost>"
                // Additional level3 line item fields as needed
            }
        ],
        "level3_data_summary": {
            // Level3 summary data
        }
    },
    "billing_address": {
        "address_line1": "<address_line1>",
        "address_line2": "<address_line2>",
        "city": "<city>",
        "country": "<country>",
        "full_name": "<full_name>",
        "postal_code": "<postal_code>",
        "region": "<region>"
    },
    "fee": <fee_amount>,
    "health_expense_type": "<health_expense_type>",
    "invoice_id": "<invoice_id>",
    "receipt_description": "<receipt_description>",
    "payor_id": "<payor_id>",
    "recurring_id": "<recurring_id>",
    "send_receipt": <boolean>,
    "split": [
        {
            "account_code": "<account_code>",
            "amount": <amount>,
            "merchant_uid": "<merchant_uid>",
            "reference": "<reference>"
        }
    ],
    "wallet_type": "<wallet_type>"
}
```

## Field Descriptions

| Field | Description | Type | Required |
|-------|-------------|------|----------|
| `digital_wallet_payload` | The encrypted payload from the digital wallet | String | Yes |
| `merchant_uid` | The unique identifier for the merchant | String | Yes |
| `amount` | The transaction amount (in cents) | Integer | Yes |
| `payor` | Information about the person making the payment | Object | No |
| `reference` | A reference identifier for the transaction | String | No |
| `account_code` | Account code for transaction categorization | String | No |
| `metadata` | Additional metadata (must be stringified JSON) | String | No |
| `additional_purchase_data` | Level 3 data for the transaction | Object | No |
| `billing_address` | Billing address for the transaction | Object | No |
| `fee` | Fee amount for the transaction | Integer | No |
| `health_expense_type` | Type of health expense (for HSA/FSA) | String | No |
| `invoice_id` | Associated invoice ID | String | No |
| `receipt_description` | Description for the receipt | String | No |
| `payor_id` | ID of an existing payor | String | No |
| `recurring_id` | ID for recurring payments | String | No |
| `send_receipt` | Whether to send a receipt | Boolean | No |
| `split` | Split payment configuration | Array | No |
| `wallet_type` | Type of digital wallet (APPLE_PAY, GOOGLE_PAY, etc.) | String | Yes |

## Implementation Notes

1. The SDK should encrypt the payload before sending it over the websocket
2. The `sessionKey` is used for session management
3. The `publicKey` is used for encryption/decryption
4. The message follows the same structure as the GraphQL mutation `createWalletTransaction`

## Response Handling

After sending the message, the client should listen for a response on the same websocket connection. The response will contain the transaction result.
