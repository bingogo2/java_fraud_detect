package com.bguo.fraud.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsService {

    @Value("${aws.sqs.queueUrl}")
    private String queueUrl;

    @Value("${aws.region}")
    private String region;
    
    private final SqsClient sqsClient;
    private final CloudWatchService cloudWatchService;
    
    public void sendTransactionToQueue(Transaction transaction) {
        try {
            String messageBody = new ObjectMapper().writeValueAsString(transaction);
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(messageBody)
                .messageGroupId(transaction.getId()) // Add MessageGroupId
                .build();

            sqsClient.sendMessage(sendMessageRequest);
            log.info("Transaction sent to SQS queue: {}", transaction);
        } catch (Exception e) {
            log.error("Failed to send transaction to SQS", e);
        }
    }
    
    @Scheduled(fixedRate = 10000) //fixed poll each 10 second.   
    public void processMessages() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();
        try {
            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
            log.debug("SQS response exists or not: {}", response.hasMessages());

            List<Message> messages = response.messages();
            for (Message message : messages) {
                log.info("Received message: {}", message.body());
                try {
                    Transaction transaction = new ObjectMapper().readValue(message.body(), Transaction.class);
                    cloudWatchService.logTransaction(transaction);
                } catch (Exception e) {
                    log.error("Error processing message", e);
                } finally {
                    deleteMessage(message);
                }
            }
        } catch (SqsException e) {
            log.error("Error occurred while receiving messages from SQS. AWS Error: {}", e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

    private void deleteMessage(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);
        log.info("Deleted message from SQS: {}", message.receiptHandle());
    }
}
