package com.bguo.fraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //start scheduled task. used by tasks annotation of @Scheduled.
public class FraudDetectPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudDetectPocApplication.class, args);
	}

}
