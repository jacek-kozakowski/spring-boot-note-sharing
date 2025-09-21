package com.notex.student_notes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StudentNotesApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentNotesApplication.class, args);
	}

}
