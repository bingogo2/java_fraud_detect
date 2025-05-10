package com.bguo.fraud.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;

@Service
public class AmountThresholdRule implements FraudRule {
    private final double amountThreshold;
    private static final Logger logger = LoggerFactory.getLogger(AmountThresholdRule.class);

    public AmountThresholdRule(@Value("${fraud.rules.amountThreshold}") double amountThreshold) {
        this.amountThreshold = amountThreshold;
    }

    @Override
    public boolean apply(Transaction tx) {
        logger.debug("Applying fraud rule on transaction: {}", tx);
        try {
            boolean result = tx.getAmount() > amountThreshold;
            if (!result) {
                logger.debug("Transaction {} is not fraudulent for rule: {}", tx.getId(), this.getClass().getSimpleName());
            }
            return result;
        } catch (Exception e) {
            logger.error("Error applying fraud rule on transaction: {}", tx, e);
            throw e; 
        }
    }
}