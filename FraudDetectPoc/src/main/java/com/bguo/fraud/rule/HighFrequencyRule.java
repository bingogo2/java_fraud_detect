package com.bguo.fraud.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.services.HighFrequencyService;

@Service
public class HighFrequencyRule implements FraudRule {
    private final HighFrequencyService highFrequencyService;
    private static final Logger logger = LoggerFactory.getLogger(HighFrequencyRule.class);

    public HighFrequencyRule(HighFrequencyService highFrequencyService) {
        this.highFrequencyService = highFrequencyService;
    }

    @Override
    public boolean apply(Transaction tx) {
        logger.debug("Applying HighFrequency fraud rule on transaction: {}", tx);
        try {
            boolean isHighFrequency = highFrequencyService.isHighFrequencyTransaction(tx.getAccountId());
            if (isHighFrequency) {
                logger.debug("Transaction {} exceeds frequency threshold for rule: {}", tx.getId(), this.getClass().getSimpleName());
            } else {
                logger.debug("Transaction {} is not fraudulent for rule: {}", tx.getId(), this.getClass().getSimpleName());
            }
            return isHighFrequency;
        } catch (Exception e) {
            logger.error("Error applying HighFrequency fraud rule on transaction: {}", tx, e);
            throw e;
        }
    }
}
