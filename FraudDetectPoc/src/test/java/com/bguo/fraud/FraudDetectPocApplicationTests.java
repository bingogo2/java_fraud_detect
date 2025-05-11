package com.bguo.fraud;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.bguo.fraud.config.EmbeddedRedisConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
@Import(EmbeddedRedisConfig.class)
@ActiveProfiles("dev")
class FraudDetectPocApplicationTests {

	@Test
	void contextLoads() {
	 // This test will pass if the Spring application context loads successfully
	}

}
