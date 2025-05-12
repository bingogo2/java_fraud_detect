package com.bguo.fraud.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class HighFrequencyServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;  // Mock RedisTemplate

    @Mock
    private ValueOperations<String, String> valueOperations;  // Mock ValueOperations

    @InjectMocks
    private HighFrequencyService highFrequencyService;  // Inject mock into the service

    // Manually set frequencyThreshold to a desired value (e.g., 10)
    private long frequencyThreshold = 10;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks before each test
        
        // Manually set the frequencyThreshold value for testing
        highFrequencyService.setFrequencyThreshold(frequencyThreshold);

        // Mock RedisTemplate to return the mocked ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testIsHighFrequencyTransaction_ExceedsThreshold() {
        // Given: Simulate an account ID and a frequency threshold of 10 transactions
        String accountId = "acc_A1b2C3d4";
        Long mockCount = 15L;  // Simulate a count of transactions exceeding the threshold

        // When: Mock the Redis operations to return the simulated transaction count
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(mockCount);
        when(redisTemplate.expire(anyString(), eq(1L), eq(TimeUnit.MINUTES))).thenReturn(true);

        // Then: Verify that the account is flagged as high frequency
        boolean result = highFrequencyService.isHighFrequencyTransaction(accountId);
        assertTrue(result, "The account should be flagged as high frequency");

        // Verify that Redis methods were called as expected
        verify(valueOperations).increment("tx_count:" + accountId, 1L);
        verify(redisTemplate).expire("tx_count:" + accountId, 1L, TimeUnit.MINUTES);
    }

    @Test
    void testIsHighFrequencyTransaction_BelowThreshold() {
        // Given: Simulate an account ID and a frequency threshold of 10 transactions
        String accountId = "acc_A1b2C3d4";
        Long mockCount = 5L;  // Simulate a count of transactions below the threshold

        // When: Mock the Redis operations to return the simulated transaction count
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(mockCount);
        when(redisTemplate.expire(anyString(), eq(1L), eq(TimeUnit.MINUTES))).thenReturn(true);

        // Then: Verify that the account is NOT flagged as high frequency
        boolean result = highFrequencyService.isHighFrequencyTransaction(accountId);
        assertFalse(result, "The account should not be flagged as high frequency");

        // Verify that Redis methods were called as expected
        verify(valueOperations).increment("tx_count:" + accountId, 1L);
        verify(redisTemplate).expire("tx_count:" + accountId, 1L, TimeUnit.MINUTES);
    }

    @Test
    void testIsHighFrequencyTransaction_ErrorHandling() {
        // Given: Simulate a Redis error during the operation
        String accountId = "acc_A1b2C3d4";

        // When: Mock Redis operation to throw an exception
        when(valueOperations.increment(anyString(), anyLong())).thenThrow(new RuntimeException("Redis error"));

        // Then: Verify that the method throws an exception when an error occurs in Redis
        assertThrows(RuntimeException.class, () -> {
            highFrequencyService.isHighFrequencyTransaction(accountId);
        });
    }
}