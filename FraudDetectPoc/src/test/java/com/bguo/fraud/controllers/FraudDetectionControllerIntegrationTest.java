package com.bguo.fraud.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.bguo.fraud.config.EmbeddedRedisConfig;
import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.rule.SuspiciousAccountRule;
import com.bguo.fraud.services.SuspiciousAccountService;

import redis.embedded.RedisServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("dev")
public class FraudDetectionControllerIntegrationTest {
    @Autowired
    private SuspiciousAccountRule suspiciousAccountRule;
    @Lazy
    @Autowired
    private TestRestTemplate restTemplate; 
    @Autowired
    private SuspiciousAccountService suspiciousAccountService;
    private RedisServer redisServer;

    @BeforeEach
    void setUp() throws Exception {
        //don't new RedisServer instance here in case it is conflict with the one instantiated from EmbeddedRedisConfig.
//        redisServer = new RedisServer();
//        redisServer.start();
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
    
    @Test
    public void testFraudulentHighAmount() {
        // Create transaction with high amount
        Transaction tx = new Transaction();
        tx.setAccountId("acc_A1b2C3d4");
        tx.setAmount(20000);  // Exceeds the threshold (e.g., 10000)

        // Perform POST request to the API
        ResponseEntity<Boolean> response = restTemplate.postForEntity("/api/fraud/check", tx, Boolean.class);
        System.out.println(response.getBody()); 
        // Assert: Expect fraud detection due to high amount
        assertTrue(response.getBody());
    }

    @Test
    public void testIsNotSuspiciousAccount() {
        // Create transaction with a Suspicious Account
        Transaction tx = new Transaction();
        tx.setAmount(5000);
        tx.setAccountId("acc_A1b2C3d4");
        ResponseEntity<Boolean> response = restTemplate.postForEntity("/api/fraud/check", tx, Boolean.class);
        System.out.println(response.getBody()); 
        // Assert: Expect fraud detection due to SuspiciousAccount list
        assertFalse(response.getBody());
    }
}
