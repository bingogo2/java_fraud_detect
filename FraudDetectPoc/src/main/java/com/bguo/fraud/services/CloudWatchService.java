package com.bguo.fraud.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;

import jakarta.annotation.PostConstruct;
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
public class CloudWatchService {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.cloudwatch.logGroupName}")
    private String logGroupName;

    @Value("${aws.cloudwatch.logStreamName}")
    private String logStreamName;

    private CloudWatchLogsClient cloudWatchLogsClient;

    public CloudWatchService() {
    }

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
                System.out.println("Created log group: " + logGroupName);
            }
        } catch (Exception e) {
            System.err.println("Error checking log group: " + e.getMessage());
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
                System.out.println("Created log stream: " + logStreamName);
            }
        } catch (Exception e) {
            System.err.println("Error checking log stream: " + e.getMessage());
        }
    }

    public void logTransaction(Transaction transaction) {
        try {
            long timestamp = System.currentTimeMillis();

            InputLogEvent logEvent = InputLogEvent.builder()
                    .message("Processed Transaction: " + transaction)
                    .timestamp(timestamp)
                    .build();

            PutLogEventsRequest request = PutLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .logEvents(logEvent)
                    .build();

            cloudWatchLogsClient.putLogEvents(request);
            System.out.println("Logged transaction to CloudWatch: " + transaction.getId());
        } catch (Exception e) {
            System.err.println("Error logging transaction: " + e.getMessage());
        }
    }
}
