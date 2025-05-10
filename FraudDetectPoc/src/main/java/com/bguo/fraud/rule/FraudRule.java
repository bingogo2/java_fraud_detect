package com.bguo.fraud.rule;

import com.bguo.fraud.model.Transaction;

public interface FraudRule {
    boolean apply(Transaction tx);
}