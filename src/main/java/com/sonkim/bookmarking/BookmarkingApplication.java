package com.sonkim.bookmarking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class BookmarkingApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookmarkingApplication.class, args);
	}

}
