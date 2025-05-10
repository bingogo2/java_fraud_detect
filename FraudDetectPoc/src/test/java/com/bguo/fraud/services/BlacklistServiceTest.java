package com.bguo.fraud.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

class BlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private BlacklistService blacklistService;

    private final String blacklistKey = "fraud:blacklist"; // This will be used for testing

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        // Mock opsForSet() to return a mock SetOperations instance
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void testAddToBlacklist_success() {
        // Act: Add an account to the blacklist
        blacklistService.addToBlacklist(blacklistKey, "acc_A1b2C3d4");

        // Assert: Verify that the Redis `add` method was called with the correct key and value
        verify(setOperations).add(blacklistKey, "acc_A1b2C3d4");
    }

    @Test
    void testIsBlacklisted_accountInBlacklist() {
        // Mock the Redis behavior to return true (account is blacklisted)
        when(setOperations.isMember(blacklistKey, "acc_A1b2C3d4")).thenReturn(true);

        // Act: Check if account is blacklisted
        boolean isBlacklisted = blacklistService.isBlacklisted(blacklistKey, "acc_A1b2C3d4");

        // Assert: Account should be considered blacklisted
        assertTrue(isBlacklisted);

        // Verify: Redis is queried for the account
        verify(setOperations).isMember(blacklistKey, "acc_A1b2C3d4");
    }

    @Test
    void testIsBlacklisted_accountNotInBlacklist() {
        // Mock the Redis behavior to return false (account is not blacklisted)
        when(setOperations.isMember(blacklistKey, "acc_XYZ987")).thenReturn(false);

        // Act: Check if account is blacklisted
        boolean isBlacklisted = blacklistService.isBlacklisted(blacklistKey, "acc_XYZ987");

        // Assert: Account should not be considered blacklisted
        assertFalse(isBlacklisted);

        // Verify: Redis is queried for the account
        verify(setOperations).isMember(blacklistKey, "acc_XYZ987");
    }

    @Test
    void testAddToBlacklist_exception() {
        // Simulate Redis exception
        doThrow(new RuntimeException("Redis failure")).when(setOperations).add(any(String.class), any(String.class));

        // Act & Assert: Ensure that exception is thrown when Redis fails to add to blacklist
        assertThrows(RuntimeException.class, () -> blacklistService.addToBlacklist(blacklistKey, "acc_A1b2C3d4"));
    }

    @Test
    void testIsBlacklisted_redisException() {
        // Simulate Redis exception
        when(setOperations.isMember(any(String.class), any(String.class)))
                .thenThrow(new RuntimeException("Redis failure"));

        // Act & Assert: Ensure that exception is thrown when Redis fails to check the blacklist
        assertThrows(RuntimeException.class, () -> blacklistService.isBlacklisted(blacklistKey, "acc_A1b2C3d4"));
    }
}
