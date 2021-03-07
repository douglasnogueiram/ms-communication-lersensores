package com.br.ms.communication.buyprocess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;


import co.elastic.apm.attach.ElasticApmAttacher;

@SpringBootApplication
@EnableCircuitBreaker
public class ApplicationBuyProcess {

	public static void main(String[] args) throws InterruptedException {
		ElasticApmAttacher.attach();
		SpringApplication.run(ApplicationBuyProcess.class, args);
	}

}