package com.bguo.fraud.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bguo.fraud.model.Transaction;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

class CloudWatchServiceTest {

    @Mock
    private CloudWatchLogsClient cloudWatchLogsClient;  // Mock CloudWatchLogsClient
    
    @InjectMocks
    private CloudWatchService cloudWatchService;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void testLogTransaction() {
        // Arrange: Create a sample transaction using your test data
        Transaction transaction = new Transaction(
            "tx_20240501AB",
            "acc_A1b2C3d4",
            150.75,
            "USD",
            1717234567890L,
            "mch_STR12345",
            "New York, US",
            "PURCHASE"
        );

        // Act: Log the transaction to CloudWatch
        cloudWatchService.logTransaction(transaction);

        // Verify the log event is sent to CloudWatch
        verify(cloudWatchLogsClient, times(1)).putLogEvents(any(PutLogEventsRequest.class)); 

    }

    @Test
    public void testLogTransaction_invalidTransaction() {
     // Arrange: Create a malformed transaction (for testing invalid input)
        Transaction invalidTransaction = new Transaction(
            "tx_invalid",  // Invalid ID format
            "acc_invalid", // Invalid account format
            -100.0,        // Negative amount
            "USD",
            1717234567890L,
            "mch_invalid",
            "Unknown Location",
            "PURCHASE"
        );

        // Act: Attempt to log the invalid transaction
        // This should not throw an exception, but you can handle the error internally within the logTransaction method
        cloudWatchService.logTransaction(invalidTransaction);

        // Assert: Verify that the cloudWatchLogsClient.putLogEvents was still called
        // Even if the transaction is invalid, we expect logging to occur, so verify the interaction with CloudWatchLogsClient
        verify(cloudWatchLogsClient, times(1)).putLogEvents(any(PutLogEventsRequest.class));
    }
}
