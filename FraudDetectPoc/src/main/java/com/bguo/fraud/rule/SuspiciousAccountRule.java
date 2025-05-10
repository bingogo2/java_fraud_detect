package com.bguo.fraud.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.services.BlacklistService;

@Service
public class SuspiciousAccountRule implements FraudRule {
    private final String blacklistKey;
    private final BlacklistService blacklistService;
    private static final Logger logger = LoggerFactory.getLogger(SuspiciousAccountRule.class);

    public SuspiciousAccountRule(@Value("${fraud.rules.blacklistKey}") String blacklistKey,
                                 BlacklistService blacklistService) {
        this.blacklistKey = blacklistKey;
        this.blacklistService = blacklistService;
    }

    @Override
    public boolean apply(Transaction tx) {
        logger.debug("Applying SuspiciousAccount fraud rule on transaction: {}", tx);
        try {
            //check account in the blacklist 
            boolean isBlacklisted = blacklistService.isBlacklisted(blacklistKey, tx.getAccountId());
            if (isBlacklisted) {
                logger.debug("Transaction {} is fraudulent for rule: {}", tx.getId(), this.getClass().getSimpleName());
            } else {
                logger.debug("Transaction {} is not fraudulent for rule: {}", tx.getId(), this.getClass().getSimpleName());
            }
            return isBlacklisted;
        } catch (Exception e) {
            logger.error("Error applying SuspiciousAccount fraud rule on transaction: {}", tx, e);
            throw e; 
        }
    }

}
