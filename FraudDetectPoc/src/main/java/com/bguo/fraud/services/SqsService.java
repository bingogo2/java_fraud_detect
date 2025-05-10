package com.bguo.fraud.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

@Service
public class SqsService {

    @Value("${aws.sqs.queueUrl}")
    private String queueUrl;

    @Value("${aws.region}")
    private String region;
    
    private final SqsClient sqsClient;
    private final CloudWatchService cloudWatchService;

    public SqsService(SqsClient sqsClient, CloudWatchService cloudWatchService) {
        this.sqsClient = sqsClient;
        this.cloudWatchService = cloudWatchService;
    }

    public void processMessages() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);

        List<Message> messages = response.messages();
        for (Message message : messages) {
            System.out.println("Received message: " + message.body());
            // Assuming message body is a JSON representing the Transaction object
            Transaction transaction = parseTransaction(message.body());

            // Log the transaction data to CloudWatch
            cloudWatchService.logTransaction(transaction);
            
            // Delete the message after processing
            deleteMessage(message);
        }
    }

    private Transaction parseTransaction(String messageBody) {
        try {
            return new ObjectMapper().readValue(messageBody, Transaction.class);
        } catch (Exception e) {
            // Handle the error if parsing fails, perhaps logging it
            // e.printStackTrace();
            return null;
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
    }
}
