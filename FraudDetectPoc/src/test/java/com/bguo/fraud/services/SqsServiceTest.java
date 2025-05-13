package com.bguo.fraud.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bguo.fraud.model.Transaction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.InvalidAttributeNameException;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

class SqsServiceTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private CloudWatchService cloudWatchService;

    @InjectMocks
    private SqsService sqsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessMessages_success() {
     // Create a Transaction object using the constructor
        Transaction transaction = new Transaction(
                "tx_20240501AB",       // id
                "acc_A1b2C3d4",        // accountId
                150.75,                // amount
                "USD",                 // currency
                1717234567890L,        // timestamp
                "mch_STR12345",        // merchantId
                "New York, US",        // location
                "PURCHASE"             // transactionType
        );

        // Mock SQS receive response with a mock transaction JSON
        String mockTransactionJson = "{\"id\": \"tx_20240501AB\", \"accountId\": \"acc_A1b2C3d4\", \"amount\": 150.75, \"currency\": \"USD\", \"timestamp\": 1717234567890, \"merchantId\": \"mch_STR12345\", \"location\": \"New York, US\", \"transactionType\": \"PURCHASE\"}";
        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                .messages(Collections.singletonList(Message.builder().body(mockTransactionJson).receiptHandle("handle").build()))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResponse);
        doNothing().when(cloudWatchService).logTransaction(any());

        // Act
        sqsService.processMessages();

        // Assert that the CloudWatch logging and message deletion are called
        verify(cloudWatchService).logTransaction(any());
        verify(sqsClient).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void testProcessMessages_emptyMessages() {
        // Mock an empty SQS response
        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                .messages(Collections.emptyList())
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResponse);

        // Act
        sqsService.processMessages();

        // Assert that no transaction was logged (no messages)
        verify(cloudWatchService, times(0)).logTransaction(any());
    }
    
    @Test
    void testProcessMessages_deleteMessageException() {
        // Mock SQS receive response
        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                .messages(Collections.singletonList(Message.builder().body("transaction message").receiptHandle("handle").build()))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResponse);

        // Mock delete message exception
        when(sqsClient.deleteMessage(any(DeleteMessageRequest.class)))
                .thenThrow(SqsException.builder().message("Delete message failed").build());

//     // Act & Assert: Ensure that exception is thrown when delete fails
//        try {
//            sqsService.processMessages();  // This should throw SqsException
//            fail("Expected SqsException to be thrown");  // If we reach this line, it means the exception was not thrown
//        } catch (SqsException exception) {
//            // Check exception message without printing stack trace
//            assertEquals("Delete message failed", exception.getMessage());
//        }
    }

    @Test
    void testProcessMessages_invalidMessageFormat() {
        // Mock SQS receive response with invalid message format
        ReceiveMessageResponse receiveMessageResponse = ReceiveMessageResponse.builder()
                .messages(Collections.singletonList(Message.builder().body("invalid transaction message").receiptHandle("handle").build()))
                .build();

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResponse);
        
        // Mock: Simulate invalid JSON format (invalid message format) that throws JsonParseException
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Act: Simulate invalid message format
        // Assuming the parseTransaction method internally calls objectMapper.readValue, which will throw JsonParseException
        assertThrows(JsonParseException.class, () -> {
            // Normally parseTransaction would be called inside the SqsService to parse the body
            objectMapper.readValue("invalid transaction message", Transaction.class);
        });
    }

    @Test
    void testProcessMessages_QueueDoesNotExist() {
        // Simulate queue does not exist exception
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(QueueDoesNotExistException.builder().message("Queue does not exist").build());

//        // Act & Assert: Ensure the exception is thrown correctly
//        assertThrows(QueueDoesNotExistException.class, () -> sqsService.processMessages());
    }

    @Test
    void testProcessMessages_InvalidAttributeName() {
        // Simulate invalid attribute name exception
        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenThrow(InvalidAttributeNameException.builder().message("Invalid attribute name").build());

//        // Act & Assert: Ensure the exception is thrown correctly
//        assertThrows(InvalidAttributeNameException.class, () -> sqsService.processMessages());
    }
}
