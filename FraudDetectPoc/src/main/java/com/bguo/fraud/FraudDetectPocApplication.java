package com.bguo.fraud;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling //start scheduled task. used by tasks annotation of @Scheduled.
@Slf4j
public class FraudDetectPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectPocApplication.class, args);
	}
	
	@Bean
    public CommandLineRunner checkRedisConfig(RedisProperties redisProperties) {
        return args -> {
            log.debug("Redis Host: {}", redisProperties.getHost());
            log.debug("Redis Port: {}", redisProperties.getPort());
        };
    }

}
