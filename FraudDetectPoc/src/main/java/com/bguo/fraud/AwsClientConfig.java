package com.bguo.fraud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsClientConfig {

    private final AwsConfig awsConfig;

    public AwsClientConfig(AwsConfig awsConfig) {
        this.awsConfig = awsConfig;
    }

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(software.amazon.awssdk.regions.Region.of(awsConfig.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        awsConfig.getAccessKeyId(), awsConfig.getSecretAccessKey())))
                .build();
    }
}

