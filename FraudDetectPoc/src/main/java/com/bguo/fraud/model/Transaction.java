package com.bguo.fraud.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a financial transaction.
 * 
 * JSON Example:
 * {
 *   "id": "tx_20240501AB",
 *   "accountId": "acc_A1b2C3d4",
 *   "amount": 150.75,
 *   "currency": "USD",
 *   "timestamp": 1717234567890,
 *   "merchantId": "mch_STR12345",
 *   "location": "New York, US",
 *   "transactionType": "PURCHASE"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @NotBlank(message = "Transaction ID cannot be empty")
    @Pattern(regexp = "^tx_[a-zA-Z0-9]{10}$", message = "Invalid transaction ID format")
    private String id;

    @NotBlank(message = "Account ID cannot be empty")
    @Pattern(regexp = "acc_[A-Za-z0-9]{8}", message = "Invalid account format")
    private String accountId;

    @Positive(message = "Amount must be positive")
    private double amount;

    @NotBlank(message = "Currency cannot be empty")
    @Size(min = 3, max = 3, message = "Currency must be 3-letter ISO code")
    private String currency;

    @PastOrPresent(message = "Timestamp cannot be in the future")
    private long timestamp;

    @Pattern(regexp = "mch_[a-zA-Z0-9]{8}", message = "Invalid merchant ID format")
    private String merchantId;

    @NotBlank(message = "Location cannot be empty")
    private String location;

    @Pattern(regexp = "^(PURCHASE|REFUND|TRANSFER)$", 
             message = "Invalid transaction type")
    private String transactionType;
}