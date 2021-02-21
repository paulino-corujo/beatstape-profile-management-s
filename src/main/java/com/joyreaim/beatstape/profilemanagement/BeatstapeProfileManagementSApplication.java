package com.joyreaim.beatstape.profilemanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BeatstapeProfileManagementSApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeatstapeProfileManagementSApplication.class, args);
	}

}
