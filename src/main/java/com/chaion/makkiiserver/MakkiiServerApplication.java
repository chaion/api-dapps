package com.chaion.makkiiserver;

import com.chaion.makkiiserver.modules.dappmarket.DAppProperties;
import com.chaion.makkiiserver.repository.file.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({StorageProperties.class, DAppProperties.class})
public class MakkiiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MakkiiServerApplication.class, args);
	}
}
