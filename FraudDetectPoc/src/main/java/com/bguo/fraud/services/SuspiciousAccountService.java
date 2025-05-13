package com.bguo.fraud.services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuspiciousAccountService {
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${fraud.rules.suspiciousAccountKey}")
    private String redisKey;
    @Value("${fraud.rules.suspiciousAccountFileName}")
    private String suspiciousAccountFileName;

    @PostConstruct
    public void initSuspiciousAccount() {
        try {
            Resource resource = new ClassPathResource(suspiciousAccountFileName);
            List<String> accounts = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()))
                .lines()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .map(String::toLowerCase) //save as lower case
                .collect(Collectors.toList());

            if (!accounts.isEmpty()) {
                redisTemplate.opsForSet().add(redisKey, accounts.toArray(new String[0]));
                log.info("Loaded {} accounts (lowercase) to Redis key: {}", accounts.size(), redisKey);
            }
        } catch (IOException e) {
            log.error("Suspicious Account List initialization failed", e);
            throw new IllegalStateException("Critical: suspiciousAccount initialization failed", e);
        }
    }

    public boolean isSuspiciousAccount(String suspiciousAccountKey, String accountId) {
        if (accountId == null) {
            return false;
        }
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(
                        suspiciousAccountKey, 
                        accountId.toLowerCase() // convert to lower case
                    )
        );
    }
}
