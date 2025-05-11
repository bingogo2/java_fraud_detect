package com.bguo.fraud.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.rule.AmountThresholdRule;
import com.bguo.fraud.rule.FraudRule;
import com.bguo.fraud.rule.HighFrequencyRule;
import com.bguo.fraud.rule.SuspiciousAccountRule;

class FraudDetectionServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private SetOperations<String, String> setOps;
    
    @Mock
    private ValueOperations<String, String> valueOps;
    
    @InjectMocks
    private FraudDetectionService fraudDetectionService;
    @InjectMocks
    private SuspiciousAccountService suspiciousAccountService;
    @Mock
    private FraudRule amountThresholdRule;  // 模拟黑名单规则
    @Mock
    private FraudRule highFrequencyRule;
    @Mock
    private FraudRule suspiciousAccountRule;
    private final String SUSPICIOUS_ACCOUNT_KEY = "fraud:account";
    private final String SUSPICIOUS_ACCOUNT_FILE_NAME = "suspiciousAccounts.txt";

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Mock redisTemplate.opsForSet() to return setOps
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        
        // Mock redisTemplate.opsForValue() to return valueOps
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        fraudDetectionService = new FraudDetectionService(Arrays.asList(amountThresholdRule, highFrequencyRule, suspiciousAccountRule));

    }

    @Test
    void testIsFraudulent_highAmount() {
        // Create transaction with high amount
        Transaction tx = new Transaction();
        tx.setAmount(20000);  // Exceeds the threshold (e.g., 10000)

        // Create and inject only the AmountThresholdRule
        AmountThresholdRule amountThresholdRule = new AmountThresholdRule(10000);  // Amount threshold rule

        // Create FraudDetectionService with only this rule
        fraudDetectionService = new FraudDetectionService(Arrays.asList(amountThresholdRule));

        // Assert: Expect fraud detection due to high amount
        assertTrue(fraudDetectionService.isFraudulent(tx));
    }

    @Test
    void testIsSuspiciousAccount() {
        // Create transaction with a Suspicious Account List
        Transaction tx = new Transaction();
        tx.setAmount(5000);
        tx.setAccountId("acc_A1b2C3d4");

        // Mock: Simulate adding the account to the Suspicious Account List in Redis
        when(redisTemplate.opsForSet().isMember(SUSPICIOUS_ACCOUNT_KEY, "acc_A1b2C3d4")).thenReturn(true);
        
        // Mock: Simulate adding the account to the Suspicious Account List (typically in your service method)
        when(redisTemplate.opsForSet().add(SUSPICIOUS_ACCOUNT_KEY, "acc_A1b2C3d4")).thenReturn(1L);

        // Create and inject only the SuspiciousAccountRule
        SuspiciousAccountRule suspiciousAccountRule = new SuspiciousAccountRule(SUSPICIOUS_ACCOUNT_KEY, SUSPICIOUS_ACCOUNT_FILE_NAME, suspiciousAccountService);

        // Create FraudDetectionService with only this rule
        fraudDetectionService = new FraudDetectionService(Arrays.asList(suspiciousAccountRule));

        // Assert: Expect fraud detection due to Suspicious Account List
        assertTrue(fraudDetectionService.isFraudulent(tx));
    }

    @Test
    void testIsFraudulent_highFrequency() {
        // Create transaction with moderate amount and account id
        Transaction tx = new Transaction();
        tx.setAmount(5000);
        tx.setAccountId("acc_A1b2C3d4");

        // Mock: Simulate the account has already performed 5 transactions in the last minute
        when(redisTemplate.opsForValue().increment(anyString(), anyLong())).thenReturn(6L);

        // Create and inject only the HighFrequencyRule
        HighFrequencyRule highFrequencyRule = new HighFrequencyRule(5, redisTemplate);  // High-frequency rule

        // Create FraudDetectionService with only this rule
        fraudDetectionService = new FraudDetectionService(Arrays.asList(highFrequencyRule));

        // Assert: Expect fraud detection due to high transaction frequency
        assertTrue(fraudDetectionService.isFraudulent(tx));
    }
}
