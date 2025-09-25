package com.gmailreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.gmailreader"})
public class GmailreaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmailreaderApplication.class, args);
	}

}
