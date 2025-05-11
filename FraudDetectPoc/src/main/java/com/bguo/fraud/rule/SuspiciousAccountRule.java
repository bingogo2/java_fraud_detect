package com.bguo.fraud.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.services.SuspiciousAccountService;

@Service
public class SuspiciousAccountRule implements FraudRule {
    private final String suspiciousAccountKey;
    private final String suspiciousAccountFileName;
    private final SuspiciousAccountService suspiciousAccountService;
    private static final Logger logger = LoggerFactory.getLogger(SuspiciousAccountRule.class);

    public SuspiciousAccountRule(@Value("${fraud.rules.suspiciousAccountKey}") String suspiciousAccountKey,
            @Value("${fraud.rules.suspiciousAccountFileName}") String suspiciousAccountFileName,
            SuspiciousAccountService suspiciousAccountService) {
        this.suspiciousAccountKey = suspiciousAccountKey;
        this.suspiciousAccountService = suspiciousAccountService;
        this.suspiciousAccountFileName = suspiciousAccountFileName;
   }

    @Override
    public boolean apply(Transaction tx) {
        logger.debug("Applying SuspiciousAccount fraud rule on transaction: {}", tx);
        try {
            // check account in the suspiciousAccount list
            boolean isSuspiciousAccount = suspiciousAccountService.isSuspiciousAccount(suspiciousAccountKey,
                    tx.getAccountId());
            if (isSuspiciousAccount) {
                logger.debug("Transaction {} is fraudulent for rule: {}", tx.getId(), this.getClass().getSimpleName());
            } else {
                logger.debug("Transaction {} is not fraudulent for rule: {}", tx.getId(),
                        this.getClass().getSimpleName());
            }
            return isSuspiciousAccount;
        } catch (Exception e) {
            logger.error("Error applying SuspiciousAccount fraud rule on transaction: {}", tx, e);
            throw e;
        }
    }

}
