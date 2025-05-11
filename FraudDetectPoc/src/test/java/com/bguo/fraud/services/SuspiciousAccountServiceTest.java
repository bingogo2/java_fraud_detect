package com.bguo.fraud.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

class SuspiciousAccountServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private SuspiciousAccountService suspiciousAccountService;
    @Mock
    private Resource resource;
    private final String SUSPECIOUS_ACCOUNTS_FILE_NAME = "suspiciousAccounts.txt";
    private final String TEST_KEY = "fraud:account"; // This will be used for testing

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        //initialize service fields.
        ReflectionTestUtils.setField(suspiciousAccountService, "redisKey", TEST_KEY);
        ReflectionTestUtils.setField(suspiciousAccountService, "suspiciousAccountFileName", SUSPECIOUS_ACCOUNTS_FILE_NAME);
        // Mock opsForSet() to return a mock SetOperations instance
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void initSuspiciousAccount_ShouldHandleEmptyFile() throws Exception {
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        
        suspiciousAccountService.initSuspiciousAccount();
        
        verify(setOperations, never()).add(any(), any());
    }
    
    @Test
    void isSuspiciousAccount_ShouldReturnTrueForExistingAccount() {
        when(setOperations.isMember(TEST_KEY, "ACC1")).thenReturn(true);
        assertTrue(suspiciousAccountService.isSuspiciousAccount(TEST_KEY, "ACC1"));
    }

    @Test
    void isSuspiciousAccount_ShouldReturnFalseForUnknownAccount() {
        when(setOperations.isMember(TEST_KEY, "UNKNOWN")).thenReturn(false);
        assertFalse(suspiciousAccountService.isSuspiciousAccount(TEST_KEY, "UNKNOWN"));
    }

    @Test
    void isSuspiciousAccount_ShouldHandleNullInputGracefully() {
        assertFalse(suspiciousAccountService.isSuspiciousAccount(TEST_KEY, null));
        verify(setOperations, never()).isMember(any(), any());
    }

}
