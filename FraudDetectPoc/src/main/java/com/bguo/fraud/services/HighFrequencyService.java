package com.bguo.fraud.services;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HighFrequencyService {
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${fraud.rules.frequencyThreshold}")
    private long frequencyThreshold;

    public boolean isHighFrequencyTransaction(String accountId) {
        try {
            String key = "tx_count:" + accountId;
            Long count = redisTemplate.opsForValue().increment(key, 1L);
            redisTemplate.expire(key, 1, TimeUnit.MINUTES); 

            boolean result = count != null && count > frequencyThreshold;
            if (result) {
                log.debug("Account {} exceeds frequency threshold: {}", accountId, frequencyThreshold);
            }
            return result;
        } catch (Exception e) {
            log.error("Error checking high-frequency transactions for account: {}", accountId, e);
            throw e;  
        }
    }
    
    public void setFrequencyThreshold(long frequencyThreshold) {
        this.frequencyThreshold = frequencyThreshold;
    }
    
}
