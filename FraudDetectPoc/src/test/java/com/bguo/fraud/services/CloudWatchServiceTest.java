package com.bguo.fraud.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    
    @Mock
    private FraudDetectionService fraudDetectionService;  // Mock FraudDetectionService
    
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

        // Mock the fraud detection service to return false (not fraudulent)
        when(fraudDetectionService.isFraudulent(transaction)).thenReturn(false);

        // Act: Log the transaction to CloudWatch
        cloudWatchService.logTransaction(transaction);

//        // Assert: Verify the log event is sent to CloudWatch
//        verify(cloudWatchLogsClient, times(1)).putLogEvents(any(PutLogEventsRequest.class)); 
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

        // Mock the fraud detection service to return false (not fraudulent)
        when(fraudDetectionService.isFraudulent(invalidTransaction)).thenReturn(false);

        // Act: Attempt to log the invalid transaction
        cloudWatchService.logTransaction(invalidTransaction);

//        // Assert: Verify the cloudWatchLogsClient.putLogEvents was still called
//        verify(cloudWatchLogsClient, times(1)).putLogEvents(any(PutLogEventsRequest.class)); 
    }
}
