package com.bguo.fraud.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.bguo.fraud.model.Transaction;

@SpringBootTest
@ActiveProfiles("dev")  // Specify the active profile for configuration (like 'dev', 'test', etc.)
class SuspiciousAccountServiceTest {

    @Autowired
    private SuspiciousAccountService suspiciousAccountService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${fraud.rules.suspiciousAccountKey}")
    private String suspiciousAccountKey;

    @Value("${fraud.rules.suspiciousAccountFileName}")
    private String suspiciousAccountFileName;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsSuspiciousAccount() {
        // Prepare: Load the suspicious account file and initialize Redis
        // This is normally done through @PostConstruct in your service

        // Simulate an account being added to Redis through the service (this would happen in @PostConstruct in a real scenario)
        String suspiciousAccountDefinedInTxt = "acc_black_2024";
        redisTemplate.opsForSet().add(suspiciousAccountKey, suspiciousAccountDefinedInTxt.toLowerCase());

        // Create transaction with a Suspicious Account List
        Transaction tx = new Transaction();
        tx.setAmount(500);
        tx.setAccountId(suspiciousAccountDefinedInTxt);

        // Assert: The account should be recognized as suspicious
        assertTrue(suspiciousAccountService.isSuspiciousAccount(suspiciousAccountKey, suspiciousAccountDefinedInTxt));
    }

    @Test
    void testIsSuspiciousAccount_withNonSuspiciousAccount() {
        // Prepare: Load the suspicious account file and initialize Redis
        // Simulate an account being added to Redis
        String suspiciousAccountDefinedInTxt = "acc_black_2024";
        redisTemplate.opsForSet().add(suspiciousAccountKey, suspiciousAccountDefinedInTxt.toLowerCase());

        // Create transaction with a Non-Suspicious Account
        Transaction tx = new Transaction();
        tx.setAmount(500);
        tx.setAccountId("acc_unknown");

        // Assert: The account should NOT be recognized as suspicious
        assertTrue(!suspiciousAccountService.isSuspiciousAccount(suspiciousAccountKey, tx.getAccountId()));
    }
}
