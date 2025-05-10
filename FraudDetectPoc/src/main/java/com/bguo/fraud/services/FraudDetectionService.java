package com.bguo.fraud.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.rule.FraudRule;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {
    private final List<FraudRule> fraudRules;
    private static final Logger logger = LoggerFactory.getLogger(FraudDetectionService.class);

    public boolean isFraudulent(Transaction tx) {
        try {
            return fraudRules.stream()
                    .anyMatch(rule -> {
                        try {
                            return rule.apply(tx);
                        } catch (Exception e) {
                            logger.error("Error applying fraud rule: {} on transaction: {}", rule.getClass().getSimpleName(), tx, e);
                            return false; 
                        }
                    });
        } catch (Exception e) {
            logger.error("Unexpected error occurred while evaluating fraud detection for transaction: {}", tx, e);
            throw e; 
        }    }
}