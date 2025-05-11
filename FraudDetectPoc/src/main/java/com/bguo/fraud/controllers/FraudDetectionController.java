package com.bguo.fraud.controllers;

import com.bguo.fraud.model.Transaction;
import com.bguo.fraud.services.FraudDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraud")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    @Autowired
    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    // POST method to check if a transaction is fraud
    @PostMapping("/check")
    public boolean checkFraud(@RequestBody Transaction transaction) {
        return fraudDetectionService.isFraudulent(transaction);
    }
}
