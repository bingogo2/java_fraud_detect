package com.bguo.fraud.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.rule.AmountThresholdRule;
import com.bguo.fraud.rule.HighFrequencyRule;
import com.bguo.fraud.rule.SuspiciousAccountRule;

@SpringBootTest
@ActiveProfiles("dev")
class FraudDetectionServiceTest {

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private SuspiciousAccountService suspiciousAccountService;
    @Mock
    private HighFrequencyService highFrequencyService;  // Mock HighFrequencyService
    @Value("${fraud.rules.suspiciousAccountKey}")
    private String suspiciousAccountKey;

    @Value("${fraud.rules.suspiciousAccountFileName}")
    private String suspiciousAccountFileName;
    @Value("${fraud.rules.frequencyThreshold}") 
    private long frequencyThreshold;
    @Value("${fraud.rules.amountThreshold}")
    private double amountThreshold;    
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
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
        tx.setAmount(500);
        String suspicousAccountDefinedInTxt = "aCC_BLACK_2024";
        tx.setAccountId(suspicousAccountDefinedInTxt);

        // Create SuspiciousAccountRule with @Value injected values
        SuspiciousAccountRule suspiciousAccountRule = new SuspiciousAccountRule(suspiciousAccountKey, suspiciousAccountFileName, suspiciousAccountService);

        // Mock: Simulate adding the account to the Suspicious Account List in Redis
        // Ensure RedisTemplate is correctly configured in the Spring context
        // when(redisTemplate.opsForSet().isMember(suspiciousAccountKey, suspicousAccountDefinedInTxt)).thenReturn(true);

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

        // Mock the HighFrequencyService to return true for any account id
        when(highFrequencyService.isHighFrequencyTransaction(anyString())).thenReturn(true);  // Simulate frequency fraud detection

        // Create and inject only the HighFrequencyRule
        HighFrequencyRule highFrequencyRule = new HighFrequencyRule(highFrequencyService);  // High-frequency rule

        // Create FraudDetectionService with only this rule
        fraudDetectionService = new FraudDetectionService(Arrays.asList(highFrequencyRule));

        // Assert: Expect fraud detection due to high transaction frequency
        assertTrue(fraudDetectionService.isFraudulent(tx));  // Should return true because of the mock
    }
}
