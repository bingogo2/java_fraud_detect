package com.bguo.fraud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "aws")
@Getter
@Setter
public class AwsConfig {

    @Value("${aws.region}")
    private String region;
    @Value("${aws.accessKeyId}")
    private String accessKeyId;
    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;
    private SqsConfig sqs;
    private CloudWatchConfig cloudwatch;

    @Getter
    @Setter
    public static class SqsConfig {
        private String queueUrl;
    }

    @Getter
    @Setter
    public static class CloudWatchConfig {
        private String logGroupName;
        private String logStreamName;
    }
}
