package com.bguo.fraud.rule;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;

@Service
public class HighFrequencyRule implements FraudRule {
    private final long frequencyThreshold;
    private final RedisTemplate<String, String> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(HighFrequencyRule.class);

    public HighFrequencyRule(@Value("${fraud.rules.frequencyThreshold}") long frequencyThreshold,
            RedisTemplate<String, String> redisTemplate) {
        this.frequencyThreshold = frequencyThreshold;
        this.redisTemplate = redisTemplate;
}

    @Override
    public boolean apply(Transaction tx) {
        logger.debug("Applying HighFrequency fraud rule on transaction: {}", tx);
        try {
            // calculate transactions in 1 minute. 
            String key = "tx_count:" + tx.getAccountId();
            Long count = redisTemplate.opsForValue().increment(key, 1L);
            redisTemplate.expire(key, 1, TimeUnit.MINUTES); 
            
            boolean result = count != null && count > frequencyThreshold;
            if (result) {
                logger.debug("Transaction {} exceeds frequency threshold for rule: {}", tx.getId(), this.getClass().getSimpleName());
            } else {
                logger.debug("Transaction {} is not fraudulent for rule: {}", tx.getId(), this.getClass().getSimpleName());
            }
            return result;
        } catch (Exception e) {
            logger.error("Error applying HighFrequency fraud rule on transaction: {}", tx, e);
            throw e;  
        }
     }
    
}
