package com.bguo.fraud.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;
@Configuration
@Profile("dev")
@Slf4j
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            // start Redis in dev environment.
            redisServer = new RedisServer(6379);
            redisServer.start();
            log.debug("Embedded Redis started on port 6379.");
        } catch (IOException e) {
            log.error("Failed to start embedded Redis", e);
            throw new RuntimeException("Failed to start embedded Redis", e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            try {
                redisServer.stop();
                log.info("Embedded Redis stopped.");
            } catch (IOException e) {
                log.warn("Failed to stop embedded Redis", e);
            }
        }
    }

    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() {
        return redisServer; // return only instance.
    }}
