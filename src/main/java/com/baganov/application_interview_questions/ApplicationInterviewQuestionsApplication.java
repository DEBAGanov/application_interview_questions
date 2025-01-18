package com.baganov.application_interview_questions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.baganov.application_interview_questions.model")
@EnableJpaRepositories("com.baganov.application_interview_questions.repository")
public class ApplicationInterviewQuestionsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationInterviewQuestionsApplication.class, args);
    }
}