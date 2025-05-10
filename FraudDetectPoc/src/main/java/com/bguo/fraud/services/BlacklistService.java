package com.bguo.fraud.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class BlacklistService {
    private final RedisTemplate<String, String> redisTemplate;

    public BlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String blacklistKey, String accountId) {
        Long result = redisTemplate.opsForSet().add(blacklistKey, accountId);
        // Log the result to check if account was added successfully
        System.out.println("Added account to blacklist: " + accountId + ", result: " + result);
    }

    public boolean isBlacklisted(String blacklistKey, String accountId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(blacklistKey, accountId));
    }
}
