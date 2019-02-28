package com.chaion.makkiserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MakkiserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(MakkiserverApplication.class, args);
	}

}
