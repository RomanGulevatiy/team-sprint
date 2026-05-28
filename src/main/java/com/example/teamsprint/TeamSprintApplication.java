package com.example.teamsprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TeamSprintApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamSprintApplication.class, args);
    }

}
