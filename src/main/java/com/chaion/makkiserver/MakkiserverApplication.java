package com.chaion.makkiserver;

import com.chaion.makkiserver.dapps.DAppProperties;
import com.chaion.makkiserver.file.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({StorageProperties.class, DAppProperties.class})
public class MakkiserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(MakkiserverApplication.class, args);
	}

}
