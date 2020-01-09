package com.chaion.makkiiserver;

import com.chaion.makkiiserver.modules.dappmarket.DAppProperties;
import com.chaion.makkiiserver.repository.file.StorageProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({StorageProperties.class, DAppProperties.class})
public class MakkiiServerApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(MakkiiServerApplication.class);
		application.setBanner((environment, sourceClass, out) -> {
		    out.println(" __  __          _  ___  _______ _____");
		    out.println("|  \\/  |   /\\   | |/ / |/ /_   _|_   _|");
		    out.println("| \\  / |  /  \\  | ' /| ' /  | |   | |");
		    out.println("| |\\/| | / /\\ \\ |  < |  <   | |   | |");
		    out.println("| |  | |/ ____ \\| . \\| . \\ _| |_ _| |_");
		    out.println("|_|  |_/_/    \\_\\_|\\_\\_|\\_\\_____|_____|");

		    Properties gitProperties = new Properties();
			ClassPathResource classPathResource = new ClassPathResource("git.properties");
			InputStream is = null;
			try {
				is = classPathResource.getInputStream();
				gitProperties.load(is);
				String commit = gitProperties.getProperty("git.commit.id.describe");
				String version = gitProperties.getProperty("git.build.version");
				out.println("version: " + version);
				out.println("git commit: " + commit);
				out.println();
			} catch (IOException e) {
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		});
		application.run(args);
	}
}
