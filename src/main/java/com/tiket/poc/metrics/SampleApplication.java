package com.tiket.poc.metrics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author zakyalvan
 */
@SpringBootApplication(proxyBeanMethods = false)
public class SampleApplication {
	public static void main(String[] args) {
		SpringApplication.run(SampleApplication.class, args);
	}
}
