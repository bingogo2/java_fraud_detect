package com.bguo.fraud.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudWatchService {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.cloudwatch.logGroupName}")
    private String logGroupName;

    @Value("${aws.cloudwatch.logStreamName}")
    private String logStreamName;

    private final FraudDetectionService fraudDetectionService;
    private CloudWatchLogsClient cloudWatchLogsClient;

    @PostConstruct
    public void init() {
        cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .build();
        createLogGroupIfNotExists();
        createLogStreamIfNotExists();
    }

    private void createLogGroupIfNotExists() {
        try {
            DescribeLogGroupsRequest describeLogGroupsRequest = DescribeLogGroupsRequest.builder()
                    .logGroupNamePrefix(logGroupName)
                    .build();

            DescribeLogGroupsResponse result = cloudWatchLogsClient.describeLogGroups(describeLogGroupsRequest);

            if (result.logGroups().isEmpty()) {
                CreateLogGroupRequest createLogGroupRequest = CreateLogGroupRequest.builder()
                        .logGroupName(logGroupName)
                        .build();
                cloudWatchLogsClient.createLogGroup(createLogGroupRequest);
                log.info("Created log group: {}", logGroupName);
            }
        } catch (Exception e) {
            log.error("Error checking log group: {}", e.getMessage(), e);
        }
    }

    private void createLogStreamIfNotExists() {
        try {
            DescribeLogStreamsRequest describeLogStreamsRequest = DescribeLogStreamsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamNamePrefix(logStreamName)
                    .build();

            DescribeLogStreamsResponse result = cloudWatchLogsClient.describeLogStreams(describeLogStreamsRequest);

            if (result.logStreams().isEmpty()) {
                CreateLogStreamRequest createLogStreamRequest = CreateLogStreamRequest.builder()
                        .logGroupName(logGroupName)
                        .logStreamName(logStreamName)
                        .build();
                cloudWatchLogsClient.createLogStream(createLogStreamRequest);
                log.info("Created log stream: {}", logStreamName);
            }
        } catch (Exception e) {
            log.error("Error checking log stream: {}", e.getMessage(), e);
        }
    }

    public void logTransaction(Transaction transaction) {
        try {
            boolean isFraudulent = fraudDetectionService.isFraudulent(transaction); // Check fraud status here
            long timestamp = System.currentTimeMillis();
            String fraudStatus = isFraudulent ? "Fraudulent" : "Normal";
            // If transaction is invalid, log a warning and proceed with logging to CloudWatch
            if (transaction.getAmount() <= 0) {
                log.warn("Invalid transaction amount: {} for transaction: {}", transaction.getAmount(), transaction);
                fraudStatus = "Invalid"; 
            }

            // Create the log event with the fraud status or invalid status
            InputLogEvent logEvent = InputLogEvent.builder()
                    .message("Transaction processed: " + transaction + ", Fraud Status: " + fraudStatus)
                    .timestamp(timestamp)
                    .build();
            // Send the log event to CloudWatch
            PutLogEventsRequest request = PutLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .logEvents(logEvent)
                    .build();
            cloudWatchLogsClient.putLogEvents(request);
            log.info("Logged transaction to CloudWatch: {} - Fraud Status: {}", transaction.getId(), fraudStatus);
        } catch (Exception e) {
            log.error("Error logging transaction to CloudWatch: {}", e.getMessage(), e);
        }
    }
}
