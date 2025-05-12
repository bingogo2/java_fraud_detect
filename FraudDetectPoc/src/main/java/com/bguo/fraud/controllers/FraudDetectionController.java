package com.bguo.fraud.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.services.FraudDetectionService;
import com.bguo.fraud.services.SqsService;

@RestController
@RequestMapping("/api/fraud")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;
    private final SqsService sqsService;

    @Autowired
    public FraudDetectionController(FraudDetectionService fraudDetectionService, SqsService sqsService) {
        this.fraudDetectionService = fraudDetectionService;
        this.sqsService = sqsService;
    }

    // POST method to check if a transaction is fraud
    @PostMapping("/check")
    public boolean checkFraud(@RequestBody Transaction transaction) {
        sqsService.sendTransactionToQueue(transaction);
        return fraudDetectionService.isFraudulent(transaction);
    }
}
