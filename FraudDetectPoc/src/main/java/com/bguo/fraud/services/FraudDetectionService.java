package com.bguo.fraud.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.rule.FraudRule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {
    private final List<FraudRule> fraudRules;

    public boolean isFraudulent(Transaction tx) {
        try {
            return fraudRules.stream().anyMatch(rule -> {
                try {
                    return rule.apply(tx);
                } catch (Exception e) {
                    log.error("Error applying fraud rule: {} on transaction: {}", rule.getClass().getSimpleName(),
                            tx, e);
                    return false;
                }
            });
        } catch (Exception e) {
            log.error("Unexpected error occurred while evaluating fraud detection for transaction: {}", tx, e);
            throw e;
        }
    }
}