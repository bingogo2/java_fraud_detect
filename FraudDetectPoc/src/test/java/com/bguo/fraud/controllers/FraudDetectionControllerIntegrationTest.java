package com.bguo.fraud.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.services.BlacklistService;

import redis.embedded.RedisServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
public class FraudDetectionControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate; 
    @Autowired
    private BlacklistService blacklistService;
    private RedisServer redisServer;

    @BeforeEach
    void setUp() throws Exception {
        redisServer = new RedisServer();
        redisServer.start();
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
    public void testFraudulentBlacklistedAccount() {
        // Create transaction with a blacklisted account
        Transaction tx = new Transaction();
        tx.setAmount(5000);
        tx.setAccountId("acc_A1b2C3d4");
     // Add account to the blacklist before making the transaction
        blacklistService.addToBlacklist("fraud:blacklist", "acc_A1b2C3d4");
        // Perform POST request to the API
        ResponseEntity<Boolean> response = restTemplate.postForEntity("/api/fraud/check", tx, Boolean.class);
        System.out.println(response.getBody()); 
        // Assert: Expect fraud detection due to blacklisted account
        assertTrue(response.getBody());
    }
}
