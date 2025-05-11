package com.bguo.fraud.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;
@Configuration
@Profile("dev")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            // start Redis in dev environment.
            redisServer = new RedisServer(6379);
            redisServer.start();
            System.out.println("Embedded Redis started on port 6379.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start embedded Redis", e);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            try {
                redisServer.stop();
                System.out.println("Embedded Redis stopped.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() {
        return redisServer; // return only instance.
    }}
